package com.kutay.scraper.scrape;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kutay.scraper.util.Constants.PRODUCT_TYPE;
import com.kutay.scraper.util.Constants.TRADE_TYPE;
import com.kutay.scraper.util.ScraperException;

class ScrapeControllerTest {
    private static ScrapeController scrapeController;
    private static ScrapeService mockScrapeService;
    private static SearchScraper mockSearchScraper;
    private static ScrapeRequest scrapeRequest;
    private static final String mockSiteName = "mockSiteName";
    private static final TRADE_TYPE mockTradeType = TRADE_TYPE.SALE;
    private static final PRODUCT_TYPE mockProductType = PRODUCT_TYPE.HOUSE;

    @BeforeEach
    void createMocks() {
        mockSearchScraper = Mockito.mock(SearchScraper.class);
        mockScrapeService = Mockito.mock(ScrapeService.class);
        scrapeController = new ScrapeController(mockScrapeService);
    }

    @Test
    void test_validateRequest_throwsWhen_SiteNameNull() throws ScraperException {
        scrapeRequest = new ScrapeRequest(null, mockTradeType, mockProductType, null);

        assertThrowsExactly(ScraperException.class, () -> scrapeController.validateRequest(scrapeRequest),
                String.format(ScrapeController.CAN_NOT_BE_EMPTY, "SiteName"));
        verify(mockSearchScraper, times(0)).scrape(null);
    }

    @Test
    void test_validateRequest_throwsWhen_SiteNameEmpty() throws ScraperException {
        scrapeRequest = new ScrapeRequest("", mockTradeType, mockProductType, null);

        assertThrowsExactly(ScraperException.class, () -> scrapeController.validateRequest(scrapeRequest),
                String.format(ScrapeController.CAN_NOT_BE_EMPTY, "SiteName"));
        verify(mockSearchScraper, times(0)).scrape(null);
    }

    @Test
    void test_validateRequest_throwsWhen_TradeTypeNull() throws ScraperException {
        scrapeRequest = new ScrapeRequest(mockSiteName, null, mockProductType, null);

        assertThrowsExactly(ScraperException.class, () -> scrapeController.validateRequest(scrapeRequest),
                String.format(ScrapeController.CAN_NOT_BE_EMPTY, "TradeType"));
        verify(mockSearchScraper, times(0)).scrape(null);
    }

    @Test
    void test_validateRequest_throwsWhen_ProductTypeNull() throws ScraperException {
        scrapeRequest = new ScrapeRequest(mockSiteName, mockTradeType, null, null);

        assertThrowsExactly(ScraperException.class, () -> scrapeController.validateRequest(scrapeRequest),
                String.format(ScrapeController.CAN_NOT_BE_EMPTY, "ProductType"));
        verify(mockSearchScraper, times(0)).scrape(null);
    }

    @WebMvcTest(ScrapeController.class)
    @Nested
    class ScrapeControllerTest_withMockMVC {
        @Autowired
        private MockMvc mockMvc;

        @MockBean
        ScrapeService mockScrapeServiceBean;

        @Test
        void test_scrape() throws Exception {
            scrapeRequest = new ScrapeRequest(mockSiteName, mockTradeType, mockProductType, null);

            mockMvc.perform(post("/scraper/scrape")
                    .content(new ObjectMapper().writeValueAsString(scrapeRequest))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }
}
