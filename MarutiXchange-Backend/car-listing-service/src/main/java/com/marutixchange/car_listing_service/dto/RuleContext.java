package com.marutixchange.car_listing_service.dto;

public class RuleContext {

    private String type;
    private Double price;
    private Long userId;
    private String brand;

    private boolean blacklisted;
    private boolean risky;
    private boolean approved;
    private String message;

    private int year;
    private boolean duplicate;
    private boolean sellerVerified;

    private boolean ruleMatched;
    private String fuelType;
    private String transmission;

    // ✅ DEFAULT VALUES (IMPORTANT)
    public RuleContext() {
        this.approved = true;
        this.message = "Approved";
        this.blacklisted = false;
        this.risky = false;
    }

    // GETTERS & SETTERS

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public boolean isBlacklisted() {
        return blacklisted;
    }

    public void setBlacklisted(boolean blacklisted) {
        this.blacklisted = blacklisted;
    }

    public boolean isRisky() {
        return risky;
    }

    public void setRisky(boolean risky) {
        this.risky = risky;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

    public boolean isSellerVerified() {
        return sellerVerified;
    }

    public void setSellerVerified(boolean sellerVerified) {
        this.sellerVerified = sellerVerified;
    }

    public boolean isRuleMatched() {
        return ruleMatched;
    }

    public void setRuleMatched(boolean ruleMatched) {
        this.ruleMatched = ruleMatched;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }
}