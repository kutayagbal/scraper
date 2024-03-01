package com.kutay.scraper.db.entity.site;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

@Entity
public class ApiResponsePath {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer alternativePathIndex;
    private String attribute;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "apiResponsePath")
    @OrderBy("functionIndex ASC")
    private List<ApiResponseFunction> apiResponseFunctions = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "apiResponsePath")
    @OrderBy("functionIndex ASC")
    private List<ApiStringFunction> apiStringFunctions = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "alternative")
    @OrderBy("alternativePathIndex ASC")
    private List<ApiResponsePath> alternatives = new ArrayList<>();

    public ApiResponsePath() {
    }

    public ApiResponsePath(String name, Integer alternativePathIndex, String attribute,
            List<ApiResponseFunction> apiResponseFunctions, List<ApiStringFunction> apiStringFunctions,
            List<ApiResponsePath> alternatives) {
        this.name = name;
        this.alternativePathIndex = alternativePathIndex;
        this.attribute = attribute;
        this.apiResponseFunctions = apiResponseFunctions;
        this.apiStringFunctions = apiStringFunctions;
        this.alternatives = alternatives;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getAlternativePathIndex() {
        return alternativePathIndex;
    }

    public String getAttribute() {
        return attribute;
    }

    public List<ApiResponseFunction> getApiResponseFunctions() {
        return apiResponseFunctions;
    }

    public List<ApiStringFunction> getApiStringFunctions() {
        return apiStringFunctions;
    }

    public List<ApiResponsePath> getAlternatives() {
        return alternatives;
    }

    @Override
    public String toString() {
        return "ApiResponsePath [name=" + name + ", alternativePathIndex=" + alternativePathIndex + ", attribute="
                + attribute + ", apiResponseFunctions=" + apiResponseFunctions + ", apiStringFunctions="
                + apiStringFunctions + ", alternatives=" + alternatives + "]";
    }

}
