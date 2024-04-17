package com.tuum.backend.dtos;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class CreateAccountDto {
    private final Integer customerId;
    private final String country;
    private final List<String> currencies;
}
