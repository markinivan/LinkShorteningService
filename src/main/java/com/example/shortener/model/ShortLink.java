package com.example.shortener.model;
public class ShortLink {
    private String shortCode;
    private String originalUrl;
    private String ownerId;
    private int maxClicks;
    private int currentClicks;
    private long createdAtMillis;
    private long ttlMillis;

    public ShortLink() {}

    public ShortLink(String shortCode, String originalUrl, String ownerId, int maxClicks, long ttlMillis) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.ownerId = ownerId;
        this.maxClicks = maxClicks;
        this.currentClicks = 0;
        this.createdAtMillis = System.currentTimeMillis();
        this.ttlMillis = ttlMillis;
    }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public int getMaxClicks() { return maxClicks; }
    public void setMaxClicks(int maxClicks) { this.maxClicks = maxClicks; }
    public int getCurrentClicks() { return currentClicks; }
    public void setCurrentClicks(int currentClicks) { this.currentClicks = currentClicks; }
    public long getCreatedAtMillis() { return createdAtMillis; }
    public void setCreatedAtMillis(long createdAtMillis) { this.createdAtMillis = createdAtMillis; }
    public long getTtlMillis() { return ttlMillis; }
    public void setTtlMillis(long ttlMillis) { this.ttlMillis = ttlMillis; }

    public boolean isExpired() {
        return System.currentTimeMillis() > (createdAtMillis + ttlMillis);
    }

    public boolean isLimitReached() {
        return currentClicks >= maxClicks;
    }
}
