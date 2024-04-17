package com.tuum.backend.controllers;

import com.tuum.backend.dtos.CompletedTransactionDto;
import com.tuum.backend.dtos.CreateTransactionDto;
import com.tuum.backend.dtos.TransactionDto;
import com.tuum.backend.errors.ApplicationException;
import com.tuum.backend.services.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tuum/api")
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/transaction")
    public CompletedTransactionDto createTransaction(@Valid @RequestBody CreateTransactionDto dto) throws ApplicationException {
        return transactionService.createTransaction(dto);
    }

    @GetMapping("/transaction")
    public ResponseEntity<List<TransactionDto>> getTransaction(Long id) {
        return transactionService.getTransactions(id);
    }
}
