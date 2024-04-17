package com.tuum.backend.controllers;

import com.tuum.backend.dtos.AccountDto;
import com.tuum.backend.dtos.CreateAccountDto;
import com.tuum.backend.exceptions.AccountNotFoundException;
import com.tuum.backend.exceptions.InvalidCurrencyException;
import com.tuum.backend.services.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tuum/api")
public class AccountController {
    private final AccountService accountService;
    @PostMapping("/account")
    public AccountDto createAccount(@Valid @RequestBody CreateAccountDto dto) throws InvalidCurrencyException {
        return accountService.createAccount(dto);
    }

    @GetMapping("/account")
    public AccountDto getAccount(@RequestParam Long id) throws AccountNotFoundException {
        return accountService.getAccount(id);
    }
}
