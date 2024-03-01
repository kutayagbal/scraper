package com.kutay.scraper.db.entity.product;

import java.time.LocalDateTime;

import com.kutay.scraper.util.Constants;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ActionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime time;
    private Constants.ACTION_TYPE type;
    private String description;

    public ActionHistory() {
    }

    public ActionHistory(Constants.ACTION_TYPE type, LocalDateTime time, String description) {
        this.type = type;
        this.time = time;
        this.description = description;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public Constants.ACTION_TYPE getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

}
