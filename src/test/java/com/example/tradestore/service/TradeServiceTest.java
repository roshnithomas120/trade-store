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
import org.mockito.*;
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
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldSaveTradeSuccessfully_whenValidRequest() {
        // Given
        TradeRequest request = new TradeRequest("T1", 2, "CP-1", "B1", LocalDate.now().plusDays(1));
        TradeIdVersion id = new TradeIdVersion("T1", 2);
        Trade trade = new Trade();
        trade.setId(id);
        trade.setCounterPartyId("CP-1");
        trade.setBookId("B1");
        trade.setMaturityDate(request.maturityDate());
        trade.setExpired("N");
        trade.setCreatedDate(LocalDateTime.now());

        when(tradeRepository.findMaxVersion("T1")).thenReturn(Optional.of(1));
        when(tradeRepository.findById(id)).thenReturn(Optional.empty());
        when(tradeRepository.save(any())).thenReturn(trade);

        // When
        TradeResponse response = tradeService.upsert(request);

        // Then
        assertNotNull(response);
        assertEquals("T1", response.tradeId());
        assertEquals(2, response.version());
        assertEquals("N", response.expired());

        verify(kafkaTemplate).send(eq("trade-metrics"), any(TradeMetrics.class));
        verify(tradeRepository).save(any());
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
    void shouldThrowTradeException_whenTradeIsExpired() {
        // Given
        TradeRequest request = new TradeRequest("T1", 1, "CP-1", "B1", LocalDate.now().minusDays(1));

        when(tradeRepository.findMaxVersion("T1")).thenReturn(Optional.of(0));

        // When / Then
        TradeException ex = assertThrows(TradeException.class, () -> tradeService.upsert(request));
        assertEquals("Cannot insert expired trade records", ex.getMessage());
        verify(tradeRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any());
    }

    @Test
    void shouldUpdateExistingTrade_whenTradeExists() {
        // Given
        TradeRequest request = new TradeRequest("T1", 3, "CP-2", "B2", LocalDate.now().plusDays(10));
        TradeIdVersion id = new TradeIdVersion("T1", 3);

        Trade existing = new Trade();
        existing.setId(id);
        existing.setCreatedDate(LocalDateTime.now().minusDays(1));

        when(tradeRepository.findMaxVersion("T1")).thenReturn(Optional.of(2));
        when(tradeRepository.findById(id)).thenReturn(Optional.of(existing));
        when(tradeRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // When
        TradeResponse response = tradeService.upsert(request);

        // Then
        assertNotNull(response);
        assertEquals("CP-2", response.counterPartyId());
        assertEquals("B2", response.bookId());

        verify(tradeRepository).save(any());
        verify(kafkaTemplate).send(eq("trade-metrics"), any(TradeMetrics.class));
    }
}
