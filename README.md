# AI 视频笔记生成器 (AI Video Notes Generator)

这是一个基于 Java 21 和 Spring Boot 3 构建的 AI 服务，能够根据视频内容自动生成结构化的学习笔记。

## 🚀 项目亮点 (Features)

*   **现代化技术栈**: 采用 Java 21 + Spring Boot 3，并开启虚拟线程以支持高并发处理。
*   **AI 驱动**: 集成 Google Gemini API，利用其强大的语言能力生成高质量笔记。
*   **高性能 I/O**: API 调用采用流式请求 (Streaming Request)，实现近乎实时的响应。
*   **灵活架构**: 基于策略模式设计，轻松扩展支持不同的 AI 服务提供商 (如未来的 OpenAI)。
*   **专业工程实践**:
    *   通过环境变量管理敏感配置，保证代码安全。
    *   将 Prompt 从业务代码中分离至外部模板文件，易于维护和迭代。
    *   核心业务流程采用异步处理，提升系统吞吐量。

## 🛠️ 技术栈 (Tech Stack)

*   **后端**: Spring Boot, Spring Data JPA
*   **语言**: Java 21
*   **数据库**: H2 (开发阶段), PostgreSQL (生产环境)
*   **AI 模型**: Google Gemini 1.5 Flash
*   **构建工具**: Maven
*   **其他**: Lombok, Jasypt, Jackson

## 🏁 第一阶段成果

*   [x] 完成项目基础框架搭建。
*   [x] 成功对接 Google Gemini API，并实现高性能的流式调用。
*   [x] 实现了基于模拟文本的笔记生成核心功能。
*   [x] 解决了本地开发环境的网络代理和依赖兼容性问题。

## 📝 TODO (第二阶段计划)

- [ ] 集成 `yt-dlp` 和 `ffmpeg` 实现视频下载与音频提取。
- [ ] 集成 Whisper API 实现语音转文字。
- [ ] 实现数据持久化与任务状态查询 API。

## 运行项目 (Running the Project)

1.  克隆仓库: `git clone [你的仓库URL]`
2.  配置环境变量:
    *   `GEMINI_API_KEY`: 我的 Google Gemini API Key。
    *   `JASYPT_ENCRYPTOR_PASSWORD`: 用于配置加密的密码。
3.  (如果需要网络代理) 在 `AiVideoNotesApplication.java` 中配置你的代理地址和端口。
4.  运行 `AiVideoNotesApplication.java` 主类。