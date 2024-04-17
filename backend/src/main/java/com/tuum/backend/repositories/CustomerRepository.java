package com.tuum.backend.repositories;

import com.tuum.backend.entities.Customer;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface CustomerRepository {

    String SELECT_FROM_CUSTOMER = "SELECT * FROM customer";

    @Select(SELECT_FROM_CUSTOMER)
    List<Customer> findAll();
    @Select("SELECT * FROM customer WHERE id = ${id}")
    Customer getCustomerById(@Param("id") Integer id);
}
