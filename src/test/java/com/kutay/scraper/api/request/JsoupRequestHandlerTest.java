package com.kutay.scraper.api.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.web.util.UriComponentsBuilder;

import com.kutay.scraper.api.response.ApiResponseParser;
import com.kutay.scraper.api.response.JsoupResponseParser;
import com.kutay.scraper.db.entity.site.ApiEndpoint;
import com.kutay.scraper.db.entity.site.ApiParameter;
import com.kutay.scraper.util.Constants.PRODUCT_TYPE;
import com.kutay.scraper.util.Constants.TRADE_TYPE;
import com.kutay.scraper.util.ScraperException;

class JsoupRequestHandlerTest {
    private static final TRADE_TYPE SALE = TRADE_TYPE.SALE;
    private static final PRODUCT_TYPE HOUSE = PRODUCT_TYPE.HOUSE;
    private static final String testProtocol = "testProtocol";
    private static final String testHost = "testHost";
    private static final int testPort = 80;
    private static final String testPath = "testPath";

    private static final String testParameterName = "testParameterName";
    private static final String testParameterApiName = "testParameterApiName";
    private static final String testParameterValue = "testParameterValue";
    private static final String testParameterPrefix = "testParameterPrefix";
    private static final String testParameterSuffix = "testParameterSuffix";
    private static final String testParameterInnerPrefix = "testParameterInnerPrefix";
    private static final String testParameterInnerSuffix = "testParameterInnerSuffix";

    private static final String testParameterName1 = "testParameterName1";
    private static final String testParameterApiName1 = "testParameterApiName1";
    private static final String testParameterPrefix1 = "testParameterPrefix1";
    private static final String testParameterSuffix1 = "testParameterSuffix1";

    private static MockedStatic<Jsoup> mockJsoup;
    private static Connection mockConnection;
    private static JsoupRequestHandler jsoupRequestHandler;

    @BeforeEach
    void createMocks() {
        mockJsoup = Mockito.mockStatic(Jsoup.class);
        mockConnection = Mockito.mock(Connection.class);
        jsoupRequestHandler = new JsoupRequestHandler();
    }

    @AfterEach
    void closeStaticMocks() {
        mockJsoup.close();
    }

    @Test
    void test_buildURL_withParameters() throws ScraperException {
        ApiParameter testParameter = new ApiParameter(testParameterName, testParameterApiName,
                testParameterPrefix,
                testParameterSuffix, testParameterInnerPrefix, testParameterInnerSuffix, ",");
        ApiParameter testParameter1 = new ApiParameter(testParameterName1, testParameterApiName1,
                testParameterPrefix1,
                testParameterSuffix1, null, null, null);

        ApiEndpoint mockAPIEndpoint = new ApiEndpoint(SALE, HOUSE, testProtocol, testHost, testPort, testPath,
                List.of(testParameter, testParameter1));

        Map<String, List<String>> testRequestParameters = new HashMap<>();
        testRequestParameters.put(testParameterName, List.of(testParameterValue));

        ApiRequest testRequest = new ApiRequest(mockAPIEndpoint, testRequestParameters);
        String actualURLString = jsoupRequestHandler.buildURL(testRequest);
        assertEquals(createMockUrlString(true), actualURLString);
    }

    @Test
    void test_buildURL_withoutParameters() throws ScraperException {
        ApiEndpoint mockAPIEndpoint = new ApiEndpoint(SALE, HOUSE, testProtocol, testHost, testPort, testPath, null);

        ApiRequest mockAPIRequest = new ApiRequest(mockAPIEndpoint, null);
        String actualURLString = jsoupRequestHandler.buildURL(mockAPIRequest);
        assertEquals(createMockUrlString(false), actualURLString);
    }

    @Test
    void test_buildURL_throwsWhen_UriComponentsBuilderThrows() throws ScraperException {
        ApiEndpoint mockAPIEndpoint = new ApiEndpoint(SALE, HOUSE, null, null, null, null, null);

        MockedStatic<UriComponentsBuilder> mockUriComponentsBuilder = Mockito.mockStatic(UriComponentsBuilder.class);
        mockUriComponentsBuilder.when(() -> UriComponentsBuilder.newInstance()).thenThrow(new RuntimeException());

        ApiRequest mockAPIRequest = new ApiRequest(mockAPIEndpoint, null);
        assertThrowsExactly(ScraperException.class, () -> jsoupRequestHandler.buildURL(mockAPIRequest),
                String.format(JsoupRequestHandler.COULD_NOT_CONVERT_TO_URL, mockAPIEndpoint));

        mockUriComponentsBuilder.close();
    }

    @Test
    void test_handle() throws ScraperException, IOException {
        Document expectedDocument = new Document("mock base uri");

        ApiEndpoint mockAPIEndpoint = new ApiEndpoint(SALE, HOUSE, testProtocol, testHost, testPort, testPath, null);
        ApiRequest mockAPIRequest = new ApiRequest(mockAPIEndpoint, null);

        mockJsoup.when(() -> Jsoup.connect(createMockUrlString(false))).thenReturn(mockConnection);
        when(mockConnection.cookie("language-preference", JsoupResponseParser.LANGUAGE)).thenReturn(mockConnection);
        when(mockConnection.get()).thenReturn(expectedDocument);

        ApiResponseParser apiResponseParser = jsoupRequestHandler.handle(mockAPIRequest);

        assertEquals(JsoupResponseParser.class, apiResponseParser.getClass());
        assertEquals(expectedDocument, apiResponseParser.getResponse());
    }

    @Test
    void test_handle_throwsWhen_JsoupConnectFails() throws IOException {
        ApiEndpoint mockAPIEndpoint = new ApiEndpoint(SALE, HOUSE, testProtocol, testHost, testPort, testPath, null);
        ApiRequest mockAPIRequest = new ApiRequest(mockAPIEndpoint, null);

        mockJsoup.when(() -> Jsoup.connect(createMockUrlString(false))).thenThrow(new RuntimeException());

        assertThrowsExactly(ScraperException.class,
                () -> jsoupRequestHandler.handle(mockAPIRequest),
                String.format(JsoupRequestHandler.COULD_NOT_GET_DOCUMENT, createMockUrlString(false)));
    }

    @Test
    void test_handle_throwsWhen_JsoupGetFails() throws IOException {
        ApiEndpoint mockAPIEndpoint = new ApiEndpoint(SALE, HOUSE, testProtocol, testHost, testPort, testPath, null);
        ApiRequest mockAPIRequest = new ApiRequest(mockAPIEndpoint, null);

        mockJsoup.when(() -> Jsoup.connect(createMockUrlString(false))).thenReturn(mockConnection);
        when(mockConnection.get()).thenThrow(new IOException());

        assertThrowsExactly(ScraperException.class,
                () -> jsoupRequestHandler.handle(mockAPIRequest),
                String.format(JsoupRequestHandler.COULD_NOT_GET_DOCUMENT, createMockUrlString(false)));
    }

    private String createMockUrlString(boolean withMockQueryParameters) {
        StringBuilder urlStringBuilder = new StringBuilder();
        urlStringBuilder.append(testProtocol);
        urlStringBuilder.append("://");
        urlStringBuilder.append(testHost);
        urlStringBuilder.append(":");
        urlStringBuilder.append(testPort);
        urlStringBuilder.append("/");
        urlStringBuilder.append(testPath);

        if (withMockQueryParameters) {
            urlStringBuilder.append("?");
            urlStringBuilder.append(testParameterApiName);
            urlStringBuilder.append("=");
            urlStringBuilder.append(testParameterPrefix);
            urlStringBuilder.append(testParameterInnerPrefix);
            urlStringBuilder.append(testParameterValue);
            urlStringBuilder.append(testParameterInnerSuffix);
            urlStringBuilder.append(testParameterSuffix);
        }
        return urlStringBuilder.toString();
    }
}
