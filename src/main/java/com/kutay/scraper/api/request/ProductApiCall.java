package com.kutay.scraper.api.request;

import java.util.List;
import java.util.concurrent.Callable;

import com.kutay.scraper.api.response.ApiResponseParser;
import com.kutay.scraper.db.ProductFactory;
import com.kutay.scraper.db.entity.site.ApiResponsePath;
import com.kutay.scraper.scrape.ScrapedProduct;

public class ProductApiCall implements Callable<ScrapedProduct> {
    ApiRequestHandler requestHandler;
    ApiRequest productRequest;
    List<ApiResponsePath> apiResponsePaths;
    ProductFactory productFactory;

    public ProductApiCall(ApiRequestHandler requestHandler, ApiRequest productRequest,
            List<ApiResponsePath> apiResponsePaths, ProductFactory productFactory) {
        this.requestHandler = requestHandler;
        this.productRequest = productRequest;
        this.apiResponsePaths = apiResponsePaths;
        this.productFactory = productFactory;
    }

    @Override
    public ScrapedProduct call() throws Exception {
        ApiResponseParser productResponseParser = requestHandler.handle(productRequest);
        productResponseParser.setApiResponsePaths(apiResponsePaths);
        return productFactory.create(productRequest.getApiEndpoint(), productResponseParser);
    }

}
