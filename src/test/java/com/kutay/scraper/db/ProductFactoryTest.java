package com.kutay.scraper.db;

import static com.kutay.scraper.db.ProductFactory.CANT_PARSE_MANDATORY_FIELD;
import static com.kutay.scraper.db.ProductFactory.EMPTY_MANDATORY_FIELD_PARSED;
import static com.kutay.scraper.db.ProductFactory.EMPTY_TEXT_FOR_NUMBER;
import static com.kutay.scraper.db.ProductFactory.ID_IN_SITE_DELIMITER;
import static com.kutay.scraper.db.ProductFactory.NO_NUMBER_IN_TEXT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.kutay.scraper.api.response.ApiResponseParser;
import com.kutay.scraper.db.entity.product.Product;
import com.kutay.scraper.db.entity.site.ApiEndpoint;
import com.kutay.scraper.db.repo.HouseRepo;
import com.kutay.scraper.util.Constants;
import com.kutay.scraper.util.Constants.PRODUCT_TYPE;
import com.kutay.scraper.util.Constants.TRADE_TYPE;
import com.kutay.scraper.util.ScraperException;

class ProductFactoryTest {
        private static final TRADE_TYPE SALE = TRADE_TYPE.SALE;
        private static final PRODUCT_TYPE HOUSE = PRODUCT_TYPE.HOUSE;
        private static final String testProtocol = "testProtocol";
        private static final String testHost = "testHost";
        private static final Integer testPort = -1;
        private static final String testPath = "testPath";

        private static final ApiEndpoint testEndpoint = new ApiEndpoint(SALE, HOUSE, testProtocol, testHost, testPort,
                        testPath,
                        null);

        private static final String expectedIdInSite = testProtocol + ProductFactory.ID_IN_SITE_DELIMITER + testHost
                        + ProductFactory.ID_IN_SITE_DELIMITER + testPort
                        + ProductFactory.ID_IN_SITE_DELIMITER + testPath;

        private static final String testSiteName = "testSiteName";
        private static final String testFieldName = "testFieldName";
        private static final String testErrorMessage = "testErrorMessage";

        private static final String testProductName = "testProductName";
        private static final String testProductPriceString = "3.14";
        private static final String testProductDesc = "testProductDesc";
        private static final String testProductStatus = "testProductStatus";

        private static final String testProductName1 = "testProductName1";
        private static final String testProductPriceString1 = "758";
        private static final String testProductDesc1 = "testProductDesc1";
        private static final String testProductStatus1 = "testProductStatus1";

        private static ApiResponseParser mockResponseParser;
        private static HouseRepo mockHouseRepo;
        private HouseFactory houseFactory;

        @BeforeEach
        void createMocks() {
                mockResponseParser = Mockito.mock(ApiResponseParser.class);
                mockHouseRepo = Mockito.mock(HouseRepo.class);
                houseFactory = new HouseFactory(testSiteName, SALE, mockHouseRepo);
        }

        @Test
        void test_createProduct() throws ScraperException {
                when(mockResponseParser.parseField("price")).thenReturn(testProductPriceString);
                when(mockResponseParser.parseField("name")).thenReturn(testProductName);
                when(mockResponseParser.parseField("description")).thenReturn(testProductDesc);
                when(mockResponseParser.parseField("status")).thenReturn(testProductStatus);

                Product actualProduct = houseFactory.createProduct(testEndpoint, mockResponseParser);

                assertNotEquals(null, actualProduct);
                assertEquals(testEndpoint, actualProduct.getApiEndpoint());
                assertEquals(expectedIdInSite, actualProduct.getIdInSite());
                assertEquals(new BigDecimal(testProductPriceString), actualProduct.getPrice());
                assertEquals(testProductName, actualProduct.getName());
                assertEquals(testProductDesc, actualProduct.getDescription());
                assertEquals(testProductStatus, actualProduct.getStatus());
                assertNull(actualProduct.getImageFolder());
                assertNull(actualProduct.getActionHistory());
                assertNull(actualProduct.getPriceHistory());
                assertNull(actualProduct.getStatusHistory());
                assertNull(actualProduct.getImageApiEndpoints());
        }

        @Test
        void test_createProduct_returnsNull_whenParseFieldThrowsForMandatoryField() throws ScraperException {
                when(mockResponseParser.parseField("price")).thenThrow(new RuntimeException(testErrorMessage));

                Product actualProduct = houseFactory.createProduct(testEndpoint, mockResponseParser);

                assertNull(actualProduct);
        }

        @Test
        void test_createProduct_returnsProduct_whenParseFieldThrowsForNonMandatoryField() throws ScraperException {
                when(mockResponseParser.parseField("price")).thenReturn(testProductPriceString);
                when(mockResponseParser.parseField("name")).thenThrow(new RuntimeException(testErrorMessage));
                when(mockResponseParser.parseField("description")).thenReturn(testProductDesc);
                when(mockResponseParser.parseField("status")).thenReturn(testProductStatus);

                Product actualProduct = houseFactory.createProduct(testEndpoint, mockResponseParser);

                assertNotEquals(null, actualProduct);
                assertEquals(testEndpoint, actualProduct.getApiEndpoint());
                assertEquals(expectedIdInSite, actualProduct.getIdInSite());
                assertEquals(new BigDecimal(testProductPriceString), actualProduct.getPrice());
                assertEquals(null, actualProduct.getName());
                assertEquals(testProductDesc, actualProduct.getDescription());
                assertEquals(testProductStatus, actualProduct.getStatus());
                assertNull(actualProduct.getImageFolder());
                assertNull(actualProduct.getActionHistory());
                assertNull(actualProduct.getPriceHistory());
                assertNull(actualProduct.getStatusHistory());
                assertNull(actualProduct.getImageApiEndpoints());
        }

        @Test
        void test_buildIdInSite() throws ScraperException {
                ApiEndpoint testProductEndpoint = new ApiEndpoint(SALE, HOUSE, testProtocol, testHost, testPort,
                                testPath, null);

                String expectedIdInSite = testProductEndpoint.getProtocol() + ID_IN_SITE_DELIMITER
                                + testProductEndpoint.getHost() + ID_IN_SITE_DELIMITER
                                + testProductEndpoint.getPort() + ID_IN_SITE_DELIMITER
                                + testProductEndpoint.getPath();
                String actualIdInSite = houseFactory.buildIdInSite(testProductEndpoint);

                assertEquals(expectedIdInSite, actualIdInSite);
        }

        @Test
        void test_getFieldValue_forInteger() throws ScraperException {
                String expectedStr = "13";
                when(mockResponseParser.parseField(testFieldName)).thenReturn(expectedStr);
                Object actualValue = houseFactory.getFieldValue(mockResponseParser, testFieldName, Integer.class,
                                false);
                assertEquals(13, actualValue);
        }

        @Test
        void test_getFieldValue_forBigDecimal() throws ScraperException {
                String expectedStr = "3.14";
                when(mockResponseParser.parseField(testFieldName)).thenReturn(expectedStr);
                Object actualValue = houseFactory.getFieldValue(mockResponseParser, testFieldName, BigDecimal.class,
                                false);
                assertEquals(new BigDecimal(expectedStr), actualValue);
        }

        @Test
        void test_getFieldValue_forBoolean() throws ScraperException {
                String expectedStr = "truE";
                when(mockResponseParser.parseField(testFieldName)).thenReturn(expectedStr);
                Object actualValue = houseFactory.getFieldValue(mockResponseParser, testFieldName, Boolean.class,
                                false);
                assertEquals(Boolean.TRUE, actualValue);
        }

        @Test
        void test_getFieldValue_forString() throws ScraperException {
                String expectedStr = "a.sdkbf adsf";
                when(mockResponseParser.parseField(testFieldName)).thenReturn(expectedStr);
                Object actualValue = houseFactory.getFieldValue(mockResponseParser, testFieldName, String.class,
                                false);
                assertEquals(expectedStr, actualValue);
        }

        @Test
        void test_getFieldValue_throwsWhen_parseFieldForMandatoryFieldThrowsException() throws ScraperException {
                when(mockResponseParser.parseField(testFieldName)).thenThrow(new RuntimeException(testErrorMessage));

                assertThrowsExactly(ScraperException.class,
                                () -> houseFactory.getFieldValue(mockResponseParser, testFieldName, String.class,
                                                true),
                                String.format(CANT_PARSE_MANDATORY_FIELD, testFieldName, testErrorMessage));
        }

        @Test
        void test_getFieldValue_throwsWhen_parseFieldForMandatoryFieldThrowsScraperException() throws ScraperException {
                when(mockResponseParser.parseField(testFieldName)).thenThrow(new ScraperException(testErrorMessage));

                assertThrowsExactly(ScraperException.class,
                                () -> houseFactory.getFieldValue(mockResponseParser, testFieldName, String.class,
                                                true),
                                testErrorMessage);
        }

        @Test
        void test_getFieldValue_throwsWhen_parseFieldForMandatoryStringNull() throws ScraperException {
                when(mockResponseParser.parseField(testFieldName)).thenReturn(null);

                assertThrowsExactly(ScraperException.class,
                                () -> houseFactory.getFieldValue(mockResponseParser, testFieldName, String.class,
                                                true),
                                String.format(EMPTY_MANDATORY_FIELD_PARSED, testFieldName));
        }

        @Test
        void test_getFieldValue_throwsWhen_parseFieldForMandatoryStringEmpty() throws ScraperException {
                when(mockResponseParser.parseField(testFieldName)).thenReturn(" ");

                assertThrowsExactly(ScraperException.class,
                                () -> houseFactory.getFieldValue(mockResponseParser, testFieldName, String.class,
                                                true),
                                String.format(EMPTY_MANDATORY_FIELD_PARSED, testFieldName));
        }

        @Test
        void test_getFieldValue_returnsNullWhen_parseFieldForNonMandatoryStringNull() throws ScraperException {
                when(mockResponseParser.parseField(testFieldName)).thenReturn(null);

                Object actualValue = houseFactory.getFieldValue(mockResponseParser, testFieldName,
                                String.class,
                                false);
                assertNull(actualValue);
        }

        @Test
        void test_getFieldValue_returnsNullWhen_parseFieldForNonMandatoryFieldThrowsException()
                        throws ScraperException {
                when(mockResponseParser.parseField(testFieldName)).thenThrow(new RuntimeException(testErrorMessage));

                Object actualValue = houseFactory.getFieldValue(mockResponseParser, testFieldName, String.class,
                                false);
                assertNull(actualValue);
        }

        @Test
        void test_getFieldValue_returnsNullWhen_parseFieldForNonMandatoryFieldThrowsScraperException()
                        throws ScraperException {
                when(mockResponseParser.parseField(testFieldName)).thenThrow(new ScraperException(testErrorMessage));

                Object actualValue = houseFactory.getFieldValue(mockResponseParser, testFieldName, String.class,
                                false);
                assertNull(actualValue);
        }

        @Test
        void test_getFirstNumberIn_withFloatingPoint() throws ScraperException {
                String testText = " a3.14 b";
                String actualText = houseFactory.getFirstNumberIn(testText);

                assertEquals("3.14", actualText);
        }

        @Test
        void test_getFirstNumberIn_withInteger() throws ScraperException {
                String testText = "sa,dkj14bsa ";
                String actualText = houseFactory.getFirstNumberIn(testText);

                assertEquals("14", actualText);
        }

        @Test
        void test_getFirstNumberIn_throwsWhen_NullText() throws ScraperException {
                String testText = null;
                assertThrowsExactly(ScraperException.class, () -> houseFactory.getFirstNumberIn(testText),
                                String.format(EMPTY_TEXT_FOR_NUMBER, testText));
        }

        @Test
        void test_getFirstNumberIn_throwsWhen_EmptyText() throws ScraperException {
                String testText = "";
                assertThrowsExactly(ScraperException.class, () -> houseFactory.getFirstNumberIn(testText),
                                String.format(EMPTY_TEXT_FOR_NUMBER, testText));
        }

        @Test
        void test_getFirstNumberIn_throwsWhen_NoNumberInText() throws ScraperException {
                String testText = ";kauf .af,d";
                assertThrowsExactly(ScraperException.class, () -> houseFactory.getFirstNumberIn(testText),
                                String.format(NO_NUMBER_IN_TEXT, testText));
        }

        @Test
        void test_updateWithScrapedValues() throws ScraperException {
                Product existingProduct = new Product(expectedIdInSite, testProductName, SALE, HOUSE, testSiteName,
                                testProductDesc, new BigDecimal(testProductPriceString), testProductStatus,
                                testEndpoint, null,
                                new ArrayList<>(),
                                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

                Product scrapedProduct = new Product(expectedIdInSite, testProductName1, SALE, HOUSE, testSiteName,
                                testProductDesc1, new BigDecimal(testProductPriceString1), testProductStatus1,
                                testEndpoint, null, null,
                                null, null, List.of(testEndpoint));

                houseFactory.updateWithScrapedValues(existingProduct, scrapedProduct);

                assertEquals(scrapedProduct.getName(), existingProduct.getName());
                assertEquals(scrapedProduct.getDescription(), existingProduct.getDescription());
                assertEquals(scrapedProduct.getPrice(), existingProduct.getPrice());
                assertEquals(scrapedProduct.getStatus(), existingProduct.getStatus());
                assertEquals(scrapedProduct.getImageApiEndpoints(), existingProduct.getImageApiEndpoints());

                assertEquals(0, existingProduct.getActionHistory().size());
                assertEquals(1, existingProduct.getStatusHistory().size());
                assertNotNull(existingProduct.getStatusHistory().get(0).getTime());
                assertEquals(testProductStatus, existingProduct.getStatusHistory().get(0).getStatus());

                assertEquals(1, existingProduct.getPriceHistory().size());
                assertNotNull(existingProduct.getPriceHistory().get(0).getTime());
                assertEquals(new BigDecimal(testProductPriceString),
                                existingProduct.getPriceHistory().get(0).getPrice());
        }

        @Test
        void test_remove() {
                Product testProduct = new Product(expectedIdInSite, testProductName, SALE, HOUSE, testSiteName,
                                testProductDesc, new BigDecimal(testProductPriceString), testProductStatus,
                                testEndpoint, null,
                                null, null, new ArrayList<>(), null);

                houseFactory.remove(testProduct);

                assertEquals(Constants.PRODUCT_STATE.REMOVED.name(), testProduct.getStatus());
                assertEquals(1, testProduct.getStatusHistory().size());
                assertNotNull(testProduct.getStatusHistory().get(0).getTime());
                assertEquals(testProductStatus, testProduct.getStatusHistory().get(0).getStatus());
        }

        @Test
        void test_unfollow() {
                Product testProduct = new Product(expectedIdInSite, testProductName, SALE, HOUSE, testSiteName,
                                testProductDesc, new BigDecimal(testProductPriceString), testProductStatus,
                                testEndpoint, null,
                                new ArrayList<>(), null, null, null);
                String testUnfollowDescription = "testUnfollowDescription";

                houseFactory.unfollow(testProduct, testUnfollowDescription);

                assertEquals(1, testProduct.getActionHistory().size());
                assertNotNull(testProduct.getActionHistory().get(0).getTime());
                assertEquals(Constants.ACTION_TYPE.UNFOLLOWED, testProduct.getActionHistory().get(0).getType());
                assertEquals(testUnfollowDescription, testProduct.getActionHistory().get(0).getDescription());
        }
}
