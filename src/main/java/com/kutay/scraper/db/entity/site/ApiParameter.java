package com.kutay.scraper.db.entity.site;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ApiParameter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String apiName;
    private String prefix;
    private String suffix;
    private String innerPrefix;
    private String innerSuffix;
    private String delimiter;

    public ApiParameter() {
    }

    public ApiParameter(String name, String apiName, String prefix, String suffix, String innerPrefix,
            String innerSuffix, String delimiter) {
        this.name = name;
        this.apiName = apiName;
        this.prefix = prefix;
        this.suffix = suffix;
        this.innerPrefix = innerPrefix;
        this.innerSuffix = innerSuffix;
        this.delimiter = delimiter;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getApiName() {
        return apiName;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getInnerPrefix() {
        return innerPrefix;
    }

    public String getInnerSuffix() {
        return innerSuffix;
    }

    public String getDelimiter() {
        return delimiter;
    }
}
