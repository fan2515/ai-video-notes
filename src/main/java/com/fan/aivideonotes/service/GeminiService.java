package com.fan.aivideonotes.service;

import com.fan.aivideonotes.controller.dto.VideoLinkRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GeminiService implements AiServiceProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String promptTemplate;

    @Value("${app.api.gemini.key}")
    private String geminiApiKey;
    @Value("${app.api.gemini.url.flash}")
    private String geminiFlashApiUrl;
    @Value("${app.api.gemini.url.pro}")
    private String geminiProApiUrl;

    public GeminiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.promptTemplate = loadPromptTemplate();
    }

    /**
     * 从音频文件生成笔记。此方法负责编码音频、构建请求、发送到Gemini API并返回AI生成的笔记内容。
     *
     * @param audioFile  要处理的音频文件
     * @param mode       生成模式 (PRO 或 FLASH)
     * @return 代表笔记结构的纯净JSON字符串
     * @throws RuntimeException 如果在任何步骤中发生严重错误
     */
    public String generateNotesFromAudio(File audioFile, VideoLinkRequest.GenerationMode mode) {
        System.out.println("Generating notes from audio using Base64 inline method with RestTemplate...");
        try {
            // 1. 读取音频文件并进行 Base64 编码
            byte[] fileContent = Files.readAllBytes(audioFile.toPath());
            String encodedString = Base64.getEncoder().encodeToString(fileContent);
            System.out.println("Audio file encoded to Base64. Length: " + encodedString.length());

            // 2. 根据模式选择 API URL
            String apiUrl = switch (mode) {
                case PRO -> geminiProApiUrl;
                default -> geminiFlashApiUrl;
            };
            System.out.println("Using generation mode: " + mode + " with URL: " + apiUrl);

            // 3. 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-goog-api-key", geminiApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 4. 构建包含Prompt和内联音频数据的请求体
            Map<String, Object> textData = Map.of("text", promptTemplate);
            Map<String, Object> inlineData = Map.of("inlineData", Map.of("mimeType", "audio/mpeg", "data", encodedString));
            Map<String, Object> content = Map.of("parts", List.of(textData, inlineData));
            Map<String, Object> requestBody = Map.of("contents", List.of(content));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 5. 使用 RestTemplate 发送请求
            String response = restTemplate.postForObject(apiUrl, entity, String.class);

            // 6. 解析响应并返回结果
            return parseGeminiResponse(response);

        } catch (IOException e) {
            System.err.println("File handling error during audio processing: " + e.getMessage());
            throw new RuntimeException("Failed to read or encode audio file.", e);
        } catch (Exception e) {
            // 捕获所有其他异常，例如网络问题或RestTemplate的错误
            System.err.println("Error in Gemini Base64 process: " + e.getMessage());
            throw new RuntimeException("Failed to generate notes from audio: " + e.getMessage(), e);
        }
    }

    private String loadPromptTemplate() {
        try {
            Resource resource = new ClassPathResource("prompts/notes_generation_prompt_multimodal.txt");
            if (!resource.exists()) {
                throw new IOException("Prompt template not found at: prompts/notes_generation_prompt_multimodal.txt");
            }
            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                return FileCopyUtils.copyToString(reader);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load prompt template", e);
        }
    }

    /**
     * 解析来自Gemini API的原始JSON响应字符串。
     * 此方法旨在提取AI生成的笔记内容，进行清理，并验证其是否为有效的JSON格式。
     *
     * @param jsonResponse Gemini API返回的原始JSON字符串
     * @return 清理和验证后的、代表笔记结构的JSON字符串
     * @throws IOException 如果响应为空、格式无效、包含API错误，或AI未按要求返回有效的JSON内容
     */
    private String parseGeminiResponse(String jsonResponse) throws IOException {
        if (jsonResponse == null || jsonResponse.isBlank()) {
            throw new IOException("Empty or blank response from Gemini.");
        }

        System.out.println("===== RAW RESPONSE FROM GEMINI =====");
        System.out.println(jsonResponse);
        System.out.println("======================================");

        JsonNode root;
        try {
            root = objectMapper.readTree(jsonResponse);
        } catch (IOException e) {
            // 如果Gemini返回的不是一个合法的JSON，记录错误并抛出
            System.err.println("Failed to parse Gemini response as JSON.");
            throw new IOException("Gemini returned invalid JSON: " + jsonResponse, e);
        }

        // 检查是否有错误信息，例如 API Key 无效或请求格式错误
        if (root.has("error")) {
            String errorMessage = root.path("error").path("message").asText("Unknown error");
            throw new IOException("Gemini API returned an error: " + errorMessage);
        }

        // 检查 candidates 数组是否存在且不为空
        JsonNode candidates = root.path("candidates");
        if (candidates.isMissingNode() || !candidates.isArray() || candidates.isEmpty()) {
            // 有时内容会被安全过滤器拦截，导致 candidates 为空。我们检查具体原因。
            String finishReason = root.path("promptFeedback").path("blockReason").asText("unknown reason");
            throw new IOException("Gemini response is missing 'candidates'. Generation may have been blocked for reason: " + finishReason);
        }

        // 定位到我们需要的文本内容
        JsonNode textNode = candidates.path(0).path("content").path("parts").path(0).path("text");

        if (textNode.isMissingNode() || !textNode.isTextual()) {
            throw new IOException("Could not find 'text' field in a valid Gemini response structure.");
        }

        String rawText = textNode.asText();
        System.out.println("Extracted raw text from AI: " + rawText);

        // 清理AI可能额外添加的Markdown代码块标记
        // 使用 trim() 去除首尾空白，然后用正则表达式替换
        String cleanedJson = rawText.trim().replaceAll("^```json\\s*", "").replaceAll("\\s*```$", "");

        // 【关键验证】在返回前，验证清理后的字符串本身是否是一个有效的JSON
        try {
            objectMapper.readTree(cleanedJson);
            System.out.println("Cleaned text is a valid JSON. Ready to be saved.");
        } catch (IOException e) {
            System.err.println("The cleaned text from AI is NOT a valid JSON: " + cleanedJson);
            throw new IOException("AI did not return a valid JSON string as requested by the prompt.", e);
        }

        // 返回最终纯净的、代表笔记内容的JSON字符串
        return cleanedJson;
    }

    /**
     * 【重要修改】
     * 此方法现在调用新的、专门的文本解析器。
     */
    @Override
    public String generateNotes(String textPrompt, Optional<String> userApiKey) {
        System.out.println("Generating text response from Gemini...");
        try {
            String apiUrl = geminiFlashApiUrl;

            // ... (构建请求头和请求体的代码保持不变) ...
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-goog-api-key", geminiApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> textPart = Map.of("text", textPrompt);
            Map<String, Object> content = Map.of("parts", List.of(textPart));
            Map<String, Object> requestBody = Map.of("contents", List.of(content));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String response = restTemplate.postForObject(apiUrl, entity, String.class);

            // 【关键修改】调用新的解析方法！
            return parseGeminiTextResponse(response);

        } catch (Exception e) {
            System.err.println("Error in Gemini text generation: " + e.getMessage());
            throw new RuntimeException("Failed to generate text from Gemini", e);
        }
    }

    /**
     * 【新增方法】
     * 专用于解析【纯文本】响应的私有方法。
     * 它只提取文本，不进行JSON验证。
     *
     * @param jsonResponse Gemini API返回的原始JSON字符串
     * @return 提取出的纯文本内容
     * @throws IOException 如果响应结构不符合预期
     */
    private String parseGeminiTextResponse(String jsonResponse) throws IOException {
        if (jsonResponse == null || jsonResponse.isBlank()) {
            throw new IOException("Empty or blank response from Gemini.");
        }

        System.out.println("===== RAW TEXT RESPONSE FROM GEMINI =====");
        System.out.println(jsonResponse);
        System.out.println("=========================================");

        JsonNode root = objectMapper.readTree(jsonResponse);

        if (root.has("error")) {
            String errorMessage = root.path("error").path("message").asText("Unknown error");
            throw new IOException("Gemini API returned an error: " + errorMessage);
        }

        JsonNode candidates = root.path("candidates");
        if (candidates.isMissingNode() || !candidates.isArray() || candidates.isEmpty()) {
            String finishReason = root.path("promptFeedback").path("blockReason").asText("unknown reason");
            throw new IOException("Gemini response is missing 'candidates'. Generation may have been blocked for reason: " + finishReason);
        }

        JsonNode textNode = candidates.path(0).path("content").path("parts").path(0).path("text");

        if (textNode.isMissingNode() || !textNode.isTextual()) {
            throw new IOException("Could not find 'text' field in a valid Gemini response structure.");
        }

        // 直接返回文本，不做任何JSON相关的清理或验证
        return textNode.asText();
    }

    @Override
    public boolean supports(Optional<String> userApiKey) {
        return userApiKey.isEmpty();
    }


}