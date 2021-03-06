package com.example.seckillweb.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.seckillservice.service.OrderService;
import com.example.seckillservice.service.StockService;
import com.example.seckillservice.service.UserService;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author : wucong
 * @Date : 2020/9/22 23:08
 * @Description :
 */
@RestController
public class OrderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;
    @Autowired
    private StockService stockService;

    /**
     * 每秒放行10个请求
     */
    private final RateLimiter rateLimiter = RateLimiter.create(10);

    @Autowired
    private AmqpTemplate rabbitTemplate;

    /**
     * 开设一个线程池，在线程中删除key，
     * 而不是使用Thread.sleep进行等待，这样会阻塞用户的请求。
     * 延迟双删除线程池
     */
    private static ExecutorService cachedThreadPool = new ThreadPoolExecutor(
            0,
            Integer.MAX_VALUE,
            60L,
            TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());

    /**
     * 延时时间：预估读数据库数据业务逻辑的耗时，用来做缓存再删除
     */
    private static final int DELAY_MILLSECONDS = 1000;

    /**
     * 秒杀0.1版本，无锁操作，错误版本
     * @param sid
     * @return
     */
    @RequestMapping(value = "/createWrongOrder/{sid}")
    public String createWrongOrder(@PathVariable int sid) {
        int id = 0;
        try {
            id = orderService.createWrongOrder(sid);
            LOGGER.info("创建订单id: [{}]", id);
        } catch (Exception e) {
            LOGGER.info("Exception", e);
        }
        return String.valueOf(id);
    }

    /**
     * 0.11版本，使用乐观锁处理卖出问题
     * 加令牌桶算法
     * @param sid
     * @return
     */
    @RequestMapping(value = "/createOptimisticOrder/{sid}")
    public String createOptimisticOrder(@PathVariable int sid) {
        //1.阻塞式
            LOGGER.info("等待时间："+rateLimiter.acquire());
//       2. 非阻塞式令牌算法
//        if (!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)){
//            LOGGER.warn("被限制流量，请求失败");
//            return "未秒杀到商品";
//        }
        int remain = 0;
        try {
            remain = orderService.createOptimisticOrder(sid);
            LOGGER.info("购买成功,库存剩余：{}", remain);
        } catch (Exception e) {
            LOGGER.error("购买失败：{}", e.getMessage());
            return "购买失败，库存不足";
        }
        return String.format("购买成功，剩余库存为：%d", remain);
    }

    /**
     * V0.12
     * 下单接口：悲观锁更新库存 事务for update更新库存
     * @param sid
     * @return
     */
    @RequestMapping("/createPessimisticOrder/{sid}")
    public String createPessimisticOrder(@PathVariable int sid) {
        int remain = 0;
        try {
            remain = orderService.createPessimisticOrder(sid);
            LOGGER.info("购买成功,库存剩余：[{}]", remain);
        } catch (Exception e) {
            LOGGER.error("购买失败：[{}]", e.getMessage());
            return "购买失败，库存不足";
        }
        return String.format("购买成功，剩余库存为：%d", remain);
    }

    /**
     * V0.2
     * 验证接口：下单前用户获取验证值
     * @return
     */
    @RequestMapping(value = "/getVerifyHash", method = {RequestMethod.GET})
    public String getVerifyHash(@RequestParam(value = "sid") Integer sid,
                                @RequestParam(value = "userId") Integer userId) {
        String hash;
        try {
            hash = userService.getVerifyHash(sid, userId);
        } catch (Exception e) {
            LOGGER.error("获取验证hash失败,原因：[{}]", e.getMessage());
            return "获取验证hash失败";
        }
        return String.format("请求抢购验证hash值为： %s", hash);
    }

    /**
     * V0.2
     * 下单接口：携带验证值进行下单请求
     * @param sid
     * @param userId
     * @param verifyHash
     * @return
     */
    @RequestMapping(value = "/createOrderWithVerifiedUrl", method = RequestMethod.GET)
    public String createOrderWithVerifiedUrl(@RequestParam(value = "sid") Integer sid,
                                             @RequestParam(value = "userId") Integer userId,
                                             @RequestParam(value = "verifyHash") String verifyHash) {
        int stockLeft;
        try {
            stockLeft = orderService.createVerifiedOrder(sid, userId, verifyHash);
            LOGGER.info("购买成功，剩余库存为: [{}]", stockLeft);
        } catch (Exception e) {
            LOGGER.error("购买失败： [{}]", e.getMessage());
            return e.getMessage();
        }
        return String.format("购买成功，剩余库存为：%d", stockLeft);
    }

    /**
     * V 0.21
     * 要求验证的抢购接口 + 单用户限制访问频率
     * @param sid
     * @param userId
     * @param verifyHash
     * @return
     */
    @RequestMapping(value = "/createOrderWithVerifiedUrlAndLimit", method = {RequestMethod.GET})
    private String createOrderWithVerifiedUrlAndLimit(@RequestParam(value = "sid") Integer sid,
                                                      @RequestParam(value = "userId") Integer userId,
                                                      @RequestParam(value = "verifyHash") String verifyHash) {
            int stockLeft = 0;
        try {
            int count = userService.addUserCount(userId);
            LOGGER.info("用户截至该次的访问次数为: [{}]", count);
            boolean isBanned = userService.getUserIsBanned(userId);
            if (isBanned) {
                return "购买失败，超过频率限制";
            }
            stockLeft = orderService.createVerifiedOrder(sid, userId, verifyHash);
            LOGGER.info("购买成功，剩余库存为: [{}]", stockLeft);
        } catch (Exception e) {
            LOGGER.error("购买失败： [{}]", e.getMessage());
            return e.getMessage();
        }
        return String.format("购买成功，剩余库存：[%d]", stockLeft);
    }

    /**
     * V 0.31
     * 下单接口：先删除缓存，再更新数据库
     * @param sid
     * @return
     */
    @RequestMapping(value = "/createOrderWithCacheV1/{sid}")
    public String createOrderWithCacheV1(@PathVariable int sid) {
        int count = 0;
        try {
            // 先删除缓存
            stockService.delStockCountCache(sid);
            // 更新库存，下单操作
            count = orderService.createPessimisticOrder(sid);
        } catch (Exception e) {
            LOGGER.error("购买失败：[{}]", e.getMessage());
            return "购买失败，库存不足";
        }
        LOGGER.info("购买成功，剩余库存为: [{}]", count);
        return String.format("购买成功，剩余库存为：%d", count);
    }

    /**
     * V 0.32
     * 下单接口：先更新数据库，再删缓存
     * @param sid
     * @return
     */
    @RequestMapping("/createOrderWithCacheV2/{sid}")
    public String createOrderWithCacheV2(@PathVariable int sid) {
        int count = 0;
        try {
            // 更新库存，下单操作
            count = orderService.createPessimisticOrder(sid);
            // 先删除缓存
            stockService.delStockCountCache(sid);
        } catch (Exception e) {
            LOGGER.error("购买失败：[{}]", e.getMessage());
            return "购买失败，库存不足";
        }
        LOGGER.info("购买成功，剩余库存为: [{}]", count);
        return String.format("购买成功，剩余库存为：%d", count);
    }

    /**
     * V 0.33
     * 下单接口: 先删除缓存，再更新数据库，缓存延时双删
     * @param sid
     * @return
     */
    @RequestMapping(value = "/createOrderWithCacheV3/{sid}")
    public String createOrderWithCacheV3(@PathVariable int sid) {
        int count = 0;
        try {
            // 删除缓存
            stockService.delStockCountCache(sid);
            // 更新库存，生成订单
            count = orderService.createPessimisticOrder(sid);
            LOGGER.info("完成下单事务");
            // 延迟指定时间之后进行再次删除缓存
            cachedThreadPool.execute(new DelCacheByThread(sid));
        } catch (Exception e) {
            LOGGER.error("购买失败：[{}]", e.getMessage());
            return "购买失败，库存不足";
        }
        LOGGER.info("购买成功，剩余库存为: [{}]", count);
        return String.format("购买成功，剩余库存为：%d", count);
    }

    /**
     * V 0.34
     * 下单接口：先更新数据库，再删缓存，删除缓存失败重试，通知消息队列
     * @param sid
     * @return
     */
    @RequestMapping("/createOrderWithCacheV4/{sid}")
    public String createOrderWithCacheV4(@PathVariable int sid) {
        int count = 0;
        try {
            // 完成下单业务
            count = orderService.createPessimisticOrder(sid);
            LOGGER.info("成功下单");
            // 删除库存缓存数据
            stockService.delStockCountCache(sid);
            // 延迟双删除 缓存，目的：达到最终的缓存一致
            cachedThreadPool.execute(new DelCacheByThread(sid));
            LOGGER.info("假设延迟双删失败：");
            // 假设上述再次删除缓存没成功，通知消息队列进行删除缓存
            sendToDelCache(String.valueOf(sid));
        } catch (Exception e) {
            LOGGER.error("购买失败：[{}]", e.getMessage());
            return "购买失败，库存不足";
        }
        LOGGER.info("购买成功，剩余库存为: [{}]", count);
        return String.format("购买成功，剩余库存为：%d", count);
    }

    /**
     * V 0.4
     * 下单接口：异步处理订单
     * @param sid
     * @param userId
     * @return
     */
    @RequestMapping(value = "/createUserOrderWithMq", method = {RequestMethod.GET})
    public String createUserOrderWithMq(@RequestParam(value = "sid") Integer sid,
                                        @RequestParam(value = "userId") Integer userId) {
        try {
            // 检查缓存中该用户是否已经下单过
//            Boolean hasOrder = orderService.checkUserOrderInfoInCache(sid, userId);
//            if (hasOrder != null && hasOrder) {
//                LOGGER.info("该用户已经抢购过");
//                return "你已经抢购过了，不要太贪心.....";
//            }
            // 没有下单的用户，检查缓存中是否还有库存
            LOGGER.info("没有抢购过，检查缓存中商品是否还有库存");
            int count = stockService.getStockCount(sid);
            if (count == 0) {
                return "秒杀请求失败，库存不足.....";
            }
            // 有库存，则将用户id和商品id封装为消息体传给消息队列处理
            // 注意这里的有库存和已经下单都是缓存中的结论，存在不可靠性，在消息队列中会查表再次验证
            LOGGER.info("库存剩余数量：[{}]", count);
            JSONObject object = new JSONObject();
            object.put("sid", sid);
            object.put("userId", userId);
            sendToOrderQueue(object.toJSONString());
            return "秒杀请求提交成功";
        } catch (Exception e) {
            LOGGER.error("下单接口：异步处理订单异常：[{}]", e.getMessage());
            return "秒杀请求失败，服务器正忙.....";
        }
    }

    /**
     * 检查缓存中用户是否已经生成订单
     * @param sid
     * @return
     */
    @RequestMapping(value = "/checkOrderByUserIdInCache", method = {RequestMethod.GET})
    public String checkOrderByUserIdInCache(@RequestParam(value = "sid") Integer sid,
                                            @RequestParam(value = "userId") Integer userId) {
        // 检查缓存中该用户是否已经下单过
        try {
            Boolean hasOrder = orderService.checkUserOrderInfoInCache(sid, userId);
            if (hasOrder != null && hasOrder) {
                return "恭喜您，已经抢购成功！";
            }
        } catch (Exception e) {
            LOGGER.error("检查订单异常：[{}]", e.getMessage());
        }
        return "很抱歉，你的订单尚未生成，继续排队。";
    }

    /**
     * 向消息队列delCache发送消息
     * @param message
     */
    private void sendToDelCache(String message){
        LOGGER.info("这就去通知消息队列开始重试删除缓存：[{}]", message);
        rabbitTemplate.convertAndSend("delCache", message);
    }

    /**
     * 向OrderQueue消息队列中发送消息
     * @param message
     */
    private void sendToOrderQueue(String message) {
        LOGGER.info("去通知消息队列开始下单业务：[{}]", message);
        rabbitTemplate.convertAndSend("orderQueue", message);
    }


    /**
     * 延迟双删 线程
     */
    private class DelCacheByThread implements Runnable {
        private int sid;

        public DelCacheByThread(int sid) {
            this.sid = sid;
        }

        @Override
        public void run() {
            try {
                LOGGER.info("异步执行缓存再删除，商品id：[{}]， 首先休眠：[{}] 毫秒", sid, DELAY_MILLSECONDS);
                Thread.sleep(DELAY_MILLSECONDS);
                stockService.delStockCountCache(sid);
                LOGGER.info("再次删除商品id：[{}] 缓存", sid);
            } catch (InterruptedException e) {
                LOGGER.error("延迟双删线程执行失败,{[]}", e.getMessage());
            }
        }
    }
}
