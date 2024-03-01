package com.kutay.scraper.util;

public class ScraperException extends Exception {

    public ScraperException(String message) {
        super(message);
    }

    public ScraperException(String message, Throwable e) {
        super(message, e);
    }

}
