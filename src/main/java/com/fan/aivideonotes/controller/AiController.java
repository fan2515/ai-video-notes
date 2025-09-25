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

    /**
     * [重构后]
     * 处理术语解释的请求。
     * 它现在可以接收 provider 字段来动态选择 AI 模型。
     */
    @PostMapping("/explain")
    public ResponseEntity<ExplainResponse> getExplanation(@RequestBody ExplainRequest request) {
        // 输入校验保持不变
        if (request.getTerm() == null || request.getTerm().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ExplainResponse("请求参数不完整: 'term' 是必需的。"));
        }

        try {
            // 调用重构后的 Service 方法，并传入 provider
            // 注意：request.getProvider() 此时可能为 null，Service 层的工厂类会处理这种情况
            String answer = aiInteractionService.getExplanation(
                    request.getTerm(),
                    request.getShortExplanation(),
                    request.getContext(),
                    request.getProvider() // 将 provider 传递下去
            );
            return ResponseEntity.ok(new ExplainResponse(answer));

        } catch (UnsupportedOperationException e) {
            // 捕获模型不支持操作的特定异常
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ExplainResponse("选择的模型不支持此操作: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ExplainResponse("AI解释服务出现内部错误: " + e.getMessage()));
        }
    }
}