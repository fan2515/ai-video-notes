// GeminiService.java (最终多模态版)
package com.fan.aivideonotes.service;

import com.fan.aivideonotes.controller.dto.VideoLinkRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

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

    private final WebClient webClient;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final String promptTemplate;

    @Value("${app.api.gemini.key}")
    private String geminiApiKey;
    @Value("${app.api.gemini.url.flash}")
    private String geminiFlashApiUrl;
    @Value("${app.api.gemini.url.pro}")
    private String geminiProApiUrl;

    public GeminiService(WebClient webClient, ResourceLoader resourceLoader, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.resourceLoader = resourceLoader; // 尽管不再直接用它加载，但保留注入是好习惯
        this.objectMapper = objectMapper;
        // 直接调用，不再传递参数
        this.promptTemplate = loadPromptTemplate();
    }

    public String generateNotesFromAudio(File audioFile, VideoLinkRequest.GenerationMode mode) {
        System.out.println("Generating notes from audio using Base64 inline method...");
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

            // 3. 构建包含内联数据的请求体
            Map<String, Object> inlineData = Map.of("inlineData", Map.of("mimeType", "audio/mpeg", "data", encodedString));
            Map<String, Object> textData = Map.of("text", promptTemplate);
            Map<String, Object> content = Map.of("parts", List.of(textData, inlineData));
            Map<String, Object> requestBody = Map.of("contents", List.of(content));

            // 4. 发送请求
            String response = webClient.post()
                    .uri(apiUrl)
                    .header("X-goog-api-key", geminiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseGeminiResponse(response);

        } catch (Exception e) {
            System.err.println("Error in Gemini Base64 process: " + e.getMessage());
            throw new RuntimeException("Failed to generate notes from audio via Base64", e);
        }
    }

    // generateContent 方法需要用 WebClient 重写
    private String generateContent(String fileName, String apiUrl) throws IOException {
        Map<String, Object> fileData = Map.of("fileData", Map.of("mimeType", "audio/mpeg", "name", fileName));
        Map<String, Object> textData = Map.of("text", promptTemplate);
        Map<String, Object> content = Map.of("parts", List.of(textData, fileData));
        Map<String, Object> requestBody = Map.of("contents", List.of(content));

        String response = webClient.post()
                .uri(apiUrl)
                .header("X-goog-api-key", geminiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return parseGeminiResponse(response);
    }

    private String parseGeminiResponse(String jsonResponse) throws IOException {
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            return "Error: Empty response from Gemini.";
        }
        JsonNode root = objectMapper.readTree(jsonResponse);
        String text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("Error: 'text' field not found.");
        return text;
    }

    private String loadPromptTemplate() {
        try {
            // --- START: 核心修改 ---
            // 我们不再使用 location 参数，而是直接在这里硬编码 ClassPathResource
            // 这能确保 Spring 总是从 src/main/resources 下去查找，而不会用错加载器
            Resource resource = new ClassPathResource("prompts/notes_generation_prompt_multimodal.txt");
            // --- END: 核心修改 ---

            if (!resource.exists()) {
                throw new IOException("Prompt template not found at: " + "prompts/notes_generation_prompt_multimodal.txt");
            }

            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                return FileCopyUtils.copyToString(reader);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load prompt template", e);
        }
    }

    @Override
    public String generateNotes(String transcript, Optional<String> userApiKey) {
        throw new UnsupportedOperationException("Use generateNotesFromAudio for multimodal generation.");
    }

    @Override
    public boolean supports(Optional<String> userApiKey) {
        return userApiKey.isEmpty();
    }
}