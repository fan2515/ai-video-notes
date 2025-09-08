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

            // 4. 构建包含内联数据的请求体
            Map<String, Object> inlineData = Map.of("inlineData", Map.of("mimeType", "audio/mpeg", "data", encodedString));
            Map<String, Object> textData = Map.of("text", promptTemplate);
            Map<String, Object> content = Map.of("parts", List.of(textData, inlineData));
            Map<String, Object> requestBody = Map.of("contents", List.of(content));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 5. 使用 RestTemplate 发送请求
            String response = restTemplate.postForObject(apiUrl, entity, String.class);
            return parseGeminiResponse(response);

        } catch (Exception e) {
            System.err.println("Error in Gemini Base64 process: " + e.getMessage());
            throw new RuntimeException("Failed to generate notes from audio via Base64", e);
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

    private String parseGeminiResponse(String jsonResponse) throws IOException {
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            return "Error: Empty response from Gemini.";
        }
        JsonNode root = objectMapper.readTree(jsonResponse);
        String text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("Error: 'text' field not found.");
        return text;
    }

    @Override
    public String generateNotes(String transcript, Optional<String> userApiKey) {
        throw new UnsupportedOperationException("This service uses multimodal generation, please use generateNotesFromAudio.");
    }

    @Override
    public boolean supports(Optional<String> userApiKey) {
        return userApiKey.isEmpty();
    }
}