package com.kutay.scraper.api.request;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.kutay.scraper.api.response.ApiResponseParser;
import com.kutay.scraper.api.response.JsoupResponseParser;
import com.kutay.scraper.db.entity.site.ApiEndpoint;
import com.kutay.scraper.db.entity.site.ApiParameter;
import com.kutay.scraper.util.ScraperException;

public class JsoupRequestHandler implements ApiRequestHandler {
    protected static final String COULD_NOT_GET_DOCUMENT = "Can not get document for URL: %s";
    public static final String COULD_NOT_CONVERT_TO_URL = "API Endpoint could not be converted to URL. APIEndpoint: %s";

    @Override
    public ApiResponseParser handle(ApiRequest request) throws ScraperException {
        Document document = null;
        String url = buildURL(request);
        try {
            document = Jsoup.connect(url).cookie("language-preference", JsoupResponseParser.LANGUAGE).get();
        } catch (Exception ex) {
            throw new ScraperException(String.format(COULD_NOT_GET_DOCUMENT,
                    url), ex);
        }
        return new JsoupResponseParser(document);
    }

    protected String buildURL(ApiRequest request) throws ScraperException {
        ApiEndpoint apiEndpoint = request.getApiEndpoint();
        try {
            return UriComponentsBuilder.newInstance()
                    .scheme(apiEndpoint.getProtocol())
                    .host(apiEndpoint.getHost())
                    .port(apiEndpoint.getPort() != null ? apiEndpoint.getPort() : -1)
                    .path(apiEndpoint.getPath())
                    .queryParams(
                            generateQueryParameters((Map<String, List<String>>) request.getRequest(),
                                    apiEndpoint.getParameters()))
                    .build()
                    .toUriString();
        } catch (Exception ex) {
            throw new ScraperException(String.format(COULD_NOT_CONVERT_TO_URL, apiEndpoint), ex);
        }
    }

    protected MultiValueMap<String, String> generateQueryParameters(Map<String, List<String>> requestParameters,
            List<ApiParameter> apiParameters) {
        MultiValueMap<String, String> result = new LinkedMultiValueMap<>();

        if (requestParameters != null) {
            for (Map.Entry<String, List<String>> requestParameter : requestParameters.entrySet()) {
                Optional<ApiParameter> apiParameterOpt = apiParameters.stream()
                        .filter(apiParameter -> apiParameter.getName().equalsIgnoreCase(requestParameter.getKey()))
                        .findAny();

                if (apiParameterOpt.isPresent()) {
                    result.add(apiParameterOpt.get().getApiName(),
                            prefixSuffixRequestParameterValues(requestParameter.getValue(), apiParameterOpt.get()));
                }
            }
        }
        return result;
    }

    private String prefixSuffixRequestParameterValues(List<String> requestParameterValues, ApiParameter apiParameter) {
        StringBuilder resultBuilder = new StringBuilder();

        if (StringUtils.hasText(apiParameter.getPrefix())) {
            resultBuilder.append(apiParameter.getPrefix());
        }

        if (requestParameterValues != null && !requestParameterValues.isEmpty()) {
            List<String> appendedParamValues = prefixSuffixValues(requestParameterValues, apiParameter.getInnerPrefix(),
                    apiParameter.getInnerSuffix());
            if (appendedParamValues != null && !appendedParamValues.isEmpty()) {
                if (apiParameter.getDelimiter() != null) {
                    resultBuilder.append(String.join(apiParameter.getDelimiter(), appendedParamValues));
                } else {
                    resultBuilder.append(String.join("", appendedParamValues));
                }
            }
        }

        if (StringUtils.hasText(apiParameter.getSuffix())) {
            resultBuilder.append(apiParameter.getSuffix());
        }

        return resultBuilder.toString();
    }

    private List<String> prefixSuffixValues(List<String> values, String prefix, String suffix) {
        return values.stream().map(value -> {
            StringBuilder result = new StringBuilder();
            if (StringUtils.hasText(value)) {
                if (StringUtils.hasText(prefix)) {
                    result.append(prefix);
                }

                result.append(value);

                if (StringUtils.hasText(suffix)) {
                    result.append(suffix);
                }
                return result.toString();
            } else {
                return result.append("").toString();
            }
        }).collect(Collectors.toList());
    }
}
