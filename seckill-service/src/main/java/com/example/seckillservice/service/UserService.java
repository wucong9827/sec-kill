package com.example.seckillservice.service;

/**
 * @author : wucong
 * @Date : 2020/10/2 10:44
 * @Description :
 */
public interface UserService {
    /**
     * 获取用户验证hash
     * @param sid
     * @param userId
     * @return
     * @throws Exception
     */
    String getVerifyHash(Integer sid, Integer userId) throws Exception;
}
