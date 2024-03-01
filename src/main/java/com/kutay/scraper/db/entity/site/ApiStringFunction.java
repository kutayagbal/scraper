package com.kutay.scraper.db.entity.site;

import com.kutay.scraper.util.Constants.API_STRING_FUNCTION_TYPE;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ApiStringFunction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private API_STRING_FUNCTION_TYPE type;

    private Integer functionIndex;
    private String keyParameter;
    private String valueParameter;
    private Integer keyIndex;
    private Integer valueIndex;

    public ApiStringFunction() {
    }

    public ApiStringFunction(API_STRING_FUNCTION_TYPE type, Integer functionIndex, String keyParameter,
            String valueParameter, Integer keyIndex, Integer valueIndex) {
        this.type = type;
        this.functionIndex = functionIndex;
        this.keyParameter = keyParameter;
        this.valueParameter = valueParameter;
        this.keyIndex = keyIndex;
        this.valueIndex = valueIndex;
    }

    public Long getId() {
        return id;
    }

    public API_STRING_FUNCTION_TYPE getType() {
        return type;
    }

    public Integer getFunctionIndex() {
        return functionIndex;
    }

    public String getKeyParameter() {
        return keyParameter;
    }

    public String getValueParameter() {
        return valueParameter;
    }

    public Integer getKeyIndex() {
        return keyIndex;
    }

    public Integer getValueIndex() {
        return valueIndex;
    }
}
