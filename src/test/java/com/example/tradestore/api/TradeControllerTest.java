package com.example.tradestore.api;

import com.example.tradestore.dto.Trade;
import com.example.tradestore.dto.TradeIdVersion;
import com.example.tradestore.repo.TradeRepository;
import com.example.tradestore.service.TradeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
}
