// TradeController.java
package com.example.tradestore.api;

import com.example.tradestore.dto.Trade;
import com.example.tradestore.repo.TradeRepository;
import com.example.tradestore.service.TradeService;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService service;
    private final TradeRepository repo;

    public TradeController(TradeService service, TradeRepository repo) {
        this.service = service; this.repo = repo;
    }

    @PostMapping
    public TradeResponse upsert(@RequestBody TradeRequest request) {

        return service.upsert(request);
    }

    @GetMapping
    public List<Trade> list() {
        // Global order: createdDate, tradeId, version
        return repo.findAll(Sort.by("createdDate").ascending()
                .and(Sort.by("id.tradeId").ascending())
                .and(Sort.by("id.version").ascending()));
    }
}
