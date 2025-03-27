package com.sixnations.rugby_blog.security;

import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.models.User.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {

    private User user;
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testuser");
        user.setPassword("securePassword");
        user.setRole(Role.USER);
        user.setSuspended(false);

        customUserDetails = new CustomUserDetails(user);
    }

    @Test
    void testGetUsername() {
        assertEquals("testuser", customUserDetails.getUsername());
    }

    @Test
    void testGetPassword() {
        assertEquals("securePassword", customUserDetails.getPassword());
    }

    @Test
    void testGetAuthorities() {
        Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testIsAccountNonExpired() {
        assertTrue(customUserDetails.isAccountNonExpired());
    }

    @Test
    void testIsAccountNonLocked_WhenNotSuspended() {
        assertTrue(customUserDetails.isAccountNonLocked());
    }

    @Test
    void testIsAccountNonLocked_WhenSuspended() {
        user.setSuspended(true);
        customUserDetails = new CustomUserDetails(user);
        assertFalse(customUserDetails.isAccountNonLocked());
    }

    @Test
    void testIsCredentialsNonExpired() {
        assertTrue(customUserDetails.isCredentialsNonExpired());
    }

    @Test
    void testIsEnabled_WhenNotSuspended() {
        assertTrue(customUserDetails.isEnabled());
    }

    @Test
    void testIsEnabled_WhenSuspended() {
        user.setSuspended(true);
        customUserDetails = new CustomUserDetails(user);
        assertFalse(customUserDetails.isEnabled());
    }

    @Test
    void testGetUser() {
        assertEquals(user, customUserDetails.getUser());
    }
}
