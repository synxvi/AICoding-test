package com.sh.cursortest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ConsumerApplication {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
        logger.info("RocketMQ Consumer Application Started");
    }
    
    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            logger.info("Consumer is ready to receive messages from topics:");
            logger.info("- TOPIC_NORMAL (普通消息和异步消息)");
            logger.info("- TOPIC_DELAY (延迟消息)");
            logger.info("- TOPIC_ORDER (顺序消息)");
        };
    }
} 