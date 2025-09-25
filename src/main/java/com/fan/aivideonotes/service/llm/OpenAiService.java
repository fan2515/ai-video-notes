package com.fan.aivideonotes.service.llm;

import com.fan.aivideonotes.controller.dto.VideoLinkRequest;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * [重构后]
 * 实现了 LLMService 接口，作为 OpenAI API 的一个占位符服务。
 * 当前版本尚未实现具体功能，但已为未来扩展做好了准备。
 */
@Service("openAiService")
public class OpenAiService implements LLMService {

    @Override
    public String generateNotesFromAudio(File audioFile, VideoLinkRequest request) {
        // OpenAI 的标准 Chat Completions API 不直接支持音频文件输入。
        // 如果要实现，需要先调用 Whisper API 进行语音转文本。
        throw new UnsupportedOperationException("OpenAIService: Direct audio-to-note generation is not supported. Please use an ASR service first.");
    }

    @Override
    public String generateTextResponse(String prompt) {
        // 这是未来需要实现的核心逻辑。
        // 在这里，我们会使用 RestTemplate 或 OpenAI 的官方 SDK 来调用 API。
        // 通常需要从 application.properties 或用户请求中获取 API Key。
        System.out.println("Executing OpenAI Service (Not Implemented)...");
        throw new UnsupportedOperationException("OpenAIService: Text generation feature is not yet implemented.");
    }

    @Override
    public String getProviderKey() {
        // 返回一个唯一的、大写的标识符。
        return "OPENAI";
    }
}