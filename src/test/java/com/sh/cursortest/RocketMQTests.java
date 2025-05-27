package com.sh.cursortest;

import com.sh.cursortest.entity.OrderMessage;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RocketMQTests {
    
    private static final String NAME_SERVER = "127.0.0.1:9876";
    private static final String TOPIC = "TestTopic";
    private static final String PRODUCER_GROUP = "test_producer_group";
    private static final String CONSUMER_GROUP = "test_consumer_group";
    
    // 配置 ObjectMapper 支持 Java 8 时间类型
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * 1. 同步发送消息
     */
    @Test
    void testSyncSendMessage() throws Exception {
        System.out.println("=== 同步发送消息演示 ===");
        
        // 创建生产者
        DefaultMQProducer producer = new DefaultMQProducer(PRODUCER_GROUP);
        producer.setNamesrvAddr(NAME_SERVER);
        producer.start();
        
        try {
            for (int i = 0; i < 3; i++) {
                // 创建消息
                OrderMessage orderMsg = OrderMessage.createSample("ORDER_" + i);
                String messageBody = objectMapper.writeValueAsString(orderMsg);
                
                Message message = new Message(
                    TOPIC,
                    "TagA",
                    ("SyncMessage_" + i).getBytes(RemotingHelper.DEFAULT_CHARSET)
                );
                message.setBody(messageBody.getBytes(RemotingHelper.DEFAULT_CHARSET));
                
                // 同步发送消息
                SendResult sendResult = producer.send(message);
                System.out.println("同步发送消息 " + i + ": " + sendResult);
            }
        } finally {
            producer.shutdown();
        }
        
        System.out.println();
    }

    /**
     * 2. 异步发送消息
     */
    @Test
    void testAsyncSendMessage() throws Exception {
        System.out.println("=== 异步发送消息演示 ===");
        
        DefaultMQProducer producer = new DefaultMQProducer(PRODUCER_GROUP);
        producer.setNamesrvAddr(NAME_SERVER);
        producer.start();
        
        CountDownLatch latch = new CountDownLatch(3);
        
        try {
            for (int i = 0; i < 3; i++) {
                final int index = i;
                OrderMessage orderMsg = OrderMessage.createSample("ASYNC_ORDER_" + i);
                String messageBody = objectMapper.writeValueAsString(orderMsg);
                
                Message message = new Message(
                    TOPIC,
                    "TagB",
                    ("AsyncMessage_" + i).getBytes(RemotingHelper.DEFAULT_CHARSET)
                );
                message.setBody(messageBody.getBytes(RemotingHelper.DEFAULT_CHARSET));
                
                // 异步发送消息
                producer.send(message, new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        System.out.println("异步发送消息 " + index + " 成功: " + sendResult.getMsgId());
                        latch.countDown();
                    }
                    
                    @Override
                    public void onException(Throwable e) {
                        System.err.println("异步发送消息 " + index + " 失败: " + e.getMessage());
                        latch.countDown();
                    }
                });
            }
            
            // 等待异步发送完成
            latch.await(10, TimeUnit.SECONDS);
        } finally {
            producer.shutdown();
        }
        
        System.out.println();
    }

    /**
     * 3. 单向发送消息（不关心发送结果）
     */
    @Test
    void testOneWaySendMessage() throws Exception {
        System.out.println("=== 单向发送消息演示 ===");
        
        DefaultMQProducer producer = new DefaultMQProducer(PRODUCER_GROUP);
        producer.setNamesrvAddr(NAME_SERVER);
        producer.start();
        
        try {
            for (int i = 0; i < 3; i++) {
                Message message = new Message(
                    TOPIC,
                    "TagC",
                    ("OneWayMessage_" + i).getBytes(RemotingHelper.DEFAULT_CHARSET)
                );
                
                // 单向发送消息
                producer.sendOneway(message);
                System.out.println("单向发送消息 " + i + " 已发送（不等待响应）");
            }
        } finally {
            producer.shutdown();
        }
        
        System.out.println();
    }

    /**
     * 4. 顺序消息发送
     */
    @Test
    void testOrderedMessage() throws Exception {
        System.out.println("=== 顺序消息发送演示 ===");
        
        DefaultMQProducer producer = new DefaultMQProducer(PRODUCER_GROUP);
        producer.setNamesrvAddr(NAME_SERVER);
        producer.start();
        
        try {
            String[] orders = {"订单创建", "订单支付", "订单发货", "订单完成"};
            
            for (int i = 0; i < orders.length; i++) {
                String orderId = "ORDER_12345";
                Message message = new Message(
                    TOPIC,
                    "TagOrder",
                    (orderId + "_" + orders[i]).getBytes(RemotingHelper.DEFAULT_CHARSET)
                );
                
                // 使用MessageQueueSelector确保同一订单的消息发送到同一队列
                SendResult sendResult = producer.send(message, new MessageQueueSelector() {
                    @Override
                    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                        String orderId = (String) arg;
                        int index = Math.abs(orderId.hashCode()) % mqs.size();
                        return mqs.get(index);
                    }
                }, orderId);
                
                System.out.println("顺序消息发送: " + orders[i] + ", QueueId: " + sendResult.getMessageQueue().getQueueId());
            }
        } finally {
            producer.shutdown();
        }
        
        System.out.println();
    }

    /**
     * 5. 延时消息发送
     */
    @Test
    void testDelayMessage() throws Exception {
        System.out.println("=== 延时消息发送演示 ===");
        
        DefaultMQProducer producer = new DefaultMQProducer(PRODUCER_GROUP);
        producer.setNamesrvAddr(NAME_SERVER);
        producer.start();
        
        try {
            Message message = new Message(
                TOPIC,
                "TagDelay",
                "延时消息内容".getBytes(RemotingHelper.DEFAULT_CHARSET)
            );
            
            // 设置延时级别：1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
            // 对应级别：1  2  3   4   5  6  7  8  9  10 11 12 13 14  15  16  17 18
            message.setDelayTimeLevel(3); // 10秒延时
            
            SendResult sendResult = producer.send(message);
            System.out.println("延时消息发送成功，10秒后消费者将收到消息: " + sendResult.getMsgId());
            System.out.println("当前时间: " + System.currentTimeMillis());
        } finally {
            producer.shutdown();
        }
        
        System.out.println();
    }

    /**
     * 6. 批量消息发送
     */
    @Test
    void testBatchMessage() throws Exception {
        System.out.println("=== 批量消息发送演示 ===");
        
        DefaultMQProducer producer = new DefaultMQProducer(PRODUCER_GROUP);
        producer.setNamesrvAddr(NAME_SERVER);
        producer.start();
        
        try {
            java.util.List<Message> messages = new java.util.ArrayList<>();
            
            for (int i = 0; i < 5; i++) {
                Message message = new Message(
                    TOPIC,
                    "TagBatch",
                    ("批量消息_" + i).getBytes(RemotingHelper.DEFAULT_CHARSET)
                );
                messages.add(message);
            }
            
            // 批量发送消息
            SendResult sendResult = producer.send(messages);
            System.out.println("批量发送 " + messages.size() + " 条消息成功: " + sendResult.getMsgId());
        } finally {
            producer.shutdown();
        }
        
        System.out.println();
    }

    /**
     * 7. 事务消息发送
     */
    @Test
    void testTransactionMessage() throws Exception {
        System.out.println("=== 事务消息发送演示 ===");
        
        TransactionMQProducer producer = new TransactionMQProducer(PRODUCER_GROUP);
        producer.setNamesrvAddr(NAME_SERVER);
        
        // 设置事务监听器
        producer.setTransactionListener(new TransactionListener() {
            @Override
            public org.apache.rocketmq.client.producer.LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                System.out.println("执行本地事务: " + new String(msg.getBody()));
                
                // 模拟本地事务执行
                try {
                    Thread.sleep(1000);
                    // 模拟事务成功
                    if (Math.random() > 0.3) {
                        System.out.println("本地事务执行成功");
                        return org.apache.rocketmq.client.producer.LocalTransactionState.COMMIT_MESSAGE;
                    } else {
                        System.out.println("本地事务执行失败");
                        return org.apache.rocketmq.client.producer.LocalTransactionState.ROLLBACK_MESSAGE;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return org.apache.rocketmq.client.producer.LocalTransactionState.ROLLBACK_MESSAGE;
                }
            }
            
            @Override
            public org.apache.rocketmq.client.producer.LocalTransactionState checkLocalTransaction(MessageExt msg) {
                System.out.println("检查本地事务状态: " + msg.getTransactionId());
                // 这里应该检查本地事务的实际状态
                return org.apache.rocketmq.client.producer.LocalTransactionState.COMMIT_MESSAGE;
            }
        });
        
        producer.start();
        
        try {
            Message message = new Message(
                TOPIC,
                "TagTransaction",
                "事务消息内容".getBytes(RemotingHelper.DEFAULT_CHARSET)
            );
            
            // 发送事务消息
            TransactionSendResult sendResult = producer.sendMessageInTransaction(message, null);
            System.out.println("事务消息发送结果: " + sendResult.getLocalTransactionState());
            
            // 等待事务处理完成
            Thread.sleep(3000);
        } finally {
            producer.shutdown();
        }
        
        System.out.println();
    }

    /**
     * 8. 消息消费者演示
     */
    @Test
    void testMessageConsumer() throws Exception {
        System.out.println("=== 消息消费者演示 ===");
        
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(CONSUMER_GROUP);
        consumer.setNamesrvAddr(NAME_SERVER);
        
        // 订阅主题和标签
        consumer.subscribe(TOPIC, "*");
        
        // 设置消息监听器
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(
                List<MessageExt> messages,
                ConsumeConcurrentlyContext context) {
                
                for (MessageExt message : messages) {
                    try {
                        String body = new String(message.getBody(), RemotingHelper.DEFAULT_CHARSET);
                        System.out.println("消费消息: " + 
                            "MsgId=" + message.getMsgId() + 
                            ", Tag=" + message.getTags() + 
                            ", Body=" + body +
                            ", QueueId=" + message.getQueueId() +
                            ", 接收时间=" + System.currentTimeMillis());
                    } catch (UnsupportedEncodingException e) {
                        System.err.println("消息解码失败: " + e.getMessage());
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }
                }
                
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        
        // 启动消费者
        consumer.start();
        System.out.println("消费者已启动，等待消息...");
        
        // 运行10秒后关闭
        Thread.sleep(10000);
        consumer.shutdown();
        
        System.out.println();
    }

    /**
     * 9. 消息过滤演示
     */
    @Test
    void testMessageFilter() throws Exception {
        System.out.println("=== 消息过滤演示 ===");
        
        // 发送带有不同属性的消息
        DefaultMQProducer producer = new DefaultMQProducer(PRODUCER_GROUP);
        producer.setNamesrvAddr(NAME_SERVER);
        producer.start();
        
        try {
            for (int i = 0; i < 5; i++) {
                Message message = new Message(
                    TOPIC,
                    "TagFilter",
                    ("过滤消息_" + i).getBytes(RemotingHelper.DEFAULT_CHARSET)
                );
                
                // 设置消息属性
                message.putUserProperty("level", i % 2 == 0 ? "high" : "low");
                message.putUserProperty("type", "filter_test");
                
                SendResult sendResult = producer.send(message);
                System.out.println("发送过滤消息 " + i + ", level=" + message.getUserProperty("level"));
            }
        } finally {
            producer.shutdown();
        }
        
        // 创建消费者，只消费level=high的消息
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(CONSUMER_GROUP + "_filter");
        consumer.setNamesrvAddr(NAME_SERVER);
        
        // 使用SQL过滤（需要broker支持）
        consumer.subscribe(TOPIC, "TagFilter", "level = 'high'");
        
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(
                List<MessageExt> messages,
                ConsumeConcurrentlyContext context) {
                
                for (MessageExt message : messages) {
                    try {
                        String body = new String(message.getBody(), RemotingHelper.DEFAULT_CHARSET);
                        System.out.println("过滤消费消息: " + body + 
                            ", level=" + message.getUserProperty("level"));
                    } catch (UnsupportedEncodingException e) {
                        System.err.println("消息解码失败: " + e.getMessage());
                    }
                }
                
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        
        consumer.start();
        Thread.sleep(5000);
        consumer.shutdown();
        
        System.out.println();
    }

    /**
     * 10. 消息重试机制演示
     */
    @Test
    void testMessageRetry() throws Exception {
        System.out.println("=== 消息重试机制演示 ===");
        
        // 发送消息
        DefaultMQProducer producer = new DefaultMQProducer(PRODUCER_GROUP);
        producer.setNamesrvAddr(NAME_SERVER);
        producer.start();
        
        try {
            Message message = new Message(
                TOPIC,
                "TagRetry",
                "重试测试消息".getBytes(RemotingHelper.DEFAULT_CHARSET)
            );
            
            SendResult sendResult = producer.send(message);
            System.out.println("发送重试测试消息: " + sendResult.getMsgId());
        } finally {
            producer.shutdown();
        }
        
        // 创建消费者，模拟消费失败
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(CONSUMER_GROUP + "_retry");
        consumer.setNamesrvAddr(NAME_SERVER);
        consumer.subscribe(TOPIC, "TagRetry");
        
        // 设置最大重试次数
        consumer.setMaxReconsumeTimes(3);
        
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            private int consumeCount = 0;
            
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(
                List<MessageExt> messages,
                ConsumeConcurrentlyContext context) {
                
                for (MessageExt message : messages) {
                    consumeCount++;
                    System.out.println("第 " + consumeCount + " 次消费消息: " + 
                        new String(message.getBody()) + 
                        ", 重试次数: " + message.getReconsumeTimes());
                    
                    // 前3次消费失败，第4次成功
                    if (consumeCount < 4) {
                        System.out.println("模拟消费失败，将重试");
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    } else {
                        System.out.println("消费成功");
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                }
                
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        
        consumer.start();
        Thread.sleep(30000); // 等待重试完成
        consumer.shutdown();
        
        System.out.println();
    }
}