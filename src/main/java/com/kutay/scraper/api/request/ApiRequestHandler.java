package com.kutay.scraper.api.request;

import com.kutay.scraper.api.response.ApiResponseParser;
import com.kutay.scraper.util.ScraperException;

public interface ApiRequestHandler {
    ApiResponseParser handle(ApiRequest request) throws ScraperException;
}
