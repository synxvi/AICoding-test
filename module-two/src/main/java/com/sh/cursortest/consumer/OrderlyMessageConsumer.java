package com.sh.cursortest.consumer;

import com.sh.cursortest.model.MessageModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = "TOPIC_ORDER",
        consumerGroup = "orderly-consumer-group",
        consumeMode = ConsumeMode.ORDERLY
)
public class OrderlyMessageConsumer implements RocketMQListener<MessageModel> {

    @Override
    public void onMessage(MessageModel message) {
        log.info("Orderly consumer received message: {}", message);
        try {
            // 模拟处理时间
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
} 