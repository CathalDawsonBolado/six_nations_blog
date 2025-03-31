package com.sixnations.rugby_blog.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomPasswordEncoderTest {

    private CustomPasswordEncoder customPasswordEncoder;

    @BeforeEach
    void setUp() {
        customPasswordEncoder = new CustomPasswordEncoder();
    }

    @Test
    void testEncodeAndMatch() {
        String rawPassword = "securePassword123";
        String encodedPassword = customPasswordEncoder.encode(rawPassword);

        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword); // Should be hashed

        // Should match the original password
        assertTrue(customPasswordEncoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void testDoesNotMatchWrongPassword() {
        String rawPassword = "password1";
        String wrongPassword = "password2";
        String encodedPassword = customPasswordEncoder.encode(rawPassword);

        assertFalse(customPasswordEncoder.matches(wrongPassword, encodedPassword));
    }
}

