package com.fan.aivideonotes.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class VideoProcessingService {

    /**
     * 从给定的 URL 下载视频，并智能地尝试使用不同浏览器的 Cookies.
     * @param videoUrl 视频的 URL (例如 YouTube 链接)
     * @return 下载好的视频文件对象
     */
    public File downloadVideo(String videoUrl) {
        try {
            // 1. 创建一个唯一的临时目录
            Path tempDir = Files.createTempDirectory("video-processing-" + UUID.randomUUID());
            System.out.println("Created temporary directory: " + tempDir);

            // 2. 定义要尝试的浏览器列表
            //    你可以根据自己常用的浏览器调整顺序，Firefox 通常最稳定
            String[] browsersToTry = {"firefox", "edge", "chrome"};

            // 3. 循环尝试每个浏览器
            for (String browser : browsersToTry) {
                // 确保对应的浏览器已关闭，特别是 Chrome
                System.out.println("Attempting to download using cookies from browser: " + browser);
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "yt-dlp",
                        "--cookies-from-browser", browser,
                        "-o", tempDir.resolve("%(title)s.%(ext)s").toString(),
                        "-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best",
                        "--max-filesize", "500m",
                        videoUrl
                );

                try {
                    // 尝试用当前浏览器执行命令
                    executeCommand(processBuilder);
                    // 如果 executeCommand 没有抛出异常，说明成功了！
                    System.out.println("Successfully downloaded using " + browser + " cookies.");
                    return findDownloadedFile(tempDir);
                } catch (Exception e) {
                    // 如果失败了，打印错误信息，然后循环会继续尝试下一个浏览器
                    System.err.println("Failed to download using cookies from " + browser + ". Reason: " + e.getMessage());
                }
            }

            // 4. 如果所有浏览器都尝试失败了，才最终抛出异常
            throw new RuntimeException("Failed to download video after trying all available browsers. Please ensure you are logged into YouTube on at least one browser and the browser is closed.");

        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary directory for video download", e);
        }
    }

    /**
     * 从视频文件中提取 MP3 音频.
     * @param videoFile 本地视频文件
     * @return 提取出的音频文件对象
     */
    public File extractAudio(File videoFile) {
        System.out.println("Attempting to extract audio from: " + videoFile.getAbsolutePath());
        try {
            String outputFileName = videoFile.getName().replaceFirst("[.][^.]+$", "") + ".mp3";
            File audioFile = new File(videoFile.getParentFile(), outputFileName);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "ffmpeg",
                    "-i", videoFile.getAbsolutePath(),
                    "-vn",
                    "-acodec", "libmp3lame",
                    "-q:a", "2",
                    "-y", // Overwrite output file if it exists
                    audioFile.getAbsolutePath()
            );

            executeCommand(processBuilder);

            if (audioFile.exists() && audioFile.length() > 0) {
                return audioFile;
            } else {
                throw new IOException("ffmpeg execution finished, but output audio file was not created or is empty.");
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to extract audio", e);
        }
    }

    /**
     * 在指定目录下查找第一个非 .part 的文件.
     * @param directory 搜索的目录
     * @return 找到的文件
     * @throws IOException 如果找不到文件
     */
    private File findDownloadedFile(Path directory) throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(p -> !p.toString().endsWith(".part"))
                    .findFirst()
                    .map(Path::toFile)
                    .orElseThrow(() -> new IOException("Downloaded file not found in directory: " + directory));
        }
    }

    /**
     * 执行一个命令行进程，并实时打印其输出.
     * @param processBuilder 已经配置好的 ProcessBuilder 对象
     * @throws IOException 如果命令执行失败 (退出码非0)
     * @throws InterruptedException 如果线程被中断
     */
    private void executeCommand(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        System.out.println("Executing command: " + String.join(" ", processBuilder.command()));

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[CMD Output] " + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Command execution failed with exit code " + exitCode);
        }
        System.out.println("Command executed successfully.");
    }
}