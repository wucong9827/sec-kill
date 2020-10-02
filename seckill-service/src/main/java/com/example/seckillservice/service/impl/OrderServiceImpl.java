package com.example.seckillservice.service.impl;

import com.example.seckilldao.dao.Stock;
import com.example.seckilldao.dao.StockOrder;
import com.example.seckilldao.mapper.OrderMapper;
import com.example.seckilldao.mapper.StockMapper;
import com.example.seckillservice.service.OrderService;
import com.example.seckillservice.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author : wucong
 * @Date : 2020/9/22 23:11
 * @Description :
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private StockService stockService;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 错误的秒杀功能
     * @param sid 库存ID
     * @return
     * @throws Exception
     */
    @Override
    public int createWrongOrder(int sid) throws Exception{
        //校验库存
        Stock stock = checkStock(sid);
        //扣库存
        saleStock(stock);
        //创建订单
        return createOrder(stock);
    }

    @Override
    public int createOptimisticOrder(int sid) throws Exception {
        //校验库存
        Stock stock = checkStock(sid);
        //乐观锁更新库存
        saleStockOptimistic(stock);
        //创建订单
        createOrder(stock);
        return stock.getCount() - (stock.getSale() + 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public int createPessimisticOrder(int sid) {
        //悲观锁的方式检验库存
        Stock stock = checkStockForUpdate(sid);
        //更新库存
        saleStock(stock);
        //创建订单
        createOrder(stock);
        return stock.getCount() - (stock.getSale());
    }


    private Stock checkStockForUpdate(int sid) {
        Stock stock = stockService.getStockByIdForUpdate(sid);
        if (stock.getSale().equals(stock.getCount())) {
            throw new RuntimeException("库存不足");
        }
        return stock;
    }

    /**
     * 检查库存
     * @param sid
     * @return
     */
    private Stock checkStock(int sid) {
        Stock stock = stockService.getStockById(sid);
        if (stock.getCount().equals(stock.getSale())) {
            throw new RuntimeException("库存不足");
        }
        return stock;
    }

    /**
     * 库存更新
     * @param stock
     * @return
     */
    private int saleStock(Stock stock) {
        LOGGER.info("库存更新");
        stock.setSale(stock.getSale() + 1);
        return stockService.updateStockById(stock);
    }

    /**
     * 乐观锁库存更新
     * @param stock
     */
    private void saleStockOptimistic(Stock stock) {
        LOGGER.info("乐观锁库存更新");
        int count = stockService.updateStockByOptimistic(stock);
        if (count != 1) {
            throw new RuntimeException("库存更新失败");
        }
    }

    /**
     * 创建订单
     * @param stock
     * @return
     */
    private int createOrder(Stock stock) {
        StockOrder stockOrder = new StockOrder();
        stockOrder.setSid(stock.getId());
        stockOrder.setName(stock.getName());
        orderMapper.insertSelective(stockOrder);
        return stockOrder.getId();
    }
}
