package com.tuum.backend.dtos;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
public class CompletedTransactionDto {
    private final Long accountId;
    private final Long transactionId;
    private final BigDecimal amount;
    private final String currency;
    private final String direction;
    private final String description;
    private final BigDecimal newBalance;
}
