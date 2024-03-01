package com.kutay.scraper.scrape;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.kutay.scraper.api.request.ApiRequest;
import com.kutay.scraper.api.request.ApiRequestHandler;
import com.kutay.scraper.api.response.ApiResponseParser;
import com.kutay.scraper.api.response.JsoupResponseParser;
import com.kutay.scraper.db.ProductFactory;
import com.kutay.scraper.db.entity.product.House;
import com.kutay.scraper.db.entity.site.ApiEndpoint;
import com.kutay.scraper.db.entity.site.ApiParameter;
import com.kutay.scraper.db.entity.site.ApiResponsePath;
import com.kutay.scraper.db.entity.site.Site;
import com.kutay.scraper.util.Constants.PRODUCT_TYPE;
import com.kutay.scraper.util.Constants.TRADE_TYPE;
import com.kutay.scraper.util.ScraperException;

class SearchScraperTest {
        private static Site mockSite;
        private static ApiRequestHandler mockRequestHandler;
        private static ProductFactory mockProductFactory;
        private static ApiResponseParser mockSearchResponseParser;
        private static ApiResponseParser mockProductResponseParser;
        private static ApiResponseParser mockProductResponseParser1;

        private static SearchScraper searchScraper;
        private static ApiEndpoint testSearchEndpoint;
        private static ApiEndpoint testSearchEndpointWithPagingParam;
        private static ApiEndpoint testProductEndpoint;
        private static ApiEndpoint testProductEndpoint1;
        private static ApiRequest testSearchRequest;
        private static ApiRequest testProductRequest;
        private static ApiRequest testProductRequest1;
        private static ApiParameter testPagingApiParam;

        private static final String testErrorMessage = "testErrorMessage";
        private static final String testSiteName = "testSiteName";
        private static final TRADE_TYPE SALE = TRADE_TYPE.SALE;
        private static final TRADE_TYPE RENT = TRADE_TYPE.RENT;
        private static final PRODUCT_TYPE HOUSE = PRODUCT_TYPE.HOUSE;
        private static final String testSearchProtocol = "testSearchProtocol";
        private static final String testSearchHost = "testSearchHost";
        private static final int testSearchPort = 80;
        private static final String testSearchPath = "testSearchPath";

        private static final String testProductProtocol = "testProductProtocol";
        private static final String testProductHost = "testProductHost";
        private static final int testProductPort = 8080;
        private static final String testProductPath = "testProductPath";

        private static final String testProductProtocol1 = "testProductProtocol1";
        private static final String testProductHost1 = "testProductHost1";
        private static final int testProductPort1 = 8181;
        private static final String testProductPath1 = "testProductPath1";

        private static ScrapedProduct testScrapedProduct = new House("mockCity", "mockPostalCode",
                        "mockEnergyLabel",
                        "mockPropertyType", 10, 1900, 2, 1, 2, 4, null, null, null);
        private static ScrapedProduct testScrapedProduct1 = new House("mockCity1", "mockPostalCode1",
                        "mockEnergyLabel1",
                        "mockPropertyType1", 110, 1901, 1, 1, 1, 1, null, null, null);

        @BeforeEach
        void createMocks() {
                mockSite = Mockito.mock(Site.class);
                mockRequestHandler = Mockito.mock(ApiRequestHandler.class);
                mockProductFactory = Mockito.mock(ProductFactory.class);
                mockSearchResponseParser = Mockito.mock(ApiResponseParser.class);
                mockProductResponseParser = Mockito.mock(ApiResponseParser.class);
                mockProductResponseParser1 = Mockito.mock(ApiResponseParser.class);

                testSearchEndpoint = new ApiEndpoint(SALE, HOUSE, testSearchProtocol,
                                testSearchHost,
                                testSearchPort,
                                testSearchPath,
                                null);

                testPagingApiParam = new ApiParameter(SearchScraper.PAGING_API_PARAMETER_NAME, null, null,
                                null,
                                null, null, null);

                testSearchEndpointWithPagingParam = new ApiEndpoint(SALE, HOUSE,
                                testSearchProtocol,
                                testSearchHost,
                                testSearchPort,
                                testSearchPath,
                                List.of(testPagingApiParam));

                testProductEndpoint = new ApiEndpoint(SALE, HOUSE,
                                testProductProtocol,
                                testProductHost,
                                testProductPort,
                                testProductPath,
                                null);
                testProductEndpoint1 = new ApiEndpoint(SALE, HOUSE,
                                testProductProtocol1,
                                testProductHost1,
                                testProductPort1,
                                testProductPath1,
                                null);

                testSearchRequest = new ApiRequest(testSearchEndpoint, null);

                testProductRequest = new ApiRequest(testProductEndpoint, null);
                testProductRequest1 = new ApiRequest(testProductEndpoint1, null);

                testScrapeRequest = new ScrapeRequest(testSiteName,
                                SALE, HOUSE,
                                null);

                searchScraper = new SearchScraper(SALE, HOUSE, mockSite, mockRequestHandler,
                                mockProductFactory);
        }

        @Test
        void test_findAPIEndpoint() throws ScraperException {
                when(mockSite.getApiEndpoints())
                                .thenReturn(List.of(testSearchEndpoint));

                ApiEndpoint actualAPIEndpoint = searchScraper.findSiteEndpoint();

                assertEquals(testSearchEndpoint, actualAPIEndpoint);
        }

        @Test
        void test_findAPIEndpoint_throwsWhen_NullAPIEndpoints() throws ScraperException {
                when(mockSite.getApiEndpoints()).thenReturn(null);

                assertThrowsExactly(ScraperException.class,
                                () -> searchScraper.findSiteEndpoint(),
                                String.format(Scraper.SITE_HAS_NO_ENDPOINT, mockSite.getName()));
        }

        @Test
        void test_findAPIEndpoint_throwsWhen_EmptyAPIEndpoints() throws ScraperException {
                when(mockSite.getApiEndpoints()).thenReturn(Collections.emptyList());

                assertThrowsExactly(ScraperException.class,
                                () -> searchScraper.findSiteEndpoint(),
                                String.format(Scraper.SITE_HAS_NO_ENDPOINT, mockSite.getName()));
        }

        @Test
        void test_findAPIEndpoint_throwsWhen_APIEndpointNotFound() throws ScraperException {
                ApiEndpoint rentHouseEndpoint = new ApiEndpoint(RENT, HOUSE, testSearchProtocol, testSearchHost,
                                testSearchPort, testProductPath, null);
                when(mockSite.getApiEndpoints())
                                .thenReturn(List.of(rentHouseEndpoint));

                assertThrowsExactly(ScraperException.class,
                                () -> searchScraper.findSiteEndpoint(),
                                String.format(Scraper.API_ENDPOINT_NOT_FOUND, mockSite.getName(), RENT,
                                                HOUSE));
        }

        @Test
        void test_scrape() throws ScraperException {
                when(mockSite.getApiEndpoints()).thenReturn(List.of(testSearchEndpoint));
                when(mockRequestHandler.handle(testSearchRequest)).thenReturn(mockSearchResponseParser);
                when(mockSearchResponseParser.parseProductApiRequests())
                                .thenReturn(List.of(testProductRequest));

                when(mockRequestHandler.handle(testProductRequest)).thenReturn(mockProductResponseParser);
                when(mockProductFactory.create(testProductEndpoint, mockProductResponseParser))
                                .thenReturn(testScrapedProduct);

                searchScraper.scrape(null);

                verify(mockProductFactory,
                                times(1)).updateProducts(List.of(testScrapedProduct),
                                                null);
        }

        @Test
        void test_scrape_withPaging() throws ScraperException {
                Map<String, List<String>> p2RequestParameters = new HashMap<>();
                p2RequestParameters.put(SearchScraper.PAGING_API_PARAMETER_NAME, List.of(String.valueOf(2)));
                ApiRequest testSearchRequestP2 = new ApiRequest(testSearchEndpointWithPagingParam, p2RequestParameters);
                ApiResponseParser mockSearchResponseParserP2 = Mockito.mock(ApiResponseParser.class);

                ApiRequest testSearchRequestWithApiEndpointWithPagingParam = new ApiRequest(
                                testSearchEndpointWithPagingParam, null);

                when(mockSite.getApiEndpoints()).thenReturn(List.of(testSearchEndpointWithPagingParam));
                when(mockRequestHandler.handle(testSearchRequestWithApiEndpointWithPagingParam))
                                .thenReturn(mockSearchResponseParser);

                when(mockSearchResponseParser.parseProductApiRequests())
                                .thenReturn(List.of(testProductRequest));

                when(mockSearchResponseParser.parseField(SearchScraper.LAST_PAGE_NO_FIELD_NAME)).thenReturn("2");
                when(mockRequestHandler.handle(testSearchRequestP2)).thenReturn(mockSearchResponseParserP2);
                when(mockSearchResponseParserP2.parseProductApiRequests())
                                .thenReturn(List.of(testProductRequest1));

                when(mockRequestHandler.handle(testProductRequest)).thenReturn(mockProductResponseParser);
                when(mockRequestHandler.handle(testProductRequest1)).thenReturn(mockProductResponseParser1);

                when(mockProductFactory.create(testProductEndpoint,
                                mockProductResponseParser))
                                .thenReturn(testScrapedProduct);
                when(mockProductFactory.create(testProductEndpoint1,
                                mockProductResponseParser1))
                                .thenReturn(testScrapedProduct1);

                searchScraper.scrape(null);

                verify(mockProductFactory, times(1)).updateProducts(
                                List.of(testScrapedProduct, testScrapedProduct1),
                                null);
        }

        @Test
        void test_scrape_throwsWhen_HandleForSearchAPIThrows() throws ScraperException {
                when(mockSite.getApiEndpoints()).thenReturn(List.of(testSearchEndpoint));
                when(mockRequestHandler.handle(testSearchRequest)).thenThrow(new ScraperException(testErrorMessage));

                assertThrowsExactly(ScraperException.class, () -> searchScraper.scrape(null),
                                testErrorMessage);
        }

        @Test
        void test_scrape_throwsWhen_ParseProductAPIRequestsThrows() throws ScraperException {
                when(mockSite.getApiEndpoints()).thenReturn(List.of(testSearchEndpoint));
                when(mockRequestHandler.handle(testSearchRequest)).thenReturn(mockSearchResponseParser);
                when(mockSearchResponseParser.parseProductApiRequests())
                                .thenThrow(new ScraperException(testErrorMessage));

                assertThrowsExactly(ScraperException.class, () -> searchScraper.scrape(null),
                                testErrorMessage);
        }

        @Test
        void test_scrape_doesntThrowWhen_NullProductAPIRequests() throws ScraperException {
                when(mockSite.getApiEndpoints()).thenReturn(List.of(testSearchEndpoint));
                when(mockRequestHandler.handle(testSearchRequest)).thenReturn(mockSearchResponseParser);
                when(mockSearchResponseParser.parseProductApiRequests()).thenReturn(null);

                searchScraper.scrape(null);

                verifyNoInteractions(mockProductFactory);
        }

        @Test
        void test_scrape_doesntThrowWhen_EmptyProductAPIRequests() throws ScraperException {
                when(mockSite.getApiEndpoints()).thenReturn(List.of(testSearchEndpoint));
                when(mockRequestHandler.handle(testSearchRequest)).thenReturn(mockSearchResponseParser);
                when(mockSearchResponseParser.parseProductApiRequests())
                                .thenReturn(Collections.emptyList());

                searchScraper.scrape(null);

                verifyNoInteractions(mockProductFactory);
        }

        @Test
        void test_scrape_doesntThrowWhen_HandleForProductAPIThrows() throws ScraperException {
                when(mockSite.getApiEndpoints()).thenReturn(List.of(testSearchEndpoint));
                when(mockRequestHandler.handle(testSearchRequest)).thenReturn(mockSearchResponseParser);
                when(mockSearchResponseParser.parseProductApiRequests())
                                .thenReturn(List.of(testProductRequest));
                when(mockRequestHandler.handle(testProductRequest))
                                .thenThrow(new ScraperException(testErrorMessage));

                searchScraper.scrape(null);

                verify(mockProductFactory,
                                times(1)).updateProducts(Collections.emptyList(),
                                                null);
        }

        @Test
        void test_scrape_doesntThrowWhen_createProductThrows() throws ScraperException {
                when(mockSite.getApiEndpoints()).thenReturn(List.of(testSearchEndpoint));
                when(mockRequestHandler.handle(testSearchRequest)).thenReturn(mockSearchResponseParser);
                when(mockSearchResponseParser.parseProductApiRequests())
                                .thenReturn(List.of(testProductRequest));
                when(mockRequestHandler.handle(testProductRequest)).thenReturn(mockProductResponseParser);
                when(mockProductFactory.create(testProductEndpoint,
                                mockProductResponseParser)).thenThrow(new RuntimeException(testErrorMessage));

                searchScraper.scrape(null);

                verify(mockProductFactory,
                                times(1)).updateProducts(Collections.emptyList(),
                                                null);
        }

        @Test
        void test_scrape_ThrowsWhen_processScrapedProductsThrows() throws ScraperException {
                when(mockSite.getApiEndpoints()).thenReturn(List.of(testSearchEndpoint));
                when(mockRequestHandler.handle(testSearchRequest)).thenReturn(mockSearchResponseParser);
                when(mockSearchResponseParser.parseProductApiRequests())
                                .thenReturn(List.of(testProductRequest));
                when(mockRequestHandler.handle(testProductRequest))
                                .thenReturn(mockProductResponseParser);
                when(mockProductFactory.create(testProductEndpoint,
                                mockProductResponseParser)).thenReturn(testScrapedProduct);
                doThrow(new RuntimeException(testErrorMessage)).when(mockProductFactory)
                                .updateProducts(List.of(testScrapedProduct), null);

                assertThrowsExactly(RuntimeException.class, () -> searchScraper.scrape(null),
                                testErrorMessage);
        }

        @Test
        void test_scrapeWithPaging() throws ScraperException {
                Map<String, List<String>> testRequestParameters = new HashMap<>();
                testRequestParameters.put("mockParameterName",
                                List.of("mockParameterValue"));
                testRequestParameters.put(SearchScraper.PAGING_API_PARAMETER_NAME, List.of(String.valueOf(2)));

                testSearchRequest = new ApiRequest(testSearchEndpoint, testRequestParameters);

                when(mockRequestHandler.handle(testSearchRequest)).thenReturn(mockSearchResponseParser);
                when(mockSearchResponseParser.parseProductApiRequests())
                                .thenReturn(List.of(testProductRequest));
                when(mockSearchResponseParser.parseField(SearchScraper.LAST_PAGE_NO_FIELD_NAME)).thenReturn("2");

                testScrapeRequest = new ScrapeRequest(testSiteName, SALE, HOUSE, testRequestParameters);

                List<ApiRequest> actualApiRequests = searchScraper.scrapeWithPaging(
                                testSearchEndpoint, mockSearchResponseParser, testRequestParameters);

                assertEquals(List.of(testProductRequest), actualApiRequests);
        }

        @Test
        void test_handlePaging() throws ScraperException {
                Map<String, List<String>> testRequestParameters = new HashMap<>();
                testRequestParameters.put(SearchScraper.PAGING_API_PARAMETER_NAME, List.of(String.valueOf(2)));

                testSearchRequest = new ApiRequest(testSearchEndpointWithPagingParam, testRequestParameters);

                when(mockRequestHandler.handle(testSearchRequest)).thenReturn(mockSearchResponseParser);
                when(mockSearchResponseParser.parseProductApiRequests())
                                .thenReturn(List.of(testProductRequest));
                when(mockSearchResponseParser.parseField(SearchScraper.LAST_PAGE_NO_FIELD_NAME)).thenReturn("2");

                List<ApiRequest> actualApiRequests = searchScraper.handlePaging(testSearchEndpointWithPagingParam,
                                mockSearchResponseParser, null);

                assertEquals(List.of(testProductRequest), actualApiRequests);
        }

        @Test
        void test_scrapeProducts() throws ScraperException {
                ApiResponsePath mockResponsePath = Mockito.mock(ApiResponsePath.class);
                ApiResponseParser testProductResponseParser = new JsoupResponseParser(null);

                when(mockRequestHandler.handle(testProductRequest))
                                .thenReturn(testProductResponseParser);
                when(mockSite.getApiResponsePaths()).thenReturn(List.of(mockResponsePath));

                searchScraper.scrapeProducts(List.of(testProductRequest));

                assertEquals(List.of(mockResponsePath), testProductResponseParser.getApiResponsePaths());
                verify(mockProductFactory,
                                times(1))
                                .create(testProductEndpoint, testProductResponseParser);
        }

        @Test
        void test_scrapeProducts_returnsNullWhen_handleThrows() throws ScraperException {
                ApiResponseParser testProductResponseParser = new JsoupResponseParser(null);

                when(mockRequestHandler.handle(testProductRequest))
                                .thenThrow(new ScraperException(testErrorMessage));

                searchScraper.scrapeProducts(List.of(testProductRequest));

                assertEquals(null, testProductResponseParser.getApiResponsePaths());
                verifyNoInteractions(mockProductFactory);
        }

        @Test
        void test_scrapeProducts_returnsNullWhen_createThrows() throws ScraperException {
                ApiResponsePath mockResponsePath = Mockito.mock(ApiResponsePath.class);
                ApiResponseParser testProductResponseParser = new JsoupResponseParser(null);

                when(mockRequestHandler.handle(testProductRequest))
                                .thenReturn(testProductResponseParser);
                when(mockSite.getApiResponsePaths()).thenReturn(List.of(mockResponsePath));
                when(mockProductFactory.create(testProductEndpoint, testProductResponseParser))
                                .thenThrow(new RuntimeException(testErrorMessage));

                searchScraper.scrapeProducts(List.of(testProductRequest));

                assertEquals(List.of(mockResponsePath), testProductResponseParser.getApiResponsePaths());
        }
}
