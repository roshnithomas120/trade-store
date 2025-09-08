package com.example.tradestore.dto;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;


@Embeddable
@Setter
@Getter
public class TradeIdVersion implements Serializable {
    private String tradeId;
    private int version;

    public TradeIdVersion() {}
    public TradeIdVersion(String tradeId, int version) {
        this.tradeId = tradeId; this.version = version;
    }

    public String getTradeId() { return tradeId; }
    public int getVersion() { return version; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TradeIdVersion)) return false;
        TradeIdVersion that = (TradeIdVersion) o;
        return version == that.version && Objects.equals(tradeId, that.tradeId);
    }
    @Override public int hashCode() { return Objects.hash(tradeId, version); }
}