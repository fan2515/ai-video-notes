package com.fan.aivideonotes.controller.dto;

import lombok.Data;

@Data
public class ExplainRequest {
    private String term;    // 被点击的知识点
    private String context; // 笔记的上下文
}
