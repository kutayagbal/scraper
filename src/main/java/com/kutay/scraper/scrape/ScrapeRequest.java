package com.kutay.scraper.scrape;

import java.util.List;
import java.util.Map;

import com.kutay.scraper.util.Constants.PRODUCT_TYPE;
import com.kutay.scraper.util.Constants.TRADE_TYPE;

public class ScrapeRequest {

    private String siteName;
    private PRODUCT_TYPE productType;
    private TRADE_TYPE tradeType;

    private Map<String, List<String>> parameters;

    public ScrapeRequest(String siteName, TRADE_TYPE tradeType, PRODUCT_TYPE productType,
            Map<String, List<String>> parameters) {
        this.siteName = siteName;
        this.productType = productType;
        this.tradeType = tradeType;
        this.parameters = parameters;
    }

    public PRODUCT_TYPE getProductType() {
        return productType;
    }

    public TRADE_TYPE getTradeType() {
        return tradeType;
    }

    public String getSiteName() {
        return siteName;
    }

    public Map<String, List<String>> getParameters() {
        return parameters;
    }
}
