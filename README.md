# AI 视频笔记生成器 (AI Video Notes Generator)

这是一个基于 Java 21 和 Spring Boot 3 构建的高性能后端服务，能够根据视频内容，通过多模态 AI 模型自动生成结构化的学习笔记。

## 🏛️ 系统架构 (System Architecture)

![系统架构图](docs/images/architecture.png)

## 🚀 项目亮点 (Features)

*   **现代化技术栈**: 采用 **Java 21 + Spring Boot 3**，并开启**虚拟线程 (Virtual Threads)** 以极低的资源消耗支持高并发 I/O 操作。
*   **端到端自动化**: 实现了从 `API 请求` -> `音频提取` -> `AI 分析` -> `数据持久化` -> `结果查询` 的全自动化处理流程。
*   **前沿多模态 AI 集成**: 成功对接 **Google Gemini 1.5**，实现了直接处理**音频文件**生成笔记的**多模态**能力，架构更简洁、高效。
*   **动态模型选择**: 后端支持根据 API 请求参数，在 **`Flash` (高速) 模型**和 **`Pro` (高质量) 模型**之间动态切换，兼顾了响应速度与分析质量。
*   **健壮的外部进程控制**: 稳定集成 **`ffmpeg`**，并构建了包含日志重定向和错误处理的健壮的外部命令执行框架。
*   **专业的工程实践**:
    *   **配置安全**: 所有敏感信息（API Key, 密码）均通过**环境变量**管理，代码库中无任何硬编码凭证。
    *   **Prompt 工程**: 将 Prompt 内容**从业务代码中分离**至外部模板文件，实现了业务逻辑与 AI 指令的解耦，便于快速迭代和优化。
    *   **异步处理**: 核心业务流程采用 `@Async` 实现**完全异步化**，确保 API 接口能够瞬时响应。
    *   **API 设计**: 提供了 RESTful 风格的 API，并通过 **DTO (Data Transfer Objects)** 模式实现了前后端的数据解耦。

## 🛠️ 技术栈 (Tech Stack)

*   **后端**: Spring Boot, Spring WebFlux (`WebClient`), Spring Data JPA
*   **语言**: Java 21
*   **AI 模型**: Google Gemini 1.5 Pro / Flash (多模态)
*   **媒体处理**: FFmpeg
*   **数据库**: H2 (开发阶段), PostgreSQL (生产环境)
*   **构建工具**: Maven
*   **其他**: Lombok, Jackson, Jasypt

## 🏁 已完成里程碑 (Milestones Achieved)

*   **[✓] 阶段一: 核心链路验证**
    *   完成项目基础框架搭建。
    *   成功对接 Google Gemini API 的纯文本接口。
    *   解决了复杂的本地开发环境网络代理和依赖兼容性问题。
*   **[✓] 阶段二: 真实流程实现**
    *   成功集成 `ffmpeg` 实现从本地视频文件提取音频。
    *   将 AI 调用升级为**多模态处理**，直接分析音频文件。
    *   实现了笔记结果的**数据库持久化**和**API 查询**功能。
    *   完成了项目的**精加工**，包括美化 API 响应、创建 DTO 和完善文档。

## 🚀 未来计划 (Future Roadmap)

*   **[ ] 阶段三: 真实数据源 & 质量保障**
    *   集成 `yt-dlp` 以支持从在线视频链接（如 YouTube）自动下载和处理。
    *   编写 **JUnit 5 和 Mockito 单元测试**，确保核心业务逻辑的稳定可靠。
*   **[ ] 阶段四: 迈向全栈**
    *   开发一个 **Chrome/Edge 浏览器插件**作为前端，实现一键式交互，提供极致的用户体验。

## 快速开始 (Quick Start)

1.  **克隆仓库**
    ```bash
    git clone https://github.com/fan2515/ai-video-notes.git
    cd ai-video-notes
    ```
2.  **准备素材**
    *   在项目根目录下放置一个名为 `test.mp4` 的视频文件。

3.  **配置环境变量**
    *   在 IntelliJ IDEA 的运行配置中，设置以下环境变量：
        *   `GEMINI_API_KEY`: 你的 Google Gemini API Key。
        *   `JASYPT_ENCRYPTOR_PASSWORD`: 用于配置加密的自定义密码。

4.  **配置网络代理 (如果需要)**
    *   在 `AiVideoNotesApplication.java` 的 `webClient()` Bean 中，修改代理的主机和端口。

5.  **运行项目**
    *   直接运行 `AiVideoNotesApplication.java` 主类。

6.  **测试 API**
    *   **创建笔记 (POST):**
      ```bash
      curl -X POST http://localhost:8080/api/notes/generate \
      -H "Content-Type: application/json" \
      -d '{"userId": 1, "url": "local-test", "mode": "FLASH"}'
      ```
    *   **查询笔记 (GET):**
      ```bash
      curl http://localhost:8080/api/notes/{noteId}
      ```
