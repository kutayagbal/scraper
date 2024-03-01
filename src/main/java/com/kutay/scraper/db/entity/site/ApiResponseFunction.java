package com.kutay.scraper.db.entity.site;

import com.kutay.scraper.util.Constants.API_FUNCTION_INDEX_TYPE;
import com.kutay.scraper.util.Constants.API_RESPONSE_FUNCTION_TYPE;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ApiResponseFunction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private API_RESPONSE_FUNCTION_TYPE type;

    @Enumerated(EnumType.STRING)
    private API_FUNCTION_INDEX_TYPE resultIndexType;

    private Integer functionIndex;
    private String keyParameter;
    private String valueParameter;
    private Integer resultIndex;

    public ApiResponseFunction() {
    }

    public ApiResponseFunction(Integer functionIndex, String keyParameter, String valueParameter, Integer resultIndex,
            API_RESPONSE_FUNCTION_TYPE type,
            API_FUNCTION_INDEX_TYPE resultIndexType) {
        this.functionIndex = functionIndex;
        this.keyParameter = keyParameter;
        this.valueParameter = valueParameter;
        this.resultIndex = resultIndex;
        this.type = type;
        this.resultIndexType = resultIndexType;
    }

    public Integer getFunctionIndex() {
        return functionIndex;
    }

    public API_RESPONSE_FUNCTION_TYPE getType() {
        return type;
    }

    public String getKeyParameter() {
        return keyParameter;
    }

    public String getValueParameter() {
        return valueParameter;
    }

    public Long getId() {
        return id;
    }

    public Integer getResultIndex() {
        return resultIndex;
    }

    public API_FUNCTION_INDEX_TYPE getResultIndexType() {
        return resultIndexType;
    }

    @Override
    public String toString() {
        return "ApiResponseFunction [type=" + type + ", resultIndexType=" + resultIndexType + ", functionIndex="
                + functionIndex + ", keyParameter=" + keyParameter + ", valueParameter=" + valueParameter
                + ", resultIndex=" + resultIndex + "]";
    }

}
