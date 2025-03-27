package com.sixnations.rugby_blog.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private UserDetailsService userDetailsService;
    private JwtAuthenticationFilter jwtFilter;

    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userDetailsService = mock(UserDetailsService.class);
        jwtFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        filterChain = mock(FilterChain.class);

        // Clear the security context before each test
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_WithValidToken_SetsAuthentication() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String username = "testuser";
        List<String> roles = List.of("ROLE_USER");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails userDetails = mock(UserDetails.class);
        when(jwtService.extractUsername(token)).thenReturn(username);
        when(jwtService.extractUserRoles(token)).thenReturn(roles);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

        jwtFilter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(auth);
        assertTrue(auth instanceof UsernamePasswordAuthenticationToken);
        assertEquals(userDetails, auth.getPrincipal());
        assertEquals(1, auth.getAuthorities().size());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("USER")));

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_WithNoAuthorizationHeader_DoesNotAuthenticate() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest(); // No header set
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_WithInvalidToken_DoesNotAuthenticate() throws ServletException, IOException {
        String token = "invalid.jwt.token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername(token)).thenThrow(new RuntimeException("Invalid token"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_AuthenticationAlreadyExists_DoesNotOverwrite() throws ServletException, IOException {
        UsernamePasswordAuthenticationToken existingAuth = new UsernamePasswordAuthenticationToken("alreadyAuth", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        String token = "valid.jwt.token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilterInternal(request, response, filterChain);

        // Authentication should remain unchanged
        assertEquals("alreadyAuth", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }
}

