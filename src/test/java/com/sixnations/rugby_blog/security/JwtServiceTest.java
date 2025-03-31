package com.sixnations.rugby_blog.security;

import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.models.User.Role;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setRole(Role.USER);

        userDetails = new CustomUserDetails(user);
    }

    @Test
    void testGenerateToken_NotNull() {
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
    }

    @Test
    void testExtractUsername() {
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void testExtractUserRoles() {
        String token = jwtService.generateToken(userDetails);
        List<String> roles = jwtService.extractUserRoles(token);
        assertEquals(1, roles.size());
        assertTrue(roles.contains("USER"));
    }

    @Test
    void testIsTokenValid_ReturnsTrueForCorrectUser() {
        String token = jwtService.generateToken(userDetails);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void testIsTokenValid_ReturnsFalseForWrongUser() {
        String token = jwtService.generateToken(userDetails);

        User otherUser = new User();
        otherUser.setUsername("wronguser");
        otherUser.setEmail("wrong@example.com");
        otherUser.setRole(Role.USER);

        CustomUserDetails wrongDetails = new CustomUserDetails(otherUser);

        assertFalse(jwtService.isTokenValid(token, wrongDetails));
    }

    @Test
    void testExtractUserRoles_WhenRolesIsString() throws Exception {
        // Use reflection to access private method getSigningKey
        var getSigningKeyMethod = JwtService.class.getDeclaredMethod("getSigningKey");
        getSigningKeyMethod.setAccessible(true);
        Key signingKey = (Key) getSigningKeyMethod.invoke(jwtService);

        // Build token with "roles" as a String instead of List
        String fakeToken = Jwts.builder()
                .claim("username", "testuser")
                .claim("roles", "USER") // intentionally a string
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 100000)) // short validity
                .signWith(signingKey, SignatureAlgorithm.HS256) // CORRECT ORDER
                .compact();

        // Validate roles are extracted properly even when stored as string
        List<String> roles = jwtService.extractUserRoles(fakeToken);
        assertEquals(1, roles.size());
        assertEquals("USER", roles.get(0));
    }

}

