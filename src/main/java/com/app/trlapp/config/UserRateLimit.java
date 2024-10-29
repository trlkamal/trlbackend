package com.app.trlapp.config;

import java.time.Instant;

public class UserRateLimit {

    private int requestCount;
    private Instant lastRequestTime;

    public UserRateLimit(String username) {
        this.requestCount = 0;
        this.lastRequestTime = Instant.now();
    }

    public int getRequestCount() {
        return requestCount;
    }

    public Instant getLastRequestTime() {
        return lastRequestTime;
    }

    public void incrementRequestCount() {
        this.requestCount++;
        this.lastRequestTime = Instant.now();
    }

    public void reset(Instant now) {
        this.requestCount = 0;
        this.lastRequestTime = now;
    }
}

