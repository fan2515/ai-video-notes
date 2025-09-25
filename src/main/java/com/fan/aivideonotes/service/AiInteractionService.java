package com.fan.aivideonotes.service;

import com.fan.aivideonotes.model.GlossaryTerm;
import com.fan.aivideonotes.repository.GlossaryTermRepository;
import com.fan.aivideonotes.service.llm.LLMService; // 【注意】导入新的接口
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AiInteractionService {

    // 注入新的 LLMServiceProvider
    private final LLMServiceProvider llmServiceProvider;
    private final GlossaryTermRepository glossaryRepository;

    @Autowired
    public AiInteractionService(LLMServiceProvider llmServiceProvider,
                                GlossaryTermRepository glossaryRepository) {
        this.llmServiceProvider = llmServiceProvider;
        this.glossaryRepository = glossaryRepository;
    }

    /**
     * [重构后]
     * 获取术语的深度解释，实现“缓存优先”策略。
     * 此方法现在通过 LLMServiceProvider 动态选择 AI 模型。
     *
     * @param term             要解释的术语
     * @param shortExplanation 术语的简短解释 (用于首次存储)
     * @param context          笔记上下文
     * @param providerKey      用户选择的模型提供商 (e.g., "GEMINI", "KIMI")
     * @return 高质量的深度解释字符串
     */
    @Transactional
    public String getExplanation(String term, String shortExplanation, String context, String providerKey) {
        // 1. 先尝试从数据库中查找
        Optional<GlossaryTerm> existingTermOpt = glossaryRepository.findByTerm(term);

        GlossaryTerm termEntity;

        // 【核心修正逻辑】
        if (existingTermOpt.isPresent()) {
            termEntity = existingTermOpt.get();
            // 如果已经存在，并且已经有长解释了，直接返回，不再执行后续操作
            if (termEntity.getLongExplanation() != null && !termEntity.getLongExplanation().isBlank()) {
                System.out.println("CACHE HIT: Found explanation for '" + term + "' in database.");
                return termEntity.getLongExplanation();
            }
        } else {
            // 如果不存在，创建一个新的实例
            termEntity = new GlossaryTerm();
            termEntity.setTerm(term);
            termEntity.setShortExplanation(shortExplanation);
        }

        // 2. 如果代码能执行到这里，说明需要调用 AI 生成
        System.out.println("CACHE MISS or EXPLANATION NEEDED: Generating new explanation for '" + term + "' via AI.");
        String prompt = buildPrompt(term, context);

        LLMService selectedLlmService = llmServiceProvider.getProvider(providerKey);
        String longExplanation = selectedLlmService.generateTextResponse(prompt);

        // 3. 将新生成的解释设置到实体上，然后保存（这时JPA会智能判断是 INSERT 还是 UPDATE）
        termEntity.setLongExplanation(longExplanation);
        glossaryRepository.save(termEntity);
        System.out.println("SAVED/UPDATED: Explanation for '" + term + "' has been saved to the database.");

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
    
                请你用通俗易懂、循循诱导的方式，向这位学生详细解释 "%s" 是什么。请专注于以下几点：
                1. 它的核心思想或解决了什么问题？
                2. 为什么在上述笔记的上下文中会提到它？
                3. (可选) 给出一个简单的例子或比喻来帮助理解。
    
                你的回答应该结构清晰，重点突出。请直接返回解释内容，无需客套。
                """,
                term, context, term
        );
    }
}