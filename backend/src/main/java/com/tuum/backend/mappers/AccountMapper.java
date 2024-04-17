package com.tuum.backend.mappers;

import com.tuum.backend.dtos.AccountDto;
import com.tuum.backend.dtos.CreateAccountDto;
import com.tuum.backend.entities.Account;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {
    Account dtoToAccount(CreateAccountDto dto);
    AccountDto accountToDto(Account account);
}
