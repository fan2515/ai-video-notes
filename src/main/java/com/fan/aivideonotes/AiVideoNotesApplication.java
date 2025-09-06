package com.fan.aivideonotes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

@SpringBootApplication
@EnableAsync
public class AiVideoNotesApplication {

    public static void main(String[] args) {

        SpringApplication.run(AiVideoNotesApplication.class, args);
    }

    // 将 RestTemplate 注册为一个 Bean，方便在各个 Service 中注入使用
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        // 你的 Clash 代理地址和端口
        String proxyHost = "127.0.0.1";
        int proxyPort = 7897;

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        requestFactory.setProxy(proxy);

        // --- START: 新增超时配置 ---
        // 设置连接超时时间，单位为毫秒 (例如 10 秒)
        requestFactory.setConnectTimeout(10000);
        // 设置读取超时时间，单位为毫秒 (例如 60 秒)
        requestFactory.setReadTimeout(60000);
        // --- END: 新增超时配置 ---

        System.out.println("!!! RestTemplate is configured with proxy and timeouts !!!");

        return new RestTemplate(requestFactory);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
