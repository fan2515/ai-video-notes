package com.fan.aivideonotes.service;

import com.fan.aivideonotes.controller.dto.VideoLinkRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Optional;

@Service
public class OpenAiService implements AiServiceProvider{

    /**
     * 补上接口中定义的 generateNotesFromAudio 方法。
     * 因为 OpenAI 的标准 API 不直接处理音频文件，我们明确地抛出“不支持操作”的异常。
     */
    @Override
    public String generateNotesFromAudio(File audioFile, VideoLinkRequest.GenerationMode mode) {
        throw new UnsupportedOperationException("OpenAI service does not support direct audio file input for note generation in this implementation.");
    }

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
