package com.kutay.scraper.db.entity.site;

import java.util.List;

import com.kutay.scraper.util.Constants.PRODUCT_TYPE;
import com.kutay.scraper.util.Constants.TRADE_TYPE;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

@Entity
public class ApiEndpoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TRADE_TYPE tradeType;

    @Enumerated(EnumType.STRING)
    private PRODUCT_TYPE productType;

    private String protocol;
    private String host;
    private Integer port;
    private String path;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "apiEndpoint")
    private List<ApiParameter> parameters;

    public ApiEndpoint() {
    }

    public ApiEndpoint(TRADE_TYPE tradeType, PRODUCT_TYPE productType, String protocol, String host, Integer port,
            String path,
            List<ApiParameter> parameters) {
        this.tradeType = tradeType;
        this.productType = productType;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.path = path;
        this.parameters = parameters;
    }

    public Long getId() {
        return id;
    }

    public TRADE_TYPE getTradeType() {
        return tradeType;
    }

    public PRODUCT_TYPE getProductType() {
        return productType;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public String getPath() {
        return path;
    }

    public List<ApiParameter> getParameters() {
        return parameters;
    }

    public Integer getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "ApiEndpoint [tradeType=" + tradeType + ", productType=" + productType + ", protocol=" + protocol
                + ", host=" + host + ", port=" + port + ", path=" + path + "]";
    }

}
