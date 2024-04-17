package com.tuum.backend.repositories;

import com.tuum.backend.entities.Transaction;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface TransactionRepository {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO transaction (account_id, currency, amount, direction, description) VALUES (${accountId}, '${currency}', ${amount}, '${direction}', '${description}')")
    @Result(column = "id")
    long createTransaction(Transaction transaction);

    @Select("SELECT * FROM transaction WHERE account_id = ${id}")
    List<Transaction> getTransactionsByAccountId(@Param("id") Long id);
}
