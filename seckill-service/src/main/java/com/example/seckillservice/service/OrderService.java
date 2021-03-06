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

    /**
     * 创建正确订单：验证库存 + 用户 + 时间 合法性 + 下单乐观锁
     * @param sid
     * @param userId
     * @param verifyHash
     * @return
     * @throws Exception
     */
    int createVerifiedOrder(int sid, int userId, String verifyHash) throws Exception;

    /**
     * 检查缓存中用户是否已经有订单
     * @param sid
     * @param userId
     * @return
     * @throws Exception
     */
    Boolean checkUserOrderInfoInCache(Integer sid, Integer userId) throws Exception;

    /**
     * 创建正确订单：验证库存 + 下单乐观锁 + 更新订单信息到缓存
     * @param sid
     * @param userId
     * @throws Exception
     */
    void createOrderByMq(Integer sid, Integer userId) throws Exception;


}
