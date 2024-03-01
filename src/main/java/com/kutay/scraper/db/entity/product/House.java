package com.kutay.scraper.db.entity.product;

import java.math.BigDecimal;

import com.kutay.scraper.scrape.ScrapedProduct;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class House extends ScrapedProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String city;
    private String postalCode;
    private String energyLabel;
    private String propertyType;
    private Integer livingArea;
    private Integer constructionYear;
    private Integer story;
    private Integer totalStory;
    private Integer bedroom;
    private Integer totalRoom;
    private Boolean furnished;
    private BigDecimal serviceCost;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "product")
    private Product product;

    public House() {
    }

    public House(String city, String postalCode, String energyLabel, String propertyType, Integer livingArea,
            Integer constructionYear, Integer story, Integer totalStory, Integer bedroom, Integer totalRoom,
            Boolean furnished, BigDecimal serviceCost, Product product) {
        this.city = city;
        this.postalCode = postalCode;
        this.energyLabel = energyLabel;
        this.propertyType = propertyType;
        this.livingArea = livingArea;
        this.constructionYear = constructionYear;
        this.story = story;
        this.totalStory = totalStory;
        this.bedroom = bedroom;
        this.totalRoom = totalRoom;
        this.furnished = furnished;
        this.serviceCost = serviceCost;
        this.product = product;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getEnergyLabel() {
        return energyLabel;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public Integer getLivingArea() {
        return livingArea;
    }

    public Integer getConstructionYear() {
        return constructionYear;
    }

    public Integer getStory() {
        return story;
    }

    public Integer getTotalStory() {
        return totalStory;
    }

    public Integer getBedroom() {
        return bedroom;
    }

    public Integer getTotalRoom() {
        return totalRoom;
    }

    public Boolean getFurnished() {
        return furnished;
    }

    public BigDecimal getServiceCost() {
        return serviceCost;
    }

    public Product getProduct() {
        return product;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public void setEnergyLabel(String energyLabel) {
        this.energyLabel = energyLabel;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public void setLivingArea(Integer livingArea) {
        this.livingArea = livingArea;
    }

    public void setConstructionYear(Integer constructionYear) {
        this.constructionYear = constructionYear;
    }

    public void setStory(Integer story) {
        this.story = story;
    }

    public void setTotalStory(Integer totalStory) {
        this.totalStory = totalStory;
    }

    public void setBedroom(Integer bedroom) {
        this.bedroom = bedroom;
    }

    public void setTotalRoom(Integer totalRoom) {
        this.totalRoom = totalRoom;
    }

    public void setFurnished(Boolean furnished) {
        this.furnished = furnished;
    }

    public void setServiceCost(BigDecimal serviceCost) {
        this.serviceCost = serviceCost;
    }
}