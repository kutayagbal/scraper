package com.kutay.scraper.db.repo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kutay.scraper.db.entity.product.House;
import com.kutay.scraper.util.Constants.PRODUCT_TYPE;
import com.kutay.scraper.util.Constants.TRADE_TYPE;

public interface HouseRepo extends JpaRepository<House, Long> {
        @Query("SELECT h FROM Product p"
                        + " JOIN House h ON p = h.product"
                        + " WHERE p.idInSite = :idInSite"
                        + " AND p.siteName = :siteName"
                        + " AND p.tradeType = :tradeType"
                        + " AND p.productType = :productType")
        Optional<House> findHouse(@Param("idInSite") String idInSite,
                        @Param("siteName") String siteName,
                        @Param("tradeType") TRADE_TYPE tradeType,
                        @Param("productType") PRODUCT_TYPE productType);

        @Query("SELECT house FROM House house"
                        + " JOIN Product product ON product = house.product"
                        + " WHERE product.status != 'REMOVED'"
                        + " AND product.siteName = :siteName"
                        + " AND product.tradeType = :tradeType"
                        + " AND product.productType = :productType"
                        + " AND product.price <= :maxPrice AND product.price >= :minPrice"
                        + " AND house.city IN :cities"
                        + " AND house.propertyType IN :propertyTypes")
        List<House> searchProduct(String siteName,
                        @Param("tradeType") TRADE_TYPE tradeType,
                        @Param("productType") PRODUCT_TYPE productType,
                        @Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice,
                        @Param("cities") List<String> cities,
                        @Param("propertyTypes") List<String> propertyTypes);
}
