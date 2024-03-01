package com.kutay.scraper.db;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kutay.scraper.api.response.ApiResponseParser;
import com.kutay.scraper.db.entity.product.House;
import com.kutay.scraper.db.entity.product.Product;
import com.kutay.scraper.db.entity.site.ApiEndpoint;
import com.kutay.scraper.db.repo.HouseRepo;
import com.kutay.scraper.scrape.ScrapedProduct;
import com.kutay.scraper.util.Constants;
import com.kutay.scraper.util.Constants.PRODUCT_TYPE;
import com.kutay.scraper.util.Constants.TRADE_TYPE;
import com.kutay.scraper.util.ScraperException;

public class HouseFactory extends ProductFactory {
    private static final Log logger = LogFactory.getLog(HouseFactory.class);

    public HouseFactory(String siteName, TRADE_TYPE tradeType, HouseRepo houseRepo) {
        super(siteName, tradeType, PRODUCT_TYPE.HOUSE, houseRepo);
    }

    @Override
    public House create(ApiEndpoint apiEndpoint, ApiResponseParser apiResponseParser) {

        Product product = createProduct(apiEndpoint, apiResponseParser);

        if (product == null) {
            return null;
        }

        String city = null;
        String postalCode = null;
        try {
            city = (String) getFieldValue(apiResponseParser, "city", String.class, true);
            postalCode = (String) getFieldValue(apiResponseParser, "postalCode", String.class, true);
        } catch (ScraperException e) {
            logger.error(String.format(CANT_PARSE_MANDATORY_FIELD, apiEndpoint, e.getMessage()));
            return null;
        }

        String energyLabel = null;
        String propertyType = null;
        Integer constructionYear = null;
        Integer livingArea = null;
        Integer story = null;
        Integer totalStory = null;
        Integer bedroom = null;
        Integer totalBedroom = null;
        Boolean furnished = null;
        BigDecimal serviceCost = null;
        try {
            energyLabel = (String) getFieldValue(apiResponseParser, "energyLabel", String.class, false);
            propertyType = (String) getFieldValue(apiResponseParser, "propertyType", String.class, false);
            constructionYear = (Integer) getFieldValue(apiResponseParser, "constructionYear", Integer.class,
                    false);
            livingArea = (Integer) getFieldValue(apiResponseParser, "livingArea", Integer.class, false);
            story = (Integer) getFieldValue(apiResponseParser, "story", Integer.class, false);
            totalStory = (Integer) getFieldValue(apiResponseParser, "totalStory", Integer.class, false);
            bedroom = (Integer) getFieldValue(apiResponseParser, "bedroom", Integer.class, false);
            totalBedroom = (Integer) getFieldValue(apiResponseParser, "totalBedroom", Integer.class, false);
            furnished = (Boolean) getFieldValue(apiResponseParser, "furnished", Boolean.class, false);
            serviceCost = (BigDecimal) getFieldValue(apiResponseParser, "serviceCost", BigDecimal.class, false);
        } catch (ScraperException e) {
            logger.error(String.format(UNEXPECTED_EXCEPTION_FOR_NON_MANDATORY_FIELD, apiEndpoint, e.getMessage()));
        }

        return new House(city, postalCode, energyLabel, propertyType, livingArea, constructionYear, story,
                totalStory, bedroom, totalBedroom, furnished, serviceCost,
                product);
    }

    @Override
    public void updateProducts(List<ScrapedProduct> scrapedProducts, Map<String, List<String>> requestParameters) {
        logger.info(String.format("Scraped House Count: %s", scrapedProducts.size()));
        saveOrUpdate(scrapedProducts);
        remove(scrapedProducts, requestParameters);
    }

    protected void saveOrUpdate(List<ScrapedProduct> scrapedHouses) {
        scrapedHouses.stream().forEach(scraped -> {
            House scrapedHouse = (House) scraped;
            Product scrapedProduct = scrapedHouse.getProduct();
            Optional<House> houseOpt = ((HouseRepo) repository)
                    .findHouse(scrapedProduct.getIdInSite(), scrapedProduct.getSiteName(),
                            scrapedProduct.getTradeType(), scrapedProduct.getProductType());
            if (houseOpt.isPresent()) {
                updateWithScrapedValues(houseOpt.get(), scrapedHouse);
            } else {
                ((HouseRepo) repository).save(scrapedHouse);
            }
        });
    }

    protected void remove(List<ScrapedProduct> scrapedProducts, Map<String, List<String>> requestParameters) {
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        List<String> cities = null;
        List<String> propertyTypes = null;

        if (requestParameters != null) {
            List<String> prices = requestParameters.get("prices");
            if (prices != null && !prices.isEmpty()) {
                minPrice = new BigDecimal(prices.get(0));

                if (prices.size() > 1) {
                    maxPrice = new BigDecimal(prices.get(1));
                }
            }
            cities = requestParameters.get("cities");
            propertyTypes = requestParameters.get("propertyTypes");
        }

        ((HouseRepo) repository).searchProduct(
                siteName, tradeType, productType,
                minPrice, maxPrice, cities, propertyTypes)
                .stream()
                .forEach(house -> {
                    if (scrapedProducts.stream()
                            .filter(scraped -> ((House) scraped).getProduct().getIdInSite()
                                    .equals(house.getProduct().getIdInSite()))
                            .findAny().isEmpty()) {
                        remove(house.getProduct());
                        unfollow(house.getProduct(), Constants.PRODUCT_STATE.REMOVED.name());
                    }
                });
    }

    protected void updateWithScrapedValues(House house,
            House scrapedHouse) {
        super.updateWithScrapedValues(house.getProduct(), scrapedHouse.getProduct());

        house.setServiceCost(scrapedHouse.getServiceCost());
        house.setCity(scrapedHouse.getCity());
        house.setPostalCode(scrapedHouse.getPostalCode());
        house.setEnergyLabel(scrapedHouse.getEnergyLabel());
        house.setPropertyType(scrapedHouse.getPropertyType());
        house.setLivingArea(scrapedHouse.getLivingArea());
        house.setConstructionYear(scrapedHouse.getConstructionYear());
        house.setStory(scrapedHouse.getStory());
        house.setTotalStory(scrapedHouse.getTotalStory());
        house.setBedroom(scrapedHouse.getBedroom());
        house.setTotalRoom(scrapedHouse.getTotalRoom());
    }
}
