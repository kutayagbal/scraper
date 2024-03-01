package com.kutay.scraper.db.entity.site;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

@Entity
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "site")
    private List<Component> components;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "site")
    private List<ApiEndpoint> apiEndpoints;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "site")
    private List<ApiResponsePath> apiResponsePaths;

    public Site() {
    }

    public Site(String name, List<Component> components,
            List<ApiEndpoint> apiEndpoints,
            List<ApiResponsePath> apiResponsePaths) {
        this.name = name;
        this.components = components;
        this.apiEndpoints = apiEndpoints;
        this.apiResponsePaths = apiResponsePaths;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<ApiResponsePath> getApiResponsePaths() {
        return apiResponsePaths;
    }

    public List<ApiEndpoint> getApiEndpoints() {
        return apiEndpoints;
    }

    public List<Component> getComponents() {
        return components;
    }

}
