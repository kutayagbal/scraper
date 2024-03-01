package com.kutay.scraper.scrape;

import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.kutay.scraper.api.request.ApiRequestHandler;
import com.kutay.scraper.db.ProductFactory;
import com.kutay.scraper.db.entity.site.Component;
import com.kutay.scraper.db.entity.site.Site;
import com.kutay.scraper.db.repo.SiteRepo;
import com.kutay.scraper.util.Constants.PRODUCT_TYPE;
import com.kutay.scraper.util.Constants.TRADE_TYPE;
import com.kutay.scraper.util.ScraperException;

import jakarta.transaction.Transactional;

@Service
public class ScrapeService {
    protected static final String NO_SITE = "No site found for SiteName: %s, TradeType: %s, ProductType: %s";
    protected static final String CANT_INSTANTIATE_SCRAPER = "Can't instantiate scraper. Scraper: %s";
    protected static final String CANT_INSTANTIATE_API_REQUEST_HANDLER = "Can't instantiate requestHandler. RequestHandler: %s";
    protected static final String CANT_INSTANTIATE_PRODUCT_FACTORY = "Can't instantiate productFactory. ProductFactory: %s";
    protected static final String NO_COMPONENT = "No component for TradeType: %s, ProductType: %s";
    protected static final String CANT_FIND_COMPONENT = "Component cannot be found. tradeType: %s productType: %s";
    protected static final String PRODUCT_REPO_NOT_FOUND = "Product Repository can not be found. ProductType: %s";

    private SiteRepo siteRepo;
    private ApplicationContext applicationContext;

    public ScrapeService(SiteRepo siteRepo, ApplicationContext applicationContext) {
        this.siteRepo = siteRepo;
        this.applicationContext = applicationContext;
    }

    @Transactional
    public void scrape(ScrapeRequest scrapeRequest) throws ScraperException {
        Optional<Site> siteOpt = siteRepo.findByName(scrapeRequest.getSiteName());
        if (siteOpt.isPresent()) {
            Site site = siteOpt.get();
            Component component = findComponent(site.getComponents(), scrapeRequest.getTradeType(),
                    scrapeRequest.getProductType());

            instantiateScraper(scrapeRequest.getSiteName(), scrapeRequest.getTradeType(),
                    scrapeRequest.getProductType(), site, component)
                    .scrape(scrapeRequest.getParameters());
        } else {
            throw new ScraperException(
                    String.format(NO_SITE, scrapeRequest.getSiteName(), scrapeRequest.getTradeType().name(),
                            scrapeRequest.getProductType().name()));
        }
    }

    protected Component findComponent(List<Component> components, TRADE_TYPE tradeType, PRODUCT_TYPE productType)
            throws ScraperException {
        if (components != null && !components.isEmpty()) {
            Optional<Component> component = components.stream()
                    .filter(comp -> tradeType.equals(comp.getTradeType()) && productType.equals(comp.getProductType()))
                    .findAny();
            if (!component.isPresent()) {
                throw new ScraperException(
                        String.format(CANT_FIND_COMPONENT, tradeType.name(), productType.name()));
            }

            return component.get();
        } else {
            throw new ScraperException(String.format(NO_COMPONENT, tradeType.name(), productType.name()));
        }
    }

    protected Scraper instantiateScraper(String siteName, TRADE_TYPE tradeType, PRODUCT_TYPE productType, Site site,
            Component component)
            throws ScraperException {
        try {
            return (Scraper) Class.forName(component.getScraper())
                    .getConstructor(TRADE_TYPE.class, PRODUCT_TYPE.class, Site.class, ApiRequestHandler.class,
                            ProductFactory.class)
                    .newInstance(tradeType, productType, site, instantiateApiRequestHandler(component),
                            instantiateProductFactory(siteName, tradeType, productType, component));
        } catch (Exception exception) {
            throw new ScraperException(String.format(CANT_INSTANTIATE_SCRAPER, component.getScraper()),
                    exception);
        }
    }

    protected ApiRequestHandler instantiateApiRequestHandler(Component component) throws ScraperException {
        try {
            return (ApiRequestHandler) Class.forName(component.getApiRequestHandler())
                    .getConstructor()
                    .newInstance();
        } catch (Exception exception) {
            throw new ScraperException(
                    String.format(CANT_INSTANTIATE_API_REQUEST_HANDLER, component.getApiRequestHandler()),
                    exception);
        }
    }

    protected ProductFactory instantiateProductFactory(String siteName, TRADE_TYPE tradeType, PRODUCT_TYPE productType,
            Component component)
            throws ScraperException {
        try {
            return (ProductFactory) Class.forName(component.getProductFactory())
                    .getConstructor(String.class, TRADE_TYPE.class, productType.getRepoClass())
                    .newInstance(siteName, tradeType, applicationContext.getBean(productType.getRepoClass()));
        } catch (Exception exception) {
            throw new ScraperException(
                    String.format(CANT_INSTANTIATE_PRODUCT_FACTORY, component.getProductFactory()),
                    exception);
        }
    }
}
