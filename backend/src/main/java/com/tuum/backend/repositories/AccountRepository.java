package com.tuum.backend.repositories;

import com.tuum.backend.entities.Account;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface AccountRepository {
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO account (customer_id, country) VALUES (${customerId}, '${country}');")
    @Result(column = "id")
    long createAccount(Account account);

    @Select("SELECT * FROM account WHERE id=${id}")
    Account getAccountById(@Param("id") Long id);
}
