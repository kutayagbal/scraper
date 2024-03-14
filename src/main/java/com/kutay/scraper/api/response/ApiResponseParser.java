package com.kutay.scraper.api.response;

import java.util.List;
import java.util.Optional;

import com.kutay.scraper.api.request.ApiRequest;
import com.kutay.scraper.db.entity.site.ApiEndpoint;
import com.kutay.scraper.db.entity.site.ApiResponsePath;
import com.kutay.scraper.db.entity.site.ApiStringFunction;
import com.kutay.scraper.util.ScraperException;

public abstract class ApiResponseParser {
    protected static final String API_RESPONSE_PATH_NOT_FOUND = "Api response path could not be found for fieldName: %s";

    private Object response;
    private List<ApiResponsePath> apiResponsePaths;

    protected ApiResponseParser(Object response) {
        this.response = response;
    }

    public abstract String parseField(String fieldName)
            throws ScraperException;

    public abstract List<String> parseFields(String fieldName)
            throws ScraperException;

    public abstract List<ApiRequest> parseProductApiRequests()
            throws ScraperException;

    public abstract List<ApiEndpoint> parseEndpoints(String fieldName) throws ScraperException;

    protected ApiResponsePath findApiResponsePath(String fieldName) throws ScraperException {
        if (apiResponsePaths != null && !apiResponsePaths.isEmpty()) {
            Optional<ApiResponsePath> apiResponsePathOpt = apiResponsePaths.stream()
                    .filter(dp -> dp.getName().equals(fieldName))
                    .findAny();
            if (apiResponsePathOpt.isPresent()) {
                return apiResponsePathOpt.get();
            }
        }

        throw new ScraperException(String.format(API_RESPONSE_PATH_NOT_FOUND, fieldName));
    }

    protected String findString(ApiResponsePath apiResponsePath, String text) {
        List<ApiStringFunction> apiStringFunctions = apiResponsePath.getApiStringFunctions();
        if (apiStringFunctions == null || apiStringFunctions.isEmpty()) {
            return text.trim();
        }

        String currentString = text;
        for (ApiStringFunction apiStringFunction : apiStringFunctions) {
            currentString = getApiString(apiStringFunction, currentString);
        }
        return currentString;
    }

    protected String getApiString(ApiStringFunction apiStringFunction, String apiString) {
        String result = apiString;
        switch (apiStringFunction.getFunctionType()) {
            case SPLIT:
                String[] strArr = apiString.split(apiStringFunction.getKeyParameter());
                if (apiStringFunction.getKeyIndex() != null && apiStringFunction.getKeyIndex() < strArr.length) {
                    result = strArr[apiStringFunction.getKeyIndex()];
                } else {
                    result = strArr[0];
                }
                break;
            case REPLACE:
                result = apiString.replaceAll(apiStringFunction.getKeyParameter(),
                        apiStringFunction.getValueParameter());
                break;
            case SUBSTRING:
                if (apiString.length() > apiStringFunction.getValueIndex()) {
                    result = apiString.substring(apiStringFunction.getKeyIndex(), apiStringFunction.getValueIndex());
                }
        }
        return result.trim();
    }

    public Object getResponse() {
        return response;
    }

    public List<ApiResponsePath> getApiResponsePaths() {
        return apiResponsePaths;
    }

    public void setApiResponsePaths(List<ApiResponsePath> apiResponsePaths) {
        this.apiResponsePaths = apiResponsePaths;
    }

}
