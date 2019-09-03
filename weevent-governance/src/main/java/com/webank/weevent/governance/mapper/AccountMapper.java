package com.webank.weevent.governance.mapper;

import java.util.List;

import com.webank.weevent.governance.entity.AccountEntity;
import com.webank.weevent.governance.entity.AccountExample;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AccountMapper {

    int countByExample(AccountExample example);

    int deleteByExample(AccountExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AccountEntity record);

    int insertSelective(AccountEntity record);

    List<AccountEntity> selectByExample(AccountExample example);

    AccountEntity selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") AccountEntity record, @Param("example") AccountExample example);

    int updateByExample(@Param("record") AccountEntity record, @Param("example") AccountExample example);

    int updateByPrimaryKeySelective(AccountEntity record);

    int updateByPrimaryKey(AccountEntity record);
}
