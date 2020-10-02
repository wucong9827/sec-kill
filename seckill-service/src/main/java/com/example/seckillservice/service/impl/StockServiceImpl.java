package com.example.seckillservice.service.impl;

import com.example.seckilldao.dao.Stock;
import com.example.seckilldao.mapper.StockMapper;
import com.example.seckillservice.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author : wucong
 * @Date : 2020/9/23 00:46
 * @Description :
 */
@Service
public class StockServiceImpl implements StockService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockServiceImpl.class);

    @Autowired
    private StockMapper stockMapper;

    @Override
    public Stock getStockById(int id) {
        return stockMapper.selectByPrimaryKey(id);
    }

    @Override
    public Stock getStockByIdForUpdate(int sid) {
        return stockMapper.selectByPrimaryKeyForUpdate(sid);
    }

    @Override
    public int updateStockById(Stock stock) {
        return stockMapper.updateByPrimaryKeySelective(stock);
    }

    @Override
    public int updateStockByOptimistic(Stock stock) {
        return stockMapper.updateByOptimistic(stock);
    }
}
