package com.kutay.scraper.db.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kutay.scraper.db.entity.site.Site;

public interface SiteRepo extends JpaRepository<Site, Long> {
        Optional<Site> findByName(String name);

        // Optional<Site> findByNameAndTradeTypeAndProductType(String name, TRADE_TYPE
        // tradeType,
        // PRODUCT_TYPE productType);

        // @Query("SELECT DISTINCT(s.productSearcher) FROM Site s WHERE s.tradeType =
        // :tradeType AND s.productType = :productType")
        // String getProductSearcherName(PRODUCT_TYPE productType,
        // TRADE_TYPE tradeType);

        // @Query("SELECT DISTINCT(s.name) FROM Site s WHERE s.tradeType = :tradeType
        // AND s.productType = :productType")
        // List<String> getSiteNames(TRADE_TYPE tradeType, PRODUCT_TYPE productType);

        // @Query("SELECT DISTINCT(s.tradeType) FROM Site s")
        // List<TRADE_TYPE> getTradeTypes();

        // @Query("SELECT DISTINCT(s.productType) FROM Site s WHERE s.tradeType =
        // :tradeType")
        // List<PRODUCT_TYPE> getProductTypes(TRADE_TYPE tradeType);

        // @Query("SELECT DISTINCT(s.productScraper) FROM Site s WHERE s.name IN
        // :siteNames AND s.tradeType = :tradeType AND s.productType = :productType")
        // List<String> getProductScrapers(String siteName, PRODUCT_TYPE productType,
        // TRADE_TYPE tradeType);
}
