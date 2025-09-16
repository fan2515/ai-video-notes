// popup.js

// 全局变量，用于存储原始的Markdown笔记内容和视频URL
let rawMarkdownNote = '';
let currentVideoUrl = ''; // 用于导出时的 source

// ================== 全局事件监听 ==================
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('generateButton').addEventListener('click', onGenerateButtonClick);

    // 为新按钮绑定事件
    document.getElementById('copyNoteBtn').addEventListener('click', copyNote);
    document.getElementById('exportMdBtn').addEventListener('click', exportMarkdown);
    document.getElementById('copyAnswerBtn').addEventListener('click', copyAnswer);
});


// ================== 主要功能函数 ==================
async function onGenerateButtonClick() {
    const button = document.getElementById('generateButton');
    const statusDiv = document.getElementById('status');
    const resultContainer = document.getElementById('resultContainer');
    const noteActions = document.getElementById('noteActions');

    button.disabled = true;
    statusDiv.textContent = '初始化...';
    resultContainer.style.display = 'none';
    noteActions.style.display = 'none'; // 确保操作按钮也隐藏

    try {
        statusDiv.textContent = '正在获取页面信息...';
        const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
        if (!tab) throw new Error('无法获取当前标签页！');

        const videoUrl = tab.url;
        currentVideoUrl = videoUrl; // 保存当前视频 URL

        if (!videoUrl.includes("bilibili.com/video") && !videoUrl.includes("youtube.com/watch")) {
            throw new Error('目前仅支持Bilibili和YouTube视频页面！');
        }

        statusDiv.textContent = '已获取 URL，正在创建任务...';

        // ========================= 【核心修改点】 =========================

        // 1. 从下拉框获取用户选择的模型
        const selectedMode = document.getElementById('modelSelect').value;

        // 2. 在构建请求体时，使用上面获取到的 selectedMode
        const requestData = {
            userId: 1,
            url: videoUrl,
            mode: selectedMode // 不再写死 "FLASH"
        };

        // =================================================================

        console.log("Sending request to backend with data:", requestData); // 加一行日志，方便调试

        const generateResponse = await fetch('http://localhost:8080/api/notes/generate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestData),
        });
        if (!generateResponse.ok) throw new Error(`创建任务失败: ${generateResponse.statusText}`);

        const taskData = await generateResponse.json();
        const taskId = taskData.taskId;

        statusDiv.textContent = `任务已创建... 正在处理中...`;
        await pollTaskStatusAndRender(taskId);

    } catch (error) {
        handleError(error);
    } finally {
        button.disabled = false;
    }
}
async function pollTaskStatusAndRender(taskId) {
    const statusDiv = document.getElementById('status');
    const maxAttempts = 60;
    let attempt = 0;

    while (attempt < maxAttempts) {
        attempt++;
        const response = await fetch(`http://localhost:8080/api/tasks/${taskId}/status`);
    if (!response.ok) throw new Error('查询状态失败！');

const task = await response.json();
statusDiv.textContent = `处理中 (${attempt}/${maxAttempts}): ${task.statusMessage}`;

if (task.status === 'COMPLETED') {
    statusDiv.textContent = '笔记生成成功！正在渲染结果...';
    await fetchAndRenderResult(taskId);
    return;
} else if (task.status === 'FAILED') {
    throw new Error(`任务处理失败: ${task.statusMessage}`);
}

await new Promise(resolve => setTimeout(resolve, 5000));
}
throw new Error("任务处理超时，请稍后再试。");
}

async function fetchAndRenderResult(taskId) {
    const response = await fetch(`http://localhost:8080/api/tasks/${taskId}/result`);
    if (!response.ok) throw new Error('获取最终结果失败！');

    const note = await response.json();
    if (!note || !note.content) throw new Error('获取到的笔记内容为空！');

    const noteData = JSON.parse(note.content);
    if (!noteData || !Array.isArray(noteData.notes)) throw new Error('笔记内容格式不正确。');

    const container = document.getElementById('notes-content');
    container.innerHTML = '';

    rawMarkdownNote = ''; // 重置
    let currentList = null;

    noteData.notes.forEach(block => {
        if (block.type !== 'list_item' && currentList) {
            container.appendChild(currentList);
            currentList = null;
        }

        let element;
        const content = block.content;

        switch (block.type) {
            case 'heading':
                element = document.createElement('h2');
                element.innerHTML = marked.parseInline(String(content || ''));
                container.appendChild(element);
                rawMarkdownNote += `## ${content}\n\n`;
                break;
            case 'paragraph':
                element = document.createElement('p');
                element.innerHTML = marked.parseInline(String(content || ''));
                container.appendChild(element);
                rawMarkdownNote += `${content}\n\n`;
                break;
            case 'list_item':
                if (!currentList) currentList = document.createElement('ul');
                element = document.createElement('li');
                element.innerHTML = marked.parseInline(String(content || ''));
                currentList.appendChild(element);
                rawMarkdownNote += `* ${content}\n`;
                break;
            case 'knowledge_point':
                element = createKnowledgePointElement(content);
                const p = document.createElement('p');
                p.appendChild(element);
                container.appendChild(p);
                rawMarkdownNote += `> **${content.term}**: ${content.explanation}\n\n`;
                break;
        }
    });

    if (currentList) {
        container.appendChild(currentList);
        rawMarkdownNote += '\n';
    }

    document.getElementById('noteActions').style.display = 'block';
    document.getElementById('resultContainer').style.display = 'block';
    document.getElementById('status').textContent = '笔记已生成！';
}


// ================== 复制/导出/对话 功能函数 ==================

function copyNote() {
    navigator.clipboard.writeText(rawMarkdownNote)
        .then(() => {
            const btn = document.getElementById('copyNoteBtn');
            const originalText = btn.textContent;
            btn.textContent = '已复制!';
            setTimeout(() => { btn.textContent = originalText; }, 2000);
        })
        .catch(err => {
            console.error('复制失败: ', err);
            alert('复制失败，请检查浏览器权限。');
        });
}

/**
 * 【最终优化版】
 * 将笔记导出为 Markdown 文件，并在文件头部添加 Frontmatter 元数据。
 */
function exportMarkdown() {
    // 1. 自动生成标签
    const tags = new Set();
    const keywords = ['Spring AI', 'LangChain', 'Spring Boot', 'Java', 'AI', 'RAG', 'LLM', '多模态', '数据库'];
    keywords.forEach(keyword => {
        if (rawMarkdownNote.toLowerCase().includes(keyword.toLowerCase())) {
            tags.add(keyword.replace(/\s/g, '-')); // 替换空格为-
        }
    });

    // 2. 构建 Frontmatter 字符串
    const frontmatter = `---
source: ${currentVideoUrl}
generated_at: ${new Date().toISOString().split('T')[0]}
tags:
${Array.from(tags).map(tag => `  - ${tag}`).join('\n')}
---

`;

    // 3. 合并内容
    const markdownContent = frontmatter + rawMarkdownNote;

    // 4. 创建并触发下载
    const blob = new Blob([markdownContent], { type: 'text/markdown;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    const title = rawMarkdownNote.split('\n')[0].replace('## ', '').trim() || 'ai-video-note';
    a.download = `${title}.md`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

function copyAnswer() {
    const modalBody = document.getElementById('ai-modal').querySelector('.modal-body');
    const answerText = modalBody.innerText;
    navigator.clipboard.writeText(answerText)
        .then(() => {
            const btn = document.getElementById('copyAnswerBtn');
            const originalText = btn.textContent;
            btn.textContent = '已复制!';
            setTimeout(() => { btn.textContent = originalText; }, 2000);
        })
        .catch(err => {
            console.error('复制回答失败: ', err);
            alert('复制失败！');
        });
}

function createKnowledgePointElement(knowledgePointData) {
    const element = document.createElement('span');
    element.className = 'knowledge-point';
    element.textContent = knowledgePointData.term;
    element.title = knowledgePointData.explanation;

    element.addEventListener('click', async () => {
        const term = knowledgePointData.term;
        const context = document.getElementById('notes-content').innerText;
        showModal(`正在解释: ${term}`, '<div class="spinner"></div>');
        try {
            const response = await fetch('http://localhost:8080/api/ai/explain', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ term, context }),
            });
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.answer || 'AI 服务响应错误');
            }
            const data = await response.json();
            const htmlContent = marked.parse(data.answer);
            updateModalContent(htmlContent);
        } catch (error) {
            updateModalContent(`<p style="color: red;">抱歉，无法获取解释：<br>${error.message}</p>`);
        }
    });
    return element;
}

function showModal(title, contentHtml) {
    const modal = document.getElementById('ai-modal');
    const modalTitle = modal.querySelector('.modal-title');
    const modalBody = modal.querySelector('.modal-body');
    const closeBtn = modal.querySelector('.modal-close-btn');
    const overlay = modal.querySelector('.modal-overlay');
    modalTitle.textContent = title;
    modalBody.innerHTML = contentHtml;
    const closeModal = () => modal.style.display = 'none';
    closeBtn.onclick = closeModal;
    overlay.onclick = closeModal;
    modal.style.display = 'flex';
}

function updateModalContent(contentHtml) {
    const modalBody = document.getElementById('ai-modal').querySelector('.modal-body');
    modalBody.innerHTML = contentHtml;
}

function handleError(error) {
    console.error('An error occurred:', error);
    document.getElementById('status').textContent = `发生错误: ${error.message}`;
}
