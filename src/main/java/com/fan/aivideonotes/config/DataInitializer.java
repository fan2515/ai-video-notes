package com.fan.aivideonotes.config;

import com.fan.aivideonotes.model.User;
import com.fan.aivideonotes.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Autowired
    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Initializing test data...");

        // 1. 创建一个没有自定义 API Key 的普通用户 (将使用 Gemini)
        User normalUser = new User();
        normalUser.setUsername("normal_user");
        userRepository.save(normalUser);
        System.out.println("Created user: " + normalUser.getUsername() + " with ID: " + normalUser.getId());

        // 2. 创建一个配置了自定义 API Key 的高级用户 (未来将使用 OpenAI)
        User proUser = new User();
        proUser.setUsername("pro_user");
        // 注意：这里我们存入一个假的 Key，因为 Jasypt 会自动加密它
        // 在真实场景中，这将通过一个更新用户信息的 API 来设置
        proUser.setOpenaiApiKey("sk-this-is-a-fake-key-for-testing-purpose");
        userRepository.save(proUser);
        System.out.println("Created user: " + proUser.getUsername() + " with ID: " + proUser.getId());
    }

}
