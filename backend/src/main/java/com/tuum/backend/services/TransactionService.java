package com.tuum.backend.services;

import com.tuum.backend.dtos.CompletedTransactionDto;
import com.tuum.backend.dtos.CreateTransactionDto;
import com.tuum.backend.dtos.TransactionDto;
import com.tuum.backend.entities.Account;
import com.tuum.backend.entities.Balance;
import com.tuum.backend.entities.Transaction;
import com.tuum.backend.errors.ApplicationException;
import com.tuum.backend.exceptions.*;
import com.tuum.backend.mappers.TransactionMapper;
import com.tuum.backend.rabbitmq.MessagingConfig;
import com.tuum.backend.repositories.AccountRepository;
import com.tuum.backend.repositories.BalanceRepository;
import com.tuum.backend.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@RequestMapping("/tuum/api")
public class TransactionService {
    private final RabbitTemplate template;
    private final TransactionMapper transactionMapper;
    private final TransactionRepository transactionRepository;
    private final BalanceRepository balanceRepository;
    private final AccountRepository accountRepository;

    public ResponseEntity<List<TransactionDto>> getTransactions(Long id) {
        List<TransactionDto> dtoList = new ArrayList<>();
        List<Transaction> transactions = transactionRepository.getTransactionsByAccountId(id);
        if (transactions.isEmpty()) {
            throw new AccountNotFoundException("No account with id: " + id);
        }
        for (Transaction transaction: transactions) {
            dtoList.add(transactionMapper.transactionToDto(transaction));
        }
        return ResponseEntity.of(Optional.of(dtoList));
    }

    /**
     * Creates transaction and adds it to the database. Updates balance for account who transaction was made for.
     * Publishes the result to RabbitMQ.
     * @param dto CreateTransactionDto
     * @return CompletedTransactionDto with accounts new balance
     * @throws ApplicationException thrown when dto validation fails
     */
    public CompletedTransactionDto createTransaction(CreateTransactionDto dto) throws ApplicationException {
        validateTransaction(dto);
        Transaction transaction = transactionMapper.dtoToTransaction(dto);
        transactionRepository.createTransaction(transaction);
        BigDecimal amount = transaction.getAmount();
        if (dto.getDirection().equals("OUT")) {
            amount = transaction.getAmount().negate();
        }
        balanceRepository.addToBalance(amount, transaction.getAccountId(), transaction.getCurrency());
        Balance newBalance = balanceRepository.getBalanceByIdAndCcy(dto.getAccountId(), dto.getCurrency());
        CompletedTransactionDto result = new CompletedTransactionDto(dto.getAccountId(), transaction.getId(), dto.getAmount(), dto.getCurrency(), transaction.getDirection(), transaction.getDescription(), newBalance.getAmount());
        template.convertAndSend(MessagingConfig.EXCHANGE_NAME, MessagingConfig.ROUTING_KEY, result);
        return result;
    }

    private void validateTransaction(CreateTransactionDto dto) throws ApplicationException {
        if (dto.getAccountId() == null) throw new ApplicationException("ID cannot be null");
        validateAccount(dto.getAccountId());

        if (dto.getDescription() == null) throw new ApplicationException("Description cannot be null");
        String description = dto.getDescription();
        validateDescription(description);

        if (dto.getAmount() == null) throw new ApplicationException("Amount cannot be null");
        BigDecimal amount = dto.getAmount();
        validateTransactionAmount(amount);

        if (dto.getDirection() == null) throw new ApplicationException("Direction cannot be null");
        String direction = dto.getDirection();
        validateDirection(direction);

        if (dto.getCurrency() == null) throw new ApplicationException("Currency cannot be null");
        String currency = dto.getCurrency();
        List<Balance> balances = validateFunds(direction, currency, amount, dto.getAccountId());
        validateCurrency(currency, balances);
    }

    /**
     * Checks if the user has a balance with the given currency
     * @param currency currency to check for
     * @param balances accounts balances
     */
    private static void validateCurrency(String currency, List<Balance> balances) {
        List<String> currencies = balances.stream().map(Balance::getCurrency).toList();
        if (!currencies.contains(currency)) {
            throw new InvalidCurrencyException("Invalid currency type: " + currency);
        }
    }

    private List<Balance> validateFunds(String direction, String currency, BigDecimal amount, Long id) {
        List<Balance> balances = balanceRepository.getBalancesById(id);
        if (direction.equals("OUT")) {
            for (Balance balance : balances) {
                if (balance.getCurrency().equals(currency) && (balance.getAmount().compareTo(amount) < 0)) {
                    throw new InsufficientFundsException("Insufficient funds");
                }
            }
        }
        return balances;
    }

    private static void validateDirection(String direction) {
        List<String> allowedDirections = List.of("IN", "OUT");
        if (!allowedDirections.contains(direction)) {
            throw new InvalidDirectionException("Not allowed direction: " + direction);
        }
    }

    private static void validateTransactionAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Invalid amount in transaction");
        }
    }

    private static void validateDescription(String description) {
        if (description.isBlank()) {
            throw new DescriptionMissingException("No description on transaction");
        }
    }

    private void validateAccount(long id) {
        Account account = accountRepository.getAccountById(id);
        if (account == null) {
            throw new AccountNotFoundException("No account with given id: " + id);
        }
    }
}
