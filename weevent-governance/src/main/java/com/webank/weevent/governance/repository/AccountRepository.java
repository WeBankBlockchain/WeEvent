package com.webank.weevent.governance.repository;

import java.util.List;

import com.webank.weevent.governance.entity.AccountEntity;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    List<AccountEntity> findAllByUsernameAndDeleteAt(String username, Long deleteAt);

    @Modifying
    @Transactional
    @Query(value = "update t_account set delete_at=:deleteAt where username=:username", nativeQuery = true)
    void deleteByUserName(@Param("username") String username, @Param("deleteAt") Long deleteAt);

}
