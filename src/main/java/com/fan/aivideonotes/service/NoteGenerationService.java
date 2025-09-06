package com.fan.aivideonotes.service;


import com.fan.aivideonotes.model.User;
import com.fan.aivideonotes.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NoteGenerationService {

    private final List<AiServiceProvider> aiServiceProviders;
    private final UserRepository userRepository;

    @Autowired
    public NoteGenerationService(List<AiServiceProvider> aiServiceProviders, UserRepository userRepository) {
        this.aiServiceProviders = aiServiceProviders;
        this.userRepository = userRepository;
    }

    @Async("taskExecutor") // 明确指定线程池，更规范
    public void generateNotesForVideo(Long userId) {
        // 模拟耗时操作
        try {
            // 模拟视频下载和音频提取
            System.out.println("Starting video processing for user: " + userId);
            Thread.sleep(5000); // 模拟5秒耗时
            String transcript = "AI is a branch of computer science.";
            System.out.println("Transcript extracted.");

            // --- 核心调度逻辑 ---
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            Optional<String> userApiKey = Optional.ofNullable(user.getOpenaiApiKey())
                    .filter(key -> !key.isBlank());

            AiServiceProvider provider = aiServiceProviders.stream()
                    .filter(p -> p.supports(userApiKey))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No suitable AI service provider found for the given configuration."));

            String generatedNotes = provider.generateNotes(transcript, userApiKey);

            System.out.println("----- NOTES GENERATED -----");
            System.out.println(generatedNotes);
            System.out.println("---------------------------");
            // TODO: Save notes to the database
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Task was interrupted.");
        }
    }
}
