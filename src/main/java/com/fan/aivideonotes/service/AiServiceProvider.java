package com.fan.aivideonotes.service;

import java.util.Optional;

public interface AiServiceProvider {

    /**
     * 根据文本记录生成笔记.
     *
     * @param transcript 视频的文字记录
     * @param userApiKey 用户可选提供的 API Key. 如果为 Optional.empty(), 则使用默认服务.
     * @return 生成的笔记内容 (通常是 JSON 格式的字符串)
     */
    String generateNotes(String transcript, Optional<String> userApiKey);

    /**
     * 判断此服务提供商是否能处理给定的用户 Key.
     * @param userApiKey 用户提供的 API Key (可能为空)
     * @return 如果能处理，返回 true
     */
    boolean supports(Optional<String> userApiKey);



}
