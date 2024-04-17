package com.tuum.backend.services;

import com.tuum.backend.dtos.AccountDto;
import com.tuum.backend.dtos.BalanceDto;
import com.tuum.backend.dtos.CreateAccountDto;
import com.tuum.backend.entities.Account;
import com.tuum.backend.entities.Balance;
import com.tuum.backend.errors.ApplicationException;
import com.tuum.backend.exceptions.AccountNotFoundException;
import com.tuum.backend.exceptions.CustomerNotFoundException;
import com.tuum.backend.exceptions.InvalidCurrencyException;
import com.tuum.backend.mappers.AccountMapper;
import com.tuum.backend.mappers.BalanceMapper;
import com.tuum.backend.rabbitmq.MessagingConfig;
import com.tuum.backend.repositories.AccountRepository;
import com.tuum.backend.repositories.BalanceRepository;
import com.tuum.backend.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AccountService {
    private static final List<String> ALLOWED_CURRENCIES = List.of("EUR", "SEK", "GBP", "USD");
    private final RabbitTemplate template;
    private final AccountMapper accountMapper;
    private final AccountRepository accountRepository;
    private final BalanceRepository balanceRepository;
    private final CustomerRepository customerRepository;
    private final BalanceMapper balanceMapper;

    /**
     * Create account and balances for account, inserts them into the database.
     * @param dto CreateAccountDto
     * @return accountDto with balances
     * @throws ApplicationException throws exception if dto validation failed
     */
    public AccountDto createAccount(CreateAccountDto dto) throws ApplicationException {
        validateDto(dto);
        validateCustomer(dto.getCustomerId());
        // Removing duplicates from currencies
        List<String> currencies = new ArrayList<>(new HashSet<>(dto.getCurrencies()));
        validateCurrencies(currencies);
        Account account = accountMapper.dtoToAccount(dto);
        account.setCountry(dto.getCountry());
        accountRepository.createAccount(account);
        List<BalanceDto> balances = createBalances(currencies, account.getId());
        AccountDto accountDto = accountMapper.accountToDto(account);
        accountDto.setBalances(balances);
        accountDto.setAccountId(account.getId());
        template.convertAndSend(MessagingConfig.EXCHANGE_NAME, MessagingConfig.ROUTING_KEY, accountDto);
        return accountDto;
    }

    private void validateDto(CreateAccountDto dto) {
        if (dto.getCountry() == null || dto.getCountry().isEmpty()) {
            throw new ApplicationException("Country is a required field");
        }
        if (dto.getCustomerId() == null) {
            throw new ApplicationException("Customer ID is a required field");
        }
        if (dto.getCurrencies() == null || dto.getCurrencies().isEmpty()) {
            throw new InvalidCurrencyException("No currencies given");
        }
    }
    private void validateCustomer(Integer id) {
        if (customerRepository.getCustomerById(id) == null) {
            throw new CustomerNotFoundException("Customer not found with id: " + id);
        }
    }

    private static void validateCurrencies(List<String> currencies) {
        for (String ccy: currencies) {
            if (!ALLOWED_CURRENCIES.contains(ccy)) {
                throw new InvalidCurrencyException("Not allowed type: " + ccy);
            }
        }
    }

    /**
     * Create balances with list of currencies and store them in the database.
     * @param currencies List of String currencies
     * @param id id of account that currencies are for
     * @return return list of balance objects
     */
    private List<BalanceDto> createBalances(List<String> currencies, Long id) {
        List<BalanceDto> balances = new ArrayList<>();
        for (String ccy: currencies) {
            Balance balance = new Balance(id, ccy, BigDecimal.ZERO);
            balanceRepository.createBalance(balance);
            balances.add(balanceMapper.balanceToDto(balance));
        }
        return balances;
    }

    /**
     * Get account for given ID
     * @param id long id
     * @return returns accountDto
     * @throws ApplicationException thrown if no account is found
     */
    public AccountDto getAccount(Long id) throws ApplicationException {
        Account account = accountRepository.getAccountById(id);
        if (account == null) {
            throw new AccountNotFoundException("Account with id " + id + " not found.");
        }
        List<Balance> balancesList = balanceRepository.getBalancesById(account.getId());
        List<BalanceDto> balances = new ArrayList<>();
        for (Balance bal: balancesList) {
            balances.add(balanceMapper.balanceToDto(bal));
        }
        return new AccountDto(id, account.getCustomerId(), balances);
    }
}
