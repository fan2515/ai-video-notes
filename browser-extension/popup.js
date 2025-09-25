// popup.js

document.addEventListener('DOMContentLoaded', () => {
    // --- 全局变量 ---
    let rawMarkdownNote = '';
    let currentVideoUrl = '';
    let currentNoteId = null;

    // --- UI 元素引用 ---
    const providerSelect = document.getElementById('providerSelect');
    const geminiModeGroup = document.getElementById('geminiModeGroup');
    const geminiModeSelect = document.getElementById('geminiModeSelect');
    const apiKeyGroup = document.getElementById('apiKeyGroup');
    const apiKeyInput = document.getElementById('apiKeyInput');
    const generateButton = document.getElementById('generateButton');
    const statusDiv = document.getElementById('status');
    const resultContainer = document.getElementById('resultContainer');
    const notesContent = document.getElementById('notes-content');
    const noteActions = document.getElementById('noteActions');

    // 解释区面板元素
    const explanationContainer = document.getElementById('explanation-container');
    const dragger = document.getElementById('dragger');
    const explanationTitle = document.querySelector('.explanation-title');
    const explanationBody = document.querySelector('.explanation-body');
    const closeExplanationBtn = document.getElementById('closeExplanationBtn');
    const copyAnswerBtn = document.getElementById('copyAnswerBtn');

    // ================== 事件监听 ==================
    generateButton.addEventListener('click', onGenerateButtonClick);
    document.getElementById('copyNoteBtn').addEventListener('click', copyNote);
    document.getElementById('exportMdBtn').addEventListener('click', exportMarkdown);
    copyAnswerBtn.addEventListener('click', copyAnswer);
    providerSelect.addEventListener('change', onProviderChange);
    closeExplanationBtn.addEventListener('click', hideExplanationPanel);

    // 初始化
    onProviderChange();
    setupPanelResizing();

    // ================== UI 交互 ==================
    function onProviderChange() {
        const selectedProvider = providerSelect.value;
        if (selectedProvider === 'GEMINI') {
            geminiModeGroup.style.display = 'block';
            apiKeyGroup.style.display = 'none';
            apiKeyInput.value = '';
        } else {
            geminiModeGroup.style.display = 'none';
            apiKeyGroup.style.display = 'block';
        }
    }

    function showExplanationPanel(title, contentHtml) {
        explanationTitle.textContent = title;
        explanationBody.innerHTML = contentHtml;

        explanationContainer.style.display = 'flex';
        dragger.style.display = 'block';

        // 设置初始比例
        resultContainer.style.flexBasis = '60%';
        explanationContainer.style.flexBasis = '40%';
    }

    function hideExplanationPanel() {
        explanationContainer.style.display = 'none';
        dragger.style.display = 'none';
        resultContainer.style.flexBasis = '100%';
    }

    function updateExplanationContent(contentHtml) {
        explanationBody.innerHTML = contentHtml;
    }

    // ================== 主要功能 (基于V1.3.0) ==================
    async function onGenerateButtonClick() {
        generateButton.disabled = true;
        statusDiv.textContent = '初始化...';
        resultContainer.style.display = 'none';
        noteActions.style.display = 'none';
        hideExplanationPanel();

        try {
            statusDiv.textContent = '正在获取页面信息...';
            const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
            if (!tab) throw new Error('无法获取当前标签页！');
            currentVideoUrl = tab.url;

            if (!currentVideoUrl || !["bilibili.com/video", "youtube.com/watch"].some(host => currentVideoUrl.includes(host))) {
                throw new Error('请在支持的视频页面使用！');
            }

            statusDiv.textContent = '正在创建任务...';
            const requestData = {
                userId: 1,
                url: currentVideoUrl,
                mode: geminiModeSelect.value,
                provider: providerSelect.value,
                apiKey: apiKeyInput.value.trim()
            };

            const generateResponse = await fetch('http://localhost:8080/api/notes/generate', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(requestData),
            });
            if (!generateResponse.ok) throw new Error(`创建任务失败: ${await generateResponse.text()}`);

            const taskData = await generateResponse.json();
            await pollTaskStatusAndRender(taskData.taskId);

        } catch (error) {
            handleError(error);
        } finally {
            generateButton.disabled = false;
        }
    }

    async function pollTaskStatusAndRender(taskId) {
        statusDiv.textContent = `任务已创建...`;
        for (let i = 0; i < 60; i++) {
            const response = await fetch(`http://localhost:8080/api/tasks/${taskId}/status`);
            if (!response.ok) throw new Error('查询状态失败！');
            const task = await response.json();
            statusDiv.textContent = `处理中 (${i + 1}/60): ${task.statusMessage}`;
            if (task.status === 'COMPLETED') {
                statusDiv.textContent = '笔记生成成功！';
                await fetchAndRenderResult(taskId);
                return;
            } else if (task.status === 'FAILED') {
                throw new Error(`任务失败: ${task.statusMessage}`);
            }
            await new Promise(resolve => setTimeout(resolve, 5000));
        }
        throw new Error("任务处理超时。");
    }

    async function fetchAndRenderResult(taskId) {
        const response = await fetch(`http://localhost:8080/api/tasks/${taskId}/result`);
        if (!response.ok) throw new Error('获取结果失败！');
        const note = await response.json();
        currentNoteId = note.id;
        const noteData = JSON.parse(note.content);

        notesContent.innerHTML = '';
        rawMarkdownNote = '';
        let currentList = null;

        noteData.notes.forEach(block => {
            if (block.type !== 'list_item' && currentList) { notesContent.appendChild(currentList); currentList = null; }
            let element; const content = block.content;
            switch (block.type) {
                case 'heading': element = document.createElement('h2'); element.innerHTML = marked.parseInline(String(content || '')); notesContent.appendChild(element); rawMarkdownNote += `## ${content}\n\n`; break;
                case 'paragraph': element = document.createElement('p'); element.innerHTML = marked.parseInline(String(content || '')); notesContent.appendChild(element); rawMarkdownNote += `${content}\n\n`; break;
                case 'list_item': if (!currentList) currentList = document.createElement('ul'); element = document.createElement('li'); element.innerHTML = marked.parseInline(String(content || '')); currentList.appendChild(element); rawMarkdownNote += `* ${content}\n`; break;
                case 'knowledge_point':
                    element = createKnowledgePointElement(content);
                    const p = document.createElement('p');
                    p.appendChild(element);
                    notesContent.appendChild(p);
                    rawMarkdownNote += `> **${content.term}**: ${content.explanation}\n\n`;
                    break;
            }
        });
        if (currentList) notesContent.appendChild(currentList);

        noteActions.style.display = 'block';
        resultContainer.style.display = 'flex';
    }

    function createKnowledgePointElement(knowledgePointData) {
        const element = document.createElement('span');
        element.className = 'knowledge-point';
        element.textContent = knowledgePointData.term;
        element.title = `点击解释: ${knowledgePointData.explanation}`;

        element.addEventListener('click', async () => {
            const { term, explanation: shortExplanation } = knowledgePointData;
            const context = notesContent.innerText;

            showExplanationPanel(`正在解释: ${term}`, '<div class="spinner"></div>');
            try {
                const response = await fetch('http://localhost:8080/api/ai/explain', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        term, context, shortExplanation,
                        provider: providerSelect.value,
                        apiKey: apiKeyInput.value.trim()
                    }),
                });
                if (!response.ok) throw new Error((await response.json()).answer || 'AI 服务响应错误');
                const data = await response.json();
                updateExplanationContent(marked.parse(data.answer));
                explanationTitle.textContent = `解释: ${term}`;
            } catch (error) {
                updateExplanationContent(`<p style="color: red;">抱歉，无法获取解释：<br>${error.message}</p>`);
                explanationTitle.textContent = `解释失败: ${term}`;
            }
        });
        return element;
    }

    function copyNote() {
        navigator.clipboard.writeText(rawMarkdownNote).then(() => {
            const btn = document.getElementById('copyNoteBtn');
            const originalText = btn.textContent;
            btn.textContent = '已复制!';
            setTimeout(() => { btn.textContent = originalText; }, 2000);
        }).catch(err => console.error('复制失败:', err));
    }

    async function exportMarkdown() {
        if (!currentNoteId) {
            alert("错误：没有可导出的笔记ID。");
            return;
        }

        const btn = document.getElementById('exportMdBtn');
        const originalText = btn.textContent;
        btn.textContent = '转换中...';
        btn.disabled = true;

        try {
            // 【核心修正】在请求头中加入 provider 和 apiKey
            const headers = { 'Content-Type': 'application/json' };
            const selectedProvider = providerSelect.value;
            const userApiKey = apiKeyInput.value.trim();

            headers['X-Provider'] = selectedProvider;
            if (userApiKey) {
                headers['X-API-Key'] = userApiKey;
            }

            const response = await fetch(`http://localhost:8080/api/notes/export/${currentNoteId}`, {
                method: 'POST',
                headers: headers // 使用我们构建的 headers
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`导出失败: ${errorText}`);
            }

            // ... 后续下载逻辑不变 ...
            const markdownContent = await response.text();
            const blob = new Blob([markdownContent], { type: 'text/markdown;charset=utf-8' });
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
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
        navigator.clipboard.writeText(explanationBody.innerText).then(() => {
            const btn = document.getElementById('copyAnswerBtn');
            const originalText = btn.textContent;
            btn.textContent = '已复制!';
            setTimeout(() => { btn.textContent = originalText; }, 2000);
        }).catch(err => console.error('复制回答失败:', err));
    }
    function handleError(error) {
        console.error('Error:', error);
        statusDiv.textContent = `错误: ${error.message}`;
    }

    // ================== 面板拖拽逻辑 ==================
    function setupPanelResizing() {
        let isResizing = false;
        dragger.addEventListener('mousedown', () => { isResizing = true; document.body.style.cursor = 'ns-resize'; document.body.style.userSelect = 'none'; });
        document.addEventListener('mouseup', () => { isResizing = false; document.body.style.cursor = 'default'; document.body.style.userSelect = ''; });
        document.addEventListener('mousemove', (e) => {
            if (!isResizing) return;
            const contentWrapper = document.getElementById('content-wrapper');
            const wrapperRect = contentWrapper.getBoundingClientRect();
            const pointerRelativeY = e.clientY - wrapperRect.top;
            let noteAreaHeightPercent = (pointerRelativeY / wrapperRect.height) * 100;
            const minPercent = 15, maxPercent = 85;
            if (noteAreaHeightPercent < minPercent) noteAreaHeightPercent = minPercent;
            if (noteAreaHeightPercent > maxPercent) noteAreaHeightPercent = maxPercent;
            resultContainer.style.flexBasis = `${noteAreaHeightPercent}%`;
            explanationContainer.style.flexBasis = `${100 - noteAreaHeightPercent}%`;
        });
    }
});
