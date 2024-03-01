package com.kutay.scraper.scrape;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.kutay.scraper.api.request.ApiRequestHandler;
import com.kutay.scraper.db.ProductFactory;
import com.kutay.scraper.db.entity.site.ApiEndpoint;
import com.kutay.scraper.db.entity.site.Site;
import com.kutay.scraper.util.Constants.PRODUCT_TYPE;
import com.kutay.scraper.util.Constants.TRADE_TYPE;
import com.kutay.scraper.util.ScraperException;

@Component
public abstract class Scraper {
    protected static final String API_ENDPOINT_NOT_FOUND = "API Endpoint can not be found for siteName: %s ,tradeType: %s, productType: %s";
    protected static final String SITE_HAS_NO_ENDPOINT = "No API Endpoints for siteName: %s";

    protected TRADE_TYPE tradeType;
    protected PRODUCT_TYPE productType;
    protected Site site;
    protected ApiRequestHandler requestHandler;
    protected ProductFactory productFactory;

    protected Scraper(TRADE_TYPE tradeType, PRODUCT_TYPE productType, Site site, ApiRequestHandler requestHandler,
            ProductFactory productFactory) {
        this.tradeType = tradeType;
        this.productType = productType;
        this.site = site;
        this.requestHandler = requestHandler;
        this.productFactory = productFactory;
    }

    abstract void scrape(Map<String, List<String>> requestParameters) throws ScraperException;

    public ApiEndpoint findSiteEndpoint() throws ScraperException {
        List<ApiEndpoint> siteEndpoints = site.getApiEndpoints();

        if (siteEndpoints != null && !siteEndpoints.isEmpty()) {
            Optional<ApiEndpoint> endpoint = siteEndpoints.stream()
                    .filter(s -> s.getTradeType().equals(tradeType) && s.getProductType().equals(productType))
                    .findAny();
            if (endpoint.isPresent()) {
                return endpoint.get();
            } else {
                throw new ScraperException(
                        String.format(API_ENDPOINT_NOT_FOUND,
                                site.getName(), tradeType, productType));
            }
        } else {
            throw new ScraperException(String.format(SITE_HAS_NO_ENDPOINT,
                    site.getName()));
        }
    }

}
