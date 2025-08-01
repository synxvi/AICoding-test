package com.sh.cursortest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CursorTestApplication {

    private static final Logger logger = LoggerFactory.getLogger(CursorTestApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CursorTestApplication.class, args);
        logger.info("RocketMQ Producer Application Started");
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            logger.info("Producer is ready to send messages. Access API at http://localhost:8081/api/messages/");
            logger.info("Available endpoints:");
            logger.info("POST /api/messages/normal?content=YOUR_MESSAGE - 发送普通消息");
            logger.info("POST /api/messages/async?content=YOUR_MESSAGE - 发送异步消息");
            logger.info("POST /api/messages/delay?content=YOUR_MESSAGE&delayLevel=3 - 发送延迟消息");
            logger.info("POST /api/messages/orderly?content=YOUR_MESSAGE&orderId=123 - 发送顺序消息");
        };
    }
}

