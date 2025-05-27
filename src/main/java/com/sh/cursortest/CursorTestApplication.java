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
    }

}

