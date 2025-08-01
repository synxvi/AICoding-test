package com.sh.cursortest.service;

import com.sh.cursortest.model.MessageModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class MessageProducerService {

    private static final String TOPIC_NORMAL = "TOPIC_NORMAL";
    private static final String TOPIC_DELAY = "TOPIC_DELAY";
    private static final String TOPIC_TRANSACTION = "TOPIC_TRANSACTION";
    private static final String TOPIC_ORDER = "TOPIC_ORDER";
    private static final String TOPIC_BATCH = "TOPIC_BATCH";
    
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    
    // 发送普通消息
    public SendResult sendNormalMessage(String content) {
        MessageModel message = createMessage(content, "NORMAL");
        log.info("Sending normal message: {}", message);
        return rocketMQTemplate.syncSend(TOPIC_NORMAL, message);
    }
    
    // 发送异步消息
    public void sendAsyncMessage(String content) {
        MessageModel message = createMessage(content, "ASYNC");
        log.info("Sending async message: {}", message);
        rocketMQTemplate.asyncSend(TOPIC_NORMAL, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("Async message sent successfully, msgId: {}", sendResult.getMsgId());
            }
            
            @Override
            public void onException(Throwable throwable) {
                log.error("Failed to send async message", throwable);
            }
        });
    }
    
    // 发送延迟消息
    public SendResult sendDelayMessage(String content, int delayLevel) {
        MessageModel message = createMessage(content, "DELAY");
        log.info("Sending delay message with level {}: {}", delayLevel, message);
        return rocketMQTemplate.syncSend(TOPIC_DELAY, MessageBuilder.withPayload(message).build(), 
                3000, delayLevel);
    }
    
    // 发送顺序消息
    public SendResult sendOrderlyMessage(String content, String orderId) {
        MessageModel message = createMessage(content, "ORDERLY");
        log.info("Sending orderly message for orderId {}: {}", orderId, message);
        // 使用orderId作为hashKey确保相同订单ID的消息发送到同一队列
        return rocketMQTemplate.syncSendOrderly(TOPIC_ORDER, message, orderId);
    }
    
    // 创建消息对象
    private MessageModel createMessage(String content, String type) {
        return MessageModel.builder()
                .id(UUID.randomUUID().toString())
                .content(content)
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .type(type)
                .build();
    }
} 