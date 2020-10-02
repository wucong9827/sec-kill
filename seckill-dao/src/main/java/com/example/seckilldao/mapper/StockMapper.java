package com.example.seckilldao.mapper;

import com.example.seckilldao.dao.Stock;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author : wucong
 * @Date : 2020/9/22 23:56
 * @Description :
 */
@Mapper
public interface StockMapper {

    Stock selectByPrimaryKey(Integer id);

    Stock selectByPrimaryKeyForUpdate(Integer id);

    int updateByPrimaryKeySelective(Stock stock);

    int updateByOptimistic(Stock stock);
}
