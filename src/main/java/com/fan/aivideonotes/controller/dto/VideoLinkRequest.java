package com.fan.aivideonotes.controller.dto;

import lombok.Data;

@Data
public class VideoLinkRequest {

    private String url;
    private Long userId;

    private GenerationMode mode = GenerationMode.FLASH; // 默认使用快速模式

    public enum GenerationMode {
        FLASH, // 快速模式
        PRO    // 高质量模式
    }
}
