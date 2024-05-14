package com.kutay.scraper.api.request;

import java.util.List;
import java.util.concurrent.Callable;

import com.kutay.scraper.api.response.ApiResponseParser;
import com.kutay.scraper.db.entity.site.ApiResponsePath;

public class PagedSearchApiCall implements Callable<List<ApiRequest>> {
    ApiRequestHandler requestHandler;
    ApiRequest searchRequest;
    List<ApiResponsePath> apiResponsePaths;

    public PagedSearchApiCall(ApiRequestHandler requestHandler, ApiRequest searchRequest,
            List<ApiResponsePath> apiResponsePaths) {
        this.requestHandler = requestHandler;
        this.searchRequest = searchRequest;
        this.apiResponsePaths = apiResponsePaths;
    }

    @Override
    public List<ApiRequest> call() throws Exception {
        ApiResponseParser searchResponseParser = requestHandler.handle(searchRequest);
        searchResponseParser.setApiResponsePaths(apiResponsePaths);
        return searchResponseParser.parseProductApiRequests();
    }

}
