package com.kutay.scraper.scrape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import com.kutay.scraper.api.request.ApiRequest;
import com.kutay.scraper.api.request.ApiRequestHandler;
import com.kutay.scraper.api.response.ApiResponseParser;
import com.kutay.scraper.db.ProductFactory;
import com.kutay.scraper.db.entity.site.ApiEndpoint;
import com.kutay.scraper.db.entity.site.ApiParameter;
import com.kutay.scraper.db.entity.site.Site;
import com.kutay.scraper.util.Constants.PRODUCT_TYPE;
import com.kutay.scraper.util.Constants.TRADE_TYPE;
import com.kutay.scraper.util.ScraperException;

public class SearchScraper extends Scraper {
    public static final Log logger = LogFactory.getLog(SearchScraper.class);
    public static final String SITE_NOT_FOUND_BY_NAME = "Site can not be found by name: %s";
    public static final String LAST_PAGE_NO_FIELD_NAME = "lastPageNo";
    public static final String PAGING_API_PARAMETER_NAME = "searchPagination";
    public static final String PRODUCT_API_REQUEST_COULD_NOT_BE_HANDLED = "Product API Request could not be handled APIRequest: %s";
    public static final String PRODUCT_REQUESTS_PER_PAGE = "%s product requests for page %s";

    public SearchScraper(TRADE_TYPE tradeType, PRODUCT_TYPE productType, Site site, ApiRequestHandler apiRequestHandler,
            ProductFactory productFactory) {
        super(tradeType, productType, site, apiRequestHandler, productFactory);
    }

    @Override
    public void scrape(Map<String, List<String>> requestParameters) throws ScraperException {
        List<ApiRequest> totalProductRequests = new ArrayList<>();

        ApiEndpoint searchEndpoint = findSiteEndpoint();
        ApiResponseParser searchResponseParser = handleSearchRequest(
                new ApiRequest(searchEndpoint, requestParameters));

        List<ApiRequest> productRequests = searchResponseParser.parseProductApiRequests();
        if (productRequests != null) {
            logger.info(String.format(PRODUCT_REQUESTS_PER_PAGE, productRequests.size(), 1));
            totalProductRequests.addAll(productRequests);
        }

        productRequests = handlePaging(searchEndpoint, searchResponseParser, requestParameters);
        if (productRequests != null) {
            totalProductRequests.addAll(productRequests);
        }

        if (!totalProductRequests.isEmpty()) {
            productFactory.updateProducts(scrapeProducts(totalProductRequests), requestParameters);
        }
    }

    protected ApiResponseParser handleSearchRequest(ApiRequest searchAPIRequest) throws ScraperException {
        ApiResponseParser searchResponseParser = requestHandler.handle(searchAPIRequest);
        searchResponseParser.setApiResponsePaths(site.getApiResponsePaths());
        return searchResponseParser;
    }

    protected List<ScrapedProduct> scrapeProducts(List<ApiRequest> requests) {
        return requests.stream().map(request -> {
            ApiResponseParser responseParser = null;
            try {
                responseParser = requestHandler.handle(request);
                responseParser.setApiResponsePaths(site.getApiResponsePaths());
            } catch (ScraperException e) {
                logger.warn(String.format(PRODUCT_API_REQUEST_COULD_NOT_BE_HANDLED, request), e);
                return null;
            }

            try {
                return productFactory.create(request.getApiEndpoint(), responseParser);
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    protected List<ApiRequest> handlePaging(ApiEndpoint searchEndpoint, ApiResponseParser searchResponseParser,
            Map<String, List<String>> requestParameters) throws ScraperException {
        if (searchEndpoint.getParameters() != null && !searchEndpoint.getParameters().isEmpty()) {
            Optional<ApiParameter> pagingParamOpt = searchEndpoint.getParameters().stream()
                    .filter(param -> param.getName().equalsIgnoreCase(PAGING_API_PARAMETER_NAME)).findAny();

            if (pagingParamOpt.isPresent()) {
                return scrapeWithPaging(searchEndpoint, searchResponseParser, requestParameters);
            }
        }
        return Collections.emptyList();
    }

    protected List<ApiRequest> scrapeWithPaging(ApiEndpoint searchEndpoint, ApiResponseParser searchResponseParser,
            Map<String, List<String>> requestParameters) throws ScraperException {
        List<ApiRequest> productRequests = new ArrayList<>();
        String lastPageNoStr = searchResponseParser.parseField(LAST_PAGE_NO_FIELD_NAME);

        if (StringUtils.hasText(lastPageNoStr)) {
            int lastPageNo = Integer.parseInt(lastPageNoStr);
            if (lastPageNo >= 2) {
                Map<String, List<String>> parameters = new HashMap<>();
                if (requestParameters != null) {
                    parameters.putAll(requestParameters);
                }

                ApiRequest searchRequest = null;
                List<ApiRequest> requests = new ArrayList<>();
                for (int i = 2; i <= lastPageNo; i++) {
                    parameters.put(PAGING_API_PARAMETER_NAME, List.of(String.valueOf(i)));

                    searchRequest = new ApiRequest(searchEndpoint, parameters);
                    searchResponseParser = requestHandler.handle(searchRequest);
                    searchResponseParser.setApiResponsePaths(site.getApiResponsePaths());

                    requests = searchResponseParser.parseProductApiRequests();
                    logger.info(String.format(PRODUCT_REQUESTS_PER_PAGE, requests.size(), i));
                    productRequests.addAll(requests);
                }
            }
        }

        return productRequests;
    }
}
