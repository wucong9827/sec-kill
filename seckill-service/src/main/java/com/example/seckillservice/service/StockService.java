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

    /**
     * 获取剩余库存：查数据库
     * @param id
     * @return
     */
    int getStockCountByDB(int id);

    /**
     * 获取剩余库存: 查缓存
     * @param id
     * @return
     */
    Integer getStockCountByCache(int id);

    /**
     * 查询库存：通过缓存查询库存
     * 缓存命中：返回库存
     * 缓存未命中：查询数据库写入缓存并返回
     * @param id
     * @return
     */
    Integer getStockCount(int id);

    /**
     * 将库存插入缓存
     * @param id
     * @param count
     * @return
     */
    void setStockCountCache(int id, int count);

    /**
     * 删除库存缓存
     * @param id
     */
    void delStockCountCache(int id);


}
