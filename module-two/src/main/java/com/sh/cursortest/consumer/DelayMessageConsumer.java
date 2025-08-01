package com.sh.cursortest.consumer;

import com.sh.cursortest.model.MessageModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = "TOPIC_DELAY",
        consumerGroup = "delay-consumer-group"
)
public class DelayMessageConsumer implements RocketMQListener<MessageModel> {

    @Override
    public void onMessage(MessageModel message) {
        log.info("Delay consumer received message at {}: {}", 
                LocalDateTime.now(), message);
        log.info("Message timestamp: {}", message.getTimestamp());
    }
} 