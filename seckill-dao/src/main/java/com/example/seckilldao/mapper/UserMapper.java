package com.example.seckilldao.mapper;

import com.example.seckilldao.dao.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author : wucong
 * @Date : 2020/10/2 11:09
 * @Description :
 */
@Mapper
public interface UserMapper {

    User selectByPrimaryKey(Integer id);
}
