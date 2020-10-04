package com.example.seckillservice.service.impl;

import com.example.seckilldao.dao.Stock;
import com.example.seckilldao.dao.User;
import com.example.seckilldao.mapper.UserMapper;
import com.example.seckilldao.util.CacheKey;
import com.example.seckillservice.service.StockService;
import com.example.seckillservice.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

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
    private StringRedisTemplate stringRedisTemplate;

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
        String hashKey = CacheKey.HASH_KEY.getKey() + "_" + sid + "_" + userId;
        stringRedisTemplate.opsForValue().set(hashKey, verifyHash, 3600, TimeUnit.SECONDS);
        LOGGER.info("Redis 写入： [{}] [{}]", hashKey, verifyHash);
        return verifyHash;
    }

    @Override
    public int addUserCount(Integer userId) {
        String limitKey = CacheKey.LIMIT_KEY.getKey() + "_" + userId;
        String limitNum = stringRedisTemplate.opsForValue().get(limitKey);
        int limit = -1;
        if (limitNum == null) {
            stringRedisTemplate.opsForValue().set(limitKey, "0", 3600, TimeUnit.SECONDS);
        } else {
            limit = Integer.parseInt(limitNum) + 1;
            stringRedisTemplate.opsForValue().set(limitKey, String.valueOf(limit), 3600, TimeUnit.SECONDS);
        }
        return limit;
    }

    @Override
    public boolean getUserIsBanned(Integer userId) {
        String limitKey = CacheKey.LIMIT_KEY.getKey() + "_" + userId;
        String limitNum = stringRedisTemplate.opsForValue().get(limitKey);
        if (limitNum == null) {
            LOGGER.info("该用户没有访问申请验证值记录，疑似异常操作");
            return true;
        }
        return Integer.parseInt(limitNum) > ALLOW_COUNT;
    }
}
