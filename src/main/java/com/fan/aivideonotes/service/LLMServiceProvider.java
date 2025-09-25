package com.fan.aivideonotes.service;

import com.fan.aivideonotes.service.llm.LLMService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LLMServiceProvider {
    private final Map<String, LLMService> providerMap;
    private static final String DEFAULT_PROVIDER = "GEMINI";

    public LLMServiceProvider(List<LLMService> services) {
        this.providerMap = services.stream()
                .collect(Collectors.toMap(
                        llmService -> llmService.getProviderKey().toUpperCase(),
                        llmService -> llmService
                ));
    }

    public LLMService getProvider(String providerKey) {
        final String key = (providerKey == null || providerKey.isBlank()) ? DEFAULT_PROVIDER : providerKey.toUpperCase();
        return Optional.ofNullable(providerMap.get(key))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported LLM provider: " + key));
    }
}