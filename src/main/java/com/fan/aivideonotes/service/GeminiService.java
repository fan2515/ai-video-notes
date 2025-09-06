package com.fan.aivideonotes.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GeminiService implements AiServiceProvider {

    private final RestTemplate restTemplate;
    private final ResourceLoader resourceLoader;
    private final String promptTemplate;

    // 从 application.properties 注入配置
    @Value("${app.api.gemini.key}")
    private String geminiApiKey;

    @Value("${app.api.gemini.url}")
    private String geminiApiUrl;


    private final ObjectMapper objectMapper;

    // 修改构造函数
    public GeminiService(RestTemplate restTemplate, ResourceLoader resourceLoader, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper; // 注入 ObjectMapper
        this.promptTemplate = loadPromptTemplate("classpath:prompts/notes_generation_prompt.txt");
    }

    // 替换 generateNotes 方法
    @Override
    public String generateNotes(String transcript, Optional<String> userApiKey) {
        System.out.println("Executing Gemini Service with STREAMING request...");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-goog-api-key", geminiApiKey);

        String prompt = createPromptForNotes(transcript);
        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(textPart));
        Map<String, Object> requestBody = Map.of("contents", List.of(content));

        try {
            StringBuilder fullResponse = new StringBuilder();
            restTemplate.execute(geminiApiUrl, HttpMethod.POST, request -> {
                request.getHeaders().putAll(headers);
                try (OutputStream os = request.getBody()) {
                    // 使用 ObjectMapper 将 Map 写入输出流，这是正确的做法
                    objectMapper.writeValue(os, requestBody);
                }
            }, response -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
                    String line;
                    System.out.println("--- Streaming Response Started ---");
                    while ((line = reader.readLine()) != null) {
                        System.out.println("Received chunk: " + line); // 实时打印收到的每个数据块
                        fullResponse.append(line).append("\n");
                    }
                    System.out.println("--- Streaming Response Ended ---");
                }
                return null;
            });

            return parseStreamingResponse(fullResponse.toString());

        } catch (Exception e) {
            System.err.println("Error calling Gemini Streaming API: " + e.getMessage());
            return "Error: Failed to generate notes from Gemini. Details: " + e.getMessage();
        }
    }

    // 新增一个方法来解析流式响应
    private String parseStreamingResponse(String rawResponse) {
        StringBuilder contentBuilder = new StringBuilder();
        try {
            // 1. 将整个原始响应字符串，解析成一个包含 Map 对象的 List
            //    每个 Map 对象就代表我们收到的一个数据块 (chunk)
            List<Map<String, Object>> chunks = objectMapper.readValue(rawResponse, new TypeReference<>() {});

            // 2. 遍历每一个数据块
            for (Map<String, Object> chunk : chunks) {
                // 3. 按照 JSON 结构，一层一层地往下找，直到找到 "text"
                //    这比正则表达式健壮得多！
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) chunk.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    if (content != null) {
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            String textChunk = (String) parts.get(0).get("text");
                            if (textChunk != null) {
                                contentBuilder.append(textChunk);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error parsing streaming JSON response: " + e.getMessage());
            return "Error: Could not parse content from streaming response. Raw data: " + rawResponse;
        }

        if (contentBuilder.isEmpty()) {
            return "Error: No text content found in streaming response.";
        }

        // 4. 返回最终拼接好的、干净的完整内容
        return contentBuilder.toString();
    }

    @Override
    public boolean supports(Optional<String> userApiKey) {
        return userApiKey.isEmpty();
    }

    private String createPromptForNotes(String transcript) {
        return String.format(promptTemplate, transcript);
    }

    private String loadPromptTemplate(String location) {
        try {
            Resource resource = resourceLoader.getResource(location);
            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                return FileCopyUtils.copyToString(reader);
            }
        } catch (IOException e) {
            System.err.println("FATAL: Could not load prompt template from " + location);
            throw new IllegalStateException("Failed to load prompt template", e);
        }
    }

    private String parseGeminiResponse(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            return "Error: Empty response from Gemini.";
        }

        System.out.println("\n===== RAW RESPONSE FROM GEMINI START =====");
        System.out.println(jsonResponse);
        System.out.println("===== RAW RESPONSE FROM GEMINI END =====\n");

        try {
            // 这是一个更健壮的解析方法，它会找到第一个 "text": "..." 的内容
            String searchText = "\"text\": \"";
            int startIndex = jsonResponse.indexOf(searchText);
            if (startIndex == -1) {
                return "Error: Cannot find 'text' field in Gemini's response.";
            }

            startIndex += searchText.length();

            int endIndex = jsonResponse.indexOf("\"", startIndex);
            if (endIndex == -1) {
                return "Error: Cannot find closing quote for 'text' field.";
            }

            String extractedText = jsonResponse.substring(startIndex, endIndex);

            // 清理转义字符
            return extractedText.replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\t", "\t");

        } catch (Exception e) {
            return "Error: Exception while parsing Gemini's response. Details: " + e.getMessage();
        }
    }
}


