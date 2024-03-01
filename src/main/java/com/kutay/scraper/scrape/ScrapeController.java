package com.kutay.scraper.scrape;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kutay.scraper.util.ScraperException;

@RestController
@CrossOrigin
@RequestMapping("/scraper")
public class ScrapeController {
    protected static final String CAN_NOT_BE_EMPTY = "%s can not be empty!";
    private final ScrapeService scrapeService;

    public ScrapeController(ScrapeService scrapeService) {
        this.scrapeService = scrapeService;
    }

    @PostMapping("/scrape")
    public void scrape(@RequestBody ScrapeRequest request) throws ScraperException {
        validateRequest(request);
        scrapeService.scrape(request);
    }

    protected void validateRequest(ScrapeRequest scrapeRequest) throws ScraperException {
        if (!StringUtils.hasText(scrapeRequest.getSiteName())) {
            throw new ScraperException(String.format(CAN_NOT_BE_EMPTY, "SiteName", scrapeRequest));
        }
        if (scrapeRequest.getTradeType() == null) {
            throw new ScraperException(String.format(CAN_NOT_BE_EMPTY, "TradeType", scrapeRequest));
        }
        if (scrapeRequest.getProductType() == null) {
            throw new ScraperException(String.format(CAN_NOT_BE_EMPTY, "ProductType", scrapeRequest));
        }
    }
}
