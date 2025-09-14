// background.js

console.log("background.js service worker has started.");

// 监听当用户点击插件图标时的事件
// background.js (简洁可靠的最终版)

chrome.action.onClicked.addListener((tab) => {
    // open API 会自动处理打开和聚焦的逻辑
    // 它打开的是当前窗口的全局侧边栏
    chrome.sidePanel.open({ windowId: tab.windowId });
});