package com.webank.weevent.governance.repository;

import java.util.List;

import com.webank.weevent.governance.entity.AccountEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    List<AccountEntity> findAllByUsernameAndDeleteAt(String username,String deleteAt);

}
