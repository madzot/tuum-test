package com.tuum.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class AccountDto {
    private Long accountId;
    private Integer customerId;
    private List<BalanceDto> balances;
}

