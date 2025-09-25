package com.fan.aivideonotes.controller.dto;

import lombok.Data;

@Data
public class VideoLinkRequest {

    private String url;
    private Long userId;

    private GenerationMode mode = GenerationMode.FLASH; // 默认使用快速模式

    // 模型选择相关数据 (为 V1.4.0 准备)
    private String provider; // 用户选择的模型提供商, e.g., "GEMINI", "KIMI"
    private String apiKey;   // 用户可选提供的 API Key
    public enum GenerationMode {
        FLASH, // 快速模式
        PRO    // 高质量模式
    }
}
