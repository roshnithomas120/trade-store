package com.example.tradestore.jobs;

import com.example.tradestore.dto.Trade;
import com.example.tradestore.repo.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ExpirySchedulerTest {

    private TradeRepository repo;
    private ExpiryScheduler scheduler;

    @BeforeEach
    void setUp() {
        repo = mock(TradeRepository.class);
        scheduler = new ExpiryScheduler(repo);
    }

    @Test
    void shouldMarkTradeAsExpired_whenMaturityDateIsBeforeToday() {
        // Arrange
        Trade activeTrade = new Trade();
        activeTrade.setMaturityDate(LocalDate.now().minusDays(1));
        activeTrade.setExpired("N");
        activeTrade.setCreatedDate(LocalDateTime.now());

        when(repo.findAll()).thenReturn(List.of(activeTrade));

        // Act
        scheduler.updateExpiredFlags();

        // Assert
        assertThat(activeTrade.getExpired()).isEqualTo("Y");
    }

    @Test
    void shouldNotChangeExpiredFlag_whenTradeAlreadyExpired() {
        Trade alreadyExpired = new Trade();
        alreadyExpired.setMaturityDate(LocalDate.now().minusDays(10));
        alreadyExpired.setExpired("Y");
        alreadyExpired.setCreatedDate(LocalDateTime.now());

        when(repo.findAll()).thenReturn(List.of(alreadyExpired));

        scheduler.updateExpiredFlags();

        assertThat(alreadyExpired.getExpired()).isEqualTo("Y");
    }

    @Test
    void shouldNotMarkFutureTradesAsExpired() {
        Trade futureTrade = new Trade();
        futureTrade.setMaturityDate(LocalDate.now().plusDays(5));
        futureTrade.setExpired("N");
        futureTrade.setCreatedDate(LocalDateTime.now());

        when(repo.findAll()).thenReturn(List.of(futureTrade));

        scheduler.updateExpiredFlags();

        assertThat(futureTrade.getExpired()).isEqualTo("N");
    }
}
