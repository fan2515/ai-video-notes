// popup.js

// 全局变量，用于存储原始的Markdown笔记内容和视频URL
let rawMarkdownNote = '';
let currentVideoUrl = '';
let currentNoteId = null;

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

        // ========================= 【核心修改点】 =========================

        // 1. 【新增】在使用 videoUrl 之前，进行健壮性检查
        if (!videoUrl) {
            throw new Error('无法获取页面URL，请在正常的网页(如Bilibili)上使用。');
        }

        // 2. 【修改】将原来的 if 判断整合进来
        const supportedHosts = ["bilibili.com/video", "youtube.com/watch"];
        if (!supportedHosts.some(host => videoUrl.includes(host))) {
            throw new Error('请在支持的视频页面（Bilibili, YouTube）使用！');
        }

        // =================================================================

        statusDiv.textContent = '已获取 URL，正在创建任务...';

        const selectedMode = document.getElementById('modelSelect').value;
        const requestData = {
            userId: 1,
            url: videoUrl,
            mode: selectedMode
        };

        console.log("Sending request to backend with data:", requestData);

        const generateResponse = await fetch('http://localhost:8080/api/notes/generate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestData),
        });
        if (!generateResponse.ok) {
            const errorText = await generateResponse.text();
            throw new Error(`创建任务失败: ${generateResponse.statusText} - ${errorText}`);
        }

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
    currentNoteId = note.id; // 保存ID
    const noteData = JSON.parse(note.content);
    if (!noteData || !Array.isArray(noteData.notes)) throw new Error('笔记内容格式不正确。');
    const container = document.getElementById('notes-content');
    container.innerHTML = '';
    rawMarkdownNote = '';
    let currentList = null;
    noteData.notes.forEach(block => {
        if (block.type !== 'list_item' && currentList) { container.appendChild(currentList); currentList = null; }
        let element; const content = block.content;
        switch (block.type) {
            case 'heading': element = document.createElement('h2'); element.innerHTML = marked.parseInline(String(content || '')); container.appendChild(element); rawMarkdownNote += `## ${content}\n\n`; break;
            case 'paragraph': element = document.createElement('p'); element.innerHTML = marked.parseInline(String(content || '')); container.appendChild(element); rawMarkdownNote += `${content}\n\n`; break;
            case 'list_item': if (!currentList) currentList = document.createElement('ul'); element = document.createElement('li'); element.innerHTML = marked.parseInline(String(content || '')); currentList.appendChild(element); rawMarkdownNote += `* ${content}\n`; break;
            case 'knowledge_point': element = createKnowledgePointElement(content); const p = document.createElement('p'); p.appendChild(element); container.appendChild(p); rawMarkdownNote += `> **${content.term}**: ${content.explanation}\n\n`; break;
        }
    });
    if (currentList) { container.appendChild(currentList); rawMarkdownNote += '\n'; }
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
async function exportMarkdown() {
    if (!currentNoteId) {
        alert("错误：没有可导出的笔记ID。请先生成笔记。");
        return;
    }

    const btn = document.getElementById('exportMdBtn');
    const originalText = btn.textContent;
    btn.textContent = '转换中...';
    btn.disabled = true;

    try {
        // 1. 调用后端新的 /export API
        const response = await fetch(`http://localhost:8080/api/notes/export/${currentNoteId}`, {
            method: 'POST'
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`导出失败: ${errorText}`);
        }

        // 2. 获取后端返回的、已经包含 Frontmatter 和 <details> 的完整 Markdown 文本
        const markdownContent = await response.text();

        // 3. 创建 Blob 和下载链接
        const blob = new Blob([markdownContent], { type: 'text/markdown;charset=utf-8' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;

        // 从返回的 Markdown 中智能提取标题作为文件名
        const titleMatch = markdownContent.match(/##\s*(.*)/);
        const title = titleMatch ? titleMatch[1].trim().replace(/[<>:"/\\|?*]/g, '') : 'ai-video-note';
        a.download = `${title}.md`;

        document.body.appendChild(a);
        a.click();

        document.body.removeChild(a);
        URL.revokeObjectURL(url);

    } catch (error) {
        handleError(error);
    } finally {
        btn.textContent = originalText;
        btn.disabled = false;
    }
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
        const shortExplanation = knowledgePointData.explanation; // 【新增】获取一句话解释
        const context = document.getElementById('notes-content').innerText;

        showModal(`正在解释: ${term}`, '<div class="spinner"></div>');
        try {
            const response = await fetch('http://localhost:8080/api/ai/explain', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },

                // ========================= 【核心修改点】 =========================
                // 在请求体中，增加 shortExplanation 字段，与后端 DTO 保持一致
                body: JSON.stringify({
                    term: term,
                    context: context,
                    shortExplanation: shortExplanation
                }),
                // =================================================================
            });

            if (!response.ok) {
                // 【优化】尝试解析后端返回的错误详情
                const errorData = await response.json();
                throw new Error(errorData.answer || 'AI 服务响应错误');
            }

            const data = await response.json(); // 假设 ExplainResponse 还在
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
