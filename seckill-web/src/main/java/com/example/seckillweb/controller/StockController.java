package com.example.seckillweb.controller;

import com.example.seckillservice.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : wucong
 * @Date : 2020/10/3 22:10
 * @Description :
 */
@RestController
public class StockController {
    private final static Logger LOGGER = LoggerFactory.getLogger(StockController.class);

    @Autowired
    private StockService stockService;

    /**
     * 查询库存：通过数据库查询库存
     * @param sid
     * @return
     */
    @RequestMapping(value = "/getStockByDB/{sid}")
    public String getStockByDB(@PathVariable Integer sid) {
        int count = stockService.getStockCountByDB(sid);
        LOGGER.info("商品Id: [{}] 剩余库存为: [{}]", sid, count);
        return String.format("商品Id: %d 剩余库存为：%d", sid, count);
    }

    /**
     * 查询库存：通过缓存查询库存
     * 缓存命中：返回库存
     * 缓存未命中：查询数据库写入缓存并返回
     * @param sid
     * @return
     */
    @RequestMapping(value = "/getStockByCache/{sid}")
    public String getStockByCache(@PathVariable Integer sid) {
        int count = stockService.getStockCount(sid);
        LOGGER.info("商品Id: [{}] 剩余库存为: [{}]", sid, count);
        return String.format("商品Id: %d 剩余库存为：%d", sid, count);
    }
}
