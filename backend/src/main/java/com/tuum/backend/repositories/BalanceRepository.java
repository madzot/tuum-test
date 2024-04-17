package com.tuum.backend.repositories;

import com.tuum.backend.entities.Balance;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Mapper
@Repository
public interface BalanceRepository {
    @Insert("INSERT INTO balance (account_id, currency) VALUES (${accountId}, '${currency}')")
    void createBalance(Balance balance);

    @Select("SELECT * FROM balance AS B WHERE B.account_id=${id}")
    List<Balance> getBalancesById(@Param("id") Long id);

    @Select("SELECT * FROM balance AS B WHERE B.account_id=${id} AND B.currency='${ccy}'")
    Balance getBalanceByIdAndCcy(@Param("id") Long id, @Param("ccy") String currency);

    @Update("UPDATE balance SET amount = amount + ${amount} WHERE account_id=${id} AND currency='${ccy}'")
    void addToBalance(@Param("amount") BigDecimal amount, @Param("id") Long id, @Param("ccy") String ccy);
}
