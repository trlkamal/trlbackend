package com.app.trlapp.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Autowired
    private StrongKeyGenerator strongKeyGenerator;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    }

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private long jwtExpirationMs = 15 * 60 * 1000; // 15 minutes for Access Token
    private long refreshExpirationMs = 7 * 24 * 60 * 60 * 1000; // 7 days for Refresh Token

    // Clock Skew tolerance (e.g., 60 seconds)
    private long clockSkewMs = 60 * 1000;

    public String generateAccessToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateTokenFromCookies(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .setAllowedClockSkewSeconds(clockSkewMs / 1000) // Allow for clock skew
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            if (isTokenExpired(claims)) {
                logger.warn("Token has expired. Expiration Date: {}", claims.getExpiration());
                return false;
            }

            String tokenType = claims.get("type", String.class);
            if (tokenType == null || !tokenType.equals("access")) {
                logger.error("Invalid token type: {}", tokenType);
                return false;
            }

            logger.info("Token is valid and of type: {}", tokenType);
            return true;

        } catch (ExpiredJwtException e) {
            logger.error("JWT has expired: {}", e.getMessage());
            return false; // Handle token expiration
        } catch (Exception e) {
//            logger.error("An error occurred while validating the token: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean validateRefreshTokenFromCookies(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .setAllowedClockSkewSeconds(clockSkewMs / 1000) // Allow for clock skew
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            if (isTokenExpired(claims)) {
                logger.warn("Token has expired. Expiration Date: {}", claims.getExpiration());
                return false;
            }

            String tokenType = claims.get("type", String.class);
            if (tokenType == null || !tokenType.equals("refresh")) {
//                logger.error("Invalid token type: {}", tokenType);
                return false;
            }

            logger.info("Token is valid and of type: {}", tokenType);
            return true;

        } catch (ExpiredJwtException e) {
            logger.error("JWT has expired: {}", e.getMessage());
            return false; // Handle token expiration
        } catch (Exception e) {
//            logger.error("An error occurred while validating the token: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
