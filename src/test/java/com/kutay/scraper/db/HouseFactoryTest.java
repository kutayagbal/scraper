package com.kutay.scraper.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import com.kutay.scraper.api.response.ApiResponseParser;
import com.kutay.scraper.db.entity.product.House;
import com.kutay.scraper.db.entity.product.Product;
import com.kutay.scraper.db.entity.site.ApiEndpoint;
import com.kutay.scraper.db.repo.HouseRepo;
import com.kutay.scraper.scrape.ScrapedProduct;
import com.kutay.scraper.util.Constants.ACTION_TYPE;
import com.kutay.scraper.util.Constants.PRODUCT_STATE;
import com.kutay.scraper.util.Constants.PRODUCT_TYPE;
import com.kutay.scraper.util.Constants.TRADE_TYPE;
import com.kutay.scraper.util.ScraperException;

class HouseFactoryTest {
        private static final String testErrorMessage = "testErrorMessage";
        private static final String testSiteName = "testSiteName";
        private static final TRADE_TYPE SALE = TRADE_TYPE.SALE;
        private static final PRODUCT_TYPE HOUSE = PRODUCT_TYPE.HOUSE;

        private static final String testProtocol = "testProtocol";
        private static final String testHost = "testHost";
        private static final Integer testPort = -1;
        private static final String testPath = "testPath";
        private static final String testProtocol1 = "testProtocol1";
        private static final String testHost1 = "testHost1";
        private static final Integer testPort1 = 10;
        private static final String testPath1 = "testPath1";

        private static final String expectedIdInSite = testProtocol + ProductFactory.ID_IN_SITE_DELIMITER + testHost
                        + ProductFactory.ID_IN_SITE_DELIMITER + testPort
                        + ProductFactory.ID_IN_SITE_DELIMITER + testPath;

        private static final String testProductPriceString = "100.4";
        private static final String testProductName = "testProductName";
        private static final String testProductStatus = "testProductStatus";
        private static final String testProductDesc = "testProductDesc";
        private static final String testProductIdInSite = "testProductIdInSite";
        private static final String testHouseCity = "testHouseCity";
        private static final String testHousePostalCode = "testHousePostalCode";
        private static final String testHouseEnergyLabel = "testHouseEnergyLabel";
        private static final String testHousePropertyType = "testHousePropertyType";
        private static final String testHouseConstructionYearStr = "1900";
        private static final String testHouseLivingAreaStr = "100";
        private static final String testHouseStoryStr = "2";
        private static final String testHouseTotalStoryStr = "2";
        private static final String testHouseBedroomStr = "3";
        private static final String testHouseTotalBedroomStr = "6";
        private static final String testHouseFurnishedStr = "False";
        private static final String testHouseServiceCostStr = "100";

        private static final String testProductPriceString1 = "10";
        private static final String testProductName1 = "testProductName1";
        private static final String testProductStatus1 = "testProductStatus1";
        private static final String testProductDesc1 = "testProductDesc1";
        private static final String testProductIdInSite1 = "testProductIdInSite1";
        private static final String testHouseCity1 = "testHouseCity1";
        private static final String testHousePostalCode1 = "testHousePostalCode1";
        private static final String testHouseEnergyLabel1 = "testHouseEnergyLabel1";
        private static final String testHousePropertyType1 = "testHousePropertyType1";
        private static final String testHouseConstructionYearStr1 = "2000";
        private static final String testHouseLivingAreaStr1 = "50";
        private static final String testHouseStoryStr1 = "1";
        private static final String testHouseTotalStoryStr1 = "1";
        private static final String testHouseBedroomStr1 = "2";
        private static final String testHouseTotalBedroomStr1 = "4";
        private static final String testHouseFurnishedStr1 = "True";
        private static final String testHouseServiceCostStr1 = "200";

        private static final String testProductPriceString2 = "1.1";
        private static final String testProductName2 = "testProductName2";
        private static final String testProductStatus2 = "testProductStatus2";
        private static final String testProductDesc2 = "testProductDesc2";
        private static final String testProductIdInSite2 = "testProductIdInSite2";
        private static final String testHouseCity2 = "testHouseCity2";
        private static final String testHousePostalCode2 = "testHousePostalCode2";
        private static final String testHouseEnergyLabel2 = "testHouseEnergyLabel2";
        private static final String testHousePropertyType2 = "testHousePropertyType2";
        private static final String testHouseConstructionYearStr2 = "2010";
        private static final String testHouseLivingAreaStr2 = "86";
        private static final String testHouseStoryStr2 = "5";
        private static final String testHouseTotalStoryStr2 = "3";
        private static final String testHouseBedroomStr2 = "5";
        private static final String testHouseTotalBedroomStr2 = "8";
        private static final String testHouseFurnishedStr2 = "False";
        private static final String testHouseServiceCostStr2 = "500.5";

        private static Product testProduct;
        private static House testHouse;

        private static final ApiEndpoint testEndpoint = new ApiEndpoint(SALE, HOUSE, testProtocol, testHost, testPort,
                        testPath,
                        null);

        private static final ApiEndpoint testImageEndpoint0 = new ApiEndpoint(SALE, HOUSE, testProtocol, testHost,
                        testPort,
                        testPath, null);
        private static final ApiEndpoint testImageEndpoint1 = new ApiEndpoint(SALE, HOUSE, testProtocol1, testHost1,
                        testPort1,
                        testPath1,
                        null);

        private static Product testScrapedProduct = new Product(testProductIdInSite, testProductName1, SALE,
                        HOUSE,
                        testSiteName, testProductDesc1, new BigDecimal(testProductPriceString1),
                        testProductStatus1,
                        testEndpoint,
                        null, new ArrayList<>(), new ArrayList<>(),
                        new ArrayList<>(), List.of(testImageEndpoint0, testImageEndpoint1));
        private static House testScrapedHouse = new House(testHouseCity1, testHousePostalCode1,
                        testHouseEnergyLabel1,
                        testHousePropertyType1, Integer.parseInt(testHouseLivingAreaStr1),
                        Integer.parseInt(testHouseConstructionYearStr1), Integer.parseInt(testHouseStoryStr1),
                        Integer.parseInt(testHouseTotalStoryStr1), Integer.parseInt(testHouseBedroomStr1),
                        Integer.parseInt(testHouseTotalBedroomStr1),
                        Boolean.parseBoolean(testHouseFurnishedStr1), new BigDecimal(testHouseServiceCostStr1),
                        testScrapedProduct);

        private static ApiResponseParser mockResponseParser;
        private static HouseRepo mockHouseRepo;
        private HouseFactory houseFactory;

        @BeforeEach
        void createMocks() {
                testProduct = new Product(testProductIdInSite, testProductName, SALE,
                                HOUSE, testSiteName, testProductDesc, new BigDecimal(testProductPriceString),
                                testProductStatus, testEndpoint, null, new ArrayList<>(), new ArrayList<>(),
                                new ArrayList<>(), new ArrayList<>());
                testHouse = new House(testHouseCity, testHousePostalCode,
                                testHouseEnergyLabel,
                                testHousePropertyType, Integer.parseInt(testHouseLivingAreaStr),
                                Integer.parseInt(testHouseConstructionYearStr),
                                Integer.parseInt(testHouseStoryStr), Integer.parseInt(testHouseTotalStoryStr),
                                Integer.parseInt(testHouseBedroomStr),
                                Integer.parseInt(testHouseTotalBedroomStr), Boolean.parseBoolean(testHouseFurnishedStr),
                                new BigDecimal(testHouseServiceCostStr),
                                testProduct);

                mockResponseParser = Mockito.mock(ApiResponseParser.class);
                mockHouseRepo = Mockito.mock(HouseRepo.class);
                houseFactory = new HouseFactory(testSiteName, SALE, mockHouseRepo);
        }

        @Test
        void test_create() throws ScraperException {
                when(mockResponseParser.parseField("price")).thenReturn(testProductPriceString);
                when(mockResponseParser.parseField("city")).thenReturn(testHouseCity);
                when(mockResponseParser.parseField("postalCode")).thenReturn(testHousePostalCode);

                ScrapedProduct actualProduct = houseFactory.create(testEndpoint, mockResponseParser);
                assertNotEquals(null, actualProduct);
                assertEquals(House.class, actualProduct.getClass());

                House actualHouse = (House) actualProduct;
                assertEquals(expectedIdInSite, actualHouse.getProduct().getIdInSite());
                assertEquals(new BigDecimal(testProductPriceString), actualHouse.getProduct().getPrice());
                assertEquals(testEndpoint, actualHouse.getProduct().getApiEndpoint());
                assertEquals(testHouseCity, actualHouse.getCity());
                assertEquals(testHousePostalCode, actualHouse.getPostalCode());
        }

        @Test
        void test_create_returnsNullWhen_parseFieldForCityThrows()
                        throws ScraperException {
                when(mockResponseParser.parseField("price")).thenReturn(testProductPriceString);
                when(mockResponseParser.parseField("city"))
                                .thenThrow(new ScraperException(testErrorMessage));

                ScrapedProduct actualProduct = houseFactory.create(testEndpoint, mockResponseParser);
                assertEquals(null, actualProduct);
        }

        @Test
        void test_create_returnsNullWhen_parseFieldForPostalCodeThrows()
                        throws ScraperException {
                when(mockResponseParser.parseField("price")).thenReturn(testProductPriceString);
                when(mockResponseParser.parseField("city")).thenReturn(testHouseCity);
                when(mockResponseParser.parseField("postalCode"))
                                .thenThrow(new ScraperException(testErrorMessage));

                ScrapedProduct actualProduct = houseFactory.create(testEndpoint, mockResponseParser);
                assertEquals(null, actualProduct);
        }

        @ParameterizedTest
        @ValueSource(strings = { "energyLabel", "propertyType", "livingArea",
                        "constructionYear", "story", "totalStory", "bedroom", "totalBedroom", "furnished",
                        "serviceCost" })
        void test_create_returnsProductWhen_ParseFieldForNonMandatoryFieldThrows(String fieldName)
                        throws ScraperException {
                when(mockResponseParser.parseField("price")).thenReturn(testProductPriceString);
                when(mockResponseParser.parseField("city")).thenReturn(testHouseCity);
                when(mockResponseParser.parseField("postalCode")).thenReturn(testHousePostalCode);

                when(mockResponseParser.parseField("energyLabel")).thenReturn(testHouseEnergyLabel);
                when(mockResponseParser.parseField("propertyType")).thenReturn(testHousePropertyType);
                when(mockResponseParser.parseField("constructionYear")).thenReturn(testHouseConstructionYearStr);
                when(mockResponseParser.parseField("livingArea")).thenReturn(testHouseLivingAreaStr);
                when(mockResponseParser.parseField("story")).thenReturn(testHouseStoryStr);
                when(mockResponseParser.parseField("totalStory")).thenReturn(testHouseTotalStoryStr);
                when(mockResponseParser.parseField("bedroom")).thenReturn(testHouseBedroomStr);
                when(mockResponseParser.parseField("totalBedroom")).thenReturn(testHouseTotalBedroomStr);
                when(mockResponseParser.parseField("furnished")).thenReturn(testHouseFurnishedStr);
                when(mockResponseParser.parseField("serviceCost")).thenReturn(testHouseServiceCostStr);

                when(mockResponseParser.parseField(fieldName))
                                .thenThrow(new ScraperException(testErrorMessage));

                ScrapedProduct actualProduct = houseFactory.create(testEndpoint, mockResponseParser);

                assertNotEquals(null, actualProduct);
                assertEquals(House.class, actualProduct.getClass());

                House actualHouse = (House) actualProduct;
                assertEquals(expectedIdInSite, actualHouse.getProduct().getIdInSite());
                assertEquals(new BigDecimal(testProductPriceString), actualHouse.getProduct().getPrice());
                assertEquals(testEndpoint, actualHouse.getProduct().getApiEndpoint());
                assertEquals(testHouseCity, actualHouse.getCity());
                assertEquals(testHousePostalCode, actualHouse.getPostalCode());
        }

        @Test
        void test_saveOrUpdate() {
                List<ApiEndpoint> existingImageEndpoints = List.of(testImageEndpoint0);

                Product testProduct = new Product(testProductIdInSite, testProductName, SALE,
                                HOUSE, testSiteName, testProductDesc, new BigDecimal(testProductPriceString),
                                testProductStatus, testEndpoint, null, new ArrayList<>(), new ArrayList<>(),
                                new ArrayList<>(), existingImageEndpoints);
                House testHouse = new House(testHouseCity, testHousePostalCode,
                                testHouseEnergyLabel,
                                testHousePropertyType, Integer.parseInt(testHouseLivingAreaStr),
                                Integer.parseInt(testHouseConstructionYearStr),
                                Integer.parseInt(testHouseStoryStr), Integer.parseInt(testHouseTotalStoryStr),
                                Integer.parseInt(testHouseBedroomStr),
                                Integer.parseInt(testHouseTotalBedroomStr), Boolean.parseBoolean(testHouseFurnishedStr),
                                new BigDecimal(testHouseServiceCostStr),
                                testProduct);

                Product testScrapedProduct1 = new Product(testProductIdInSite2, testProductName2, SALE,
                                HOUSE,
                                testSiteName, testProductDesc2, new BigDecimal(testProductPriceString2),
                                testProductStatus2,
                                testEndpoint,
                                null, new ArrayList<>(), new ArrayList<>(),
                                new ArrayList<>(), List.of(testImageEndpoint1));
                House testScrapedHouse1 = new House(testHouseCity2, testHousePostalCode2,
                                testHouseEnergyLabel2,
                                testHousePropertyType2, Integer.parseInt(testHouseLivingAreaStr2),
                                Integer.parseInt(testHouseConstructionYearStr2), Integer.parseInt(testHouseStoryStr2),
                                Integer.parseInt(testHouseTotalStoryStr2), Integer.parseInt(testHouseBedroomStr2),
                                Integer.parseInt(testHouseTotalBedroomStr2),
                                Boolean.parseBoolean(testHouseFurnishedStr2), new BigDecimal(testHouseServiceCostStr2),
                                testScrapedProduct1);

                when(mockHouseRepo.findHouse(testProductIdInSite, testSiteName, SALE, HOUSE))
                                .thenReturn(Optional.of(testHouse));
                houseFactory.saveOrUpdate(List.of(testScrapedHouse, testScrapedHouse1));

                verify(mockHouseRepo, times(1)).save(testScrapedHouse1);

                assertEquals(testScrapedProduct.getName(), testProduct.getName());
                assertEquals(testScrapedProduct.getDescription(), testProduct.getDescription());
                assertEquals(testScrapedProduct.getPrice(), testProduct.getPrice());
                assertEquals(testScrapedProduct.getStatus(), testProduct.getStatus());
                assertEquals(testScrapedProduct.getImageApiEndpoints(),
                                testProduct.getImageApiEndpoints());

                assertEquals(testScrapedHouse.getServiceCost(), testHouse.getServiceCost());
                assertEquals(testScrapedHouse.getCity(), testHouse.getCity());
                assertEquals(testScrapedHouse.getPostalCode(), testHouse.getPostalCode());
                assertEquals(testScrapedHouse.getPropertyType(), testHouse.getPropertyType());
                assertEquals(testScrapedHouse.getEnergyLabel(), testHouse.getEnergyLabel());
                assertEquals(testScrapedHouse.getLivingArea(), testHouse.getLivingArea());
                assertEquals(testScrapedHouse.getConstructionYear(), testHouse.getConstructionYear());
                assertEquals(testScrapedHouse.getStory(), testHouse.getStory());
                assertEquals(testScrapedHouse.getTotalStory(), testHouse.getTotalStory());
                assertEquals(testScrapedHouse.getBedroom(), testHouse.getBedroom());
                assertEquals(testScrapedHouse.getTotalRoom(), testHouse.getTotalRoom());
        }

        @Test
        void test_Remove() {
                when(mockHouseRepo.searchProduct(testSiteName, SALE, HOUSE, null, null,
                                null, null))
                                .thenReturn(List.of(testHouse));

                houseFactory.remove(Collections.emptyList(), null);

                assertEquals(PRODUCT_STATE.REMOVED.name(), testProduct.getStatus());
                assertEquals(1, testProduct.getStatusHistory().size());
                assertEquals(testProductStatus, testProduct.getStatusHistory().get(0).getStatus());

                assertEquals(1, testProduct.getActionHistory().size());
                assertEquals(ACTION_TYPE.UNFOLLOWED, testProduct.getActionHistory().get(0).getType());
                assertEquals(PRODUCT_STATE.REMOVED.name(), testProduct.getActionHistory().get(0).getDescription());
        }

        @Test
        void test_Remove_WithRequestParameters() {
                Product mockProduct = Mockito.mock(Product.class);
                House mockHouse = Mockito.mock(House.class);

                Map<String, List<String>> testRequestParameters = new HashMap<>();
                testRequestParameters.put("prices", List.of(testProductPriceString1, testProductPriceString));
                testRequestParameters.put("cities", List.of(testHouseCity, testHouseCity1));
                testRequestParameters.put("propertyTypes",
                                List.of(testHousePropertyType, testHousePropertyType1, testHousePropertyType2));

                when(mockHouseRepo.searchProduct(testSiteName, SALE, HOUSE,
                                new BigDecimal(testProductPriceString1), new BigDecimal(testProductPriceString),
                                List.of(testHouseCity, testHouseCity1),
                                List.of(testHousePropertyType, testHousePropertyType1, testHousePropertyType2)))
                                .thenReturn(List.of(testHouse));
                when(mockHouse.getProduct()).thenReturn(mockProduct);
                when(mockProduct.getIdInSite()).thenReturn(testProductIdInSite1);

                houseFactory.remove(List.of(mockHouse), testRequestParameters);

                assertEquals(PRODUCT_STATE.REMOVED.name(), testProduct.getStatus());
                assertEquals(1, testProduct.getStatusHistory().size());
                assertEquals(testProductStatus, testProduct.getStatusHistory().get(0).getStatus());

                assertEquals(1, testProduct.getActionHistory().size());
                assertEquals(ACTION_TYPE.UNFOLLOWED, testProduct.getActionHistory().get(0).getType());
                assertEquals(PRODUCT_STATE.REMOVED.name(), testProduct.getActionHistory().get(0).getDescription());
        }

        @Test
        void test_updateWithScrapedValues() throws ScraperException {
                houseFactory.updateWithScrapedValues(testHouse, testScrapedHouse);

                assertEquals(testScrapedHouse.getServiceCost(), testHouse.getServiceCost());
                assertEquals(testScrapedHouse.getCity(), testHouse.getCity());
                assertEquals(testScrapedHouse.getPostalCode(), testHouse.getPostalCode());
                assertEquals(testScrapedHouse.getEnergyLabel(), testHouse.getEnergyLabel());
                assertEquals(testScrapedHouse.getPropertyType(), testHouse.getPropertyType());
                assertEquals(testScrapedHouse.getLivingArea(), testHouse.getLivingArea());
                assertEquals(testScrapedHouse.getConstructionYear(), testHouse.getConstructionYear());
                assertEquals(testScrapedHouse.getStory(), testHouse.getStory());
                assertEquals(testScrapedHouse.getTotalStory(), testHouse.getTotalStory());
                assertEquals(testScrapedHouse.getBedroom(), testHouse.getBedroom());
                assertEquals(testScrapedHouse.getTotalRoom(), testHouse.getTotalRoom());
        }

}
