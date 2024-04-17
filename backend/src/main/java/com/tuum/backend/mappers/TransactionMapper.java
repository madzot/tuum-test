package com.tuum.backend.mappers;

import com.tuum.backend.dtos.CompletedTransactionDto;
import com.tuum.backend.dtos.CreateTransactionDto;
import com.tuum.backend.dtos.TransactionDto;
import com.tuum.backend.entities.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {
    Transaction dtoToTransaction(CreateTransactionDto dto);
    CompletedTransactionDto dtoToCompletedTo(CreateTransactionDto dto);
    TransactionDto transactionToDto(Transaction transaction);
}
