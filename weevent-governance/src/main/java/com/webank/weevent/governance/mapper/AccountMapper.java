package com.webank.weevent.governance.mapper;

import java.util.List;

import com.webank.weevent.governance.entity.AccountEntity;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountMapper {


    int insertAccount(AccountEntity accountEntity);

    List<AccountEntity> accountList(AccountEntity accountEntity);

    int updateAccount(AccountEntity accountEntity);

    int deleteAccount(Integer id);

    int AccountEntity(AccountEntity accountEntity);
}
