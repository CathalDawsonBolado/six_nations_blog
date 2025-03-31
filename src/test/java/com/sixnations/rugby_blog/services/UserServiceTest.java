package com.sixnations.rugby_blog.services;

import com.sixnations.rugby_blog.dao.UserRepo;
import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.models.User.Role;
import com.sixnations.rugby_blog.security.CustomPasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepo userRepo;
    private CustomPasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepo = mock(UserRepo.class);
        passwordEncoder = mock(CustomPasswordEncoder.class);
        userService = new UserService(userRepo, passwordEncoder);
    }

    @Test
    void testFindByUserEmail() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepo.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByUserEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void testFindByUsername() {
        User user = new User();
        user.setUsername("john_doe");
        when(userRepo.findByUsernameIgnoreCase("john_doe")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByUsername("john_doe");

        assertTrue(result.isPresent());
        assertEquals("john_doe", result.get().getUsername());
    }

    @Test
    void testPromoteUserToAdmin_Success() {
        User user = new User();
        user.setRole(Role.USER);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        boolean result = userService.promoteUserToAdmin(1L);

        assertTrue(result);
        verify(userRepo).save(user);
        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    void testPromoteUserToAdmin_AlreadyAdmin() {
        User user = new User();
        user.setRole(Role.ADMIN);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        boolean result = userService.promoteUserToAdmin(1L);

        assertFalse(result);
        verify(userRepo, never()).save(any());
    }

    @Test
    void testPromoteUserToAdmin_UserNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        boolean result = userService.promoteUserToAdmin(1L);

        assertFalse(result);
    }

    @Test
    void testSuspendUser_Success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user1");
        user.setRole(Role.USER);
        user.setSuspended(false);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        String result = userService.suspendUser(1L);

        assertEquals("success", result);
        assertTrue(user.isSuspended());
        verify(userRepo).save(user);
    }

    @Test
    void testSuspendUser_AlreadySuspended() {
        User user = new User();
        user.setRole(Role.USER);
        user.setSuspended(true);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        String result = userService.suspendUser(1L);

        assertEquals("User is already suspended.", result);
        verify(userRepo, never()).save(user);
    }

    @Test
    void testSuspendUser_IsAdmin() {
        User user = new User();
        user.setRole(Role.ADMIN);
        user.setSuspended(false);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        String result = userService.suspendUser(1L);

        assertEquals("Cannot suspend an admin.", result);
        verify(userRepo, never()).save(any());
    }

    @Test
    void testSuspendUser_NotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        String result = userService.suspendUser(1L);

        assertEquals("User not found.", result);
    }

    @Test
    void testUnsuspendUser_Success() {
        User user = new User();
        user.setSuspended(true);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        boolean result = userService.unsuspendedUser(1L);

        assertTrue(result);
        assertFalse(user.isSuspended());
        verify(userRepo).save(user);
    }

    @Test
    void testUnsuspendUser_NotSuspended() {
        User user = new User();
        user.setSuspended(false);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        boolean result = userService.unsuspendedUser(1L);

        assertFalse(result);
        verify(userRepo, never()).save(user);
    }

    @Test
    void testUnsuspendUser_NotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        boolean result = userService.unsuspendedUser(1L);

        assertFalse(result);
    }

    @Test
    void testGetAllUsers() {
        List<User> mockUsers = Arrays.asList(new User(), new User());
        when(userRepo.findAll()).thenReturn(mockUsers);

        List<User> users = userService.getAllUsers();

        assertEquals(2, users.size());
    }

    @Test
    void testSaveUser_PasswordEncoded() {
        User user = new User();
        user.setPassword("plainPass");

        when(passwordEncoder.encode("plainPass")).thenReturn("encodedPass");
        when(userRepo.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User savedUser = userService.saveUser(user);

        assertEquals("encodedPass", savedUser.getPassword());
        verify(userRepo).save(user);
    }
}

