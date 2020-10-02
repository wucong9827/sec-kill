package com.example.seckillservice.service.impl;

import com.example.seckilldao.dao.Stock;
import com.example.seckilldao.dao.User;
import com.example.seckilldao.mapper.UserMapper;
import com.example.seckillservice.service.StockService;
import com.example.seckillservice.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;

/**
 * @author : wucong
 * @Date : 2020/10/2 10:44
 * @Description :
 */
@Service
public class UserServiceImpl implements UserService {

    private final static Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    private final static String SALT = "randomString";
    private final static int ALLOW_COUNT = 10;

    @Autowired
    private StockService stockService;
    @Autowired
    private UserMapper userMapper;

    @Override
    public String getVerifyHash(Integer sid, Integer userId) throws Exception {
        //验证秒杀时间是否到达定时
        LOGGER.info("验证秒杀时间是否到达定时,当前时间：[{}]", new Date(System.currentTimeMillis()).toString());

        //验证是否为注册用户
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        LOGGER.info("用户信息：[{}]", user);
        //验证商品合法性
        Stock stock = stockService.getStockById(sid);
        if (stock == null) {
            throw new RuntimeException("商品不存在于库中");
        }
        LOGGER.info("商品信息：[{}]", stock);

        //生成hash
        String verify = SALT + sid + userId;
        String verifyHash = DigestUtils.md5DigestAsHex(verify.getBytes());

        // 将hash和用户商品信息存入redis
        String hashKey =
        return null;
    }
}
