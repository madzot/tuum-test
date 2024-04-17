package com.tuum.backend.services;

import com.tuum.backend.dtos.AccountDto;
import com.tuum.backend.dtos.BalanceDto;
import com.tuum.backend.dtos.CreateAccountDto;
import com.tuum.backend.entities.Account;
import com.tuum.backend.entities.Balance;
import com.tuum.backend.entities.Customer;
import com.tuum.backend.exceptions.AccountNotFoundException;
import com.tuum.backend.exceptions.InvalidCurrencyException;
import com.tuum.backend.mappers.AccountMapper;
import com.tuum.backend.mappers.BalanceMapper;
import com.tuum.backend.repositories.AccountRepository;
import com.tuum.backend.repositories.BalanceRepository;
import com.tuum.backend.repositories.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private BalanceMapper balanceMapper;
    @Mock
    private RabbitTemplate template;

    @Mock
    private BalanceRepository balanceRepository;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void testCreateAccountThrowsExceptionIfInvalidCurrencyGiven() {
        // given
        given(customerRepository.getCustomerById(1)).willReturn(new Customer());
        // Should not allow NOK
        CreateAccountDto createAccountDto = new CreateAccountDto(1, "Estonia", List.of("EUR", "USD", "NOK"));
        // when
        assertThrows(InvalidCurrencyException.class, () -> accountService.createAccount(createAccountDto));
        // then
    }

    @Test
    void testCreateAccountAddsAccountToDatabase() {
        // given
        CreateAccountDto createAccountDto = new CreateAccountDto(1, "Estonia", List.of("EUR", "USD"));
        BalanceDto balanceDtoEUR = new BalanceDto("EUR", BigDecimal.ZERO);
        BalanceDto balanceDtoUSD = new BalanceDto("USD", BigDecimal.ZERO);
        AccountDto accountDto = new AccountDto(1L, 1, List.of(balanceDtoEUR, balanceDtoUSD));
        Account account = new Account(1L, 1, "Estonia");
        given(accountMapper.dtoToAccount(createAccountDto)).willReturn(account);
        given(accountMapper.accountToDto(account)).willReturn(accountDto);
        given(customerRepository.getCustomerById(1)).willReturn(new Customer());
        //when
        accountService.createAccount(createAccountDto);
        //then
        then(accountMapper).should().dtoToAccount(createAccountDto);
        then(accountRepository).should().createAccount(account);
        then(accountMapper).should().accountToDto(account);
    }

    @Test
    void testCreateAccountReturnsDtoWithCorrectCustomerIdAndMapOfBalances() {
        CreateAccountDto createAccountDto = new CreateAccountDto(1, "Estonia", List.of("EUR", "USD"));
        Account account = new Account(1L, 1, "Estonia");
        BalanceDto balanceDtoEUR = new BalanceDto("EUR", BigDecimal.ZERO);
        BalanceDto balanceDtoUSD = new BalanceDto("USD", BigDecimal.ZERO);
        AccountDto accountDto = new AccountDto(1L, 1, List.of(balanceDtoEUR, balanceDtoUSD));
        given(accountMapper.dtoToAccount(createAccountDto)).willReturn(account);
        given(accountMapper.accountToDto(account)).willReturn(accountDto);
        given(customerRepository.getCustomerById(1)).willReturn(new Customer());

        AccountDto result = accountService.createAccount(createAccountDto);

        then(accountMapper).should().dtoToAccount(createAccountDto);
        then(accountRepository).should().createAccount(account);
        then(accountMapper).should().accountToDto(account);
        assertEquals(2, result.getBalances().size());
        assertEquals(1, result.getAccountId());
    }

    @Test
    void testCreateAccountRemovesDuplicateCurrenciesIfPresent() {
        CreateAccountDto createAccountDto = new CreateAccountDto(1, "Estonia", List.of("EUR", "USD", "EUR"));
        Account account = new Account(1L, 1, "Estonia");
        BalanceDto balanceDtoEUR = new BalanceDto("EUR", BigDecimal.ZERO);
        BalanceDto balanceDtoUSD = new BalanceDto("USD", BigDecimal.ZERO);
        AccountDto accountDto = new AccountDto(1L, 1, List.of(balanceDtoEUR, balanceDtoUSD));
        given(accountMapper.dtoToAccount(createAccountDto)).willReturn(account);
        given(accountMapper.accountToDto(account)).willReturn(accountDto);
        given(customerRepository.getCustomerById(1)).willReturn(new Customer());

        AccountDto result = accountService.createAccount(createAccountDto);

        then(accountMapper).should().dtoToAccount(createAccountDto);
        then(accountRepository).should().createAccount(account);
        then(accountMapper).should().accountToDto(account);
        assertEquals(2, result.getBalances().size());
    }

    @Test
    void testGetAccountThrowsExceptionIfNoAccountWithGivenId() {
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccount(1L));
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccount(20L));
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccount(999999999L));
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccount(-1L));
    }

    @Test
    void testGetAccountReturnsCorrectAccountDto() {
        // Setting up 2 accounts
        Account account = new Account(1L, 1, "Estonia");
        Account account2 = new Account(2L, 2, "Finland");

        // Setting up balances
        Balance balance10 = new Balance(1L, "EUR", BigDecimal.ONE);
        Balance balance11 = new Balance(1L, "USD", BigDecimal.TEN);

        Balance balance20 = new Balance(2L, "EUR", BigDecimal.ZERO);

        BalanceDto balanceDtoEUR10 = new BalanceDto("EUR", BigDecimal.ONE);
        BalanceDto balanceDtoUSD20 = new BalanceDto("USD", BigDecimal.TEN);
        BalanceDto balanceDtoEUR21 = new BalanceDto("EUR", BigDecimal.ZERO);

        // Giving accounts and balances to repositories
        given(accountRepository.getAccountById(1L)).willReturn(account);
        given(accountRepository.getAccountById(2L)).willReturn(account2);

        given(balanceRepository.getBalancesById(1L)).willReturn(List.of(balance10, balance11));
        given(balanceRepository.getBalancesById(2L)).willReturn(List.of(balance20));

        given(balanceMapper.balanceToDto(balance10)).willReturn(balanceDtoEUR10);
        given(balanceMapper.balanceToDto(balance11)).willReturn(balanceDtoUSD20);
        given(balanceMapper.balanceToDto(balance20)).willReturn(balanceDtoEUR21);


        // Using getAccount() to get an AccountDto
        AccountDto accountDto = accountService.getAccount(1L);
        AccountDto accountDto2 = accountService.getAccount(2L);

        // then
        then(accountRepository).should().getAccountById(1L);
        then(balanceRepository).should().getBalancesById(accountDto.getAccountId());
        then(balanceMapper).should().balanceToDto(balance10);
        then(balanceMapper).should().balanceToDto(balance11);

        then(accountRepository).should().getAccountById(2L);
        then(balanceRepository).should().getBalancesById(accountDto2.getAccountId());
        then(balanceMapper).should().balanceToDto(balance20);

        // First account tests
        assertEquals(1L, accountDto.getAccountId());
        assertEquals(1, accountDto.getCustomerId());
        assertEquals(List.of(balanceDtoEUR10, balanceDtoUSD20), accountDto.getBalances());
        assertEquals(BigDecimal.ONE, accountDto.getBalances().get(0).amount());
        assertEquals(BigDecimal.TEN, accountDto.getBalances().get(1).amount());


        // Second account tests
        assertEquals(2L, accountDto2.getAccountId());
        assertEquals(2, accountDto2.getCustomerId());
        assertEquals(List.of(balanceDtoEUR21), accountDto2.getBalances());
        assertEquals(BigDecimal.ZERO, accountDto2.getBalances().get(0).amount());

    }
}
