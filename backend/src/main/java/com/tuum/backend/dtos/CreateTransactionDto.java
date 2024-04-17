package com.tuum.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CreateTransactionDto {
    private Long accountId;
    private BigDecimal amount;
    private String currency;
    private String direction;
    private String description;
}
