package com.example.tradestore.api;

import com.example.tradestore.dto.Trade;
import com.example.tradestore.dto.TradeIdVersion;
import com.example.tradestore.exception.TradeException;
import com.example.tradestore.repo.TradeRepository;
import com.example.tradestore.service.TradeService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


// Only load TradeController and web layer beans
@WebMvcTest(TradeController.class)
@AutoConfigureMockMvc
class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TradeService tradeService;

    @MockitoBean
    private TradeRepository tradeRepository;

    @InjectMocks
    TradeController tradeController;

    @Test
    void testUpsertTrade() throws Exception {
        TradeResponse mockResponse = new TradeResponse(
                "T1",
                1,
                "CP-1",
                "B1",
                LocalDate.of(2030, 12, 31),
                LocalDateTime.now(),
                "N"
        );

        Mockito.when(tradeService.upsert(any(TradeRequest.class))).thenReturn(mockResponse);

        String requestJson = """
            {
              "tradeId": "T1",
              "version": 1,
              "counterPartyId": "CP-1",
              "bookId": "B1",
              "maturityDate": "2030-12-31T00:00:00"
            }
            """;

        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tradeId").value("T1"))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.counterPartyId").value("CP-1"));
    }

    @Test
    void testListTrades() throws Exception {
        Trade trade = new Trade();
        TradeIdVersion tradeIdVersion = new TradeIdVersion();
        trade.setId(tradeIdVersion);
        tradeIdVersion.setTradeId("T1");
        tradeIdVersion.setVersion(1);
        trade.setCounterPartyId("CP-1");
        trade.setBookId("B1");
        trade.setMaturityDate(LocalDate.of(2030, 12, 31));
        trade.setCreatedDate(LocalDateTime.now());
        trade.setExpired("N");

        List<Trade> trades = Collections.singletonList(trade);

        Mockito.when(tradeRepository.findAll(any(Sort.class))).thenReturn(trades);

        mockMvc.perform(get("/api/trades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id.tradeId").value("T1"))
                .andExpect(jsonPath("$[0].id.version").value(1))
                .andExpect(jsonPath("$[0].counterPartyId").value("CP-1"))
                .andExpect(jsonPath("$[0].bookId").value("B1"))
                .andExpect(jsonPath("$[0].expired").value("N"));
    }

    @Test
    void shouldReturnBadRequest_whenTradeExceptionIsThrown() throws Exception {
        TradeRequest req = new TradeRequest("T1", 0, "CP-1", "B1", LocalDate.now().minusDays(1));

        when(tradeService.upsert(any(TradeRequest.class))).thenThrow(new TradeException("Cannot insert expired trade records"));

        String requestJson = """
            {
              "tradeId": "T1",
              "version": 1,
              "counterPartyId": "CP-1",
              "bookId": "B1",
              "maturityDate": "2030-12-31T00:00:00"
            }
            """;
        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Trade Exception"))
                .andExpect(jsonPath("$.message").value("Cannot insert expired trade records"));
    }

    @Test
    void shouldReturnInternalServerError_whenUnexpectedExceptionThrown() throws Exception {
        // Arrange: make service throw a RuntimeException
        when(tradeService.upsert(any()))
                .thenThrow(new RuntimeException("Database connection lost"));

        // Act + Assert
        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "tradeId": "T2",
                          "version": 1,
                          "counterPartyId": "CP-2",
                          "bookId": "B2",
                          "maturityDate": "2099-12-31"
                        }
                        """))
                .andExpect(status().isInternalServerError())  // ✅ 500
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Database connection lost"));
    }
}
