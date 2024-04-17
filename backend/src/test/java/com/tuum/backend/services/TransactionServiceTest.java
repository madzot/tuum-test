package com.tuum.backend.services;

import com.tuum.backend.dtos.CompletedTransactionDto;
import com.tuum.backend.dtos.CreateTransactionDto;
import com.tuum.backend.dtos.TransactionDto;
import com.tuum.backend.entities.Account;
import com.tuum.backend.entities.Balance;
import com.tuum.backend.entities.Transaction;
import com.tuum.backend.exceptions.*;
import com.tuum.backend.mappers.TransactionMapper;
import com.tuum.backend.repositories.AccountRepository;
import com.tuum.backend.repositories.BalanceRepository;
import com.tuum.backend.repositories.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private RabbitTemplate template;
    @Mock
    private TransactionMapper transactionMapper;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private BalanceRepository balanceRepository;
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void testGetTransactionReturnsListOfTransactionsByAccount() {
        Transaction transaction = new Transaction(1L, 1L, "EUR", BigDecimal.valueOf(10), "IN", "None");
        Transaction transaction1 = new Transaction(1L, 1L, "USD", BigDecimal.valueOf(20), "IN", "None");
        Transaction transaction2 = new Transaction(1L, 1L, "EUR", BigDecimal.valueOf(30), "IN", "None");
        TransactionDto dto = new TransactionDto(1L, 1L, BigDecimal.valueOf(10), "EUR", "IN", "None");
        TransactionDto dto1 = new TransactionDto(1L, 1L, BigDecimal.valueOf(20), "USD", "IN", "None");
        TransactionDto dto2 = new TransactionDto(1L, 1L, BigDecimal.valueOf(30), "EUR", "IN", "None");


        given(transactionRepository.getTransactionsByAccountId(1L)).willReturn(List.of(transaction, transaction1, transaction2));
        given(transactionMapper.transactionToDto(transaction)).willReturn(dto);
        given(transactionMapper.transactionToDto(transaction1)).willReturn(dto1);
        given(transactionMapper.transactionToDto(transaction2)).willReturn(dto2);

        ResponseEntity<List<TransactionDto>> actual = transactionService.getTransactions(1L);

        then(transactionRepository).should().getTransactionsByAccountId(1L);
        then(transactionMapper).should().transactionToDto(transaction);
        then(transactionMapper).should().transactionToDto(transaction1);
        then(transactionMapper).should().transactionToDto(transaction2);
        assertEquals(actual.getBody(), List.of(dto, dto1, dto2));
    }

    @Test
    void testGetTransactionThrowsExceptionIfNoAccountFound() {
        assertThrows(AccountNotFoundException.class, () -> transactionService.getTransactions(929358L));
        assertThrows(AccountNotFoundException.class, () -> transactionService.getTransactions(-1L));
        assertThrows(AccountNotFoundException.class, () -> transactionService.getTransactions(99999999L));
    }

    @Test
    void testCreateTransactionThrowsExceptionIfNoAccountWithGivenId() {
        CreateTransactionDto dto = new CreateTransactionDto(1L, BigDecimal.TEN, "EUR", "IN", "Description");

        assertThrows(AccountNotFoundException.class, () -> transactionService.createTransaction(dto));
    }
    @Test
    void testCreateTransactionThrowsExceptionIfNoDescription() {
        CreateTransactionDto dto = new CreateTransactionDto(1L, BigDecimal.TEN, "EUR", "IN", "");
        CreateTransactionDto dto2 = new CreateTransactionDto(1L, BigDecimal.TEN, "EUR", "IN", "     ");
        Account account = new Account(1L, 1, "Estonia");
        given(accountRepository.getAccountById(1L)).willReturn(account);

        assertThrows(DescriptionMissingException.class, () -> transactionService.createTransaction(dto));
        assertThrows(DescriptionMissingException.class, () -> transactionService.createTransaction(dto2));
    }

    @Test
    void testCreateTransactionThrowsExceptionIfAmountIsNotPositive() {
        CreateTransactionDto dto = new CreateTransactionDto(1L, BigDecimal.valueOf(-10L), "EUR", "IN", "Description");
        Account account = new Account(1L, 1, "Estonia");
        given(accountRepository.getAccountById(1L)).willReturn(account);

        assertThrows(InvalidAmountException.class, () -> transactionService.createTransaction(dto));
    }

    @Test
    void testCreateTransactionThrowsExceptionWhenDirectionIsNotInOrOut() {
        CreateTransactionDto dto2 = new CreateTransactionDto(1L, BigDecimal.valueOf(30L), "EUR", "", "Description");
        CreateTransactionDto dto3 = new CreateTransactionDto(1L, BigDecimal.valueOf(40L), "EUR", "Null", "Description");
        CreateTransactionDto dto4 = new CreateTransactionDto(1L, BigDecimal.valueOf(40L), "EUR", "INOUT", "Description");
        Account account = new Account(1L, 1, "Estonia");
        given(accountRepository.getAccountById(1L)).willReturn(account);

        assertThrows(InvalidDirectionException.class, () -> transactionService.createTransaction(dto2));
        assertThrows(InvalidDirectionException.class, () -> transactionService.createTransaction(dto3));
        assertThrows(InvalidDirectionException.class, () -> transactionService.createTransaction(dto4));
    }

    @Test
    void testCreateTransactionThrowsExceptionIfDirectionIsOutAndNotEnoughMoney() {
        CreateTransactionDto dto = new CreateTransactionDto(1L, BigDecimal.valueOf(30), "EUR", "OUT", "Description");
        Account account = new Account(1L, 1, "Estonia");
        given(accountRepository.getAccountById(1L)).willReturn(account);
        given(balanceRepository.getBalancesById(1L)).willReturn(List.of(new Balance(1L, "EUR", BigDecimal.ONE)));

        assertThrows(InsufficientFundsException.class, () -> transactionService.createTransaction(dto));
    }

    @Test
    void testCreateTransactionThrowsExceptionIfAccountDoesntHaveBalanceWithGivenCurrency() {
        CreateTransactionDto dto = new CreateTransactionDto(1L, BigDecimal.valueOf(30), "EUR", "OUT", "Description");
        Account account = new Account(1L, 1, "Estonia");
        given(accountRepository.getAccountById(1L)).willReturn(account);
        given(balanceRepository.getBalancesById(1L)).willReturn(List.of(new Balance(1L, "USD", BigDecimal.ONE)));

        assertThrows(InvalidCurrencyException.class, () -> transactionService.createTransaction(dto));
    }

    @Test
    void testCreateTransactionReturnsCorrectDto() {
        CreateTransactionDto dto = new CreateTransactionDto(2L, BigDecimal.valueOf(1), "EUR", "IN", "Description");
        Account account = new Account(2L, 1, "Estonia");
        Transaction transaction = new Transaction(0L, 2L, "EUR", BigDecimal.ONE, "IN", "Description");
        given(accountRepository.getAccountById(2L)).willReturn(account);
        given(balanceRepository.getBalancesById(2L)).willReturn(List.of(new Balance(2L, "EUR", BigDecimal.TEN)));
        given(transactionMapper.dtoToTransaction(dto)).willReturn(transaction);
        given(balanceRepository.getBalanceByIdAndCcy(2L, "EUR")).willReturn(new Balance(2L, "EUR", BigDecimal.valueOf(11)));

        CompletedTransactionDto actual = transactionService.createTransaction(dto);

        then(transactionMapper).should().dtoToTransaction(dto);
        then(transactionRepository).should().createTransaction(transaction);
        then(balanceRepository).should().addToBalance(BigDecimal.ONE, transaction.getAccountId(), transaction.getCurrency());
        then(balanceRepository).should().getBalanceByIdAndCcy(dto.getAccountId(), dto.getCurrency());

        assertEquals(2, actual.getAccountId());
        assertEquals(0, actual.getTransactionId());
        assertEquals("EUR", actual.getCurrency());
        assertEquals("IN", actual.getDirection());
        assertEquals("Description", actual.getDescription());
        assertEquals(BigDecimal.valueOf(11), actual.getNewBalance());
    }
}
