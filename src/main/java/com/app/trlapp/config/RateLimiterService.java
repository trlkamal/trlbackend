package com.app.trlapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private static final int MAX_REQUESTS = 35;
    private static final int ADMIN_API_LIMIT = 1000; // API limit for ADMINvky
    private static final Duration REFRESH_PERIOD = Duration.ofMinutes(5);
    private static final Logger logger = LoggerFactory.getLogger(RateLimiterService.class); // Logger for this class

    private Map<String, UserRateLimit> userRequests = new ConcurrentHashMap<>();

    public boolean isAllowed(String username) {
        UserRateLimit rateLimit = userRequests.computeIfAbsent(username, UserRateLimit::new);

        synchronized (rateLimit) {
            Instant now = Instant.now();

            // Refresh the request count if the time window has passed
            if (Duration.between(rateLimit.getLastRequestTime(), now).compareTo(REFRESH_PERIOD) > 0) {
                rateLimit.reset(now);
                logger.info("Rate limit for user has been reset.");
            }

            // Determine the rate limit based on the user role
            int maxRequestsForUser = username.equals("ADMINvky") ? ADMIN_API_LIMIT : MAX_REQUESTS;

            // Check if the user has exceeded the rate limit
            if (rateLimit.getRequestCount() >= maxRequestsForUser) {
                logger.warn("Rate limit exceeded for user");
                return false;
            }

            // Increment the request count
            rateLimit.incrementRequestCount();
            logger.info("User  has made {} requests.", rateLimit.getRequestCount());
            return true;
        }
    }
}
