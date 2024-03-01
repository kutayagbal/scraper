package com.kutay.scraper.scrape;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;

import com.kutay.scraper.api.request.ApiRequestHandler;
import com.kutay.scraper.db.ProductFactory;
import com.kutay.scraper.db.entity.site.Component;
import com.kutay.scraper.db.entity.site.Site;
import com.kutay.scraper.db.repo.HouseRepo;
import com.kutay.scraper.db.repo.SiteRepo;
import com.kutay.scraper.util.Constants.PRODUCT_TYPE;
import com.kutay.scraper.util.Constants.TRADE_TYPE;
import com.kutay.scraper.util.ScraperException;

class ScrapeServiceTest {
        private static final String testSiteName = "testSiteName";
        private static final String testScraperName = "com.kutay.scraper.scrape.SearchScraper";
        private static final String testApiRequestHandlerName = "com.kutay.scraper.api.request.JsoupRequestHandler";
        private static final String testProductFactoryName = "com.kutay.scraper.db.HouseFactory";
        private static final TRADE_TYPE testTradeType = TRADE_TYPE.SALE;
        private static final PRODUCT_TYPE testProductType = PRODUCT_TYPE.HOUSE;

        private static ScrapeService scrapeService;
        private static SiteRepo mockSiteRepo;
        private static HouseRepo mockHouseRepo;
        private static ApplicationContext mockApplicationContext;

        private static Site testSite;

        @MockBean
        SearchScraper scraper;

        @BeforeEach
        void createMocks() {
                mockSiteRepo = Mockito.mock(SiteRepo.class);
                mockHouseRepo = Mockito.mock(HouseRepo.class);
                mockApplicationContext = Mockito.mock(ApplicationContext.class);
                scrapeService = new ScrapeService(mockSiteRepo, mockApplicationContext);
        }

        @Test
        void test_scrape() throws ScraperException {
                Component testComponent = new Component(testTradeType, testProductType, testScraperName,
                                testApiRequestHandlerName, testProductFactoryName);
                testSite = new Site(testSiteName, List.of(testComponent), null, null);

                ScrapeRequest testScrapeRequest = new ScrapeRequest(testSiteName, testTradeType, testProductType, null);

                when(mockSiteRepo.findByName(testSiteName)).thenReturn(Optional.of(testSite));
                when(mockApplicationContext.getBean(testProductType.getRepoClass())).thenReturn(mockHouseRepo);

                assertThrows(ScraperException.class, () -> scrapeService.scrape(testScrapeRequest));
        }

        @Test
        void test_scrape_throwsWhen_SiteCantFind() throws ScraperException {
                ScrapeRequest testScrapeRequest = new ScrapeRequest(testSiteName, testTradeType, testProductType, null);

                when(mockSiteRepo.findByName(testSiteName)).thenReturn(Optional.empty());

                assertThrowsExactly(ScraperException.class,
                                () -> scrapeService.scrape(testScrapeRequest),
                                String.format(ScrapeService.NO_SITE, testSiteName, testTradeType, testProductType));
        }

        @Test
        void test_findComponent() throws ScraperException {
                Component testComponent = new Component(testTradeType, testProductType, testScraperName,
                                testApiRequestHandlerName, testProductFactoryName);
                testSite = new Site(testSiteName, List.of(testComponent), null, null);

                when(mockSiteRepo.findByName(testSiteName)).thenReturn(Optional.of(testSite));

                Component actualComponent = scrapeService.findComponent(List.of(testComponent), testTradeType,
                                testProductType);

                assertEquals(testComponent, actualComponent);
        }

        @Test
        void test_findComponent_throwsWhen_ComponentsNull() throws ScraperException {
                testSite = new Site(testSiteName, null, null, null);

                when(mockSiteRepo.findByName(testSiteName)).thenReturn(Optional.of(testSite));

                assertThrowsExactly(ScraperException.class,
                                () -> scrapeService.findComponent(null, testTradeType, testProductType),
                                String.format(ScrapeService.NO_COMPONENT, testTradeType.name(),
                                                testProductType.name()));
        }

        @Test
        void test_findComponent_throwsWhen_ComponentsEmpty() throws ScraperException {
                testSite = new Site(testSiteName, Collections.emptyList(), null, null);

                when(mockSiteRepo.findByName(testSiteName)).thenReturn(Optional.of(testSite));

                assertThrowsExactly(ScraperException.class,
                                () -> scrapeService.findComponent(Collections.emptyList(), testTradeType,
                                                testProductType),
                                String.format(ScrapeService.NO_COMPONENT, testTradeType.name(),
                                                testProductType.name()));
        }

        @Test
        void test_findComponent_throwsWhen_ComponentsCantFind() throws ScraperException {
                TRADE_TYPE mockTradeType = Mockito.mock(TRADE_TYPE.class);
                Component testComponent = new Component(testTradeType, testProductType, testScraperName,
                                testApiRequestHandlerName, testProductFactoryName);
                testSite = new Site(testSiteName, List.of(testComponent), null, null);

                when(mockSiteRepo.findByName(testSiteName)).thenReturn(Optional.of(testSite));

                assertThrowsExactly(ScraperException.class,
                                () -> scrapeService.findComponent(List.of(testComponent), mockTradeType,
                                                testProductType),
                                String.format(ScrapeService.CANT_FIND_COMPONENT, testTradeType.name(),
                                                testProductType.name()));
        }

        @Test
        void test_instantiateScraper() throws ScraperException, NoSuchFieldException, SecurityException,
                        IllegalArgumentException, IllegalAccessException {
                Component testComponent = new Component(testTradeType, testProductType, testScraperName,
                                testApiRequestHandlerName, testProductFactoryName);
                testSite = new Site(testSiteName, List.of(testComponent), null, null);

                when(mockSiteRepo.findByName(testSiteName)).thenReturn(Optional.of(testSite));
                when(mockApplicationContext.getBean(testProductType.getRepoClass())).thenReturn(mockHouseRepo);

                Scraper actualScraper = scrapeService.instantiateScraper(testSiteName, testTradeType, testProductType,
                                testSite,
                                testComponent);

                assertEquals(testSite, actualScraper.site);
                assertEquals(testApiRequestHandlerName, actualScraper.requestHandler.getClass().getName());
                assertEquals(testProductFactoryName, actualScraper.productFactory.getClass().getName());
                assertEquals(mockHouseRepo, actualScraper.productFactory.getRepository());
        }

        @Test
        void test_instantiateScraper_throwsWhen_APIRequestHandlerCantInstantiate() throws ScraperException {
                String wrongAPIRequestHandlerName = "wrongAPIRequestHandlerName";
                Component testComponent = new Component(testTradeType, testProductType, testScraperName,
                                wrongAPIRequestHandlerName, testProductFactoryName);

                testSite = new Site(testSiteName, List.of(testComponent),
                                null,
                                null);

                when(mockSiteRepo.findByName(testSiteName)).thenReturn(Optional.of(testSite));
                when(mockApplicationContext.getBean(testProductType.getRepoClass())).thenReturn(mockHouseRepo);

                assertThrowsExactly(ScraperException.class,
                                () -> scrapeService.instantiateScraper(testSiteName, testTradeType, testProductType,
                                                testSite,
                                                testComponent),
                                String.format(ScrapeService.CANT_INSTANTIATE_API_REQUEST_HANDLER,
                                                wrongAPIRequestHandlerName));
        }

        @Test
        void test_instantiateScraper_throwsWhen_ProductFactoryCantInstantiate() throws ScraperException {
                String wrongProductFactoryName = "wrongProductFactoryName";
                Component testComponent = new Component(testTradeType, testProductType, testScraperName,
                                testApiRequestHandlerName, wrongProductFactoryName);
                testSite = new Site(testSiteName, List.of(testComponent),
                                null,
                                null);

                when(mockSiteRepo.findByName(testSiteName)).thenReturn(Optional.of(testSite));
                when(mockApplicationContext.getBean(testProductType.getRepoClass())).thenReturn(mockHouseRepo);

                assertThrowsExactly(ScraperException.class,
                                () -> scrapeService.instantiateScraper(testSiteName, testTradeType, testProductType,
                                                testSite,
                                                testComponent),
                                String.format(ScrapeService.CANT_INSTANTIATE_PRODUCT_FACTORY,
                                                wrongProductFactoryName));
        }

        @Test
        void test_instantiateScraper_throwsWhen_ScraperCantInstantiate() throws ScraperException {
                String wrongScraperName = "wrongScraperName";
                Component testComponent = new Component(testTradeType, testProductType, wrongScraperName,
                                testApiRequestHandlerName, testProductFactoryName);
                testSite = new Site(testSiteName, List.of(testComponent),
                                null,
                                null);

                when(mockSiteRepo.findByName(testSiteName)).thenReturn(Optional.of(testSite));
                when(mockApplicationContext.getBean(testProductType.getRepoClass())).thenReturn(mockHouseRepo);

                assertThrowsExactly(ScraperException.class,
                                () -> scrapeService.instantiateScraper(testSiteName, testTradeType, testProductType,
                                                testSite,
                                                testComponent),
                                String.format(ScrapeService.CANT_INSTANTIATE_SCRAPER, wrongScraperName));
        }

        @Test
        void test_instantiateApiRequestHandler() throws ScraperException {
                Component testComponent = new Component(testTradeType, testProductType, testScraperName,
                                testApiRequestHandlerName, testProductFactoryName);
                ApiRequestHandler actualApiRequestHandler = scrapeService.instantiateApiRequestHandler(testComponent);

                assertNotNull(actualApiRequestHandler);
                assertEquals(testApiRequestHandlerName, actualApiRequestHandler.getClass().getName());
        }

        @Test
        void test_instantiateApiRequestHandler_throwsWhen_CanNotInstantiate() throws ScraperException {
                String wrongApiRequestHandlerName = "wrongApiRequestHandlerName";
                Component testComponent = new Component(testTradeType, testProductType, testScraperName,
                                wrongApiRequestHandlerName, testProductFactoryName);

                assertThrowsExactly(ScraperException.class,
                                () -> scrapeService.instantiateApiRequestHandler(testComponent),
                                String.format(ScrapeService.CANT_INSTANTIATE_API_REQUEST_HANDLER,
                                                wrongApiRequestHandlerName));
        }

        @Test
        void test_instantiateProductFactory() throws ScraperException {
                Component testComponent = new Component(testTradeType, testProductType, testScraperName,
                                testApiRequestHandlerName, testProductFactoryName);
                ProductFactory actualProductFactory = scrapeService.instantiateProductFactory(testSiteName,
                                testTradeType,
                                testProductType, testComponent);
                when(mockApplicationContext.getBean(testProductType.getRepoClass())).thenReturn(mockHouseRepo);

                assertNotNull(actualProductFactory);
                assertEquals(testProductFactoryName, actualProductFactory.getClass().getName());
        }

        @Test
        void test_instantiateProductFactory_throwsWhen_CanNotInstantiate() throws ScraperException {
                String wrongProductFactoryName = "wrongProductFactoryName";
                Component testComponent = new Component(testTradeType, testProductType, testScraperName,
                                testApiRequestHandlerName, wrongProductFactoryName);

                when(mockApplicationContext.getBean(testProductType.getRepoClass())).thenReturn(mockHouseRepo);

                assertThrowsExactly(ScraperException.class,
                                () -> scrapeService.instantiateProductFactory(testSiteName, testTradeType,
                                                testProductType,
                                                testComponent),
                                String.format(ScrapeService.CANT_INSTANTIATE_PRODUCT_FACTORY,
                                                wrongProductFactoryName));
        }
}
