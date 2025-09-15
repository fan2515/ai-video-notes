# AI 视频笔记生成器 (AI Video Notes Generator)

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Chrome Web Store](https://img.shields.io/badge/Chrome-v1.1.0-brightgreen?logo=google-chrome&logoColor=white)](https://www.google.com/chrome/)
[![Edge Add-ons](https://img.shields.io/badge/Edge-v1.1.0-blue?logo=microsoft-edge&logoColor=white)](https://www.microsoft.com/edge)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/fan2515/ai-video-notes/blob/main/LICENSE)
[![Version](https://img.shields.io/badge/Version-1.1.0-blue.svg)](https://github.com/fan2515/ai-video-notes/releases/tag/v1.1.0)

**一个从零到一，攻克无数真实世界工程难题，最终实现的全功能、全栈 AI 视频笔记应用原型。**

这个项目由一个强大的 **Java 后端服务**和一个**浏览器侧边栏插件**组成，能够将在线视频（当前稳定支持 Bilibili）一键转化为**可交互的、结构化的**学习笔记，提供沉浸式的学习体验。

---

### 🚀 全新 V1.1 版本演示 (Demo)

**V1.1 版本对核心体验进行了彻底重构，从临时弹窗升级为持久化侧边栏，并引入了交互式AI对话，将工具提升为真正的“学习伴侣”。**

**1. 在视频页面点击图标，唤出侧边栏并生成笔记。**
*[请在这里替换为你的新GIF/截图，展示侧边栏滑出和生成笔记的过程]*

**2. 点击笔记中的关键概念，获取AI提供的深度解释。**
*[请在这里替换为你的新GIF/截图，展示点击知识点和弹出对话框的过程]*

**3. 轻松复制内容，或将笔记导出为带元数据的 Markdown 文件。**
*[请在这里替换为你的新GIF/截图，展示导出.md文件及其内容的过程]*

---

### 🏛️ 系统架构 (System Architecture)

![系统架构图](docs/images/architecture.png)

---

### ✨ 项目亮点与技术深度 (Features & Technical Depth)

这个项目不仅仅是一个功能应用，更是一次深入真实世界后端开发的完整实践。

*   **沉浸式学习体验**:
    *   **持久化侧边栏 (Persistent Side Panel):** 彻底抛弃临时弹窗，将笔记固定在浏览器侧边，实现了视频与笔记同屏学习，避免了焦点丢失导致的中断。
    *   **交互式AI对话 (Interactive AI Chat):** 可直接点击笔记中由AI识别出的关键概念，插件会弹出模态框，提供针对该概念的深度解释。

*   **生产力工具集成**:
    *   **Markdown 支持:** 笔记和AI回答均支持 Markdown 格式渲染，排版美观，可读性强。
    *   **一键复制与导出:** 方便地将笔记内容或AI解释复制到剪贴板，或将整篇笔记导出为带专业 **Frontmatter 元数据**的 `.md` 文件，无缝融入 Notion、Obsidian 等知识管理流程。

*   **现代化技术栈**: 采用 **Java 21 + Spring Boot 3**，并开启**虚拟线程 (Virtual Threads)** 以极低的资源消耗支持高并发 I/O 操作。

*   **智能内容提纯**: 通过优化的 Prompt Engineering，AI能自动过滤视频中的广告、问候语等无关信息，确保笔记内容的专业与纯净。

*   **前沿多模态 AI 集成**: 直接利用 **Google Gemini 1.5** 的**多模态能力**，一步到位地完成了从音频到结构化文本的转换。

*   **专业的工程与架构实践**:
    *   **异步任务管理**: 实现了包含 `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED` 状态的**异步任务管理系统**。
    *   **健壮的外部系统集成**: 稳定集成 **`yt-dlp`** 和 **`ffmpeg`**。
    *   **JPA 深度应用**与**单元测试**及**配置安全**。

---

### 🛠️ 技术栈 (Tech Stack)

*   **后端**: Spring Boot, Spring Data JPA
*   **语言**: Java 21 + Virtual Threads
*   **AI 模型**: Google Gemini 1.5 Pro / Flash (多模态 & 文本)
*   **媒体处理**: yt-dlp, FFmpeg
*   **数据库**: H2 (开发阶段)
*   **测试**: JUnit 5, Mockito
*   **前端**: **Chrome & Edge Extension** (Manifest V3, HTML, CSS, JavaScript)
*   **前端库**: Marked.js (Markdown渲染)
*   **其他**: Lombok, Jackson, Jasypt

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
    *   (如果需要) 在 `AiVideoNotesApplication.java` 中配置你的网络代理。
    *   直接运行 `AiVideoNotesApplication.java` 主类。

3.  **加载浏览器插件 (Chrome / Edge)**
    *   **Chrome**: 在地址栏输入 `chrome://extensions` 并打开**开发者模式**。
    *   **Edge**: 在地址栏输入 `edge://extensions` 并打开**开发人员模式**。
    *   点击“**加载已解压的扩展程序**”，选择项目中的 `extension` 文件夹 (原 `chrome-extension` 文件夹)。

4.  **开始使用！**
    *   打开任意 Bilibili 视频页面，点击浏览器右上角的**插件图标**，**AI 笔记侧边栏**将会滑出。
    *   点击“生成笔记”，开始你的交互式学习之旅！

---
### 📝 未来计划 (Future Roadmap)

*   **功能深化**:
    *   **多轮对话**: 实现可连续追问的 AI 导师体验。
    *   **用户系统与云同步**: 实现笔记的云端持久化存储和跨设备访问。
*   **边界扩展**:
    *   **支持 YouTube**: 让工具服务更广泛的用户。
*   **架构升级**:
    *   **消息队列**: 引入 RabbitMQ 或 Redis 队列，提升系统并发处理能力和健壮性。

---
### 📄 许可证 (License)

本项目采用 [MIT License](LICENSE)。