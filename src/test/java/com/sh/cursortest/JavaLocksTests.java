package com.sh.cursortest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class JavaLocksTests {

    /**
     * 1. synchronized关键字 - 内置锁
     */
    @Test
    void testSynchronizedLock() throws InterruptedException {
        System.out.println("=== synchronized关键字演示 ===");
        
        SynchronizedCounter counter = new SynchronizedCounter();
        
        // 创建多个线程同时操作计数器
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    counter.increment();
                }
            });
            threads.add(thread);
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("synchronized计数器最终值: " + counter.getCount());
        System.out.println("预期值: 10000\n");
    }
    
    // synchronized示例类
    static class SynchronizedCounter {
        private int count = 0;
        
        // 同步方法
        public synchronized void increment() {
            count++;
        }
        
        // 同步代码块
        public void decrement() {
            synchronized (this) {
                count--;
            }
        }
        
        public synchronized int getCount() {
            return count;
        }
    }

    /**
     * 2. ReentrantLock - 可重入锁
     */
    @Test
    void testReentrantLock() throws InterruptedException {
        System.out.println("=== ReentrantLock演示 ===");
        
        ReentrantLockCounter counter = new ReentrantLockCounter();
        
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    counter.increment();
                }
            });
            threads.add(thread);
            thread.start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("ReentrantLock计数器最终值: " + counter.getCount());
        
        // 演示tryLock
        counter.demonstrateTryLock();
        System.out.println();
    }
    
    static class ReentrantLockCounter {
        private final ReentrantLock lock = new ReentrantLock();
        private int count = 0;
        
        public void increment() {
            lock.lock();
            try {
                count++;
                // 演示可重入性
                if (count % 1000 == 0) {
                    recursiveMethod();
                }
            } finally {
                lock.unlock();
            }
        }
        
        // 可重入方法
        private void recursiveMethod() {
            lock.lock();
            try {
                System.out.println("可重入锁演示 - 当前计数: " + count + ", 线程: " + Thread.currentThread().getName());
            } finally {
                lock.unlock();
            }
        }
        
        public int getCount() {
            lock.lock();
            try {
                return count;
            } finally {
                lock.unlock();
            }
        }
        
        public void demonstrateTryLock() {
            System.out.println("\n--- tryLock演示 ---");
            
            Thread thread1 = new Thread(() -> {
                lock.lock();
                try {
                    System.out.println("线程1获得锁，休眠2秒");
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                    System.out.println("线程1释放锁");
                }
            });
            
            Thread thread2 = new Thread(() -> {
                try {
                    Thread.sleep(500); // 确保线程1先获得锁
                    if (lock.tryLock(1, TimeUnit.SECONDS)) {
                        try {
                            System.out.println("线程2通过tryLock获得锁");
                        } finally {
                            lock.unlock();
                        }
                    } else {
                        System.out.println("线程2 tryLock超时，未获得锁");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            thread1.start();
            thread2.start();
            
            try {
                thread1.join();
                thread2.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 3. ReadWriteLock - 读写锁
     */
    @Test
    void testReadWriteLock() throws InterruptedException {
        System.out.println("=== ReadWriteLock演示 ===");
        
        ReadWriteLockExample example = new ReadWriteLockExample();
        
        // 创建多个读线程
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final int readerId = i;
            Thread reader = new Thread(() -> {
                for (int j = 0; j < 3; j++) {
                    example.read(readerId);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            threads.add(reader);
        }
        
        // 创建写线程
        for (int i = 0; i < 2; i++) {
            final int writerId = i;
            Thread writer = new Thread(() -> {
                for (int j = 0; j < 2; j++) {
                    example.write(writerId, "数据" + writerId + "-" + j);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            threads.add(writer);
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println();
    }
    
    static class ReadWriteLockExample {
        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        private final Lock readLock = lock.readLock();
        private final Lock writeLock = lock.writeLock();
        private String data = "初始数据";
        
        public void read(int readerId) {
            readLock.lock();
            try {
                System.out.println("读者" + readerId + " 读取数据: " + data + 
                    " [" + Thread.currentThread().getName() + "]");
                Thread.sleep(500); // 模拟读取耗时
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                readLock.unlock();
            }
        }
        
        public void write(int writerId, String newData) {
            writeLock.lock();
            try {
                System.out.println("写者" + writerId + " 开始写入数据: " + newData + 
                    " [" + Thread.currentThread().getName() + "]");
                this.data = newData;
                Thread.sleep(800); // 模拟写入耗时
                System.out.println("写者" + writerId + " 写入完成");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                writeLock.unlock();
            }
        }
    }

    /**
     * 4. StampedLock - 邮戳锁（Java 8+）
     */
    @Test
    void testStampedLock() throws InterruptedException {
        System.out.println("=== StampedLock演示 ===");
        
        StampedLockExample example = new StampedLockExample();
        
        // 创建读线程
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            final int readerId = i;
            Thread reader = new Thread(() -> {
                for (int j = 0; j < 2; j++) {
                    example.optimisticRead(readerId);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            threads.add(reader);
        }
        
        // 创建写线程
        Thread writer = new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                example.write("新数据" + i);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        threads.add(writer);
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println();
    }
    
    static class StampedLockExample {
        private final StampedLock lock = new StampedLock();
        private String data = "初始数据";
        
        // 乐观读
        public void optimisticRead(int readerId) {
            long stamp = lock.tryOptimisticRead();
            String currentData = data; // 读取数据
            
            if (!lock.validate(stamp)) {
                // 乐观读失败，升级为悲观读
                System.out.println("读者" + readerId + " 乐观读失败，升级为悲观读");
                stamp = lock.readLock();
                try {
                    currentData = data;
                } finally {
                    lock.unlockRead(stamp);
                }
            }
            
            System.out.println("读者" + readerId + " 读取数据: " + currentData + 
                " [" + Thread.currentThread().getName() + "]");
        }
        
        // 写操作
        public void write(String newData) {
            long stamp = lock.writeLock();
            try {
                System.out.println("开始写入数据: " + newData + 
                    " [" + Thread.currentThread().getName() + "]");
                this.data = newData;
                Thread.sleep(200); // 模拟写入耗时
                System.out.println("写入完成: " + newData);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlockWrite(stamp);
            }
        }
    }

    /**
     * 5. Condition - 条件变量
     */
    @Test
    void testCondition() throws InterruptedException {
        System.out.println("=== Condition条件变量演示 ===");
        
        BoundedBuffer<String> buffer = new BoundedBuffer<>(3);
        
        // 生产者线程
        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    String item = "商品" + i;
                    buffer.put(item);
                    System.out.println("生产者生产: " + item);
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // 消费者线程
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    String item = buffer.take();
                    System.out.println("消费者消费: " + item);
                    Thread.sleep(300);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        producer.start();
        consumer.start();
        
        producer.join();
        consumer.join();
        
        System.out.println();
    }
    
    // 有界缓冲区示例
    static class BoundedBuffer<T> {
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notFull = lock.newCondition();
        private final Condition notEmpty = lock.newCondition();
        private final Object[] items;
        private int putIndex, takeIndex, count;
        
        public BoundedBuffer(int capacity) {
            items = new Object[capacity];
        }
        
        public void put(T item) throws InterruptedException {
            lock.lock();
            try {
                while (count == items.length) {
                    System.out.println("缓冲区已满，生产者等待...");
                    notFull.await();
                }
                items[putIndex] = item;
                putIndex = (putIndex + 1) % items.length;
                count++;
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }
        
        @SuppressWarnings("unchecked")
        public T take() throws InterruptedException {
            lock.lock();
            try {
                while (count == 0) {
                    System.out.println("缓冲区为空，消费者等待...");
                    notEmpty.await();
                }
                T item = (T) items[takeIndex];
                items[takeIndex] = null;
                takeIndex = (takeIndex + 1) % items.length;
                count--;
                notFull.signal();
                return item;
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 6. Semaphore - 信号量
     */
    @Test
    void testSemaphore() throws InterruptedException {
        System.out.println("=== Semaphore信号量演示 ===");
        
        // 模拟停车场，只有3个停车位
        Semaphore parkingLot = new Semaphore(3);
        
        List<Thread> cars = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            final int carId = i;
            Thread car = new Thread(() -> {
                try {
                    System.out.println("车辆" + carId + " 尝试进入停车场...");
                    parkingLot.acquire(); // 获取停车位
                    System.out.println("车辆" + carId + " 成功停车，剩余停车位: " + parkingLot.availablePermits());
                    
                    Thread.sleep(2000); // 停车2秒
                    
                    System.out.println("车辆" + carId + " 离开停车场");
                    parkingLot.release(); // 释放停车位
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            cars.add(car);
        }
        
        // 启动所有车辆线程
        for (Thread car : cars) {
            car.start();
            Thread.sleep(100); // 间隔启动
        }
        
        // 等待所有车辆完成
        for (Thread car : cars) {
            car.join();
        }
        
        System.out.println();
    }

    /**
     * 7. CountDownLatch - 倒计时门闩
     */
    @Test
    void testCountDownLatch() throws InterruptedException {
        System.out.println("=== CountDownLatch倒计时门闩演示 ===");
        
        int workerCount = 5;
        CountDownLatch latch = new CountDownLatch(workerCount);
        
        // 创建工作线程
        List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < workerCount; i++) {
            final int workerId = i;
            Thread worker = new Thread(() -> {
                try {
                    System.out.println("工作者" + workerId + " 开始工作...");
                    Thread.sleep((workerId + 1) * 1000); // 不同的工作时间
                    System.out.println("工作者" + workerId + " 完成工作");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown(); // 完成一个任务
                }
            });
            workers.add(worker);
        }
        
        // 启动所有工作线程
        for (Thread worker : workers) {
            worker.start();
        }
        
        System.out.println("主线程等待所有工作者完成...");
        latch.await(); // 等待所有工作完成
        System.out.println("所有工作者都完成了，主线程继续执行\n");
    }

    /**
     * 8. CyclicBarrier - 循环屏障
     */
    @Test
    void testCyclicBarrier() throws InterruptedException {
        System.out.println("=== CyclicBarrier循环屏障演示 ===");
        
        int participantCount = 3;
        CyclicBarrier barrier = new CyclicBarrier(participantCount, () -> {
            System.out.println("*** 所有参与者都到达屏障点，开始下一阶段 ***");
        });
        
        List<Thread> participants = new ArrayList<>();
        for (int i = 0; i < participantCount; i++) {
            final int participantId = i;
            Thread participant = new Thread(() -> {
                try {
                    for (int phase = 1; phase <= 2; phase++) {
                        System.out.println("参与者" + participantId + " 完成阶段" + phase);
                        Thread.sleep((participantId + 1) * 500); // 不同的完成时间
                        
                        System.out.println("参与者" + participantId + " 等待其他参与者...");
                        barrier.await(); // 等待其他参与者
                        
                        System.out.println("参与者" + participantId + " 继续执行阶段" + (phase + 1));
                    }
                } catch (InterruptedException | BrokenBarrierException e) {
                    Thread.currentThread().interrupt();
                }
            });
            participants.add(participant);
        }
        
        // 启动所有参与者
        for (Thread participant : participants) {
            participant.start();
        }
        
        // 等待所有参与者完成
        for (Thread participant : participants) {
            participant.join();
        }
        
        System.out.println();
    }

    /**
     * 9. 锁性能对比测试
     */
    @Test
    void testLockPerformance() throws InterruptedException {
        System.out.println("=== 锁性能对比测试 ===");
        
        int threadCount = 10;
        int operationsPerThread = 100000;
        
        // 测试synchronized
        SynchronizedCounter syncCounter = new SynchronizedCounter();
        long syncTime = measureTime(() -> {
            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                Thread thread = new Thread(() -> {
                    for (int j = 0; j < operationsPerThread; j++) {
                        syncCounter.increment();
                    }
                });
                threads.add(thread);
                thread.start();
            }
            
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        
        // 测试ReentrantLock
        ReentrantLockCounter lockCounter = new ReentrantLockCounter();
        long lockTime = measureTime(() -> {
            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                Thread thread = new Thread(() -> {
                    for (int j = 0; j < operationsPerThread; j++) {
                        lockCounter.increment();
                    }
                });
                threads.add(thread);
                thread.start();
            }
            
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        
        System.out.println("synchronized耗时: " + syncTime + "ms, 最终值: " + syncCounter.getCount());
        System.out.println("ReentrantLock耗时: " + lockTime + "ms, 最终值: " + lockCounter.getCount());
        System.out.println("预期值: " + (threadCount * operationsPerThread));
    }
    
    private long measureTime(Runnable task) {
        long startTime = System.currentTimeMillis();
        task.run();
        return System.currentTimeMillis() - startTime;
    }
}