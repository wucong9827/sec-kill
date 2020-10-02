package com.example.seckilldao.mapper;

import com.example.seckilldao.dao.StockOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author : wucong
 * @Date : 2020/9/23 00:50
 * @Description :
 */
@Mapper
public interface OrderMapper {

    int insertSelective(StockOrder stockOrder);


}
