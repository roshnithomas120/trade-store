package com.example.tradestore.kafka;

import com.example.tradestore.dto.Trade;
import com.example.tradestore.dto.TradeMetrics;
import com.example.tradestore.repo.TradeMetricsRepository;
import com.example.tradestore.service.TradeService;
import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.KafkaListener;

@Service
public class TradeKafkaConsumer {
    private final TradeMetricsRepository repository;

    public TradeKafkaConsumer(TradeMetricsRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "trade-metrics", groupId = "trade-store-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(TradeMetrics metrics) {
        System.out.println("Received metrics: " + metrics.getTradeId());
        repository.save(metrics);
    }
}