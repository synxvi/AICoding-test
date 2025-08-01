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
        topic = "TOPIC_NORMAL",
        consumerGroup = "normal-consumer-group",
        messageModel = org.apache.rocketmq.spring.annotation.MessageModel.CLUSTERING
)
public class MessageConsumer implements RocketMQListener<com.sh.cursortest.model.MessageModel> {

    @Override
    public void onMessage(com.sh.cursortest.model.MessageModel message) {
        log.info("Normal consumer received message: {}", message);
    }
} 