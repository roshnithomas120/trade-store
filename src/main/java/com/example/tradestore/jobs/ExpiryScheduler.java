// ExpiryScheduler.java
package com.example.tradestore.jobs;

import com.example.tradestore.repo.TradeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ExpiryScheduler {

    private final TradeRepository repo;
    public ExpiryScheduler(TradeRepository repo) { this.repo = repo; }

    // Every day at 01:00
    @Scheduled(cron = "0 0 1 * * *")
    public void updateExpiredFlags() {
        var today = LocalDate.now();
        repo.findAll().forEach(t -> {
            var shouldBeExpired = t.getMaturityDate().isBefore(today);
            if (shouldBeExpired && !"Y".equals(t.getExpired())) {
                t.setExpired("Y");
            }
        });
        // JPA dirty checking will flush at txn end; if you want explicit, call saveAll.
    }
}
