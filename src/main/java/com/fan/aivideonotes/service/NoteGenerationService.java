package com.fan.aivideonotes.service;

import com.fan.aivideonotes.controller.dto.VideoLinkRequest;
import com.fan.aivideonotes.model.Note;
import com.fan.aivideonotes.repository.NoteRepository;
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

    @Autowired
    public NoteGenerationService(@Qualifier("geminiService") AiServiceProvider geminiProvider,
                                 NoteRepository noteRepository,
                                 VideoProcessingService videoProcessingService) {
        this.geminiProvider = geminiProvider;
        this.noteRepository = noteRepository;
        this.videoProcessingService = videoProcessingService;
    }

    @Async("taskExecutor")
    public void generateNotesForVideo(Long userId, String videoUrl, VideoLinkRequest.GenerationMode mode) {
        System.out.println("--- Starting FINAL ONLINE Pipeline Task for URL: " + videoUrl + " ---");

        File videoFile = null;
        File audioFile = null;
        // 我们将 videoFile 的父目录作为整个任务的临时目录
        File tempDirectory = null;

        try {
            // ================================================================
            // 步骤 1: 从在线 URL 下载视频
            System.out.println("Step 1: Downloading video from URL...");
            videoFile = videoProcessingService.downloadVideo(videoUrl);
            tempDirectory = videoFile.getParentFile(); // 获取临时目录的引用
            System.out.println("Step 1: Video downloaded successfully to: " + videoFile.getAbsolutePath());
            // ================================================================


            // ================================================================
            // 步骤 2: 提取音频
            System.out.println("Step 2: Extracting audio...");
            audioFile = videoProcessingService.extractAudio(videoFile);
            System.out.println("Step 2: Audio extracted successfully: " + audioFile.getAbsolutePath());
            // ================================================================


            // ================================================================
            // 步骤 3: 调用 Gemini AI，传入真实的音频文件
            System.out.println("Step 3: Generating notes from audio using multimodal AI...");
            String generatedNotes = ((GeminiService) geminiProvider).generateNotesFromAudio(audioFile, mode);

            System.out.println("----- NOTES GENERATED (Mode: " + mode + ") -----");
            System.out.println(generatedNotes);
            System.out.println("------------------------------------");
            // ================================================================


            // ================================================================
            // 步骤 4: 将结果保存到数据库
            System.out.println("Step 4: Saving generated notes to the database...");
            Note note = new Note();
            note.setUserId(userId);
            note.setVideoUrl(videoUrl); // 保存真实的视频 URL
            note.setContent(generatedNotes);

            Note savedNote = noteRepository.save(note);
            System.out.println("Step 4: Notes saved successfully with ID: " + savedNote.getId());
            // ================================================================

        } catch (Exception e) {
            System.err.println("An error occurred during the final pipeline: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // ================================================================
            // 步骤 5: 清理所有临时文件和目录
            System.out.println("Step 5: Cleaning up temporary files...");
            if (tempDirectory != null && tempDirectory.exists()) {
                deleteDirectory(tempDirectory);
                System.out.println("Cleaned up temporary directory: " + tempDirectory.getAbsolutePath());
            }
            // ================================================================
        }
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