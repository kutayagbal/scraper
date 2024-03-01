package com.kutay.scraper.db.entity.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class PriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime time;
    private BigDecimal price;

    public PriceHistory() {
    }

    public PriceHistory(LocalDateTime time, BigDecimal price) {
        this.time = time;
        this.price = price;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public BigDecimal getPrice() {
        return price;
    }

}
