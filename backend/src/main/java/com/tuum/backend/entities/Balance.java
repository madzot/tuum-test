package com.tuum.backend.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Balance {

    private Long accountId;
    private String currency;
    private BigDecimal amount;
}
