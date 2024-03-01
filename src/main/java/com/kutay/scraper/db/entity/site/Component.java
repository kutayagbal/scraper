package com.kutay.scraper.db.entity.site;

import com.kutay.scraper.util.Constants.PRODUCT_TYPE;
import com.kutay.scraper.util.Constants.TRADE_TYPE;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Component {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TRADE_TYPE tradeType;

    @Enumerated(EnumType.STRING)
    private PRODUCT_TYPE productType;

    private String scraper;
    private String apiRequestHandler;
    private String productFactory;

    public Component() {
    }

    public Component(TRADE_TYPE tradeType, PRODUCT_TYPE productType, String scraper, String apiRequestHandler,
            String productFactory) {
        this.tradeType = tradeType;
        this.productType = productType;
        this.scraper = scraper;
        this.apiRequestHandler = apiRequestHandler;
        this.productFactory = productFactory;
    }

    public Long getId() {
        return id;
    }

    public TRADE_TYPE getTradeType() {
        return tradeType;
    }

    public PRODUCT_TYPE getProductType() {
        return productType;
    }

    public String getScraper() {
        return scraper;
    }

    public String getApiRequestHandler() {
        return apiRequestHandler;
    }

    public String getProductFactory() {
        return productFactory;
    }

}
