package com.kutay.scraper.api.response;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.kutay.scraper.api.request.ApiRequest;
import com.kutay.scraper.db.entity.site.ApiEndpoint;
import com.kutay.scraper.db.entity.site.ApiResponseFunction;
import com.kutay.scraper.db.entity.site.ApiResponsePath;
import com.kutay.scraper.util.Constants.API_FUNCTION_INDEX_TYPE;
import com.kutay.scraper.util.ScraperException;

public class JsoupResponseParser extends ApiResponseParser {
    private static final Log logger = LogFactory.getLog(JsoupResponseParser.class);

    public static final String PRODUCT_API_ENDPOINTS_FIELD_NAME = "productAPIEndpoints";
    public static final String PRODUCT_LIST_PAGING_FIELD_NAME = "productListPagination";
    public static final String LANGUAGE = "en";
    protected static final String NO_API_RESPONSE_FUNCTION = "No APIResponseFunction for APIResponsePath: %s";
    protected static final String WRONG_API_RESPONSE_FUNCTION_TYPE = "Wrong APIResponseFunction type: %s";
    protected static final String API_RESPONSE_FUNCTION_COULD_NOT_BE_APPLIED = "APIResponseFunction could not be applied. APIRespoonseFunction: %s /n Element: %s";
    protected static final String ELEMENT_COULD_NOT_BE_FOUND = "Element could not be found.";
    protected static final String ELEMENT_COULD_NOT_BE_PARSED = "Element could not be parsed.";
    protected static final String INVALID_URL = "Invalid URL: %s";
    protected static final String CANNOT_CREATE_PRODUCT_API_REQUEST = "APIRequest could not be created for product. URL: %s";
    protected static final String CANNOT_CREATE_IMAGE_ENDPOINT = "Image endpoint could not be created. URL: %s";

    public JsoupResponseParser(Object response) {
        super(response);
    }

    @Override
    public String parseField(String fieldName) throws ScraperException {
        ApiResponsePath apiResponsePath = findApiResponsePath(fieldName);

        if (StringUtils.hasText(apiResponsePath.getAttribute())) {
            return findString(apiResponsePath,
                    findElement(apiResponsePath).attr(apiResponsePath.getAttribute()));
        }
        return findString(apiResponsePath, findElement(apiResponsePath).text());
    }

    protected Element findElement(ApiResponsePath apiResponsePath) throws ScraperException {
        List<ApiResponseFunction> apiResponseFunctions = apiResponsePath.getApiResponseFunctions();
        if (apiResponseFunctions == null || apiResponseFunctions.isEmpty()) {
            throw new ScraperException(String.format(NO_API_RESPONSE_FUNCTION, apiResponsePath));
        }

        Element currentElement = (Document) getResponse();
        try {
            for (ApiResponseFunction apiResponseFunction : apiResponseFunctions) {
                currentElement = getAPIElement(apiResponseFunction, currentElement);
            }
        } catch (Exception e) {
            if (apiResponsePath.getAlternatives() != null && !apiResponsePath.getAlternatives().isEmpty()) {
                currentElement = applyAlternativeFunctions(apiResponsePath);
            } else {
                throw e;
            }
        }

        return currentElement;
    }

    protected Element applyAlternativeFunctions(ApiResponsePath apiResponsePath)
            throws ScraperException {
        for (ApiResponsePath currentPath : apiResponsePath.getAlternatives()) {
            List<ApiResponseFunction> apiResponseFunctions = currentPath.getApiResponseFunctions();

            if (apiResponseFunctions == null || apiResponseFunctions.isEmpty()) {
                throw new ScraperException(String.format(NO_API_RESPONSE_FUNCTION, apiResponsePath));
            }

            Element currentElement = (Document) getResponse();
            try {
                for (ApiResponseFunction apiResponseFunction : apiResponseFunctions) {
                    currentElement = getAPIElement(apiResponseFunction, currentElement);
                }
            } catch (Exception e) {
                continue;
            }
            return currentElement;
        }

        throw new ScraperException(String.format(ELEMENT_COULD_NOT_BE_PARSED));
    }

    protected Element getAPIElement(ApiResponseFunction apiResponseFunction, Element apiElement)
            throws ScraperException {
        Elements elems = null;
        switch (apiResponseFunction.getFunctionType()) {
            case BY_ATTRIBUTE:
                elems = apiElement.getElementsByAttribute(apiResponseFunction.getKeyParameter());
                break;
            case BY_ATTRIBUTE_VALUE:
                elems = apiElement.getElementsByAttributeValue(apiResponseFunction.getKeyParameter(),
                        apiResponseFunction.getValueParameter());
                break;
            case BY_CLASS:
                elems = apiElement.getElementsByClass(apiResponseFunction.getKeyParameter());
                break;
            case BY_TAG:
                elems = apiElement.getElementsByTag(apiResponseFunction.getKeyParameter());
                break;
            case BY_OWN_TEXT:
                elems = apiElement.getElementsMatchingOwnText(apiResponseFunction.getKeyParameter());
                break;
            case NEXT_SIBLING:
                elems = new Elements(apiElement.nextElementSibling());
                break;
            case SET_TEXT_IF_TAG:
                if (apiResponseFunction.getKeyParameter().equalsIgnoreCase(apiElement.tagName())) {
                    elems = new Elements(new Element("P").appendText(apiResponseFunction.getValueParameter()));
                }
                break;
            case CHILDREN:
                elems = apiElement.children();
        }

        if (elems == null || elems.isEmpty()) {
            throw new ScraperException(
                    String.format(ELEMENT_COULD_NOT_BE_FOUND));
        }

        if (apiResponseFunction.getResultIndexType() == API_FUNCTION_INDEX_TYPE.FROM_END) {
            return elems.get(elems.size() - apiResponseFunction.getResultIndex() - 1);
        }

        return elems.get(apiResponseFunction.getResultIndex());
    }

    @Override
    public List<String> parseFields(String fieldName)
            throws ScraperException {
        ApiResponsePath apiResponsePath = findApiResponsePath(fieldName);

        if (StringUtils.hasText(apiResponsePath.getAttribute())) {
            return findElements(apiResponsePath).stream()
                    .map(element -> findString(apiResponsePath, element.attr(apiResponsePath.getAttribute())))
                    .collect(Collectors.toList());
        }

        return findElements(apiResponsePath).stream()
                .map(element -> findString(apiResponsePath, element.text()))
                .collect(Collectors.toList());
    }

    protected Elements findElements(ApiResponsePath apiResponsePath) throws ScraperException {
        List<ApiResponseFunction> apiResponseFunctions = apiResponsePath.getApiResponseFunctions();
        if (apiResponseFunctions == null || apiResponseFunctions.isEmpty()) {
            throw new ScraperException(String.format(NO_API_RESPONSE_FUNCTION, apiResponsePath));
        }

        Elements elements = getAPIElements(apiResponsePath.getApiResponseFunctions().get(0), (Document) getResponse());

        Elements result = new Elements();
        for (Element element : elements) {
            Element currentElement = element;
            for (int i = 1; i < apiResponseFunctions.size(); i++) {
                currentElement = getAPIElement(apiResponseFunctions.get(i), currentElement);
            }
            result.add(currentElement);
        }

        return result;
    }

    protected Elements getAPIElements(ApiResponseFunction apiResponseFunction, Element apiElement)
            throws ScraperException {

        switch (apiResponseFunction.getFunctionType()) {
            case BY_ATTRIBUTE_VALUE:
                return apiElement.getElementsByAttributeValue(apiResponseFunction.getKeyParameter(),
                        apiResponseFunction.getValueParameter());
            case BY_ATTRIBUTE:
                return apiElement.getElementsByAttribute(apiResponseFunction.getKeyParameter());
            case BY_CLASS:
                return apiElement.getElementsByClass(apiResponseFunction.getKeyParameter());
            case BY_TAG:
                return apiElement.getElementsByTag(apiResponseFunction.getKeyParameter());
            case BY_OWN_TEXT:
                return apiElement.getElementsMatchingOwnText(apiResponseFunction.getKeyParameter());
            default:
                throw new ScraperException(
                        String.format(WRONG_API_RESPONSE_FUNCTION_TYPE, apiResponseFunction.getFunctionType()));
        }
    }

    @Override
    public List<ApiRequest> parseProductApiRequests()
            throws ScraperException {
        List<String> productURLs = parseFields(PRODUCT_API_ENDPOINTS_FIELD_NAME);

        return productURLs.stream().map(urlStr -> {
            try {
                return new ApiRequest(createEndpoint(urlStr, LANGUAGE), null);
            } catch (ScraperException ex) {
                logger.warn(String.format(CANNOT_CREATE_PRODUCT_API_REQUEST, urlStr, ex.getMessage()));
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public List<ApiEndpoint> parseEndpoints(String fieldName) throws ScraperException {
        List<String> parsedUrls = parseFields(fieldName);

        return parsedUrls.stream().map(urlStr -> {
            try {
                return createEndpoint(urlStr, null);
            } catch (ScraperException ex) {
                logger.warn(String.format(CANNOT_CREATE_IMAGE_ENDPOINT, urlStr, ex.getMessage()));
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private ApiEndpoint createEndpoint(String urlStr, String language)
            throws ScraperException {
        if (isValidUrl(urlStr)) {
            UriComponents uriComponents = UriComponentsBuilder.fromUriString(urlStr).build();
            if (language == null || uriComponents.getPath().startsWith(language + "/")) {
                return new ApiEndpoint(null, null, uriComponents.getScheme(), uriComponents.getHost(),
                        uriComponents.getPort(), uriComponents.getPath(), null);
            } else {
                return new ApiEndpoint(null, null, uriComponents.getScheme(), uriComponents.getHost(),
                        uriComponents.getPort(), language + uriComponents.getPath(), null);
            }

        } else {
            throw new ScraperException(
                    String.format(INVALID_URL, urlStr));
        }
    }

    protected boolean isValidUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
