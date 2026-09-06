package com.marutixchange.rules_engine_service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * RuleContext - Single fact object passed to Drools rules engine.
 *
 * Industry Pattern:
 *   One unified context that carries all fields needed by any rule type.
 *   The 'type' field routes to correct Drools agenda group.
 *
 * Supported types:
 *   BID          -> bidding_rules.drl
 *   CAR_LISTING  -> car-rules.drl
 *   PAYMENT      -> payment-rules.drl
 *   NOTIFICATION -> notification_rules.drl
 *   fraud        -> fraud-detection.drl
 *   order        -> order_rules.drl
 *   refund       -> refund-eligibility.drl
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleContext {

    // ROUTING
    private String type;

    // BIDDING
    private Double bidAmount;
    private Double currentHighestBid;
    private Long   auctionId;

    // CAR LISTING
    private double  price;
    private int     year;
    private boolean duplicate;
    private boolean sellerVerified;
    private String  brand;
    private String  model;    // e.g. "Swift", "Baleno", "Grand Vitara"
    private String  segment;  // "ARENA" or "NEXA" — used for segment validation

    // USER / FRAUD
    private Long    userId;
    private Long    sellerId;
    private boolean blacklisted;
    private boolean risky;

    // NOTIFICATION
    private Integer hour;
    private Integer notificationCount;
    private String  notificationType;
    private String  channel;

    // OUTPUT (defaults: approved + message)
    private boolean approved    = true;
    private String  message     = "Approved";
    private boolean ruleMatched = false;

    public RuleContext() {
        this.approved    = true;
        this.message     = "Approved";
        this.ruleMatched = false;
    }

    // Getters & Setters
    public String  getType()              { return type; }
    public void    setType(String v)      { this.type = v; }

    public Double  getBidAmount()                { return bidAmount; }
    public void    setBidAmount(Double v)        { this.bidAmount = v; }

    public Double  getCurrentHighestBid()        { return currentHighestBid; }
    public void    setCurrentHighestBid(Double v){ this.currentHighestBid = v; }

    public Long    getAuctionId()                { return auctionId; }
    public void    setAuctionId(Long v)          { this.auctionId = v; }

    public double  getPrice()            { return price; }
    public void    setPrice(double v)    { this.price = v; }

    public int     getYear()             { return year; }
    public void    setYear(int v)        { this.year = v; }

    public boolean isDuplicate()         { return duplicate; }
    public void    setDuplicate(boolean v){ this.duplicate = v; }

    public boolean isSellerVerified()    { return sellerVerified; }
    public void    setSellerVerified(boolean v){ this.sellerVerified = v; }

    public String  getBrand()            { return brand; }
    public void    setBrand(String v)    { this.brand = v; }

    public String  getModel()            { return model; }
    public void    setModel(String v)    { this.model = v; }

    public String  getSegment()          { return segment; }
    public void    setSegment(String v)  { this.segment = v; }

    public Long    getUserId()           { return userId; }
    public void    setUserId(Long v)     { this.userId = v; }

    public Long    getSellerId()         { return sellerId; }
    public void    setSellerId(Long v)   { this.sellerId = v; }

    public boolean isBlacklisted()       { return blacklisted; }
    public void    setBlacklisted(boolean v){ this.blacklisted = v; }

    public boolean isRisky()             { return risky; }
    public void    setRisky(boolean v)   { this.risky = v; }

    public Integer getHour()             { return hour; }
    public void    setHour(Integer v)    { this.hour = v; }

    public Integer getNotificationCount()         { return notificationCount; }
    public void    setNotificationCount(Integer v){ this.notificationCount = v; }

    public String  getNotificationType()          { return notificationType; }
    public void    setNotificationType(String v)  { this.notificationType = v; }

    public String  getChannel()          { return channel; }
    public void    setChannel(String v)  { this.channel = v; }

    public boolean isApproved()          { return approved; }
    public void    setApproved(boolean v){ this.approved = v; }

    public String  getMessage()          { return message; }
    public void    setMessage(String v)  { this.message = v; }

    public boolean isRuleMatched()       { return ruleMatched; }
    public void    setRuleMatched(boolean v){ this.ruleMatched = v; }
}