package com.sh.cursortest.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessage {
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createTime;
    
    public static OrderMessage createSample(String orderId) {
        return new OrderMessage(
            orderId,
            "user_" + System.currentTimeMillis() % 1000,
            new BigDecimal("99.99"),
            "CREATED",
            LocalDateTime.now()
        );
    }
}