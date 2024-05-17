package com.kutay.scraper.scrape;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import com.kutay.scraper.api.request.ApiRequest;
import com.kutay.scraper.api.request.ApiRequestHandler;
import com.kutay.scraper.api.request.PagedSearchApiCall;
import com.kutay.scraper.api.request.ProductApiCall;
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
        List<Callable<ScrapedProduct>> productApiCalls = new ArrayList<>();

        requests.stream().forEach(request -> productApiCalls
                .add(new ProductApiCall(requestHandler, request, site.getApiResponsePaths(), productFactory)));

        List<Future<ScrapedProduct>> futureProducts = null;
        ExecutorService executor = Executors.newWorkStealingPool();
        try {
            futureProducts = executor.invokeAll(productApiCalls);
        } catch (InterruptedException e) {
            e.getCause().printStackTrace();
            Thread.currentThread().interrupt();
        }

        executor.shutdown();

        return futureProducts.stream().map(f -> {
            try {
                return f.get();
            } catch (InterruptedException e) {
                e.getCause().printStackTrace();
                Thread.currentThread().interrupt();
                return null;
            } catch (ExecutionException e) {
                e.getCause().printStackTrace();
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

                List<Callable<List<ApiRequest>>> pagedSearchApiCalls = new ArrayList<>();
                for (int i = 2; i <= lastPageNo; i++) {
                    pagedSearchApiCalls.add(new PagedSearchApiCall(requestHandler,
                            new ApiRequest(searchEndpoint, parameters), site.getApiResponsePaths()));

                }

                ExecutorService executor = Executors.newWorkStealingPool();
                List<Future<List<ApiRequest>>> futureAPIRequests = null;

                try {
                    futureAPIRequests = executor.invokeAll(pagedSearchApiCalls);
                } catch (InterruptedException e) {
                    e.getCause().printStackTrace();
                    Thread.currentThread().interrupt();
                }

                executor.shutdown();

                productRequests = futureAPIRequests.stream().map(f -> {
                    try {
                        return f.get();
                    } catch (InterruptedException e) {
                        e.getCause().printStackTrace();
                        Thread.currentThread().interrupt();
                        return null;
                    } catch (ExecutionException e) {
                        e.getCause().printStackTrace();
                        return null;
                    }
                }).filter(Objects::nonNull).flatMap(Collection::stream).collect(Collectors.toList());
            }
        }

        return productRequests;
    }
}
