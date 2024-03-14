package com.kutay.scraper.db;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.kutay.scraper.api.response.ApiResponseParser;
import com.kutay.scraper.db.entity.product.ActionHistory;
import com.kutay.scraper.db.entity.product.PriceHistory;
import com.kutay.scraper.db.entity.product.Product;
import com.kutay.scraper.db.entity.product.StatusHistory;
import com.kutay.scraper.db.entity.site.ApiEndpoint;
import com.kutay.scraper.scrape.ScrapedProduct;
import com.kutay.scraper.util.Constants;
import com.kutay.scraper.util.Constants.ACTION_TYPE;
import com.kutay.scraper.util.Constants.PRODUCT_TYPE;
import com.kutay.scraper.util.Constants.TRADE_TYPE;
import com.kutay.scraper.util.ScraperException;

@Service
public abstract class ProductFactory {
    protected static final String ID_IN_SITE_DELIMITER = "_";
    protected static final String CANT_BUILD_ID_IN_SITE = "Cant build idInSite. apiEndpoint: %s";
    protected static final String EMPTY_MANDATORY_FIELD_PARSED = "Parsed value is empty for field. fieldName: %s";
    protected static final String MANDATORY_FIELD_CAN = "Mandatory field could not be parsed. apiEndpoint: %s\n%s";
    protected static final String CANT_PARSE_MANDATORY_FIELD = "Mandatory field could not be parsed. apiEndpoint: %s\n%s";
    protected static final String SKIPPED_PRODUCT = "Product skipped. apiEndpoint: %s\n%s";
    protected static final String CANT_PARSE_FIELD = "Cant parse field. fieldName: %s\n%s";
    protected static final String UNEXPECTED_EXCEPTION_FOR_NON_MANDATORY_FIELD = "Exception thrown for non-mandatory field. apiEndpoint: %s\n%s";
    protected static final String EMPTY_TEXT_FOR_NUMBER = "Number could not be build from empty text. Text: %s";
    protected static final String NO_NUMBER_IN_TEXT = "Number could not be build from text. Text: %s";

    private static final Log logger = LogFactory.getLog(ProductFactory.class);

    protected CrudRepository<?, Long> repository;
    protected TRADE_TYPE tradeType;
    protected PRODUCT_TYPE productType;
    protected String siteName;

    protected ProductFactory(String siteName, TRADE_TYPE tradeType, PRODUCT_TYPE productType,
            CrudRepository<?, Long> repository) {
        this.siteName = siteName;
        this.productType = productType;
        this.tradeType = tradeType;
        this.repository = repository;
    }

    public CrudRepository<?, Long> getRepository() {
        return repository;
    }

    public abstract ScrapedProduct create(ApiEndpoint apiEndpoint, ApiResponseParser apiResponseParser);

    public abstract void updateProducts(List<ScrapedProduct> scrapedProducts,
            Map<String, List<String>> requestParameters);

    protected Product createProduct(ApiEndpoint apiEndpoint, ApiResponseParser apiResponseParser) {
        String idInSite = null;
        BigDecimal price = null;
        String name = null;
        String description = null;
        String status = null;
        List<ApiEndpoint> imageEndpoints = null;
        try {
            idInSite = buildIdInSite(apiEndpoint);
            price = (BigDecimal) getFieldValue(apiResponseParser, "price", BigDecimal.class, true);
        } catch (ScraperException e) {
            logger.warn(String.format(SKIPPED_PRODUCT, apiEndpoint, e.getMessage()));
            return null;
        }

        try {
            name = (String) getFieldValue(apiResponseParser, "name", String.class, false);
            description = (String) getFieldValue(apiResponseParser, "description", String.class, false);
            status = (String) getFieldValue(apiResponseParser, "status", String.class, false);
            imageEndpoints = getEndpointValues(apiResponseParser, "images", false);
        } catch (ScraperException e) {
            logger.warn(String.format(UNEXPECTED_EXCEPTION_FOR_NON_MANDATORY_FIELD, apiEndpoint, e.getMessage()));
        }

        return new Product(idInSite, name, tradeType, productType, siteName, description, price,
                status, apiEndpoint, null, null, null, null, imageEndpoints);
    }

    protected String buildIdInSite(ApiEndpoint apiEndpoint) {
        StringBuilder result = new StringBuilder();
        result.append(apiEndpoint.getProtocol());
        result.append(ID_IN_SITE_DELIMITER);
        result.append(apiEndpoint.getHost());
        result.append(ID_IN_SITE_DELIMITER);
        result.append(apiEndpoint.getPort());
        result.append(ID_IN_SITE_DELIMITER);
        result.append(apiEndpoint.getPath());
        return result.toString();
    }

    protected Object getFieldValue(ApiResponseParser apiResponseParser, String fieldName, Class type,
            boolean mandatory) throws ScraperException {
        try {
            String parsedString = apiResponseParser.parseField(fieldName);
            if (type == Integer.class) {
                return Integer.parseInt(getFirstNumberIn(parsedString));
            } else if (type == BigDecimal.class) {
                return new BigDecimal(getFirstNumberIn(parsedString));
            } else if (type == Boolean.class) {
                return Boolean.parseBoolean(parsedString);
            }

            if (StringUtils.hasText(parsedString)) {
                return parsedString;
            } else {
                if (mandatory) {
                    throw new ScraperException(String.format(EMPTY_MANDATORY_FIELD_PARSED, fieldName));
                }
            }
        } catch (ScraperException ex) {
            if (mandatory) {
                throw ex;
            }
            logger.warn(String.format(CANT_PARSE_FIELD, fieldName, ex.getMessage()));
        } catch (Exception e) {
            if (mandatory) {
                throw new ScraperException(String.format(CANT_PARSE_MANDATORY_FIELD, fieldName, e.getMessage()));
            }
            logger.warn(String.format(CANT_PARSE_FIELD, fieldName, e.getMessage()));
        }

        return null;
    }

    protected List<ApiEndpoint> getEndpointValues(ApiResponseParser apiResponseParser, String fieldName,
            boolean mandatory) throws ScraperException {
        try {
            List<ApiEndpoint> parsedEndpoints = apiResponseParser.parseEndpoints(fieldName);

            if (!parsedEndpoints.isEmpty()) {
                return parsedEndpoints;
            } else {
                if (mandatory) {
                    throw new ScraperException(String.format(EMPTY_MANDATORY_FIELD_PARSED, fieldName));
                }
            }
        } catch (ScraperException ex) {
            if (mandatory) {
                throw ex;
            }
            logger.warn(String.format(CANT_PARSE_FIELD, fieldName, ex.getMessage()));
        } catch (Exception e) {
            if (mandatory) {
                throw new ScraperException(String.format(CANT_PARSE_MANDATORY_FIELD, fieldName, e.getMessage()));
            }
            logger.warn(String.format(CANT_PARSE_FIELD, fieldName, e.getMessage()));
        }

        return null;
    }

    protected String getFirstNumberIn(String text) throws ScraperException {
        if (!StringUtils.hasText(text)) {
            throw new ScraperException(String.format(EMPTY_TEXT_FOR_NUMBER, text));
        }

        StringBuilder numberBuilder = new StringBuilder();
        boolean digitStarted = false;
        for (char c : text.toCharArray()) {
            if (Character.isDigit(c)) {
                numberBuilder.append(c);
                digitStarted = true;
            } else if (digitStarted && c == '.') {
                numberBuilder.append(c);
            } else if (digitStarted && c != ',') {
                break;
            }
        }

        if (numberBuilder.length() != 0) {
            return numberBuilder.toString();
        } else {
            throw new ScraperException(String.format(NO_NUMBER_IN_TEXT, text));
        }
    }

    protected void updateWithScrapedValues(Product product, Product scrapedProduct) {
        product.setName(scrapedProduct.getName());
        product.setDescription(scrapedProduct.getDescription());
        product.setImageApiEndpoints(scrapedProduct.getImageApiEndpoints());

        BigDecimal newPrice = scrapedProduct.getPrice();
        if ((product.getPrice() == null && newPrice != null)
                || (product.getPrice() != null && product.getPrice().compareTo(newPrice) != 0)) {
            product.getPriceHistory().add(new PriceHistory(LocalDateTime.now(), product.getPrice()));
            product.setPrice(newPrice);
        }

        String newStatus = scrapedProduct.getStatus();
        if ((product.getStatus() == null && newStatus != null)
                || (product.getStatus() != null && !product.getStatus().equals(newStatus))) {
            product.getStatusHistory().add(new StatusHistory(LocalDateTime.now(), product.getStatus()));
            product.setStatus(newStatus);
        }
    }

    protected void remove(Product product) {
        product.getStatusHistory()
                .add(new StatusHistory(LocalDateTime.now(), product.getStatus()));
        product.setStatus(Constants.PRODUCT_STATE.REMOVED.name());
    }

    protected void unfollow(Product product, String description) {
        Optional<ActionHistory> unfollowedActionHistory = product.getActionHistory()
                .stream()
                .filter(history -> history.getType().equals(ACTION_TYPE.UNFOLLOWED)).findAny();

        if (!unfollowedActionHistory.isPresent()) {
            product.getActionHistory()
                    .add(new ActionHistory(Constants.ACTION_TYPE.UNFOLLOWED,
                            LocalDateTime.now(),
                            description));
        }
    }
}
