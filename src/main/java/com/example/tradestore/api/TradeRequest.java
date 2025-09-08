package com.example.tradestore.api;

import java.time.LocalDate;

public record TradeRequest(
        String tradeId,
        int version,
        String counterPartyId,
        String bookId,
        LocalDate maturityDate
) {}