// TradeService.java
package com.example.tradestore.service;

import com.example.tradestore.api.TradeRequest;
import com.example.tradestore.api.TradeResponse;
import com.example.tradestore.dto.Trade;
import com.example.tradestore.dto.TradeIdVersion;
import com.example.tradestore.dto.TradeMetrics;
import com.example.tradestore.exception.TradeException;
import com.example.tradestore.repo.TradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class TradeService {

    private final TradeRepository repo;
    @Autowired
    private KafkaTemplate<String, TradeMetrics> kafkaTemplate;

    public TradeService(TradeRepository repo) { this.repo = repo; }

    @Transactional
    public TradeResponse upsert(TradeRequest req) {
        // Rule 1: Version check
        int incomingVersion = req.version();
        var maxVersionOpt = repo.findMaxVersion(req.tradeId());
        if (maxVersionOpt.isPresent() && incomingVersion < maxVersionOpt.get()) {
            throw new TradeException("Lower version trade rejected");
        }

        // Rule 2: Expired flag
        String expired = (req.maturityDate().isBefore(LocalDate.now())) ? "Y" : "N";
        // If you want to reject instead:
        // if (req.maturityDate().isBefore(LocalDate.now())) throw new IllegalArgumentException("Past maturity");

        var id = new TradeIdVersion(req.tradeId(), incomingVersion);
        var entity = repo.findById(id).orElseGet(Trade::new);
        if(expired.equals("Y")){
            throw new TradeException("Cannot insert expired trade records");
        }
        entity.setId(id);
        entity.setCounterPartyId(req.counterPartyId());
        entity.setBookId(req.bookId());
        entity.setMaturityDate(req.maturityDate());
        entity.setExpired(expired);
        entity.setCreatedDate(entity.getCreatedDate() == null ? LocalDateTime.now() : entity.getCreatedDate());

        var saved = repo.save(entity);

        TradeMetrics metrics = new TradeMetrics();
        metrics.setTradeId(saved.getId().getTradeId());
        metrics.setVersion(saved.getId().getVersion());
        metrics.setCreatedDate(saved.getCreatedDate().toLocalDate());
        metrics.setExpired(saved.getExpired().equals("Y"));

        kafkaTemplate.send("trade-metrics", metrics);
        return new TradeResponse(
                saved.getId().getTradeId(),
                saved.getId().getVersion(),
                saved.getCounterPartyId(),
                saved.getBookId(),
                saved.getMaturityDate(),
                saved.getCreatedDate(),
                saved.getExpired()
        );
    }
}
