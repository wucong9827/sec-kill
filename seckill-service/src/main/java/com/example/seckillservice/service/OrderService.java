package com.example.seckillservice.service;

import com.example.seckilldao.dao.Stock;

/**
 * @author : wucong
 * @Date : 2020/9/22 23:11
 * @Description :
 */
public interface OrderService {
    /**
     * 创建错误订单
     * @param sid 库存ID
     * @return 订单ID
     * @throws Exception
     */
    public int createWrongOrder(int sid) throws Exception;

    /**
     * 乐观锁创建订单
     * @param sid
     * @return
     * @throws Exception
     */
    int createOptimisticOrder(int sid) throws Exception;

    /**
     * 创建正确订单：下单悲观锁 for update
     * @param sid
     * @return
     * @throws Exception
     */
     int createPessimisticOrder(int sid);


}
