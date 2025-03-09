package com.sixnations.rugby_blog.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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

    // ✅ Generate JWT Token with Debug Logs
    public String generateToken(CustomUserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userDetails.getUser().getRole().name());
        claims.put("email", userDetails.getUser().getEmail());
        claims.put("username", userDetails.getUser().getUsername());

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUser().getUsername()) // ✅ Store username as subject
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        System.out.println("✅ JWT Token Generated: " + token);
        System.out.println("📢 Token Claims: " + claims);
        return token;
    }

    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);

        // Extract both username and email for debugging
        String extractedUsername = claims.get("username", String.class);
        String extractedEmail = claims.get("email", String.class);
        String subject = claims.getSubject();

        System.out.println("🔍 Extracted Username from Token: " + extractedUsername);
        System.out.println("📧 Extracted Email from Token: " + extractedEmail);
        System.out.println("🔑 Token Subject (sub): " + subject);

        // Ensure we return the correct identifier used in validation
        return extractedUsername != null ? extractedUsername : subject;
    }


    // ✅ Extract User Role from JWT with Debug Logs
    public String extractUserRole(String token) {
        String role = extractAllClaims(token).get("role", String.class);
        System.out.println("🔍 Extracted Role from Token: " + role);
        return role;
    }

    

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String extractedUsername = extractUsername(token);
        String extractedEmail = extractAllClaims(token).get("email", String.class);

        // Debugging logs
        System.out.println("🔍 Token Extracted Username: " + extractedUsername);
        System.out.println("📧 Token Extracted Email: " + extractedEmail);
        System.out.println("🔍 UserDetails Username: " + userDetails.getUsername());
        System.out.println("📧 UserDetails Email: " + ((CustomUserDetails) userDetails).getUser().getEmail());

        // Ensure correct comparison
        boolean isValid = extractedUsername.equals(userDetails.getUsername()) ||
                          extractedUsername.equals(((CustomUserDetails) userDetails).getUser().getEmail());

        System.out.println("✅ Token Valid: " + isValid);
        return isValid;
    }




    // ✅ Extract All Claims from JWT with Debug Logs
    public Claims extractAllClaims(String token) {
        System.out.println("🔑 Using Secret Key for Verification: " + secretKey);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // ✅ Ensure same key is used
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        System.out.println("📜 Extracted Claims: " + claims);
        return claims;
    }

    // ✅ Get Signing Key with Debug Logs
    private Key getSigningKey() {
        System.out.println("🔑 Using Signing Key: " + secretKey);
        byte[] keyBytes = Decoders.BASE64.decode(secretKey); // ✅ Fix: Proper Base64 decoding
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

