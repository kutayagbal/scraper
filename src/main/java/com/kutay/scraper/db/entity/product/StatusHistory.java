package com.kutay.scraper.db.entity.product;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class StatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime time;
    private String status;

    public StatusHistory() {
    }

    public StatusHistory(LocalDateTime time, String status) {
        this.time = time;
        this.status = status;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

}
