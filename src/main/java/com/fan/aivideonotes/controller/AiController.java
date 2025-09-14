package com.fan.aivideonotes.controller;

import com.fan.aivideonotes.controller.dto.ExplainRequest;
import com.fan.aivideonotes.controller.dto.ExplainResponse;
import com.fan.aivideonotes.service.AiInteractionService;
import org.springframework.beans.factory.annotation.Autowired;
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
        try {
            String answer = aiInteractionService.getExplanation(request.getTerm(), request.getContext());
            return ResponseEntity.ok(new ExplainResponse(answer));
        } catch (Exception e) {
            // 提供更友好的错误信息
            return ResponseEntity.internalServerError().body(new ExplainResponse("抱歉，AI解释服务出现内部错误：" + e.getMessage()));
        }
    }
}