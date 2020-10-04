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

    /**
     * 添加用户访问次数
     * @param userId
     * @return
     * @throws Exception
     */
    public int addUserCount(Integer userId);

    /**
     * 检查用户是否被禁
     * @param userId
     * @return
     */
    public boolean getUserIsBanned(Integer userId);

}
