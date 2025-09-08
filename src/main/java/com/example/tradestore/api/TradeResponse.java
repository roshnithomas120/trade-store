package com.example.tradestore.api;

import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TradeResponse(
        String tradeId,
        int version,
        String counterPartyId,
        String bookId,
        LocalDate maturityDate,
        LocalDateTime createdDate,
        String expired
) {}