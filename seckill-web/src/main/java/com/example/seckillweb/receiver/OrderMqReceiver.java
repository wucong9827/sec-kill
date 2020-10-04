package com.example.seckillweb.receiver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.seckillservice.service.OrderService;
import com.example.seckillservice.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author : wucong
 * @Date : 2020/10/4 21:43
 * @Description : 订单消息队列消费者
 */
@Component
@RabbitListener(queues = "orderQueue")
public class OrderMqReceiver {
    private final static Logger LOGGER = LoggerFactory.getLogger(OrderMqReceiver.class);

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void process(String message) {
        LOGGER.info("OrderMqReceiver收到消息开始用户下单流程: " + message);
        JSONObject object = JSONObject.parseObject(message);
        try {
            orderService.createOrderByMq(object.getInteger("sid"), object.getInteger("userId"));
        }catch(RuntimeException e) {
            LOGGER.error("异常消息：[{}]", e.getMessage());
        } catch(Exception e) {
            LOGGER.error("消息处理异常：[{}]", e.getMessage());
        }
    }
}
