package com.webank.weevent.governance.mapper;

import java.util.List;

import com.webank.weevent.governance.entity.AccountEntity;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountMapper {

    List<AccountEntity> accountList(AccountEntity accountEntity);

    int countAccount(AccountEntity accountEntity);

}
