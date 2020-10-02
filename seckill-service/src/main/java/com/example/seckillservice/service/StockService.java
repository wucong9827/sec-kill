package com.example.seckillservice.service;

import com.example.seckilldao.dao.Stock;

/**
 * @author : wucong
 * @Date : 2020/9/23 00:45
 * @Description :
 */
public interface StockService {

    /**
     * 根据库存 ID 查询数据库库存信息
     * @param id
     * @return
     */
    Stock getStockById(int id);
    /**
     * 根据库存 ID 查询数据库库存信息（悲观锁）
     * @param sid
     * @return
     */
    Stock getStockByIdForUpdate(int sid);

    /**
     * 更新数据库库存信息
     * @param stock
     * @return
     */
    int updateStockById(Stock stock);

    /**
     * Optimistic update sale
     * @param stock
     * @return
     */
    int updateStockByOptimistic(Stock stock);


}
