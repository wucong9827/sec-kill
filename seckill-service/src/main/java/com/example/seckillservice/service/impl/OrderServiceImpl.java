package com.example.seckillservice.service.impl;

import com.example.seckilldao.dao.Stock;
import com.example.seckilldao.dao.StockOrder;
import com.example.seckilldao.dao.User;
import com.example.seckilldao.mapper.OrderMapper;
import com.example.seckilldao.mapper.StockMapper;
import com.example.seckilldao.mapper.UserMapper;
import com.example.seckilldao.util.CacheKey;
import com.example.seckillservice.service.OrderService;
import com.example.seckillservice.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author : wucong
 * @Date : 2020/9/22 23:11
 * @Description :
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private StockService stockService;

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

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

    @Override
    public int createVerifiedOrder(int sid, int userId, String verifyHash) throws Exception {
        //验证秒杀时间
        LOGGER.info("当前的时间为： [{}]",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));

        //验证hash验证值
        String hashKey = CacheKey.HASH_KEY.getKey() + "_" + sid + "_" + userId;
        LOGGER.info("HashKey : [{}]", hashKey);
        String verifyHashInRedis = stringRedisTemplate.opsForValue().get(hashKey);
        LOGGER.info("verifyHashInRedis: [{}]", verifyHashInRedis);
        if (!verifyHashInRedis.equals(verifyHash)) {
            throw new RuntimeException("hash值 与 redis中不匹配");
        }
        LOGGER.info("success match");

        //验证用户
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        LOGGER.info("用户信息: [{}]", user);
        //验证商品
        Stock stock = stockService.getStockById(sid);
        if (stock == null) {
            throw new RuntimeException("商品不存在");
        }
        LOGGER.info("商品信息: [{}]", stock);

        //扣库存
        saleStockOptimistic(stock);
        LOGGER.info("success Sale");

        //创建订单
        createOrderWithUserInfoInDB(userId, stock);
        LOGGER.info("success create Order");

        return stock.getCount() - (stock.getSale() + 1);
    }

    /**
     * 创建 用户+商品的 订单
     * @param userId
     * @param stock
     * @return
     */
    private int createOrderWithUserInfoInDB(int userId, Stock stock) {
        StockOrder stockOrder = new StockOrder();
        stockOrder.setSid(stock.getId());
        stockOrder.setName(stock.getName());
        stockOrder.setUserId(userId);
        return orderMapper.insertSelective(stockOrder);
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
