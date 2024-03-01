package com.kutay.scraper.api.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.kutay.scraper.api.request.ApiRequest;
import com.kutay.scraper.db.entity.site.ApiEndpoint;
import com.kutay.scraper.db.entity.site.ApiResponseFunction;
import com.kutay.scraper.db.entity.site.ApiResponsePath;
import com.kutay.scraper.util.Constants.API_FUNCTION_INDEX_TYPE;
import com.kutay.scraper.util.Constants.API_RESPONSE_FUNCTION_TYPE;
import com.kutay.scraper.util.ScraperException;

class JsoupResponseParserTest {
        private static final String testErrorMessage = "testErrorMessage";
        private static final String mockAPIResponsePathName0 = "mockAPIResponsePathName0";
        private static final String mockKeyParameter0 = "mockKeyParameter0";
        private static final String mockKeyParameter1 = "mockKeyParameter1";
        private static final String mockValueParameter = "mockValueParameter";
        private static final String mockBaseUrl = "mockBaseUrl";
        private static final String mockTagStr = "mockTagStr";
        private static ApiResponsePath mockAPIResponsePath0;
        private static Document mockDocument;

        private static Element mockElement0;
        private static Element mockElement1;

        private static JsoupResponseParser jsoupResponseParser;

        @BeforeEach
        void createMocks() {
                mockElement0 = Mockito.mock(Element.class);
                mockElement1 = Mockito.mock(Element.class);
                mockDocument = Mockito.mock(Document.class);
                mockAPIResponsePath0 = new ApiResponsePath(mockAPIResponsePathName0, null, null, null, null, null);

                jsoupResponseParser = new JsoupResponseParser(mockDocument);
        }

        @Test
        void test_applyAlternative_byClass() throws ScraperException {
                ApiResponseFunction testAlternativeResponseFunction0 = new ApiResponseFunction(0, mockKeyParameter0,
                                null, 0, API_RESPONSE_FUNCTION_TYPE.BY_ATTRIBUTE,
                                API_FUNCTION_INDEX_TYPE.FROM_START);

                ApiResponseFunction testAlternativeResponseFunction1 = new ApiResponseFunction(0, mockKeyParameter0,
                                null, 1, API_RESPONSE_FUNCTION_TYPE.BY_CLASS,
                                API_FUNCTION_INDEX_TYPE.FROM_START);
                ApiResponsePath testAlternativeResponsePath = new ApiResponsePath(mockAPIResponsePathName0, 0, null,
                                List.of(testAlternativeResponseFunction0), null,
                                null);

                ApiResponsePath testAlternativeResponsePath1 = new ApiResponsePath(mockAPIResponsePathName0, 1, null,
                                List.of(testAlternativeResponseFunction1), null,
                                null);

                mockAPIResponsePath0 = new ApiResponsePath(mockAPIResponsePathName0, 0, null,
                                null, null, List.of(testAlternativeResponsePath, testAlternativeResponsePath1));

                Element expectedElement = new Element(Tag.valueOf(mockTagStr), mockBaseUrl, null);
                Elements expectedElements = new Elements(mockElement0, expectedElement);

                when(mockDocument.getElementsByAttribute(mockKeyParameter0))
                                .thenThrow(new RuntimeException(testErrorMessage));
                when(mockDocument.getElementsByClass(mockKeyParameter0)).thenReturn(expectedElements);

                Element actualElement = jsoupResponseParser.applyAlternativeFunctions(mockAPIResponsePath0,
                                mockDocument);

                assertEquals(expectedElement, actualElement);
        }

        @Test
        void test_applyAlternative_byTag() throws ScraperException {
                ApiResponseFunction testAlternativeResponseFunction = new ApiResponseFunction(0, mockTagStr,
                                null, 0, API_RESPONSE_FUNCTION_TYPE.BY_TAG,
                                API_FUNCTION_INDEX_TYPE.FROM_END);
                ApiResponsePath testAlternativeResponsePath = new ApiResponsePath(mockAPIResponsePathName0, 0, null,
                                List.of(testAlternativeResponseFunction), null, null);

                mockAPIResponsePath0 = new ApiResponsePath(mockAPIResponsePathName0, 0, null,
                                null, null, List.of(testAlternativeResponsePath));

                Element expectedElement = new Element(Tag.valueOf(mockTagStr), mockBaseUrl, null);
                Elements expectedElements = new Elements(mockElement0, expectedElement);

                when(mockDocument.getElementsByTag(mockTagStr)).thenReturn(expectedElements);

                Element actualElement = jsoupResponseParser.applyAlternativeFunctions(mockAPIResponsePath0,
                                mockDocument);

                assertEquals(expectedElement, actualElement);
        }

        @Test
        void test_applyAlternative_byAttributeValue() throws ScraperException {
                ApiResponseFunction testAlternativeResponseFunction = new ApiResponseFunction(0, mockKeyParameter0,
                                mockValueParameter, 0, API_RESPONSE_FUNCTION_TYPE.BY_ATTRIBUTE_VALUE,
                                API_FUNCTION_INDEX_TYPE.FROM_START);
                ApiResponsePath testAlternativeResponsePath = new ApiResponsePath(mockAPIResponsePathName0, 0, null,
                                List.of(testAlternativeResponseFunction), null, null);

                mockAPIResponsePath0 = new ApiResponsePath(mockAPIResponsePathName0, 0, null,
                                null, null, List.of(testAlternativeResponsePath));

                Element expectedElement = new Element(Tag.valueOf(mockTagStr), mockBaseUrl, null);
                Elements expectedElements = new Elements(expectedElement, mockElement0);

                when(mockDocument.getElementsByAttributeValue(mockKeyParameter0, mockValueParameter))
                                .thenReturn(expectedElements);

                Element actualElement = jsoupResponseParser.applyAlternativeFunctions(mockAPIResponsePath0,
                                mockDocument);

                assertEquals(expectedElement, actualElement);
        }

        @Test
        void test_applyAlternative_throwsWhen_NoApiResponseFunction() throws ScraperException {
                ApiResponsePath testAlternativeResponsePath = new ApiResponsePath(mockAPIResponsePathName0, 0, null,
                                Collections.emptyList(), null, null);

                mockAPIResponsePath0 = new ApiResponsePath(mockAPIResponsePathName0, 0, null,
                                null, null, List.of(testAlternativeResponsePath));

                assertThrowsExactly(ScraperException.class,
                                () -> jsoupResponseParser.applyAlternativeFunctions(mockAPIResponsePath0,
                                                mockDocument),
                                String.format(JsoupResponseParser.NO_API_RESPONSE_FUNCTION, mockAPIResponsePath0));
        }

        @Test
        void test_applyAlternative_throwsWhen_EmptyAlternativeResponsePaths() throws ScraperException {
                mockAPIResponsePath0 = new ApiResponsePath(mockAPIResponsePathName0, 0, null,
                                null, null, Collections.emptyList());

                assertThrowsExactly(ScraperException.class,
                                () -> jsoupResponseParser.applyAlternativeFunctions(mockAPIResponsePath0,
                                                mockDocument),
                                String.format(JsoupResponseParser.ELEMENT_COULD_NOT_BE_PARSED, mockAPIResponsePath0));
        }

        @Test
        void test_getAPIElement_byAttribute() throws ScraperException {
                ApiResponseFunction mockAPIResponseFunction = new ApiResponseFunction(0, mockKeyParameter0,
                                null, 0, API_RESPONSE_FUNCTION_TYPE.BY_ATTRIBUTE,
                                API_FUNCTION_INDEX_TYPE.FROM_START);

                Attributes attributes = new Attributes();
                attributes.put(mockKeyParameter0, null);
                Element expectedElement = new Element(Tag.valueOf(mockTagStr), mockBaseUrl, attributes);

                Elements expectedElements = new Elements(expectedElement, mockElement0);

                when(mockDocument.getElementsByAttribute(mockKeyParameter0)).thenReturn(expectedElements);

                Element actualElement = jsoupResponseParser.getAPIElement(mockAPIResponseFunction, mockDocument);

                assertEquals(expectedElement, actualElement);
        }

        @Test
        void test_getAPIElement_byAttributeValue() throws ScraperException {
                ApiResponseFunction mockAPIResponseFunction = new ApiResponseFunction(0, mockKeyParameter0,
                                mockValueParameter, 0, API_RESPONSE_FUNCTION_TYPE.BY_ATTRIBUTE_VALUE,
                                API_FUNCTION_INDEX_TYPE.FROM_START);

                Attributes attributes = new Attributes();
                attributes.put(mockKeyParameter0, mockValueParameter);
                Element expectedElement = new Element(Tag.valueOf(mockTagStr), mockBaseUrl, attributes);
                Elements expectedElements = new Elements(expectedElement);

                when(mockDocument.getElementsByAttributeValue(mockKeyParameter0, mockValueParameter))
                                .thenReturn(expectedElements);

                Element actualElement = jsoupResponseParser.getAPIElement(mockAPIResponseFunction, mockDocument);

                assertEquals(expectedElement, actualElement);
        }

        @Test
        void test_getAPIElement_byTag() throws ScraperException {
                ApiResponseFunction mockAPIResponseFunction = new ApiResponseFunction(0, mockTagStr,
                                null, 0, API_RESPONSE_FUNCTION_TYPE.BY_TAG,
                                API_FUNCTION_INDEX_TYPE.FROM_END);

                Element expectedElement = new Element(Tag.valueOf(mockTagStr), mockBaseUrl, null);
                Elements expectedElements = new Elements(mockElement0, expectedElement);

                when(mockDocument.getElementsByTag(mockTagStr)).thenReturn(expectedElements);

                Element actualElement = jsoupResponseParser.getAPIElement(mockAPIResponseFunction, mockDocument);

                assertEquals(expectedElement, actualElement);
        }

        @Test
        void test_getAPIElement_byClass() throws ScraperException {
                ApiResponseFunction mockAPIResponseFunction = new ApiResponseFunction(0, mockKeyParameter0,
                                null, 2, API_RESPONSE_FUNCTION_TYPE.BY_CLASS,
                                API_FUNCTION_INDEX_TYPE.FROM_END);

                Attributes attributes = new Attributes();
                attributes.put(mockKeyParameter0, null);
                Element expectedElement = new Element(Tag.valueOf(mockTagStr), mockBaseUrl, attributes);
                Elements expectedElements = new Elements(expectedElement, mockElement0, mockElement1);

                when(mockDocument.getElementsByClass(mockKeyParameter0)).thenReturn(expectedElements);

                Element actualElement = jsoupResponseParser.getAPIElement(mockAPIResponseFunction, mockDocument);

                assertEquals(expectedElement, actualElement);
        }

        @Test
        void test_getAPIElement_byOwnText() throws ScraperException {
                ApiResponseFunction mockAPIResponseFunction = new ApiResponseFunction(0, mockKeyParameter0,
                                null, 0, API_RESPONSE_FUNCTION_TYPE.BY_OWN_TEXT,
                                API_FUNCTION_INDEX_TYPE.FROM_START);

                Attributes attributes = new Attributes();
                attributes.put(mockKeyParameter0, null);
                Element expectedElement = new Element(Tag.valueOf(mockTagStr), mockBaseUrl, attributes);
                Elements expectedElements = new Elements(expectedElement, mockElement0, mockElement1);

                when(mockDocument.getElementsMatchingOwnText(mockKeyParameter0)).thenReturn(expectedElements);

                Element actualElement = jsoupResponseParser.getAPIElement(mockAPIResponseFunction, mockDocument);

                assertEquals(expectedElement, actualElement);
        }

        @Test
        void test_getAPIElement_byNextSibling() throws ScraperException {
                ApiResponseFunction mockAPIResponseFunction = new ApiResponseFunction(0, mockKeyParameter0,
                                null, 0, API_RESPONSE_FUNCTION_TYPE.NEXT_SIBLING,
                                API_FUNCTION_INDEX_TYPE.FROM_START);

                Element expectedElement = new Element(Tag.valueOf(mockTagStr), mockBaseUrl, null);

                when(mockDocument.nextElementSibling()).thenReturn(expectedElement);

                Element actualElement = jsoupResponseParser.getAPIElement(mockAPIResponseFunction, mockDocument);

                assertEquals(expectedElement, actualElement);
        }

        @Test
        void test_getAPIElement_bySetTextIfTag() throws ScraperException {
                ApiResponseFunction mockAPIResponseFunction = new ApiResponseFunction(0, mockKeyParameter0,
                                mockValueParameter, 0, API_RESPONSE_FUNCTION_TYPE.SET_TEXT_IF_TAG,
                                API_FUNCTION_INDEX_TYPE.FROM_START);

                Element expectedElement = new Element(Tag.valueOf("P"), mockBaseUrl, null)
                                .appendText(mockValueParameter);

                when(mockDocument.tagName()).thenReturn(mockKeyParameter0);

                Element actualElement = jsoupResponseParser.getAPIElement(mockAPIResponseFunction, mockDocument);
                assertEquals(expectedElement.html(), actualElement.html());
        }

        @Test
        void test_getAPIElement_byChildren() throws ScraperException {
                ApiResponseFunction mockAPIResponseFunction = new ApiResponseFunction(0, null,
                                null, 0, API_RESPONSE_FUNCTION_TYPE.CHILDREN,
                                API_FUNCTION_INDEX_TYPE.FROM_START);

                Element expectedElement = new Element(Tag.valueOf(mockTagStr), mockBaseUrl, null);
                Elements expectedElements = new Elements(expectedElement, mockElement0);

                when(mockDocument.children()).thenReturn(expectedElements);

                Element actualElement = jsoupResponseParser.getAPIElement(mockAPIResponseFunction, mockDocument);
                assertEquals(expectedElement, actualElement);
        }

        @Test
        void test_getAPIElement_throwsWhenNoElementFound() throws ScraperException {
                ApiResponseFunction mockAPIResponseFunction = new ApiResponseFunction(0, mockKeyParameter0,
                                null, 2, API_RESPONSE_FUNCTION_TYPE.BY_CLASS,
                                API_FUNCTION_INDEX_TYPE.FROM_END);

                when(mockDocument.getElementsByClass(mockKeyParameter0)).thenReturn(null);

                assertThrowsExactly(ScraperException.class,
                                () -> jsoupResponseParser.getAPIElement(mockAPIResponseFunction, mockDocument),
                                JsoupResponseParser.ELEMENT_COULD_NOT_BE_FOUND);
        }

        @Test
        void test_getAPIElements_byAttribute() throws ScraperException {
                ApiResponseFunction mockAPIResponseFunction = new ApiResponseFunction(0, mockKeyParameter0,
                                null, 0, API_RESPONSE_FUNCTION_TYPE.BY_ATTRIBUTE,
                                API_FUNCTION_INDEX_TYPE.FROM_START);

                Attributes attributes = new Attributes();
                attributes.put(mockKeyParameter0, null);
                Element expectedElement = new Element(Tag.valueOf(mockTagStr), mockBaseUrl, attributes);
                Elements expectedElements = new Elements(expectedElement, mockElement0);

                when(mockDocument.getElementsByAttribute(mockKeyParameter0)).thenReturn(expectedElements);

                Elements actualElements = jsoupResponseParser.getAPIElements(mockAPIResponseFunction, mockDocument);

                assertEquals(expectedElements, actualElements);
        }

        @Test
        void test_getAPIElements_byAttributeValue() throws ScraperException {
                ApiResponseFunction mockAPIResponseFunction = new ApiResponseFunction(0, mockKeyParameter0,
                                mockValueParameter, 0, API_RESPONSE_FUNCTION_TYPE.BY_ATTRIBUTE_VALUE,
                                API_FUNCTION_INDEX_TYPE.FROM_START);

                Attributes attributes = new Attributes();
                attributes.put(mockKeyParameter0, mockValueParameter);
                Element expectedElement = new Element(Tag.valueOf(mockTagStr), mockBaseUrl, attributes);
                Elements expectedElements = new Elements(expectedElement);

                when(mockDocument.getElementsByAttributeValue(mockKeyParameter0, mockValueParameter))
                                .thenReturn(expectedElements);

                Elements actualElements = jsoupResponseParser.getAPIElements(mockAPIResponseFunction, mockDocument);

                assertEquals(expectedElements, actualElements);
        }

        @Test
        void test_getAPIElements_byTag() throws ScraperException {
                ApiResponseFunction mockAPIResponseFunction = new ApiResponseFunction(0, mockKeyParameter0,
                                null, 0, API_RESPONSE_FUNCTION_TYPE.BY_TAG,
                                API_FUNCTION_INDEX_TYPE.FROM_START);

                Element expectedElement = new Element(Tag.valueOf(mockTagStr), mockBaseUrl, null);
                Elements expectedElements = new Elements(expectedElement);

                when(mockDocument.getElementsByTag(mockKeyParameter0))
                                .thenReturn(expectedElements);

                Elements actualElements = jsoupResponseParser.getAPIElements(mockAPIResponseFunction, mockDocument);

                assertEquals(expectedElements, actualElements);
        }

        @Test
        void test_getAPIElements_byOwnText() throws ScraperException {
                ApiResponseFunction mockAPIResponseFunction = new ApiResponseFunction(0, mockKeyParameter0,
                                mockValueParameter, 0, API_RESPONSE_FUNCTION_TYPE.BY_OWN_TEXT,
                                API_FUNCTION_INDEX_TYPE.FROM_START);

                Element expectedElement = new Element(Tag.valueOf(mockTagStr), mockBaseUrl, null);
                Elements expectedElements = new Elements(expectedElement);

                when(mockDocument.getElementsMatchingOwnText(mockKeyParameter0))
                                .thenReturn(expectedElements);

                Elements actualElements = jsoupResponseParser.getAPIElements(mockAPIResponseFunction, mockDocument);

                assertEquals(expectedElements, actualElements);
        }

        @Test
        void test_findElement() throws ScraperException {
                ApiResponseFunction mockAPIResponseFunction0 = new ApiResponseFunction(0, mockKeyParameter0,
                                null, 0, API_RESPONSE_FUNCTION_TYPE.BY_CLASS,
                                API_FUNCTION_INDEX_TYPE.FROM_START);
                ApiResponseFunction mockAPIResponseFunction1 = new ApiResponseFunction(1, mockKeyParameter1,
                                null, 0, API_RESPONSE_FUNCTION_TYPE.BY_CLASS,
                                API_FUNCTION_INDEX_TYPE.FROM_END);
                mockAPIResponsePath0 = new ApiResponsePath(mockAPIResponsePathName0, null, null,
                                List.of(mockAPIResponseFunction0, mockAPIResponseFunction1), null, null);
                JsoupResponseParser jsoupDocumentParser = new JsoupResponseParser(mockDocument);
                jsoupDocumentParser.setApiResponsePaths(List.of(mockAPIResponsePath0));

                when(mockDocument.getElementsByClass(mockKeyParameter0))
                                .thenReturn(new Elements(mockElement0, mockElement1));
                when(mockElement0.getElementsByClass(mockKeyParameter1)).thenReturn(new Elements(mockElement1));

                Element actualElement = jsoupDocumentParser.findElement(mockAPIResponsePath0, mockDocument);
                assertEquals(mockElement1, actualElement);
        }

        @Test
        void test_applyAlternativeFunctions() throws ScraperException {
                ApiResponseFunction mockAPIResponseFunction0 = new ApiResponseFunction(0, mockKeyParameter0,
                                null, 0, API_RESPONSE_FUNCTION_TYPE.BY_CLASS,
                                API_FUNCTION_INDEX_TYPE.FROM_START);
                ApiResponseFunction mockAPIResponseFunction1 = new ApiResponseFunction(1, mockKeyParameter1,
                                null, 0, API_RESPONSE_FUNCTION_TYPE.BY_CLASS,
                                API_FUNCTION_INDEX_TYPE.FROM_END);
                mockAPIResponsePath0 = new ApiResponsePath(mockAPIResponsePathName0, null, null,
                                List.of(mockAPIResponseFunction0, mockAPIResponseFunction1), null, null);
                JsoupResponseParser jsoupDocumentParser = new JsoupResponseParser(mockDocument);
                jsoupDocumentParser.setApiResponsePaths(List.of(mockAPIResponsePath0));

                when(mockDocument.getElementsByClass(mockKeyParameter0))
                                .thenReturn(new Elements(mockElement0, mockElement1));
                when(mockElement0.getElementsByClass(mockKeyParameter1)).thenReturn(new Elements(mockElement1));

                Element actualElement = jsoupDocumentParser.findElement(mockAPIResponsePath0, mockDocument);
                assertEquals(mockElement1, actualElement);
        }

        @Test
        void test_findElements() throws ScraperException {
                ApiResponseFunction mockAPIResponseFunction0 = new ApiResponseFunction(0, mockKeyParameter0,
                                null, 0, API_RESPONSE_FUNCTION_TYPE.BY_CLASS,
                                API_FUNCTION_INDEX_TYPE.FROM_START);
                ApiResponseFunction mockAPIResponseFunction1 = new ApiResponseFunction(1, mockKeyParameter1,
                                null, 0, API_RESPONSE_FUNCTION_TYPE.BY_CLASS,
                                API_FUNCTION_INDEX_TYPE.FROM_END);
                mockAPIResponsePath0 = new ApiResponsePath(mockAPIResponsePathName0, null, null,
                                List.of(mockAPIResponseFunction0, mockAPIResponseFunction1), null, null);
                JsoupResponseParser jsoupDocumentParser = new JsoupResponseParser(mockDocument);
                jsoupDocumentParser.setApiResponsePaths(List.of(mockAPIResponsePath0));

                when(mockDocument.getElementsByClass(mockKeyParameter0))
                                .thenReturn(new Elements(mockElement0, mockElement1));
                when(mockElement0.getElementsByClass(mockKeyParameter1)).thenReturn(new Elements(mockElement0));
                when(mockElement1.getElementsByClass(mockKeyParameter1)).thenReturn(new Elements(mockElement1));

                Elements actualElements = jsoupDocumentParser.findElements(mockAPIResponsePath0, mockDocument);
                assertEquals(List.of(mockElement0, mockElement1), actualElements);
        }

        @Test
        void test_findElements_throwsWhen_NullAPIResponseFunctions() throws ScraperException {
                mockAPIResponsePath0 = new ApiResponsePath(mockAPIResponsePathName0, null, null, null, null, null);
                JsoupResponseParser jsoupDocumentParser = new JsoupResponseParser(mockDocument);
                jsoupDocumentParser.setApiResponsePaths(List.of(mockAPIResponsePath0));

                assertThrowsExactly(ScraperException.class,
                                () -> jsoupDocumentParser.findElements(mockAPIResponsePath0, mockDocument),
                                String.format(JsoupResponseParser.NO_API_RESPONSE_FUNCTION, mockAPIResponsePath0));
        }

        @Test
        void test_findElements_throwsWhen_EmptyAPIResponseFunctions() throws ScraperException {
                mockAPIResponsePath0 = new ApiResponsePath(mockAPIResponsePathName0, null, null,
                                Collections.emptyList(),
                                null, null);
                JsoupResponseParser jsoupDocumentParser = new JsoupResponseParser(mockDocument);
                jsoupDocumentParser.setApiResponsePaths(List.of(mockAPIResponsePath0));

                assertThrowsExactly(ScraperException.class,
                                () -> jsoupDocumentParser.findElements(mockAPIResponsePath0, mockDocument),
                                String.format(JsoupResponseParser.NO_API_RESPONSE_FUNCTION, mockAPIResponsePath0));
        }

        @Test
        void test_findElement_throwsWhen_NullAPIResponseFunctions() throws ScraperException {
                mockAPIResponsePath0 = new ApiResponsePath(mockAPIResponsePathName0, null, null, null, null, null);
                JsoupResponseParser jsoupDocumentParser = new JsoupResponseParser(mockDocument);
                jsoupDocumentParser.setApiResponsePaths(List.of(mockAPIResponsePath0));

                assertThrowsExactly(ScraperException.class,
                                () -> jsoupDocumentParser.findElement(mockAPIResponsePath0, mockDocument),
                                String.format(JsoupResponseParser.NO_API_RESPONSE_FUNCTION, mockAPIResponsePath0));
        }

        @Test
        void test_findElement_throwsWhenEmptyAPIResponseFunctions() throws ScraperException {
                mockAPIResponsePath0 = new ApiResponsePath(mockAPIResponsePathName0, null, null,
                                Collections.emptyList(),
                                null, null);
                JsoupResponseParser jsoupDocumentParser = new JsoupResponseParser(mockDocument);
                jsoupDocumentParser.setApiResponsePaths(List.of(mockAPIResponsePath0));

                assertThrowsExactly(ScraperException.class,
                                () -> jsoupDocumentParser.findElement(mockAPIResponsePath0, mockDocument),
                                String.format(JsoupResponseParser.NO_API_RESPONSE_FUNCTION, mockAPIResponsePath0));
        }

        @Test
        void test_parseField() throws ScraperException {
                String mockElementValue = "mockElementValue";
                ApiResponseFunction mockAPIResponseFunction0 = new ApiResponseFunction(0, mockKeyParameter0,
                                null, 0, API_RESPONSE_FUNCTION_TYPE.BY_ATTRIBUTE,
                                API_FUNCTION_INDEX_TYPE.FROM_START);

                mockAPIResponsePath0 = new ApiResponsePath(mockAPIResponsePathName0, null, null,
                                List.of(mockAPIResponseFunction0), null, null);
                JsoupResponseParser jsoupDocumentParser = new JsoupResponseParser(mockDocument);
                jsoupDocumentParser.setApiResponsePaths(List.of(mockAPIResponsePath0));

                Element mockElement0 = new Element(Tag.valueOf(mockTagStr), mockBaseUrl);
                mockElement0.text(mockElementValue);
                when(mockDocument.getElementsByAttribute(mockKeyParameter0))
                                .thenReturn(new Elements(mockElement0));

                String actualValue = jsoupDocumentParser.parseField(mockAPIResponsePathName0);
                assertEquals(mockElementValue, actualValue);
        }

        @Test
        void test_parseField_withAttribute() throws ScraperException {
                String mockElementValue = "mockElementValue";
                ApiResponseFunction mockAPIResponseFunction0 = new ApiResponseFunction(0, mockKeyParameter0,
                                null, 0, API_RESPONSE_FUNCTION_TYPE.BY_ATTRIBUTE,
                                API_FUNCTION_INDEX_TYPE.FROM_START);

                mockAPIResponsePath0 = new ApiResponsePath(mockAPIResponsePathName0, null, mockKeyParameter0,
                                List.of(mockAPIResponseFunction0), null, null);
                JsoupResponseParser jsoupDocumentParser = new JsoupResponseParser(mockDocument);
                jsoupDocumentParser.setApiResponsePaths(List.of(mockAPIResponsePath0));

                Attributes attributes = new Attributes();
                attributes.put(mockKeyParameter0, mockValueParameter);
                Element mockElement0 = new Element(Tag.valueOf(mockTagStr), mockBaseUrl, attributes);
                mockElement0.text(mockElementValue);
                when(mockDocument.getElementsByAttribute(mockKeyParameter0))
                                .thenReturn(new Elements(mockElement0));

                String actualValue = jsoupDocumentParser.parseField(mockAPIResponsePathName0);
                assertEquals(mockValueParameter, actualValue);
        }

        @Test
        void test_parseFields() throws ScraperException {
                String mockElementValue0 = "mockElementValue0";
                String mockElementValue1 = "mockElementValue1";
                ApiResponseFunction mockAPIResponseFunction0 = new ApiResponseFunction(0, mockKeyParameter0,
                                null, 0, API_RESPONSE_FUNCTION_TYPE.BY_ATTRIBUTE,
                                API_FUNCTION_INDEX_TYPE.FROM_START);

                mockAPIResponsePath0 = new ApiResponsePath(mockAPIResponsePathName0, null, null,
                                List.of(mockAPIResponseFunction0), null, null);
                JsoupResponseParser jsoupDocumentParser = new JsoupResponseParser(mockDocument);
                jsoupDocumentParser.setApiResponsePaths(List.of(mockAPIResponsePath0));

                Element mockElement0 = new Element(Tag.valueOf(mockTagStr), mockBaseUrl);
                mockElement0.text(mockElementValue0);
                Element mockElement1 = new Element(Tag.valueOf(mockTagStr), mockBaseUrl);
                mockElement1.text(mockElementValue1);
                when(mockDocument.getElementsByAttribute(mockKeyParameter0))
                                .thenReturn(new Elements(mockElement0, mockElement1));

                List<String> actualValues = jsoupDocumentParser.parseFields(mockAPIResponsePathName0);
                assertEquals(List.of(mockElementValue0, mockElementValue1), actualValues);
        }

        @Test
        void test_parseFields_withAttribute() throws ScraperException {
                String mockAttributeValue0 = "mockElementValue0";
                String mockAttributeValue1 = "mockElementValue1";
                ApiResponseFunction mockAPIResponseFunction0 = new ApiResponseFunction(0, mockKeyParameter0,
                                null, 0, API_RESPONSE_FUNCTION_TYPE.BY_ATTRIBUTE,
                                API_FUNCTION_INDEX_TYPE.FROM_START);

                mockAPIResponsePath0 = new ApiResponsePath(mockAPIResponsePathName0, null, mockKeyParameter0,
                                List.of(mockAPIResponseFunction0), null, null);
                JsoupResponseParser jsoupDocumentParser = new JsoupResponseParser(mockDocument);
                jsoupDocumentParser.setApiResponsePaths(List.of(mockAPIResponsePath0));

                Attributes attributes0 = new Attributes();
                attributes0.put(mockKeyParameter0, mockAttributeValue0);
                Element mockElement0 = new Element(Tag.valueOf(mockTagStr), mockBaseUrl, attributes0);

                Attributes attributes1 = new Attributes();
                attributes1.put(mockKeyParameter0, mockAttributeValue1);
                Element mockElement1 = new Element(Tag.valueOf(mockTagStr), mockBaseUrl, attributes1);
                when(mockDocument.getElementsByAttribute(mockKeyParameter0))
                                .thenReturn(new Elements(mockElement0, mockElement1));

                List<String> actualValues = jsoupDocumentParser.parseFields(mockAPIResponsePathName0);
                assertEquals(List.of(mockAttributeValue0, mockAttributeValue1), actualValues);
        }

        @Test
        void test_parseProductAPIRequests() throws ScraperException {
                String mockProtocol = "http";
                String mockHost = "mockHost";
                int mockPort = 80;
                String mockPath = "mockPath";
                String mockElementValue = "mockElementValue";
                String mockAttributeValue = mockProtocol + "://" + mockHost + ":" + mockPort + "/" + mockPath;

                ApiResponseFunction mockAPIResponseFunction0 = new ApiResponseFunction(0,
                                "href",
                                null, 0, API_RESPONSE_FUNCTION_TYPE.BY_ATTRIBUTE,
                                API_FUNCTION_INDEX_TYPE.FROM_START);

                mockAPIResponsePath0 = new ApiResponsePath(JsoupResponseParser.PRODUCT_API_ENDPOINTS_FIELD_NAME, null,
                                "href",
                                List.of(mockAPIResponseFunction0), null, null);
                JsoupResponseParser jsoupDocumentParser = new JsoupResponseParser(mockDocument);
                jsoupDocumentParser.setApiResponsePaths(List.of(mockAPIResponsePath0));

                Attributes attributes = new Attributes();
                attributes.put("href", mockAttributeValue);
                Element mockElement = new Element(Tag.valueOf(mockTagStr), mockBaseUrl, attributes);
                mockElement.text(mockElementValue);
                when(mockDocument.getElementsByAttribute("href"))
                                .thenReturn(new Elements(mockElement));

                List<ApiRequest> actualApiRequests = jsoupDocumentParser.parseProductApiRequests();
                assertEquals(1, actualApiRequests.size());

                ApiEndpoint actualAPIEndpoint = actualApiRequests.get(0).getApiEndpoint();
                assertEquals(mockProtocol, actualAPIEndpoint.getProtocol());
                assertEquals(mockHost, actualAPIEndpoint.getHost());
                assertEquals(mockPort, actualAPIEndpoint.getPort());
                assertEquals(JsoupResponseParser.LANGUAGE + "/" + mockPath, actualAPIEndpoint.getPath());
                assertNull(actualAPIEndpoint.getTradeType());
                assertNull(actualAPIEndpoint.getProductType());
                assertEquals(null, actualApiRequests.get(0).getRequest());
        }

        @Test
        void test_parseProductAPIRequests_doesntThrowWhen_InvalidURL() throws ScraperException {
                String mockProtocol = "mockProtocol";
                String mockHost = "mockHost";
                int mockPort = 80;
                String mockPath = "mockPath";
                String mockElementValue = "mockElementValue";
                String mockAttributeValue = mockProtocol + "://" + mockHost + ":" + mockPort + "/" + mockPath;

                ApiResponseFunction mockAPIResponseFunction0 = new ApiResponseFunction(0,
                                JsoupResponseParser.PRODUCT_API_ENDPOINTS_FIELD_NAME,
                                null, 0, API_RESPONSE_FUNCTION_TYPE.BY_ATTRIBUTE,
                                API_FUNCTION_INDEX_TYPE.FROM_START);

                mockAPIResponsePath0 = new ApiResponsePath(JsoupResponseParser.PRODUCT_API_ENDPOINTS_FIELD_NAME, null,
                                null,
                                List.of(mockAPIResponseFunction0), null, null);
                JsoupResponseParser jsoupDocumentParser = new JsoupResponseParser(mockDocument);
                jsoupDocumentParser.setApiResponsePaths(List.of(mockAPIResponsePath0));

                Attributes attributes = new Attributes();
                attributes.put("href", mockAttributeValue);
                Element mockElement = new Element(Tag.valueOf(mockTagStr), mockBaseUrl, attributes);
                mockElement.text(mockElementValue);
                when(mockDocument.getElementsByAttribute(JsoupResponseParser.PRODUCT_API_ENDPOINTS_FIELD_NAME))
                                .thenReturn(new Elements(mockElement));

                List<ApiRequest> actualApiRequests = jsoupDocumentParser.parseProductApiRequests();
                assertEquals(0, actualApiRequests.size());
        }
}
