package com.fan.aivideonotes.service.llm;

import com.fan.aivideonotes.controller.dto.VideoLinkRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.HttpServerErrorException;
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



/**
 * [重构后]
 * 实现了 LLMService 接口，用于与 Google Gemini API 进行交互。
 * 这是系统默认的 AI 服务提供商。
 */
@Service("geminiService")
public class GeminiService implements LLMService {

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

    // --- LLMService 接口实现 ---

    @Override
    @Retryable(
            retryFor = { HttpServerErrorException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public String generateNotesFromAudio(File audioFile, VideoLinkRequest request) {
        System.out.println("Generating notes from audio using Gemini Multi-modal API...");
        try {
            byte[] fileContent = Files.readAllBytes(audioFile.toPath());
            String encodedString = Base64.getEncoder().encodeToString(fileContent);

            VideoLinkRequest.GenerationMode mode = request.getMode();
            String apiUrl = switch (mode) {
                case PRO -> geminiProApiUrl;
                default -> geminiFlashApiUrl;
            };
            System.out.println("Using Gemini model mode: " + mode + " with URL: " + apiUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-goog-api-key", geminiApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> textData = Map.of("text", promptTemplate);
            Map<String, Object> inlineData = Map.of("inlineData", Map.of("mimeType", "audio/mpeg", "data", encodedString));
            Map<String, Object> content = Map.of("parts", List.of(textData, inlineData));
            Map<String, Object> requestBody = Map.of("contents", List.of(content));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(apiUrl, entity, String.class);

            return parseGeminiResponse(response);

        } catch (IOException e) {
            System.err.println("File handling error during Gemini audio processing: " + e.getMessage());
            throw new RuntimeException("Failed to read or encode audio file for Gemini.", e);
        } catch (Exception e) {
            System.err.println("Error in Gemini audio processing: " + e.getMessage());
            throw new RuntimeException("Failed to generate notes from audio with Gemini: " + e.getMessage(), e);
        }
    }

    @Override
    @Retryable(
            retryFor = { HttpServerErrorException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public String generateTextResponse(String prompt) {
        System.out.println("Generating text response from Gemini API...");
        try {
            String apiUrl = geminiFlashApiUrl;

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-goog-api-key", geminiApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> textPart = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", List.of(textPart));
            Map<String, Object> requestBody = Map.of("contents", List.of(content));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String response = restTemplate.postForObject(apiUrl, entity, String.class);

            return parseGeminiTextResponse(response);

        } catch (Exception e) {
            System.err.println("Error in Gemini text generation: " + e.getMessage());
            throw new RuntimeException("Failed to generate text from Gemini", e);
        }
    }

    @Override
    public String getProviderKey() {
        return "GEMINI";
    }


    // --- 私有辅助方法 (Private Helper Methods) ---

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
     * 解析 Gemini API 返回的、期望为【JSON结构】的响应 (用于笔记生成)。
     */
    private String parseGeminiResponse(String jsonResponse) throws IOException {
        if (jsonResponse == null || jsonResponse.isBlank()) {
            throw new IOException("Empty or blank response from Gemini.");
        }

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

        String rawText = textNode.asText();
        String cleanedJson = rawText.trim().replaceAll("^```json\\s*", "").replaceAll("\\s*```$", "");

        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(cleanedJson);
        } catch (IOException e) {
            System.err.println("The cleaned text from AI is NOT a valid JSON: " + cleanedJson);
            throw new IOException("AI did not return a valid JSON string as requested by the prompt.", e);
        }

        if (rootNode.isArray()) {
            Map<String, JsonNode> wrapper = Map.of("notes", rootNode);
            cleanedJson = objectMapper.writeValueAsString(wrapper);
        } else if (rootNode.isObject() && !rootNode.has("notes")) {
            throw new IOException("AI returned a valid JSON object but is missing the required top-level 'notes' key.");
        }

        return cleanedJson;
    }

    /**
     * 解析 Gemini API 返回的、期望为【纯文本】的响应 (用于术语解释等)。
     */
    private String parseGeminiTextResponse(String jsonResponse) throws IOException {
        if (jsonResponse == null || jsonResponse.isBlank()) {
            throw new IOException("Empty or blank response from Gemini.");
        }

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

        return textNode.asText();
    }
}