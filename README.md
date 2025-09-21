# AI 视频笔记生成器 (AI Video Notes Generator)

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Chrome Web Store](https://img.shields.io/badge/Chrome-v1.2.0-brightgreen?logo=google-chrome&logoColor=white)](https://www.google.com/chrome/)
[![Edge Add-ons](https://img.shields.io/badge/Edge-v1.2.0-blue?logo=microsoft-edge&logoColor=white)](https://www.microsoft.com/edge)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/fan2515/ai-video-notes/blob/main/LICENSE)
[![Latest Release](https://img.shields.io/github/v/release/fan2515/ai-video-notes)](https://github.com/fan2515/ai-video-notes/releases/latest)

**一个从零到一，攻克无数真实世界工程难题，最终实现的全功能、全栈 AI 视频笔记应用原型。**

这个项目由一个强大的 **Java 后端服务**和一个**浏览器侧边栏插件**组成，能够将在线视频（当前稳定支持 Bilibili）一键转化为**可交互的、结构化的**学习笔记，提供沉浸式的学习体验。

---

### 🚀 全新 V1.2 版本演示 (Demo)

**V1.2 版本在 V1.1 的交互式体验基础上，引入了模型切换功能，并通过革命性的 Prompt 优化，将笔记质量提升至全新高度。**

**1. 在侧边栏选择高质量模型，一键生成排版优美的笔记。**
![选择高质量模型并生成排版优美的笔记](docs/images/demo_v1.2_high_quality_note.png)
*上图展示了从模型选择到最终生成高质量、好排版的笔记的全过程。*

**2. 点击笔记中自动解释的术语，获得由AI提供的深度讲解。**
![点击知识点获得AI深度讲解](docs/gocs/images/demo_v1.2_ai_chat.png)
*用户可以轻松点击任何不理解的技术术语，AI导师会立刻提供通俗易懂的解释。*

**3. 将专业级的笔记导出为带元数据的 Markdown 文件。**
![导出带Frontmatter元数据的Markdown文件](docs/images/demo_v1.2_export_with_frontmatter.png)
*导出的 .md 文件包含了可追溯的源链接、生成日期和自动提取的标签，完美融入 Obsidian、VS Code 等现代知识管理工作流。*

---

### ✨ 项目亮点与技术深度 (Features & Technical Depth)

这个项目不仅仅是一个功能应用，更是一次深入真实世界后端开发的完整实践。

*   **🚀 核心用户功能**:
    *   **持久化侧边栏 (Persistent Side Panel):** 将笔记固定在浏览器侧边，实现视频与笔记同屏的沉浸式学习。
    *   **交互式AI对话 (Interactive AI Chat):** 点击笔记中自动识别的关键概念，可获得AI提供的深度解释。
    *   **动态模型切换 (Dynamic Model Selection):** 允许用户在 **Flash (高速)** 和 **Pro (高质量)** 模型间自由选择，平衡速度与效果。
    *   **生产力工具集成 (Productivity Integration):** 支持一键复制，并可将笔记导出为带 **Frontmatter 元数据**的专业级 Markdown 文件。

*   **🧠 智能与质量**:
    *   **革命性的笔记质量 (Revolutionary Note Quality):** 通过引入“格式塔原则”的 **Prompt Engineering**，AI能生成结构清晰、排版优美、上下文连贯且术语解释完整的专业笔记。
    *   **智能内容提纯 (Intelligent Content Filtering):** 自动过滤视频中的广告、闲聊等无关信息。
    *   **前沿多模态 AI 集成**: 直接利用 **Google Gemini 1.5** 的**多模态能力**，一步到位地从音频生成结构化文本。

*   **🛠️ 专业工程实践**:
    *   **现代化技术栈**: 采用 **Java 21 + Spring Boot 3**，并开启**虚拟线程 (Virtual Threads)**。
    *   **异步任务管理**: 实现了包含完整状态的**异步任务系统**。
    *   **健壮的架构**: 包含单元测试、配置解耦、安全的外部系统集成等专业实践。

---

### 🛠️ 技术栈 (Tech Stack)

*   **后端**: Spring Boot, Spring Data JPA
*   **语言**: Java 21 + Virtual Threads
*   **AI 模型**: Google Gemini 1.5 Pro / Flash
*   **媒体处理**: yt-dlp, FFmpeg
*   **前端**: **Chrome & Edge Extension** (Manifest V3, Side Panel API)
*   **前端库**: Marked.js (Markdown渲染)
*   **其他**: Lombok, Jackson, Jasypt, Spring Retry

---

### 🚀 快速开始 (Quick Start)

1.  **环境准备**
    *   安装 JDK 21, Maven, `yt-dlp`, `ffmpeg` 并配置好系统环境变量 `Path`。

2.  **克隆并运行后端**
    ```bash
    git clone https://github.com/fan2515/ai-video-notes.git
    cd ai-video-notes
    ```
    *   在 IntelliJ IDEA 的运行配置中，设置环境变量 `GEMINI_API_KEY`。
    *   运行 `AiVideoNotesApplication.java` 主类。

3.  **加载浏览器插件 (Chrome / Edge)**
    *   **Chrome**: 在地址栏输入 `chrome://extensions` 并打开**开发者模式**。
    *   **Edge**: 在地址栏输入 `edge://extensions` 并打开**开发人员模式**。
    *   点击“**加载已解压的扩展程序**”，选择项目中的 `browser-extension` 文件夹。

4.  **开始使用！**
    *   打开任意 Bilibili 视频页面，点击插件图标，**AI 笔记侧边栏**将会滑出。
    *   **选择你想要的模型**，然后点击“生成笔记”！

---
### 📝 未来计划 (Future Roadmap)

*   **[ ] 功能深化**:
    *   **多轮对话**: 实现可连续追问的 AI 导师体验。
    *   **用户系统与云同步**: 实现笔记的云端持久化存储和跨设备访问。
*   **[ ] 边界扩展**:
    *   **支持 YouTube**: 让工具服务更广泛的用户。
*   **[ ] 架构升级**:
    *   **AI工作流引擎**: 重构笔记生成流程，实现更稳定、更高质量的输出。

---
### 📄 许可证 (License)

本项目采用 [MIT License](LICENSE)。