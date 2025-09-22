package com.fan.aivideonotes.controller;

import com.fan.aivideonotes.controller.dto.ExplainRequest;
import com.fan.aivideonotes.controller.dto.ExplainResponse;
import com.fan.aivideonotes.service.AiInteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiInteractionService aiInteractionService;

    @Autowired
    public AiController(AiInteractionService aiInteractionService) {
        this.aiInteractionService = aiInteractionService;
    }

    @PostMapping("/explain")
    public ResponseEntity<ExplainResponse> getExplanation(@RequestBody ExplainRequest request) {
        // 【新增】输入校验
        if (request.getTerm() == null || request.getTerm().isBlank() ||
                request.getShortExplanation() == null) { // shortExplanation 可以为空字符串，但不能为 null
            return ResponseEntity.badRequest()
                    .body(new ExplainResponse("请求参数不完整: term 和 shortExplanation 是必需的。"));
        }

        try {
            // 【修改】将所有三个参数都传递给 Service
            String answer = aiInteractionService.getExplanation(
                    request.getTerm(),
                    request.getShortExplanation(),
                    request.getContext()
            );
            return ResponseEntity.ok(new ExplainResponse(answer));

        } catch (Exception e) {
            e.printStackTrace(); // 在开发时打印完整错误，方便调试
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ExplainResponse("抱歉，AI解释服务出现内部错误：" + e.getMessage()));
        }
    }
}