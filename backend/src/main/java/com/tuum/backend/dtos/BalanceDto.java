package com.tuum.backend.dtos;

import java.math.BigDecimal;

public record BalanceDto(String currency, BigDecimal amount) {
}
