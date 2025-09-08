package com.example.tradestore.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "trade_metrics")
@Getter
@Setter
@NoArgsConstructor
public class TradeMetrics {

    @Id
    private String id;

    private String tradeId;
    private int version;
    private LocalDate createdDate;
    private boolean expired;

}