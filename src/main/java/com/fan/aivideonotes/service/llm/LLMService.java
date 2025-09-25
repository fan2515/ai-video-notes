package com.fan.aivideonotes.service.llm;

import com.fan.aivideonotes.controller.dto.VideoLinkRequest;
import java.io.File;

public interface LLMService {
    String generateNotesFromAudio(File audioFile, VideoLinkRequest request);
    String generateTextResponse(String prompt);
    String getProviderKey();
}