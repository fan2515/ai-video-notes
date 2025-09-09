package com.fan.aivideonotes.service;

import com.fan.aivideonotes.controller.dto.VideoLinkRequest;
import com.fan.aivideonotes.model.Note;
import com.fan.aivideonotes.model.Task;
import com.fan.aivideonotes.repository.NoteRepository;
import com.fan.aivideonotes.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class NoteGenerationService {

    private final AiServiceProvider geminiProvider;
    private final NoteRepository noteRepository;
    private final VideoProcessingService videoProcessingService;
    private final TaskRepository taskRepository; // 注入 TaskRepository

    @Autowired
    public NoteGenerationService(@Qualifier("geminiService") AiServiceProvider geminiProvider,
                                 NoteRepository noteRepository,
                                 VideoProcessingService videoProcessingService,
                                 TaskRepository taskRepository) { // 在构造函数中接收
        this.geminiProvider = geminiProvider;
        this.noteRepository = noteRepository;
        this.videoProcessingService = videoProcessingService;
        this.taskRepository = taskRepository;
    }

    // 方法签名现在需要接收 taskId
    @Async("taskExecutor")
    @Transactional
    public void generateNotesForVideo(String taskId, Long userId, String videoUrl, VideoLinkRequest.GenerationMode mode) {

        updateTaskStatus(taskId, "PROCESSING", "Starting video processing...");

        File videoFile = null;
        File audioFile = null;
        File tempDirectory = null;

        try {
            updateTaskStatus(taskId, "PROCESSING", "Step 1: Downloading video from URL...");
            videoFile = videoProcessingService.downloadVideo(videoUrl);
            tempDirectory = videoFile.getParentFile();

            updateTaskStatus(taskId, "PROCESSING", "Step 2: Extracting audio...");
            audioFile = videoProcessingService.extractAudio(videoFile);

            updateTaskStatus(taskId, "PROCESSING", "Step 3: Generating notes from audio with AI...");
            String generatedNotes = ((GeminiService) geminiProvider).generateNotesFromAudio(audioFile, mode);

            updateTaskStatus(taskId, "PROCESSING", "Step 4: Saving generated notes to the database...");
            Note note = new Note();
            note.setUserId(userId);
            note.setVideoUrl(videoUrl);
            note.setContent(generatedNotes);
            Note savedNote = noteRepository.save(note);

            // 最终成功，更新 Task 状态并关联 Note
            Task finalTask = taskRepository.findById(taskId).orElseThrow();
            finalTask.setStatus("COMPLETED");
            finalTask.setStatusMessage("Note generated successfully.");
            finalTask.setResultNote(savedNote);
            taskRepository.save(finalTask);
            System.out.println("Task " + taskId + " completed successfully.");

        } catch (Exception e) {
            System.err.println("An error occurred during the pipeline for task " + taskId + ": " + e.getMessage());
            e.printStackTrace();
            // 任务失败，更新 Task 状态
            updateTaskStatus(taskId, "FAILED", e.getMessage());
        } finally {
            if (tempDirectory != null && tempDirectory.exists()) {
                deleteDirectory(tempDirectory);
                System.out.println("Cleaned up temporary directory for task " + taskId);
            }
        }
    }

    // 一个辅助方法，用于更新任务状态
    private void updateTaskStatus(String taskId, String status, String message) {
        // 使用 .orElse(new Task()) 可以在任务不存在时创建一个新的
        Task task = taskRepository.findById(taskId).orElse(new Task());
        task.setId(taskId); // 确保 ID 被设置
        task.setStatus(status);
        task.setStatusMessage(message);
        taskRepository.save(task);
    }

    /**
     * 递归删除目录及其所有内容.
     * @param directory 要删除的目录
     */
    private void deleteDirectory(File directory) {
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directory.delete();
    }
}