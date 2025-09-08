package com.example.tradestore.repo;

import com.example.tradestore.dto.TradeMetrics;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TradeMetricsRepository extends MongoRepository<TradeMetrics, String> {
    boolean findByTradeId(String tradeId);
}
