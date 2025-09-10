// popup.js (Final Production Version)

// 我们把事件监听函数改成 async 函数，这样就可以在里面使用 await
document.getElementById('generateButton').addEventListener('click', async () => {
    const button = document.getElementById('generateButton');
    const statusDiv = document.getElementById('status');
    const resultContainer = document.getElementById('resultContainer');

    // 初始化界面
    button.disabled = true;
    statusDiv.textContent = '初始化...';
    resultContainer.style.display = 'none';

    try {
        // --- 1. 获取标签页 URL (使用 await，代码更简洁) ---
        statusDiv.textContent = '正在获取页面信息...';
        const tabs = await chrome.tabs.query({ active: true, currentWindow: true });

        if (!tabs || tabs.length === 0) {
            throw new Error('无法获取当前标签页！');
        }

        const videoUrl = tabs[0].url;
        if (!videoUrl.includes("youtube.com/watch") && !videoUrl.includes("bilibili.com/video")) {
            throw new Error('请在支持的视频页面使用！');
        }

        // --- 2. 发送生成请求 ---
        statusDiv.textContent = '已获取 URL，正在创建任务...';
        const requestData = { userId: 1, url: videoUrl, mode: "FLASH" };

        const generateResponse = await fetch('http://localhost:8080/api/notes/generate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestData),
        });

        if (!generateResponse.ok) {
            throw new Error(`创建任务失败: ${generateResponse.statusText}`);
        }

        const taskData = await generateResponse.json();
        const taskId = taskData.taskId;

        // --- 3. 开始轮询 ---
        statusDiv.textContent = `任务已创建 (ID: ${taskId.substring(0, 8)})... 正在处理中...`;
        await pollTaskStatus(taskId);

    } catch (error) {
        handleError(error);
    } finally {
        button.disabled = false;
    }
});

// 轮询函数
async function pollTaskStatus(taskId) {
    const statusDiv = document.getElementById('status');

    // 设置一个最长轮询时间，比如 5 分钟，防止无限循环
    const maxAttempts = 60; // 60次 * 5秒/次 = 300秒 = 5分钟
    let attempt = 0;

    while (attempt < maxAttempts) {
        attempt++;
        const response = await fetch(`http://localhost:8080/api/tasks/${taskId}/status`);
        if (!response.ok) {
            throw new Error('查询状态失败！');
        }

        const task = await response.json();
        statusDiv.textContent = `状态(${attempt}/${maxAttempts}): ${task.statusMessage}`;

        if (task.status === 'COMPLETED') {
            statusDiv.textContent = '笔记生成成功！正在获取结果...';
            await fetchAndRenderResult(taskId);
            return; // 成功，退出循环
        } else if (task.status === 'FAILED') {
            throw new Error(`任务处理失败: ${task.statusMessage}`);
        }

        // 等待 5 秒
        await new Promise(resolve => setTimeout(resolve, 5000));
    }

    throw new Error("任务处理超时，请稍后再试。");
}

// 获取并渲染结果的函数
async function fetchAndRenderResult(taskId) {
    const response = await fetch(`http://localhost:8080/api/tasks/${taskId}/result`);
    if (!response.ok) {
        throw new Error('获取最终结果失败！');
    }

    const note = await response.json();
    if (!note || !note.content) {
        throw new Error('获取到的笔记内容为空！');
    }

    const noteContent = JSON.parse(note.content);

    document.getElementById('noteTitle').textContent = noteContent.title;
    document.getElementById('noteSummary').textContent = noteContent.summary;

    const detailedNotesDiv = document.getElementById('detailedNotes');
    detailedNotesDiv.innerHTML = '';

    noteContent.detailed_notes.forEach(topic => {
        const topicElement = document.createElement('div');
        topicElement.className = 'topic';
        topicElement.textContent = topic.topic;

        const pointsList = document.createElement('ul');
        topic.points.forEach(point => {
            const pointElement = document.createElement('li');
            pointElement.textContent = point;
            pointsList.appendChild(pointElement);
        });

        detailedNotesDiv.appendChild(topicElement);
        detailedNotesDiv.appendChild(pointsList);
    });

    document.getElementById('resultContainer').style.display = 'block';
}

// 统一的错误处理函数
function handleError(error) {
    console.error('An error occurred:', error);
    document.getElementById('status').textContent = `发生错误: ${error.message}`;
}