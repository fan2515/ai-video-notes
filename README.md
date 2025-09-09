# AI 视频笔记生成器 (AI Video Notes Generator)

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/fan2515/ai-video-notes/blob/main/LICENSE)

**一个从零到一，攻克无数真实世界工程难题，最终实现的全功能、全自动 AI 视频笔记后端服务。**

---

### 🚀 最终成果演示 (Final Demo)

本项目已成功实现**从浏览器插件一键触发，到后端自动化处理在线视频（Bilibili），并最终生成高质量 AI 笔记的完整端到端流程**。

#### 插件交互与成功响应
*用户通过插件发起请求，后端处理完毕后，可通过 API 成功查询到 AI 生成的完整笔记。*
![插件与成功响应](docs/images/final_success.png)

#### 后端处理日志
*每一次成功的背后，都是这样一段行云流水的自动化处理日志。*
![成功日志](docs/images/final_log.png)

---

### 🏛️ 系统架构 (System Architecture)

![系统架构图](docs/images/architecture.png)

---

### ✨ 项目亮点与技术深度 (Features & Technical Depth)

这个项目不仅仅是一个功能应用，更是一次深入真实世界后端开发的完整实践。

*   **现代化技术栈**: 采用 **Java 21 + Spring Boot 3**，并开启**虚拟线程 (Virtual Threads)**，为高并发 I/O 密集型任务奠定了现代化的高性能基础。

*   **全栈与端到端闭环**: 实现了从**浏览器插件**（前端原型）的用户交互，到后端**异步任务处理**，再到**数据持久化**和**API 查询**的完整产品功能闭环。

*   **前沿多模态 AI 集成**: 摒弃了传统的“STT+LLM”两步法，直接利用 **Google Gemini 1.5** 的**多模态能力**，通过 **Base64 编码**将音频文件内联在请求中，一步到位地完成了从音频到结构化文本的转换，极大简化了架构并降低了外部依赖。

*   **健壮的外部系统集成**:
    *   稳定集成了 **`yt-dlp`** 和 **`ffmpeg`**，实现了对在线视频（Bilibili）的下载和处理。
    *   在开发过程中，系统性地诊断并最终解决了因 YouTube **反爬虫策略**（Cookies, DPAPI 加密, 文件锁定）导致的下载失败问题，并最终通过切换数据源（Bilibili）和采用最可靠的 `yt-dlp` 参数配置，保证了流程的稳定性。

*   **专业的工程与架构实践**:
    *   **异步任务管理**: 实现了包含 `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED` 状态的**异步任务管理系统**，并通过 API 提供了任务状态的**轮询查询**能力。
    *   **JPA 深度应用**: 解决了因**懒加载 (`LAZY`)**、**异步 (`@Async`)** 和**事务 (`@Transactional`)** 共同作用下导致的**数据一致性**难题，最终通过调整为**即时加载 (`EAGER`)** 和正确的事务管理保证了数据完整性。
    *   **动态模型选择**: 后端支持根据 API 请求参数，在 **`Flash` (高速) 模型**和 **`Pro` (高质量) 模型**之间动态切换，兼顾了响应速度与分析质量。
    *   **配置安全与解耦**: 所有敏感信息均通过**环境变量**管理；**Prompt** 从业务代码中分离至外部模板文件，实现了逻辑与内容解耦。
    *   **质量保证**: 编写了基于 **JUnit 5 + Mockito** 的**单元测试**，对核心业务逻辑进行了覆盖，确保了代码的可靠性。

---

### 🛠️ 技术栈 (Tech Stack)

*   **后端**: Spring Boot, Spring Data JPA
*   **语言**: Java 21
*   **AI 模型**: Google Gemini 1.5 Pro / Flash (多模态)
*   **媒体处理**: yt-dlp, FFmpeg
*   **数据库**: H2 (开发阶段)
*   **测试**: JUnit 5, Mockito
*   **前端原型**: Chrome/Edge Extension (HTML, CSS, JavaScript)
*   **其他**: Lombok, Jackson, Jasypt

---

### 🚀 快速开始 (Quick Start)

1.  **环境准备**
    *   安装 JDK 21, Maven, `yt-dlp`, `ffmpeg` 并配置好环境变量。

2.  **克隆仓库**
    ```bash
    git clone https://github.com/fan2515/ai-video-notes.git
    cd ai-video-notes
    ```

3.  **配置环境变量**
    *   在 IntelliJ IDEA 的运行配置中，设置 `GEMINI_API_KEY`。
    *   (可选) `JASYPT_ENCRYPTOR_PASSWORD`。

4.  **运行后端服务**
    *   在 `AiVideoNotesApplication.java` 中配置好你的网络代理（如果需要）。
    *   直接运行 `AiVideoNotesApplication.java` 主类。

5.  **开始使用！**
    *   使用 Postman 或加载 `chrome-extension` 文件夹作为浏览器插件。
    *   **创建笔记 (POST ` /api/notes/generate`):**
      ```json
      {
          "userId": 1,
          "url": "【一个Bilibili视频链接】",
          "mode": "FLASH" 
      }
      ```
    *   **查询任务状态 (GET ` /api/tasks/{taskId}/status`):**
    *   用上面返回的 `taskId` 进行轮询。
    *   **查询最终结果 (GET ` /api/tasks/{taskId}/result`):**
    *   当任务状态为 `COMPLETED` 时，调用此接口获取笔记。

---
### 许可证 (License)

本项目采用 [MIT License](LICENSE)。