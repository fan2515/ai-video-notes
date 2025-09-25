# AI 视频笔记生成器 (AI Video Notes Generator)

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/Database-PostgreSQL-blue.svg?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Chrome Web Store](https://img.shields.io/badge/Chrome-v1.4.0-brightgreen?logo=google-chrome&logoColor=white)](https://github.com/fan2515/ai-video-notes/releases/latest)
[![Edge Add-ons](https://img.shields.io/badge/Edge-v1.4.0-blue?logo=microsoft-edge&logoColor=white)](https://github.com/fan2515/ai-video-notes/releases/latest)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/fan2515/ai-video-notes/blob/main/LICENSE)
[![Latest Release](https://img.shields.io/github/v/release/fan2515/ai-video-notes)](https://github.com/fan2515/ai-video-notes/releases/latest)

**一个从零到一，攻克无数真实世界工程难题，最终实现的全功能、全栈 AI 视频笔记应用原型。**

这个项目由一个强大的 **Java 后端服务**和一个**浏览器侧边栏插件**组成，能够将在线视频（当前稳定支持 Bilibili）一键转化为**可交互的、结构化的**学习笔记，提供沉浸式的学习体验。

---

### 🚀 V1.4.0 版本核心升级：多模型引擎 & 插件交互重构

V1.4.0 版本对项目前后端进行了全面的架构重构与交互升级，为未来的多模型扩展奠定了坚实的基础，并极大地提升了用户体验。

1.  **全新分屏交互**：术语解释不再是覆盖全屏的模态框，而是优雅的底部可拖拽面板，实现了笔记与解释的同屏、独立滚动查看，学习体验更沉浸。
    *[请在这里替换为你的新GIF/截图，展示点击术语后，底部出现可拖拽面板，并且两个区域可以独立滚动]*

2.  **可插拔AI架构 (后端)**：后端 AI 调用逻辑已通过策略模式完全重构。所有模型实现均遵循统一的 `LLMService` 接口，并通过 `LLMServiceProvider` 工厂进行动态调度。未来接入新模型（如Kimi、OpenAI）将无需改动核心业务代码。
    *[可以考虑在这里放一张简单的后端架构图]*

3.  **多模型UI就绪 (前端)**：插件界面已更新，包含“AI提供商”选择框和“API Key”输入框，前端已准备好支持多模型切换。
    *[请在这里替换为你的新GIF/截图，展示新的插件UI，特别是模型选择的下拉框]*

---

### 🚀 V1.3 版本核心升级：智能知识库 & 数据库持久化

**V1.3 版本对后端架构进行了重大升级，引入了 PostgreSQL 数据库和智能知识库缓存，极大地提升了应用的性能、效率和未来的可扩展性。**

*[此部分保留V1.3的GIF或截图]*

---

### ✨ 项目亮点与技术深度 (Features & Technical Depth)

*   **🚀 可插拔 AI 架构 (Pluggable AI Architecture)**:
    *   **策略模式应用**: 核心 AI 服务层采用策略设计模式，将模型实现 (`GeminiService`, `KimiService`等) 与业务逻辑完全解耦。
    *   **面向接口编程**: 业务层依赖统一的 `LLMService` 接口，而非具体实现，极大提升了系统的灵活性和可扩展性。
    *   **动态服务调度**: 通过 `LLMServiceProvider` 工厂，可根据用户选择动态切换底层 AI 模型。

*   **🧠 沉浸式用户体验 (Immersive User Experience)**:
    *   **可拖拽分屏视图 (Resizable Split View)**: 术语解释面板支持用户自由拖拽调整大小，实现了笔记与解释区的完美分屏和独立滚动。
    *   **动态UI**: 插件界面可根据用户选择的 AI 提供商，动态显示或隐藏相关配置项（如 Gemini 质量选择、API Key 输入框）。
    *   **持久化侧边栏 (Persistent Side Panel):** 将笔记固定在浏览器侧边，实现视频与笔记同屏学习。

*   **🛠️ 专业工程实践**:
    *   **现代化技术栈**: 采用 **Java 21 + Spring Boot 3**，并开启**虚拟线程 (Virtual Threads)**。
    *   **健壮的架构**: 包含异步任务管理、**Spring Retry 自动重试**、单元测试、配置解耦等专业实践。
    *   **数据库持久化**: 采用生产级的 **PostgreSQL** 及 **`jsonb`** 类型进行数据存储。
    *   **智能缓存**: 为术语解释构建了**数据库缓存**，极大降低API成本和响应延迟。

---

### 🛠️ 技术栈 (Tech Stack)

*   **后端**: Spring Boot, Spring Data JPA, **Spring Retry**
*   **语言**: Java 21 + Virtual Threads
*   **AI 模型**: Google Gemini 1.5 Pro / Flash (可扩展)
*   **数据库**: **PostgreSQL**
*   **前端**: **Chrome & Edge Extension** (Manifest V3, Side Panel API)
*   **前端库**: Marked.js
*   **其他**: Lombok, Jackson, Jasypt, **hypersistence-utils**

---

### 🚀 快速开始 (Quick Start)

1.  **环境准备**
    *   安装 JDK 21, Maven, `yt-dlp`, `ffmpeg`。
    *   安装并运行 **PostgreSQL** (推荐使用 Docker)。
    *   在 PostgreSQL 中，**手动创建一个数据库**，例如 `ai_video_notes_db`。

2.  **克隆并配置后端**
    ```bash
    git clone https://github.com/fan2515/ai-video-notes.git
    cd ai-video-notes
    ```
    *   打开 `src/main/resources/application.properties`，**修改数据库连接信息** (`url`, `username`, `password`) 以匹配你的本地 PostgreSQL 配置。
    *   在 IntelliJ IDEA 的运行配置中，设置环境变量 `GEMINI_API_KEY`。

3.  **运行后端服务**
    *   运行 `AiVideoNotesApplication.java` 主类。应用首次启动时，Hibernate 会自动在你的 PostgreSQL 数据库中创建所有需要的表。

4.  **加载浏览器插件**
    *   **Chrome**: 打开 `chrome://extensions`； **Edge**: 打开 `edge://extensions`。
    *   启用**开发者模式**，点击“**加载已解压的扩展程序**”，选择项目中的 `browser-extension` 文件夹。

5.  **开始使用！**
    *   打开 Bilibili 视频页面，点击插件图标，开始你的学习之旅！

---
### 📝 未来计划 (Future Roadmap)

*   **[进行中] V1.5.0 - 模型扩展**: **正式接入 Kimi 等国产大模型**。
*   **[ ] 功能深化**: **多轮对话**, **用户系统与云同步**。
*   **[ ] 边界扩展**: **支持 YouTube**。
*   **[ ] 架构升级**: 引入**消息队列**, 实现**AI工作流引擎**。

---
### 📄 许可证 (License)

本项目采用 [MIT License](LICENSE)。