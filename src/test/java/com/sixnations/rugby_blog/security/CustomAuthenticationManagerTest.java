package com.sixnations.rugby_blog.security;

import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.services.CustomUserDetailsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomAuthenticationManagerTest {

    private CustomUserDetailsService userDetailsService;
    private CustomPasswordEncoder passwordEncoder;
    private CustomAuthenticationManager authManager;

    @BeforeEach
    void setUp() {
        userDetailsService = mock(CustomUserDetailsService.class);
        passwordEncoder = mock(CustomPasswordEncoder.class);
        authManager = new CustomAuthenticationManager(userDetailsService, passwordEncoder);
    }

    @Test
    void testSuccessfulAuthentication() {
        User user = new User();
        user.setUsername("john");
        user.setPassword("hashedPassword");
        user.setSuspended(false);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        when(userDetailsService.loadUserByUsername("john")).thenReturn(userDetails);
        when(passwordEncoder.matches("rawPassword", "hashedPassword")).thenReturn(true);

        Authentication auth = new UsernamePasswordAuthenticationToken("john", "rawPassword");

        Authentication result = authManager.authenticate(auth);

        assertNotNull(result);
        assertEquals(userDetails, result.getPrincipal());
        assertEquals("rawPassword", result.getCredentials());
    }

    @Test
    void testAuthenticationFailsWithInvalidPassword() {
        User user = new User();
        user.setUsername("john");
        user.setPassword("hashedPassword");
        user.setSuspended(false);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        when(userDetailsService.loadUserByUsername("john")).thenReturn(userDetails);
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        Authentication auth = new UsernamePasswordAuthenticationToken("john", "wrongPassword");

        assertThrows(BadCredentialsException.class, () -> authManager.authenticate(auth));
    }

    @Test
    void testAuthenticationFailsWithSuspendedUser() {
        User user = new User();
        user.setUsername("john");
        user.setPassword("hashedPassword");
        user.setSuspended(true);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        when(userDetailsService.loadUserByUsername("john")).thenReturn(userDetails);

        Authentication auth = new UsernamePasswordAuthenticationToken("john", "anyPassword");

        assertThrows(BadCredentialsException.class, () -> authManager.authenticate(auth));
    }

    @Test
    void testAuthenticationFailsWithInvalidUserType() {
        UserDetails invalidUserDetails = mock(UserDetails.class); // Not an instance of CustomUserDetails
        when(userDetailsService.loadUserByUsername("john")).thenReturn(invalidUserDetails);

        Authentication auth = new UsernamePasswordAuthenticationToken("john", "anyPassword");

        assertThrows(BadCredentialsException.class, () -> authManager.authenticate(auth));
    }
}

