package com.fan.aivideonotes.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NoteDto {
    private Long id;
    private String videoUrl;
    private String content; // 未来可以是一个结构化的 NoteContent 对象
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
