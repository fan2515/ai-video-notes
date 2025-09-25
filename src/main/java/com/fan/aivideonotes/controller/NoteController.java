package com.fan.aivideonotes.controller;

import com.fan.aivideonotes.controller.dto.NoteDto;
import com.fan.aivideonotes.controller.dto.TaskResponse;
import com.fan.aivideonotes.controller.dto.VideoLinkRequest;
import com.fan.aivideonotes.model.Note;
import com.fan.aivideonotes.model.Task;
import com.fan.aivideonotes.repository.NoteRepository;
import com.fan.aivideonotes.repository.TaskRepository;
import com.fan.aivideonotes.service.AiInteractionService;
import com.fan.aivideonotes.service.NoteGenerationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/notes") // 基础路径保持不变，代表“笔记”相关的操作
public class NoteController {

    private final NoteGenerationService noteGenerationService;
    private final TaskRepository taskRepository;
    private final NoteRepository noteRepository;
    private final AiInteractionService aiInteractionService;
    private final ObjectMapper objectMapper;

    @Autowired
    public NoteController(NoteGenerationService noteGenerationService,
                          TaskRepository taskRepository,
                          NoteRepository noteRepository,
                          AiInteractionService aiInteractionService,
                          ObjectMapper objectMapper) {
        this.noteGenerationService = noteGenerationService;
        this.taskRepository = taskRepository;
        this.noteRepository = noteRepository;
        this.aiInteractionService = aiInteractionService;
        this.objectMapper = objectMapper;
    }

    /**
     * [重构后]
     * 接收前端请求，创建并启动一个异步的笔记生成任务。
     */
    @PostMapping("/generate")
    public ResponseEntity<TaskResponse> generateNotes(@RequestBody VideoLinkRequest request) {
        if (request.getUserId() == null || request.getUrl() == null || request.getUrl().isBlank()) {
            return ResponseEntity.badRequest().body(new TaskResponse("User ID and video URL are required.", null));
        }

        String taskId = UUID.randomUUID().toString();
        Task task = new Task();
        task.setId(taskId);
        task.setStatus("PENDING");
        task.setStatusMessage("Task has been queued for processing.");
        taskRepository.save(task);

        // 调用重构后的 service 方法，直接传递整个 request 对象
        noteGenerationService.generateNotesForVideo(taskId, request);

        return ResponseEntity.ok(new TaskResponse("Note generation task created successfully.", taskId));
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<NoteDto> getNoteById(@PathVariable Long noteId) {
        return noteRepository.findById(noteId)
                .map(note -> {
                    NoteDto dto = new NoteDto();
                    dto.setId(note.getId());
                    dto.setVideoUrl(note.getVideoUrl());
                    dto.setContent(note.getContent());
                    dto.setCreatedAt(note.getCreatedAt());
                    return dto;
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ========================= 【V1.3 新增 API】 =========================
    /**
     * 导出笔记时，后端实时为每个术语生成高质量解释，并组装成交互式Markdown。
     */
    @PostMapping("/export/{noteId}")
    public ResponseEntity<String> exportNoteAsInteractiveMarkdown(
            @PathVariable Long noteId,
            @RequestHeader(value = "X-Provider", required = false) String provider
            // apiKey 暂时不处理，可以在 V1.4.0 中添加
            // @RequestHeader(value = "X-API-Key", required = false) String apiKey
    ) {
        try {
            Note note = noteRepository.findById(noteId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note with ID " + noteId + " not found"));

            String noteJsonContent = note.getContent();
            JsonNode rootNode = objectMapper.readTree(noteJsonContent);
            JsonNode notesArrayNode = rootNode.get("notes");
            List<Map<String, Object>> noteBlocks = objectMapper.convertValue(notesArrayNode, new TypeReference<>() {});

            List<String> termsToExplain = new ArrayList<>();
            StringBuilder contextBuilder = new StringBuilder();
            for (Map<String, Object> block : noteBlocks) {
                if ("knowledge_point".equals(block.get("type"))) {
                    Map<String, String> contentMap = (Map<String, String>) block.get("content");
                    termsToExplain.add(contentMap.get("term"));
                }
                // ... 此处上下文构建逻辑不变 ...
            }
            String noteContext = contextBuilder.toString();

            // 为每个术语获取高质量解释，并传入 provider
            Map<String, String> highQualityExplanations = new HashMap<>();
            for (String term : termsToExplain) {
                // 调用重构后的 service 方法
                String explanation = aiInteractionService.getExplanation(term, null, noteContext, provider);
                highQualityExplanations.put(term, explanation);
            }

            // 5. 【核心】用 Java 代码拼接最终的、带 <details> 的 Markdown 字符串
            StringBuilder markdownBuilder = new StringBuilder();
            for (Map<String, Object> block : noteBlocks) {
                String type = (String) block.get("type");
                Object content = block.get("content");

                switch (type) {
                    case "heading":
                        markdownBuilder.append("## ").append(content).append("\n\n");
                        break;
                    case "paragraph":
                        markdownBuilder.append(content).append("\n\n");
                        break;
                    case "list_item":
                        markdownBuilder.append("* ").append(content).append("\n");
                        break;
                    case "knowledge_point":
                        if (content instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, String> contentMap = (Map<String, String>) content;
                            String term = contentMap.get("term");
                            String shortExplanation = contentMap.get("explanation");
                            String longExplanation = highQualityExplanations.get(term);

                            String detailsHtml = String.format(
                                    "<details>\n<summary>点击展开<strong>%s</strong>详细内容</summary>\n\n> **%s**: %s\n\n%s\n\n</details>\n\n",
                                    term,
                                    term,
                                    shortExplanation,
                                    longExplanation != null ? longExplanation : "(详细解释生成失败或未找到)"
                            );
                            markdownBuilder.append(detailsHtml);
                        }
                        break;
                }
            }
            if (noteBlocks.stream().anyMatch(b -> "list_item".equals(b.get("type")))) {
                markdownBuilder.append("\n");
            }

            String markdownBody = markdownBuilder.toString();
            String frontmatter = buildFrontmatter(note.getVideoUrl(), markdownBody);

            // 6. 最终合并并返回
            String finalMarkdown = frontmatter + markdownBody; // <--- 在这里定义了 finalMarkdown 变量
            return ResponseEntity.ok(finalMarkdown);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to export note: " + e.getMessage());
        }
    }

    /**
     * 【修复版】
     * 构建用于 Markdown 文件头部的 Frontmatter 元数据字符串。
     * @param videoUrl 视频的源链接
     * @param markdownBody 笔记正文，用于智能提取标签
     * @return 格式化好的 Frontmatter 字符串
     */
    private String buildFrontmatter(String videoUrl, String markdownBody) {
        StringBuilder tagsBuilder = new StringBuilder();
        // 使用 Arrays.asList()，兼容性更好
        List<String> keywords = Arrays.asList("Agent", "LangChain", "LLM", "Spring", "MyBatis", "AI", "Java");

        keywords.stream()
                .filter(keyword -> {
                    if (markdownBody == null || markdownBody.isBlank()) {
                        return false;
                    }
                    return markdownBody.toLowerCase().contains(keyword.toLowerCase());
                })
                .forEach(tag -> tagsBuilder.append("  - ").append(tag).append("\n"));

        // 如果没有找到任何关键词标签，给一个默认的
        if (tagsBuilder.length() == 0) {
            tagsBuilder.append("  - 视频笔记\n");
        }

        // 【核心修复】返回最终拼接好的字符串
        return String.format(
                "---\nsource: %s\ngenerated_at: %s\ntags:\n%s---\n\n",
                videoUrl != null ? videoUrl : "N/A",
                java.time.LocalDate.now().toString(),
                tagsBuilder.toString()
        );
    }
}