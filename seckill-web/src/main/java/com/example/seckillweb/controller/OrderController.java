package com.example.seckillweb.controller;

import com.example.seckillservice.service.OrderService;
import com.example.seckillservice.service.UserService;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;


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

    /**
     * 每秒放行10个请求
     */
    private final RateLimiter rateLimiter = RateLimiter.create(10);

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

}
