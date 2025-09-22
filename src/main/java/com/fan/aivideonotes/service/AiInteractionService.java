package com.fan.aivideonotes.service;

import com.fan.aivideonotes.model.GlossaryTerm;
import com.fan.aivideonotes.repository.GlossaryTermRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AiInteractionService {

    private final AiServiceProvider geminiProvider;
    private final GlossaryTermRepository glossaryRepository; // 【新增】注入

    @Autowired
    public AiInteractionService(@Qualifier("geminiService") AiServiceProvider geminiProvider,
                                GlossaryTermRepository glossaryRepository) { // 【新增】
        this.geminiProvider = geminiProvider;
        this.glossaryRepository = glossaryRepository; // 【新增】
    }

    /**
     * 【V1.3 核心改造】
     * 获取术语的深度解释，实现“缓存优先”策略。
     * @param term 要解释的术语
     * @param shortExplanation 术语的简短解释 (用于首次存储)
     * @param context 笔记上下文
     * @return 高质量的深度解释字符串
     */
    @Transactional // 增加事务支持，确保数据库操作的原子性
    public String getExplanation(String term, String shortExplanation, String context) {
        // 1. 先尝试从数据库（缓存）中查找
        Optional<GlossaryTerm> existingTermOpt = glossaryRepository.findByTerm(term);

        if (existingTermOpt.isPresent() && existingTermOpt.get().getLongExplanation() != null) {
            System.out.println("CACHE HIT: Found explanation for '" + term + "' in database.");
            return existingTermOpt.get().getLongExplanation();
        }

        // 2. 如果数据库中没有，再调用 AI 生成
        System.out.println("CACHE MISS: Generating new explanation for '" + term + "' via AI.");
        String prompt = buildPrompt(term, context);
        String longExplanation = geminiProvider.generateNotes(prompt, Optional.empty());

        // 3. 将新生成的解释存入数据库
        GlossaryTerm termToSave = existingTermOpt.orElseGet(() -> {
            GlossaryTerm newTerm = new GlossaryTerm();
            newTerm.setTerm(term);
            newTerm.setShortExplanation(shortExplanation);
            return newTerm;
        });
        termToSave.setLongExplanation(longExplanation);
        glossaryRepository.save(termToSave);
        System.out.println("SAVED: New explanation for '" + term + "' has been saved to the database.");

        return longExplanation;
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