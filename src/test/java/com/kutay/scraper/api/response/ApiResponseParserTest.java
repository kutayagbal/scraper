package com.kutay.scraper.api.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.util.Collections;
import java.util.List;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.kutay.scraper.db.entity.site.ApiResponsePath;
import com.kutay.scraper.db.entity.site.ApiStringFunction;
import com.kutay.scraper.util.Constants.API_STRING_FUNCTION_TYPE;
import com.kutay.scraper.util.ScraperException;

class ApiResponseParserTest {
    private static final String testFieldName0 = "testFieldName0";
    private static final String testFieldName1 = "testFieldName1";
    private static final String testFieldName2 = "testFieldName2";

    private static ApiResponsePath testResponsePath0;
    private static ApiResponsePath testResponsePath1;
    private static Document mockDocument;

    private static JsoupResponseParser jsoupResponseParser;

    @BeforeEach
    void createMocks() {
        mockDocument = Mockito.mock(Document.class);
        testResponsePath0 = new ApiResponsePath(testFieldName0, null, null, null, null, null);
        testResponsePath1 = new ApiResponsePath(testFieldName1, null, null, null, null, null);
        jsoupResponseParser = new JsoupResponseParser(mockDocument);
    }

    @Test
    void test_findAPIResponsePath() throws ScraperException {
        jsoupResponseParser.setApiResponsePaths(List.of(testResponsePath0));

        ApiResponsePath actualAPIResponsePath = jsoupResponseParser.findApiResponsePath(testFieldName0);

        assertEquals(testResponsePath0, actualAPIResponsePath);
    }

    @Test
    void test_findAPIResponsePath_throwsWhen_APIResponsePathNotFound() throws ScraperException {
        jsoupResponseParser.setApiResponsePaths(List.of(testResponsePath0, testResponsePath1));

        assertThrowsExactly(ScraperException.class,
                () -> jsoupResponseParser.findApiResponsePath(testFieldName2),
                String.format(JsoupResponseParser.API_RESPONSE_PATH_NOT_FOUND, testFieldName0));
    }

    @Test
    void test_findAPIResponsePath_throwsWhen_EmptyAPIResponsePaths() throws ScraperException {
        jsoupResponseParser.setApiResponsePaths(Collections.emptyList());

        assertThrowsExactly(ScraperException.class,
                () -> jsoupResponseParser.findApiResponsePath(testFieldName0),
                String.format(JsoupResponseParser.API_RESPONSE_PATH_NOT_FOUND, testFieldName0));
    }

    @Test
    void test_findAPIResponsePath_throwsWhen_NullAPIResponsePaths() throws ScraperException {
        assertThrowsExactly(ScraperException.class,
                () -> jsoupResponseParser.findApiResponsePath(testFieldName0),
                String.format(JsoupResponseParser.API_RESPONSE_PATH_NOT_FOUND, testFieldName0));
    }

    @Test
    void test_findString_BySplit() {
        String testText = "1.2,a.,sk";
        String testSplitString = ",";
        ApiStringFunction testStringFunction = new ApiStringFunction(API_STRING_FUNCTION_TYPE.SPLIT, 0, testSplitString,
                null, null, null);
        ApiResponsePath testResponsePath = new ApiResponsePath(testFieldName0, null, null, null,
                List.of(testStringFunction), null);
        String actualValue = jsoupResponseParser.findString(testResponsePath, testText);
        assertEquals("1.2", actualValue);
    }

    @Test
    void test_findString_BySplitWithIndex() {
        String testText = "akjsn a.adj 12 a.,sk";
        String testSplitString = " ";
        Integer testKeyIndex = 2;
        ApiStringFunction testStringFunction = new ApiStringFunction(API_STRING_FUNCTION_TYPE.SPLIT, 0, testSplitString,
                null, testKeyIndex, null);
        ApiResponsePath testResponsePath = new ApiResponsePath(testFieldName0, null, null, null,
                List.of(testStringFunction), null);
        String actualValue = jsoupResponseParser.findString(testResponsePath, testText);
        assertEquals("12", actualValue);
    }

    @Test
    void test_findString_ByReplace() {
        String testText = "aa,aa.djaas1aa2";
        String expectedText = "bd,bd.djbds1bd2";
        String testKeyParam = "aa";
        String testValueParam = "bd";
        ApiStringFunction testStringFunction = new ApiStringFunction(API_STRING_FUNCTION_TYPE.REPLACE, 0, testKeyParam,
                testValueParam, null, null);
        ApiResponsePath testResponsePath = new ApiResponsePath(testFieldName0, null, null, null,
                List.of(testStringFunction), null);
        String actualValue = jsoupResponseParser.findString(testResponsePath, testText);
        assertEquals(expectedText, actualValue);
    }

    @Test
    void test_findString_BySubstring() {
        String testText = "kjblasdfA?s,.mv ;íafb";
        String expectedText = "?s,.mv ;í";
        Integer testKeyIndex = 9;
        Integer testValueIndex = 18;
        ApiStringFunction testStringFunction = new ApiStringFunction(API_STRING_FUNCTION_TYPE.SUBSTRING, 0,
                null, null, testKeyIndex, testValueIndex);
        ApiResponsePath testResponsePath = new ApiResponsePath(testFieldName0, null, null, null,
                List.of(testStringFunction), null);
        String actualValue = jsoupResponseParser.findString(testResponsePath, testText);
        assertEquals(expectedText, actualValue);
    }

}
