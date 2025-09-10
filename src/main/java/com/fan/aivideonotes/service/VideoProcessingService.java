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
    // 在 VideoProcessingService.java 中

    public File downloadVideo(String videoUrl) {
        try {
            Path tempDir = Files.createTempDirectory("video-processing-" + UUID.randomUUID());
            System.out.println("Created temporary directory for download: " + tempDir);

            // --- 终极简化版：无 Cookies 下载 (专为 Bilibili 等网站设计) ---

            System.out.println("Attempting to download a video without cookies: " + videoUrl);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "yt-dlp",
                    "--no-playlist",
                    "-o", tempDir.resolve("%(title)s.%(ext)s").toString(),
                    "-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best",
                    "--max-filesize", "500m",
                    videoUrl
            );

            executeCommand(processBuilder);
            return findDownloadedFile(tempDir);

        } catch (Exception e) {
            System.err.println("!!!!!! DETAILED DOWNLOAD ERROR !!!!!!");
            e.printStackTrace();
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            throw new RuntimeException("Failed to download video from the provided URL.", e);
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