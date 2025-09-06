package com.fan.aivideonotes.service;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OpenAiService implements AiServiceProvider{

    @Override
    public String generateNotes(String transcript, Optional<String> userApiKey) {
        String key = userApiKey.orElseThrow(() ->
                new IllegalArgumentException("OpenAI service requires a user-provided API key.")
        );

        System.out.println("Executing OpenAI Service with user's key (prefix: " + key.substring(0, 5) + "...)");
        // TODO (Future): Implement the logic to call the OpenAI API
        throw new UnsupportedOperationException("OpenAI BYOK feature is not yet implemented.");
    }

    @Override
    public boolean supports(Optional<String> userApiKey) {
        // If the user provides a key, this service should handle it.
        return userApiKey.isPresent();
    }
}
