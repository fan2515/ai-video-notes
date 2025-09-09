package com.fan.aivideonotes.service;

import com.fan.aivideonotes.controller.dto.VideoLinkRequest;
import com.fan.aivideonotes.model.Note;
import com.fan.aivideonotes.model.Task;
import com.fan.aivideonotes.repository.NoteRepository;
import com.fan.aivideonotes.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteGenerationServiceTest {

    @Test
    void generateNotesForVideo_shouldUpdateTaskToCompleted_whenSuccessful() {
        // --- 1. Arrange (准备阶段) ---

        // 手动创建所有的 Mock 对象
        GeminiService mockGeminiService = Mockito.mock(GeminiService.class);
        NoteRepository mockNoteRepository = Mockito.mock(NoteRepository.class);
        VideoProcessingService mockVideoProcessingService = Mockito.mock(VideoProcessingService.class);
        TaskRepository mockTaskRepository = Mockito.mock(TaskRepository.class); // 新增 TaskRepository Mock

        // 手动创建被测试的实例
        NoteGenerationService noteGenerationService = new NoteGenerationService(
                mockGeminiService,
                mockNoteRepository,
                mockVideoProcessingService,
                mockTaskRepository // 传入新的 Mock 对象
        );

        // 定义输入
        String taskId = "test-task-123";
        Long userId = 1L;
        String videoUrl = "https://example.com/video.mp4";
        VideoLinkRequest.GenerationMode mode = VideoLinkRequest.GenerationMode.FLASH;

        // 准备 Mock 对象的行为
        when(mockVideoProcessingService.downloadVideo(anyString())).thenReturn(new File("fake-video.mp4"));
        when(mockVideoProcessingService.extractAudio(any(File.class))).thenReturn(new File("fake-audio.mp3"));
        when(mockGeminiService.generateNotesFromAudio(any(File.class), any())).thenReturn("{\"title\":\"Test\"}");

        Note savedNote = new Note();
        savedNote.setId(1L);
        when(mockNoteRepository.save(any(Note.class))).thenReturn(savedNote);

        // 关键：当 Service 尝试查找 Task 时，让它能找到一个
        Task initialTask = new Task();
        initialTask.setId(taskId);
        when(mockTaskRepository.findById(taskId)).thenReturn(Optional.of(initialTask));


        // --- 2. Act (执行阶段) ---
        noteGenerationService.generateNotesForVideo(taskId, userId, videoUrl, mode);


        // --- 3. Assert (断言/验证阶段) ---

        // 验证 taskRepository.save 被调用了多次（至少两次，一次是更新状态，一次是最终完成）
        verify(mockTaskRepository, atLeast(2)).save(any(Task.class));

        // 捕获最后一次传递给 taskRepository.save 的 Task 对象
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(mockTaskRepository, atLeastOnce()).save(taskCaptor.capture());

        Task finalTask = taskCaptor.getValue();

        // 验证最终的任务状态是否为 "COMPLETED"
        assertEquals("COMPLETED", finalTask.getStatus());
        // 验证最终的 Note 是否被关联上了
        assertEquals(savedNote, finalTask.getResultNote());
    }
}