package com.sh.cursortest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
public class CompletableFutureTests {

    /**
     * 1. 创建CompletableFuture的基本方法
     */
    @Test
    void testCreateCompletableFuture() throws ExecutionException, InterruptedException {
        System.out.println("=== 创建CompletableFuture的基本方法 ===");
        
        // 方式1：使用supplyAsync创建异步任务
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("supplyAsync执行线程：" + Thread.currentThread().getName());
            return "Hello CompletableFuture";
        });
        System.out.println("supplyAsync结果：" + future1.get());
        
        // 方式2：使用runAsync创建无返回值的异步任务
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            System.out.println("runAsync执行线程：" + Thread.currentThread().getName());
            System.out.println("执行无返回值任务");
        });
        future2.get(); // 等待完成
        
        // 方式3：创建已完成的CompletableFuture
        CompletableFuture<String> completedFuture = CompletableFuture.completedFuture("已完成的任务");
        System.out.println("completedFuture结果：" + completedFuture.get());
    }

    /**
     * 2. 链式调用 - thenApply, thenAccept, thenRun
     */
    @Test
    void testChaining() throws ExecutionException, InterruptedException {
        System.out.println("\n=== 链式调用演示 ===");
        
        // thenApply：转换结果
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("初始任务执行线程：" + Thread.currentThread().getName());
            return "Hello";
        }).thenApply(result -> {
            System.out.println("thenApply执行线程：" + Thread.currentThread().getName());
            return result + " World";
        }).thenApply(result -> {
            return result + "!";
        });
        
        System.out.println("thenApply最终结果：" + future.get());
        
        // thenAccept：消费结果，无返回值
        CompletableFuture.supplyAsync(() -> "消费测试")
                .thenAccept(result -> {
                    System.out.println("thenAccept消费结果：" + result);
                }).get();
        
        // thenRun：执行后续操作，不依赖前面的结果
        CompletableFuture.supplyAsync(() -> "后续操作测试")
                .thenRun(() -> {
                    System.out.println("thenRun：执行后续操作");
                }).get();
    }

    /**
     * 3. 异步版本的链式调用 - thenApplyAsync, thenAcceptAsync, thenRunAsync
     */
    @Test
    void testAsyncChaining() throws ExecutionException, InterruptedException {
        System.out.println("\n=== 异步链式调用演示 ===");
        
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("初始任务线程：" + Thread.currentThread().getName());
            return "Async";
        }).thenApplyAsync(result -> {
            System.out.println("thenApplyAsync线程：" + Thread.currentThread().getName());
            return result + " Chain";
        }).thenAcceptAsync(result -> {
            System.out.println("thenAcceptAsync线程：" + Thread.currentThread().getName());
            System.out.println("异步链式结果：" + result);
        });
        
        future.get();
    }

    /**
     * 4. 组合多个CompletableFuture - thenCompose, thenCombine
     */
    @Test
    void testCombining() throws ExecutionException, InterruptedException {
        System.out.println("\n=== 组合多个CompletableFuture ===");
        
        // thenCompose：串行组合，第二个任务依赖第一个任务的结果
        CompletableFuture<String> composeFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("第一个任务");
            return "First";
        }).thenCompose(result -> {
            return CompletableFuture.supplyAsync(() -> {
                System.out.println("第二个任务，依赖第一个任务结果：" + result);
                return result + " Second";
            });
        });
        
        System.out.println("thenCompose结果：" + composeFuture.get());
        
        // thenCombine：并行组合，两个任务并行执行，然后合并结果
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("并行任务1完成");
            return "Task1";
        });
        
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("并行任务2完成");
            return "Task2";
        });
        
        CompletableFuture<String> combinedFuture = future1.thenCombine(future2, (result1, result2) -> {
            System.out.println("合并结果：" + result1 + " + " + result2);
            return result1 + " & " + result2;
        });
        
        System.out.println("thenCombine最终结果：" + combinedFuture.get());
    }

    /**
     * 5. 等待多个任务完成 - allOf, anyOf
     */
    @Test
    void testWaitingForMultipleTasks() throws ExecutionException, InterruptedException {
        System.out.println("\n=== 等待多个任务完成 ===");
        
        // 创建多个异步任务
        CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("任务1完成");
            return "结果1";
        });
        
        CompletableFuture<String> task2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("任务2完成");
            return "结果2";
        });
        
        CompletableFuture<String> task3 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("任务3完成");
            return "结果3";
        });
        
        // allOf：等待所有任务完成
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(task1, task2, task3);
        allTasks.get(); // 等待所有任务完成
        System.out.println("所有任务都已完成");
        
        // 获取所有任务的结果
        List<String> results = Arrays.asList(task1, task2, task3)
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        System.out.println("所有任务结果：" + results);
        
        // anyOf：等待任意一个任务完成
        CompletableFuture<String> quickTask1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "快速任务1";
        });
        
        CompletableFuture<String> quickTask2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "快速任务2";
        });
        
        CompletableFuture<Object> anyTask = CompletableFuture.anyOf(quickTask1, quickTask2);
        System.out.println("最先完成的任务结果：" + anyTask.get());
    }

    /**
     * 6. 异常处理 - handle, exceptionally, whenComplete
     */
    @Test
    void testExceptionHandling() throws ExecutionException, InterruptedException {
        System.out.println("\n=== 异常处理演示 ===");
        
        // exceptionally：处理异常情况
        CompletableFuture<String> futureWithException = CompletableFuture.supplyAsync(() -> {
            if (Math.random() > 0.5) {
                throw new RuntimeException("模拟异常");
            }
            return "正常结果";
        }).exceptionally(throwable -> {
            System.out.println("捕获异常：" + throwable.getMessage());
            return "异常处理后的默认值";
        });
        
        System.out.println("exceptionally结果：" + futureWithException.get());
        
        // handle：同时处理正常结果和异常
        CompletableFuture<String> handledFuture = CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("测试异常");
        }).handle((result, throwable) -> {
            if (throwable != null) {
                System.out.println("handle处理异常：" + throwable.getMessage());
                return "handle处理后的结果";
            }
            return result;
        }).thenApply(result -> (String) result);
        
        System.out.println("handle结果：" + handledFuture.get());
        
        // whenComplete：无论成功还是失败都会执行
        CompletableFuture<String> completeFuture = CompletableFuture.supplyAsync(() -> {
            return "whenComplete测试";
        }).whenComplete((result, throwable) -> {
            if (throwable != null) {
                System.out.println("whenComplete - 发生异常：" + throwable.getMessage());
            } else {
                System.out.println("whenComplete - 正常完成，结果：" + result);
            }
        });
        
        System.out.println("whenComplete结果：" + completeFuture.get());
    }

    /**
     * 7. 自定义线程池
     */
    @Test
    void testCustomExecutor() throws ExecutionException, InterruptedException {
        System.out.println("\n=== 自定义线程池演示 ===");
        
        // 创建自定义线程池
        ExecutorService customExecutor = Executors.newFixedThreadPool(3, r -> {
            Thread thread = new Thread(r);
            thread.setName("Custom-Thread-" + thread.getId());
            return thread;
        });
        
        try {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                System.out.println("使用自定义线程池：" + Thread.currentThread().getName());
                return "自定义线程池结果";
            }, customExecutor);
            
            System.out.println("自定义线程池结果：" + future.get());
        } finally {
            customExecutor.shutdown();
        }
    }

    /**
     * 8. 超时处理
     */
    @Test
    void testTimeout() {
        System.out.println("\n=== 超时处理演示 ===");
        
        try {
            CompletableFuture<String> timeoutFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(3000); // 模拟长时间任务
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "长时间任务完成";
            });
            
            // 设置超时时间
            String result = timeoutFuture.get(2, TimeUnit.SECONDS);
            System.out.println("超时测试结果：" + result);
        } catch (TimeoutException e) {
            System.out.println("任务超时了！");
        } catch (Exception e) {
            System.out.println("其他异常：" + e.getMessage());
        }
    }

    /**
     * 9. 实际应用场景：并行处理多个API调用
     */
    @Test
    void testRealWorldExample() throws ExecutionException, InterruptedException {
        System.out.println("\n=== 实际应用场景：模拟并行API调用 ===");
        
        long startTime = System.currentTimeMillis();
        
        // 模拟三个不同的API调用
        CompletableFuture<String> userInfo = CompletableFuture.supplyAsync(() -> {
            simulateApiCall("用户信息API", 1000);
            return "用户：张三";
        });
        
        CompletableFuture<String> orderInfo = CompletableFuture.supplyAsync(() -> {
            simulateApiCall("订单信息API", 1200);
            return "订单：3个商品";
        });
        
        CompletableFuture<String> paymentInfo = CompletableFuture.supplyAsync(() -> {
            simulateApiCall("支付信息API", 800);
            return "支付：已完成";
        });
        
        // 等待所有API调用完成并合并结果
        CompletableFuture<String> combinedResult = CompletableFuture.allOf(userInfo, orderInfo, paymentInfo)
                .thenApply(v -> {
                    String user = userInfo.join();
                    String order = orderInfo.join();
                    String payment = paymentInfo.join();
                    return String.format("综合信息 - %s, %s, %s", user, order, payment);
                });
        
        System.out.println("最终结果：" + combinedResult.get());
        
        long endTime = System.currentTimeMillis();
        System.out.println("总耗时：" + (endTime - startTime) + "ms (并行执行)");
    }
    
    /**
     * 模拟API调用
     */
    private void simulateApiCall(String apiName, int delayMs) {
        try {
            System.out.println(apiName + " 开始调用 - 线程：" + Thread.currentThread().getName());
            Thread.sleep(delayMs);
            System.out.println(apiName + " 调用完成");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}