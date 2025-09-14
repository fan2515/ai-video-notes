package com.fan.aivideonotes.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // 建议保留，为未来的用户系统做准备
    private String videoUrl;

    /**
     * 【核心修改】
     * 现在这个字段将存储由AI生成的、代表笔记结构的完整JSON字符串。
     * 例如：'{"notes": [{"type": "heading", "content": "..."}, ...]}'
     * 注解 @Lob 和 @Column(columnDefinition = "TEXT") 保持不变，非常适合存储长文本/JSON。
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}