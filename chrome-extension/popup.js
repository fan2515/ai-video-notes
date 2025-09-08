// 监听“生成笔记”按钮的点击事件
document.getElementById('generateButton').addEventListener('click', () => {
    // 禁用按钮，防止重复点击
    const button = document.getElementById('generateButton');
    const statusDiv = document.getElementById('status');
    
    button.disabled = true;
    statusDiv.textContent = '正在获取页面信息...';

    // 1. 获取当前激活的、在 YouTube 网站上的标签页
    chrome.tabs.query({ active: true, currentWindow: true, url: "*://*.youtube.com/watch*" }, (tabs) => {
        if (tabs.length === 0) {
            statusDiv.textContent = '请在 YouTube 视频页面使用！';
            button.disabled = false;
            return;
        }

        const currentTab = tabs[0];
        const videoUrl = currentTab.url;
        statusDiv.textContent = '已获取 URL，正在发送请求...';

        // 2. 准备发送给后端的数据
        const requestData = {
            userId: 1, // 暂时硬编码为用户 1
            url: videoUrl,
            mode: "FLASH" // 默认使用快速模式
        };

        // 3. 使用 fetch API 调用我们的后端服务
        fetch('http://localhost:8080/api/notes/generate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestData),
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('网络响应错误！');
            }
            return response.json();
        })
        .then(data => {
            // 成功收到后端的响应
            statusDiv.textContent = `任务已开始！任务 ID: ${data.taskId}`;
        })
        .catch((error) => {
            // 如果请求失败
            console.error('Error:', error);
            statusDiv.textContent = '请求失败！请确保后端服务正在运行。';
        })
        .finally(() => {
            // 无论成功失败，都重新启用按钮
            button.disabled = false;
        });
    });
});