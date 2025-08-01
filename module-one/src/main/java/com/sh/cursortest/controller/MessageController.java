package com.sh.cursortest.controller;

import com.sh.cursortest.service.MessageProducerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageProducerService messageProducerService;

    @PostMapping("/normal")
    public ResponseEntity<Map<String, String>> sendNormalMessage(@RequestParam String content) {
        log.info("Received request to send normal message: {}", content);
        SendResult result = messageProducerService.sendNormalMessage(content);
        return createSuccessResponse(result, "普通消息发送成功");
    }

    @PostMapping("/async")
    public ResponseEntity<Map<String, String>> sendAsyncMessage(@RequestParam String content) {
        log.info("Received request to send async message: {}", content);
        messageProducerService.sendAsyncMessage(content);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "异步消息发送请求已接收");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delay")
    public ResponseEntity<Map<String, String>> sendDelayMessage(
            @RequestParam String content, 
            @RequestParam(defaultValue = "3") int delayLevel) {
        log.info("Received request to send delay message: {}, delayLevel: {}", content, delayLevel);
        SendResult result = messageProducerService.sendDelayMessage(content, delayLevel);
        return createSuccessResponse(result, "延迟消息发送成功，延迟级别: " + delayLevel);
    }

    @PostMapping("/orderly")
    public ResponseEntity<Map<String, String>> sendOrderlyMessage(
            @RequestParam String content, 
            @RequestParam String orderId) {
        log.info("Received request to send orderly message: {}, orderId: {}", content, orderId);
        SendResult result = messageProducerService.sendOrderlyMessage(content, orderId);
        return createSuccessResponse(result, "顺序消息发送成功，订单ID: " + orderId);
    }

    private ResponseEntity<Map<String, String>> createSuccessResponse(SendResult result, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        response.put("msgId", result.getMsgId());
        response.put("queueId", String.valueOf(result.getMessageQueue().getQueueId()));
        return ResponseEntity.ok(response);
    }
} 