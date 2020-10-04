package com.example.seckillweb.receiver;

import com.example.seckillservice.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author : wucong
 * @Date : 2020/10/4 15:09
 * @Description : 消息队列处理
 */
@Component
@RabbitListener(queues = {"delCache"})
public class DelCacheReceiver {
    private final static Logger LOGGER = LoggerFactory.getLogger(DelCacheReceiver.class);

    @Autowired
    private StockService stockService;

    /**
     * 消费者处理
     * @param message
     */
    @RabbitHandler
    public void processMq(String message) {
        LOGGER.info("DelCacheReceiver收到消息: " + message);
        LOGGER.info("DelCacheReceiver开始删除缓存: " + message);
        stockService.delStockCountCache(Integer.parseInt(message));
    }
}
