package com.sixnations.rugby_blog.services;

import com.sixnations.rugby_blog.dao.UserRepo;
import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.models.User.Role;
import com.sixnations.rugby_blog.security.CustomUserDetails;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    private UserRepo userRepo;
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        userRepo = mock(UserRepo.class);
        customUserDetailsService = new CustomUserDetailsService(userRepo);
    }

    @Test
    void testLoadUserByEmailSuccess() {
        User mockUser = new User();
        mockUser.setEmail("user@example.com");
        mockUser.setUsername("username");
        mockUser.setPassword("password");
        mockUser.setRole(Role.USER);

        when(userRepo.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(mockUser));

        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername("user@example.com");

        assertEquals("user@example.com", userDetails.getUser().getEmail());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testLoadUserByUsernameSuccess() {
        User mockUser = new User();
        mockUser.setEmail("user@example.com");
        mockUser.setUsername("username");
        mockUser.setPassword("password");
        mockUser.setRole(Role.ADMIN);

        when(userRepo.findByEmailIgnoreCase("username")).thenReturn(Optional.empty());
        when(userRepo.findByUsernameIgnoreCase("username")).thenReturn(Optional.of(mockUser));

        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername("username");

        assertEquals("username", userDetails.getUser().getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testLoadUserNotFound() {
        when(userRepo.findByEmailIgnoreCase("notfound")).thenReturn(Optional.empty());
        when(userRepo.findByUsernameIgnoreCase("notfound")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername("notfound"));
    }
}
