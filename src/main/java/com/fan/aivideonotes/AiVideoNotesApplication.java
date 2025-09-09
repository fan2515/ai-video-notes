package com.fan.aivideonotes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
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
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
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



    // 1. RestTemplate Bean (带代理和超时)
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        String proxyHost = "127.0.0.1";
        int proxyPort = 7897;
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        requestFactory.setProxy(proxy);
        requestFactory.setConnectTimeout(10000);
        requestFactory.setReadTimeout(60000);
        System.out.println("!!! RestTemplate is configured with proxy and timeouts !!!");
        return new RestTemplate(requestFactory);
    }

    // 2. ObjectMapper Bean (带 JSR310 模块)
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 1. 注册 JavaTimeModule (解决 LocalDateTime 问题)
        objectMapper.registerModule(new JavaTimeModule());

        // 2. 【新增】注册 Hibernate6Module (解决懒加载代理问题)
        objectMapper.registerModule(new Hibernate6Module());

        // 3. 禁用将日期写成时间戳的行为
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        System.out.println("!!! Custom ObjectMapper with JavaTimeModule AND Hibernate6Module has been configured. !!!");
        return objectMapper;
    }

    // 3. CORS 配置 Bean
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("*")
                        .allowedHeaders("*");
            }
        };
    }

}
