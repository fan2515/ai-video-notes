package com.fan.aivideonotes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import java.net.InetSocketAddress;
import java.net.Proxy;

@SpringBootApplication
@EnableAsync
public class AiVideoNotesApplication {

    public static void main(String[] args) {

        SpringApplication.run(AiVideoNotesApplication.class, args);
    }



    @Bean
    @Primary // 告诉 Spring Boot 优先使用我们这个自定义的 Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册 JavaTimeModule 模块
        objectMapper.registerModule(new JavaTimeModule());
        System.out.println("!!! Custom ObjectMapper with JavaTimeModule has been configured. !!!");
        return objectMapper;
    }

    @Bean
    public WebClient webClient() {
        // 配置代理
        String proxyHost = "127.0.0.1";
        int proxyPort = 7897;

        HttpClient httpClient = HttpClient.create()
                .proxy(proxy -> proxy.type(ProxyProvider.Proxy.HTTP)
                        .host(proxyHost)
                        .port(proxyPort));

        System.out.println("!!! WebClient is configured to use proxy: " + proxyHost + ":" + proxyPort + " !!!");

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

}
