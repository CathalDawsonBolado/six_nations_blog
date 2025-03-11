package com.sixnations.rugby_blog.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final String secretKey = "VFblMcP2T60AvxXW2zdW9iynO7U49j6qFwV3ajkc5v+b6SIY38nl1ql0iVrqV8KXHlU8ZUUtzhrCbA4M/bc77w==";
    private final long EXPIRATION_TIME = 1000 * 60 * 60; // ✅ 1 Hour Expiry

    public String generateToken(CustomUserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // ✅ FIX: Store roles correctly as a LIST
        claims.put("roles", List.of(userDetails.getUser().getRole().name())); // ✅ FIX: No "ROLE_"


        claims.put("email", userDetails.getUser().getEmail());
        claims.put("username", userDetails.getUser().getUsername());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUser().getUsername()) 
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("username", String.class);
    }

    public List<String> extractUserRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object rolesObject = claims.get("roles");

        if (rolesObject instanceof List<?>) {
            return ((List<?>) rolesObject).stream().map(Object::toString).collect(Collectors.toList());
        } else if (rolesObject instanceof String) {
            return List.of(rolesObject.toString());
        } else {
            return List.of();
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String extractedUsername = extractUsername(token);
        return extractedUsername.equals(userDetails.getUsername());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}




