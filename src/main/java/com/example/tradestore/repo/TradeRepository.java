// TradeRepository.java
package com.example.tradestore.repo;

import com.example.tradestore.dto.Trade;
import com.example.tradestore.dto.TradeIdVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TradeRepository extends JpaRepository<Trade, TradeIdVersion> {

    @Query("select max(t.id.version) from Trade t where t.id.tradeId = :tradeId")
    Optional<Integer> findMaxVersion(@Param("tradeId") String tradeId);
}
