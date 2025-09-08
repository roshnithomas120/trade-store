package com.example.tradestore.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "trades",
        indexes = {
                @Index(name="idx_trades_maturity", columnList="maturityDate"),
                @Index(name="idx_trades_created", columnList="createdDate")
        })
public class Trade {

    @EmbeddedId
    private TradeIdVersion id;

    @Column(nullable = false)
    private String counterPartyId;

    @Column(nullable = false)
    private String bookId;

    @Column(nullable = false)
    private LocalDate maturityDate;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    @Column(nullable = false, length = 1)
    private String expired; // "Y" or "N"

    public Trade() {}


}