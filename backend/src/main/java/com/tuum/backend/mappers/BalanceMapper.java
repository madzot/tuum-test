package com.tuum.backend.mappers;

import com.tuum.backend.dtos.BalanceDto;
import com.tuum.backend.entities.Balance;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BalanceMapper {
    Balance dtoToBalance(BalanceDto dto);
    BalanceDto balanceToDto(Balance balance);
}
