package com.example.tradestore.kafka;

import com.example.tradestore.dto.TradeMetrics;
import com.example.tradestore.repo.TradeMetricsRepository;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = { "trade-metrics" }, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@ExtendWith(SpringExtension.class)
public class TradeKafkaConsumerTest {

    @Autowired
    private TradeMetricsRepository repository;

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        // If you want to run with Testcontainers MongoDB
        // You can configure registry.add("spring.data.mongodb.uri", container::getReplicaSetUrl);
    }

    private KafkaTemplate<String, TradeMetrics> createKafkaTemplate() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

    @Test
    void testConsumeAndStoreMetrics() throws Exception {
        KafkaTemplate<String, TradeMetrics> kafkaTemplate = createKafkaTemplate();

        TradeMetrics metrics = new TradeMetrics();
        metrics.setTradeId("T100");
        metrics.setVersion(1);
        metrics.setCreatedDate(LocalDate.now());
        metrics.setExpired(false);

        kafkaTemplate.send("trade-metrics", metrics);

        // give Kafka listener some time
        Thread.sleep(2000);

        TradeMetrics stored = repository.findAll()
                                        .stream()
                                        .filter(m -> m.getTradeId().equals("T100"))
                                        .findFirst()
                                        .orElse(null);

        assertThat(stored).isNotNull();
        assertThat(stored.getTradeId()).isEqualTo("T100");
        assertThat(stored.isExpired()).isFalse();
    }
}
