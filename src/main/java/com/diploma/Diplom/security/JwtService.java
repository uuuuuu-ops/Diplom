package com.diploma.Diplom.security;

import com.diploma.Diplom.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private Key signingKey;

    @PostConstruct
    private void initKey() {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private Key getSignInKey() {
        return signingKey;
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())   // jti — unique per token
                .setSubject(user.getId())
                .claim("email", user.getEmail())
                .claim("role", "ROLE_" + user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String extractUserId(String token) {
        return getClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    /** Returns the jti claim — unique token ID used for blacklisting. */
    public String extractJti(String token) {
        return getClaims(token).getId();
    }

    /**
     * Returns how long until the token expires (for blacklist TTL).
     * Returns Duration.ZERO if already expired.
     */
    public Duration getRemainingTtl(String token) {
        Date expiry = getClaims(token).getExpiration();
        long remaining = expiry.getTime() - System.currentTimeMillis();
        return remaining > 0 ? Duration.ofMillis(remaining) : Duration.ZERO;
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}