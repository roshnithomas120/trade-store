package com.example.tradestore.kafka;

import com.example.tradestore.dto.TradeMetrics;
import com.example.tradestore.repo.TradeMetricsRepository;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"trade-metrics"})
@DirtiesContext
class TradeKafkaConsumerTest {

    @Autowired
    private TradeMetricsRepository repository;

    @Autowired
    private KafkaTemplate<String, TradeMetrics> kafkaTemplate;

    @Autowired
    private TradeMetricsRepository tradeMetricsRepository;

/*    private KafkaTemplate<String, TradeMetrics> createKafkaTemplate() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }*/

    @Test
    void testConsumeAndStoreMetrics() {
      //  KafkaTemplate<String, TradeMetrics> kafkaTemplate = createKafkaTemplate();

        TradeMetrics metrics = new TradeMetrics();
        metrics.setTradeId("T100");
        metrics.setVersion(1);
        metrics.setCreatedDate(LocalDate.now());
        metrics.setExpired(false);

        // Send message to Kafka
        kafkaTemplate.send("trade-metrics", metrics);

        // Awaitility instead of Thread.sleep
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            TradeMetrics stored = repository.findAll()
                    .stream()
                    .filter(m -> m.getTradeId().equals("T100"))
                    .findFirst()
                    .orElse(null);

            assertThat(stored).isNotNull();
            assertThat(stored.getTradeId()).isEqualTo("T100");
            assertThat(stored.isExpired()).isFalse();
        });
    }
}
