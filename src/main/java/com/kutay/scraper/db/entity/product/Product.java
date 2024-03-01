package com.kutay.scraper.db.entity.product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.kutay.scraper.db.entity.site.ApiEndpoint;
import com.kutay.scraper.util.Constants.PRODUCT_TYPE;
import com.kutay.scraper.util.Constants.TRADE_TYPE;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;

@Entity
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private TRADE_TYPE tradeType;

	@Enumerated(EnumType.STRING)
	private PRODUCT_TYPE productType;

	@Column(length = 10000)
	private String description;

	private String idInSite;
	private String name;
	private String siteName;
	private BigDecimal price;
	private String status;
	private String imageFolder;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "product")
	private ApiEndpoint apiEndpoint;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "product")
	@OrderBy("time DESC")
	private List<ActionHistory> actionHistory = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "product")
	@OrderBy("time DESC")
	private List<PriceHistory> priceHistory = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "product")
	@OrderBy("time DESC")
	private List<StatusHistory> statusHistory = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "product")
	private List<ApiEndpoint> imageApiEndpoints = new ArrayList<>();

	public Product() {
	}

	public Product(String idInSite, String name, TRADE_TYPE tradeType, PRODUCT_TYPE productType, String siteName,
			String description, BigDecimal price, String status, ApiEndpoint apiEndpoint, String imageFolder,
			List<ActionHistory> actionHistory, List<PriceHistory> priceHistory, List<StatusHistory> statusHistory,
			List<ApiEndpoint> imageApiEndpoints) {
		this.idInSite = idInSite;
		this.name = name;
		this.tradeType = tradeType;
		this.productType = productType;
		this.siteName = siteName;
		this.description = description;
		this.price = price;
		this.status = status;
		this.apiEndpoint = apiEndpoint;
		this.imageFolder = imageFolder;
		this.actionHistory = actionHistory;
		this.priceHistory = priceHistory;
		this.statusHistory = statusHistory;
		this.imageApiEndpoints = imageApiEndpoints;
	}

	public String getIdInSite() {
		return idInSite;
	}

	public String getName() {
		return name;
	}

	public TRADE_TYPE getTradeType() {
		return tradeType;
	}

	public PRODUCT_TYPE getProductType() {
		return productType;
	}

	public String getSiteName() {
		return siteName;
	}

	public String getDescription() {
		return description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public String getStatus() {
		return status;
	}

	public String getImageFolder() {
		return imageFolder;
	}

	public List<ActionHistory> getActionHistory() {
		return actionHistory;
	}

	public List<PriceHistory> getPriceHistory() {
		return priceHistory;
	}

	public List<StatusHistory> getStatusHistory() {
		return statusHistory;
	}

	public Long getId() {
		return id;
	}

	public List<ApiEndpoint> getImageApiEndpoints() {
		return imageApiEndpoints;
	}

	public ApiEndpoint getApiEndpoint() {
		return apiEndpoint;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setImageApiEndpoints(List<ApiEndpoint> imageApiEndpoints) {
		this.imageApiEndpoints = imageApiEndpoints;
	}

}