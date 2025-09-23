# AI 视频笔记生成器 (AI Video Notes Generator)

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/Database-PostgreSQL-blue.svg?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Chrome Web Store](https://img.shields.io/badge/Chrome-v1.3.0-brightgreen?logo=google-chrome&logoColor=white)](https://www.google.com/chrome/)
[![Edge Add-ons](https://img.shields.io/badge/Edge-v1.3.0-blue?logo=microsoft-edge&logoColor=white)](https://www.microsoft.com/edge)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/fan2515/ai-video-notes/blob/main/LICENSE)
[![Latest Release](https://img.shields.io/github/v/release/fan2515/ai-video-notes)](https://github.com/fan2515/ai-video-notes/releases/latest)

**一个从零到一，攻克无数真实世界工程难题，最终实现的全功能、全栈 AI 视频笔记应用原型。**

这个项目由一个强大的 **Java 后端服务**和一个**浏览器侧边栏插件**组成，能够将在线视频（当前稳定支持 Bilibili）一键转化为**可交互的、结构化的**学习笔记，提供沉浸式的学习体验。

---

### 🚀 V1.3 版本核心升级：智能知识库 & 数据库持久化

**V1.3 版本对后端架构进行了重大升级，引入了 PostgreSQL 数据库和智能知识库缓存，极大地提升了应用的性能、效率和未来的可扩展性。**

**1. 首次点击术语，AI生成深度解释并存入知识库。**
*[请在这里替换为你的新GIF/截图，展示第一次点击术语，有加载过程，并成功显示高质量解释]*

**2. 再次点击同一术语，从数据库缓存中毫秒级响应！**
*[请在这里替换为你的新GIF/截图，展示第二次点击同一个术语，几乎没有延迟就显示出解释]*

**3. 导出时，批量从知识库读取，生成终极交互式 Markdown。**
*[请在这里替换为你的新GIF/截图，展示导出功能，并显示最终带有多个可折叠部分的 .md 文件]*

---

### ✨ 项目亮点与技术深度 (Features & Technical Depth)

*   **🚀 核心架构升级**:
    *   **数据库持久化 (Database Persistence):** 后端已从 **H2 内存数据库** 彻底迁移至生产级的 **PostgreSQL**。所有笔记、任务和术语解释都将被永久保存。
    *   **智能知识库 (Intelligent Glossary Cache):** 为“AI深度对话”功能构建了**数据库缓存**。首次查询术语时由AI生成并存入数据库；后续查询**直接从数据库毫秒级读取**，极大降低API成本和响应延迟。
    *   **JSONB 高级应用**: `Note` 实体的核心内容采用 PostgreSQL 强大的 **`jsonb`** 类型存储，为未来的复杂内容查询和分析提供了无限可能。

*   **🧠 智能用户体验**:
    *   **持久化侧边栏 (Persistent Side Panel):** 将笔记固定在浏览器侧边，实现视频与笔记同屏的沉浸式学习。
    *   **高质量交互式导出 (High-Quality Interactive Export):** “导出 .md”功能现在由后端驱动，通过批量查询知识库，实时生成包含**高质量深度解释**和 **Frontmatter 元数据**的交互式 Markdown 文档。
    *   **动态模型切换 (Dynamic Model Selection):** 允许用户在 **Flash (高速)** 和 **Pro (高质量)** 模型间自由选择。
    *   **革命性的笔记质量 (Revolutionary Note Quality):** 通过精密的 **Prompt Engineering**，AI能生成结构清晰、排版优美且术语解释完整的专业笔记。

*   **🛠️ 专业工程实践**:
    *   **现代化技术栈**: 采用 **Java 21 + Spring Boot 3**，并开启**虚拟线程 (Virtual Threads)**。
    *   **健壮的架构**: 包含异步任务管理、Spring Retry 自动重试、单元测试、配置解耦等专业实践。

---

### 🛠️ 技术栈 (Tech Stack)

*   **后端**: Spring Boot, Spring Data JPA, Spring Retry
*   **语言**: Java 21 + Virtual Threads
*   **AI 模型**: Google Gemini 1.5 Pro / Flash
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

*   **[ ] 功能深化**: **多轮对话**, **用户系统与云同步**。
*   **[ ] 边界扩展**: **支持 YouTube**。
*   **[ ] 架构升级**: 引入**消息队列**, 实现**AI工作流引擎**。

---
### 📄 许可证 (License)

本项目采用 [MIT License](LICENSE)。