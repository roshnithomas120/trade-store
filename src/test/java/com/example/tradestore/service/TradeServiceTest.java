// TradeServiceTest.java
package com.example.tradestore.service;

import com.example.tradestore.api.TradeRequest;
import com.example.tradestore.api.TradeResponse;
import com.example.tradestore.dto.Trade;
import com.example.tradestore.dto.TradeIdVersion;
import com.example.tradestore.dto.TradeMetrics;
import com.example.tradestore.exception.TradeException;
import com.example.tradestore.repo.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TradeServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private KafkaTemplate<String, TradeMetrics> kafkaTemplate;

    @InjectMocks
    private TradeService tradeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldThrowTradeException_whenVersionIsLower() {
        // Given
        TradeRequest request = new TradeRequest("T1", 1, "CP-1", "B1", LocalDate.now().plusDays(1));
        when(tradeRepository.findMaxVersion("T1")).thenReturn(Optional.of(2));

        // When / Then
        TradeException ex = assertThrows(TradeException.class, () -> tradeService.upsert(request));
        assertEquals("Lower version trade rejected", ex.getMessage());

        verify(tradeRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any());
    }

    @Test
    void shouldSaveTradeAndSendToKafka_whenValidTrade() {
        // Given
        TradeRequest request = new TradeRequest("T2", 5, "CP-2", "B2", LocalDate.now().plusDays(2));
        when(tradeRepository.findMaxVersion("T2")).thenReturn(Optional.of(4));

        Trade entity = new Trade();
        entity.setId(new TradeIdVersion("T2", 5));
        entity.setCounterPartyId("CP-2");
        entity.setBookId("B2");
        entity.setMaturityDate(request.maturityDate());
        entity.setCreatedDate(LocalDateTime.now());
        entity.setExpired("N");

        // Stub repo.save(..) to return a non-null Trade
        when(tradeRepository.save(any(Trade.class))).thenReturn(entity);

        // When
        TradeResponse response = tradeService.upsert(request);

        // Then
        assertNotNull(response);
        assertEquals("T2", response.tradeId());
        assertEquals(5, response.version());
        assertEquals("CP-2", response.counterPartyId());
        assertEquals("B2", response.bookId());
        assertEquals("N", response.expired());

        // Verify interactions
        verify(tradeRepository).save(any(Trade.class));

        ArgumentCaptor<TradeMetrics> metricsCaptor = ArgumentCaptor.forClass(TradeMetrics.class);
        verify(kafkaTemplate).send(eq("trade-metrics"), metricsCaptor.capture());

        TradeMetrics metrics = metricsCaptor.getValue();
        assertEquals("T2", metrics.getTradeId());
        assertEquals(5, metrics.getVersion());
        assertFalse(metrics.isExpired());
    }

    @Test
    void shouldThrowTradeException_whenTradeIsExpired() {
        // Given
        TradeRequest request = new TradeRequest("T3", 1, "CP-3", "B3", LocalDate.now().minusDays(1));

        // When / Then
        TradeException ex = assertThrows(TradeException.class, () -> tradeService.upsert(request));
        assertEquals("Cannot insert expired trade records", ex.getMessage());

        verify(tradeRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any());
    }
}
