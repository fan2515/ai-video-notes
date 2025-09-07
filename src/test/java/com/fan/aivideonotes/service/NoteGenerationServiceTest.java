package com.fan.aivideonotes.service;

import com.fan.aivideonotes.controller.dto.VideoLinkRequest;
import com.fan.aivideonotes.model.Note;
import com.fan.aivideonotes.repository.NoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteGenerationServiceTest {

    @Test
    void generateNotesForVideo_shouldSaveNote_whenSuccessful() {
        // --- 1. Arrange (准备阶段) ---

        // 手动创建所有的 Mock (假) 对象
        GeminiService mockGeminiService = Mockito.mock(GeminiService.class);
        NoteRepository mockNoteRepository = Mockito.mock(NoteRepository.class);
        VideoProcessingService mockVideoProcessingService = Mockito.mock(VideoProcessingService.class);

        // ======================= START: 核心修改 =======================
        // 手动创建被测试的对象，直接传入 mockGeminiService
        // 因为 GeminiService 实现了 AiServiceProvider，所以类型是匹配的
        NoteGenerationService noteGenerationService = new NoteGenerationService(
                mockGeminiService, // <-- 直接传递单个 Mock 对象，不再创建 List
                mockNoteRepository,
                mockVideoProcessingService
        );
        // ======================= END: 核心修改 =======================

        // 定义我们期望的输入
        Long userId = 1L;
        String videoUrl = "https://example.com/video.mp4";
        VideoLinkRequest.GenerationMode mode = VideoLinkRequest.GenerationMode.FLASH;

        // 准备假的 File 对象
        File mockAudioFile = new File("test.mp3");

        // 准备假的 AI 生成结果
        String fakeGeneratedNotes = "{\"title\":\"测试笔记\"}";

        // 准备假的数据库保存后的 Note 对象
        Note fakeSavedNote = new Note();
        fakeSavedNote.setId(99L);

        // "录制" Mock 对象的行为：
        when(mockVideoProcessingService.extractAudio(any(File.class))).thenReturn(mockAudioFile);
        when(mockGeminiService.generateNotesFromAudio(any(File.class), any(VideoLinkRequest.GenerationMode.class)))
                .thenReturn(fakeGeneratedNotes);
        when(mockNoteRepository.save(any(Note.class))).thenReturn(fakeSavedNote);


        // --- 2. Act (执行阶段) ---

        // 调用我们要测试的方法
        noteGenerationService.generateNotesForVideo(userId, videoUrl, mode);


        // --- 3. Assert (断言/验证阶段) ---

        // 验证 noteRepository.save 方法是否被确切地调用了 1 次
        verify(mockNoteRepository).save(any(Note.class));

        // 捕获传递给 save 方法的 Note 对象
        ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
        verify(mockNoteRepository).save(noteCaptor.capture());

        Note capturedNote = noteCaptor.getValue();

        // 验证被保存的 Note 对象的内容是否符合我们的预期
        assertEquals(userId, capturedNote.getUserId());
        assertEquals(fakeGeneratedNotes, capturedNote.getContent());
    }
}