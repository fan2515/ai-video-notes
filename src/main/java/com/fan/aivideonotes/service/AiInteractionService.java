package com.fan.aivideonotes.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AiInteractionService {

    private final AiServiceProvider geminiProvider;

    @Autowired
    public AiInteractionService(@Qualifier("geminiService") AiServiceProvider geminiProvider) {
        this.geminiProvider = geminiProvider;
    }

    public String getExplanation(String term, String context) {
        String prompt = buildPrompt(term, context);
        // 我们需要一种方式调用AI服务处理纯文本prompt。
        // 我们将为 AiServiceProvider 接口添加一个新方法。
        return geminiProvider.generateNotes(prompt, Optional.empty());
    }

    private String buildPrompt(String term, String context) {
        // 精心设计的 Prompt，引导 AI 给出高质量的回答
        return String.format(
                """
                你是一位资深的软件架构师和技术导师。一位学生正在学习关于 "%s" 的知识，这是他/她学习笔记的上下文：
    
                --- 笔记上下文 ---
                %s
                --- 结束 ---
    
                请你用通俗易懂、循循善诱的方式，向这位学生详细解释 "%s" 是什么。请专注于以下几点：
                1. 它的核心思想或解决了什么问题？
                2. 为什么在上述笔记的上下文中会提到它？
                3. (可选) 给出一个简单的例子或比喻来帮助理解。
    
                你的回答应该结构清晰，重点突出。请直接返回解释内容，无需客套。
                """,
                term, context, term
        );
    }
}