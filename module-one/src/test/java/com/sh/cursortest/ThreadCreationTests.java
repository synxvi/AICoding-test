package com.sh.cursortest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
public class ThreadCreationTests {

    /**
     * 方式一：继承Thread类
     */
    @Test
    void testExtendThread() throws InterruptedException {
        // 创建自定义Thread子类
        class MyThread extends Thread {
            @Override
            public void run() {
                System.out.println("方式一：继承Thread类 - " + Thread.currentThread().getName() + " 执行中");
            }
        }
        
        // 创建并启动线程
        MyThread thread = new MyThread();
        thread.start();
        
        // 等待线程执行完毕
        thread.join();
    }
    
    /**
     * 方式二：实现Runnable接口
     */
    @Test
    void testImplementRunnable() throws InterruptedException {
        // 创建实现Runnable接口的实例
        Runnable task = new Runnable() {
            @Override
            public void run() {
                System.out.println("方式二：实现Runnable接口 - " + Thread.currentThread().getName() + " 执行中");
            }
        };
        
        // 创建并启动线程
        Thread thread = new Thread(task);
        thread.start();
        
        // 等待线程执行完毕
        thread.join();
    }
    
    /**
     * 方式三：实现Callable接口，结合Future使用
     */
    @Test
    void testImplementCallable() throws Exception {
        // 创建实现Callable接口的实例
        Callable<String> task = new Callable<String>() {
            @Override
            public String call() throws Exception {
                System.out.println("方式三：实现Callable接口 - " + Thread.currentThread().getName() + " 执行中");
                return "Callable任务执行结果";
            }
        };
        
        // 使用FutureTask包装Callable
        FutureTask<String> futureTask = new FutureTask<>(task);
        
        // 创建并启动线程
        Thread thread = new Thread(futureTask);
        thread.start();
        
        // 获取任务结果（阻塞直到任务完成）
        String result = futureTask.get();
        System.out.println("Callable返回结果: " + result);
    }
    
    /**
     * 方式四：使用线程池
     */
    @Test
    void testThreadPool() throws InterruptedException {
        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        // 提交任务
        executor.submit(() -> {
            System.out.println("方式四：使用线程池 - " + Thread.currentThread().getName() + " 执行中");
        });
        
        // 等待所有任务完成
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }
    
    /**
     * 方式五：使用Lambda表达式（Java 8+）
     */
    @Test
    void testLambdaExpression() throws InterruptedException {
        // 使用Lambda创建并启动线程
        Thread thread = new Thread(() -> {
            System.out.println("方式五：使用Lambda表达式 - " + Thread.currentThread().getName() + " 执行中");
        });
        thread.start();
        
        // 等待线程执行完毕
        thread.join();
    }
    
    /**
     * 方式六：使用CompletableFuture（Java 8+）
     */
    @Test
    void testCompletableFuture() throws InterruptedException, ExecutionException {
        // 使用CompletableFuture异步执行任务
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("方式六：使用CompletableFuture - " + Thread.currentThread().getName() + " 执行中");
            return "CompletableFuture执行结果";
        });
        
        // 获取结果
        String result = future.get();
        System.out.println("CompletableFuture返回结果: " + result);
    }
    
    /**
     * 额外示例：自定义线程工厂
     */
    @Test
    void testCustomThreadFactory() {
        // 自定义线程工厂
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger(0);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("自定义线程-" + count.incrementAndGet());
                return thread;
            }
        };
        
        // 使用自定义线程工厂创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(1, threadFactory);
        
        // 提交任务
        executor.submit(() -> {
            System.out.println("使用自定义线程工厂 - " + Thread.currentThread().getName() + " 执行中");
        });
        
        // 关闭线程池
        executor.shutdown();
    }
} 