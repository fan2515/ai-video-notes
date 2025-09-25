package com.fan.aivideonotes.controller.dto;

import lombok.Data;

@Data
public class ExplainRequest {
    private String term;    // 被点击的知识点
    private String context; // 笔记的上下文
    private String shortExplanation;

    // 模型选择相关数据 (为 V1.4.0 准备)
    private String provider; // 用户选择的模型提供商, e.g., "GEMINI", "KIMI"
    private String apiKey;   // 用户可选提供的 API Key
}
