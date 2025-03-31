package com.sixnations.rugby_blog.services;

import com.sixnations.rugby_blog.dao.UserRepo;
import com.sixnations.rugby_blog.dto.RegisterDTO;
import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.models.User.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepo userRepo;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepo = mock(UserRepo.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authService = new AuthService(userRepo, passwordEncoder);
    }

    @Test
    void testRegisterUser_Success() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("john_doe");
        registerDTO.setEmail("john@example.com");
        registerDTO.setPassword("password123");

        when(userRepo.findByUsernameIgnoreCase("john_doe")).thenReturn(Optional.empty());
        when(userRepo.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPass");

        authService.registerUser(registerDTO);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(captor.capture());

        User savedUser = captor.getValue();
        assertEquals("john_doe", savedUser.getUsername());
        assertEquals("john@example.com", savedUser.getEmail());
        assertEquals("encodedPass", savedUser.getPassword());
        assertEquals(Role.USER, savedUser.getRole());
    }


    @Test
    void testRegisterUser_UsernameTaken() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("john_doe");
        registerDTO.setEmail("john@example.com");
        registerDTO.setPassword("password123");

        when(userRepo.findByUsernameIgnoreCase("john_doe")).thenReturn(Optional.of(new User()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(registerDTO);
        });

        assertEquals("Username already taken", exception.getMessage());
        verify(userRepo, never()).save(any());
    }

    @Test
    void testRegisterUser_EmailTaken() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("john_doe");
        registerDTO.setEmail("john@example.com");
        registerDTO.setPassword("password123");

        when(userRepo.findByUsernameIgnoreCase("john_doe")).thenReturn(Optional.empty());
        when(userRepo.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(new User()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(registerDTO);
        });

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepo, never()).save(any());
    }

    @Test
    void testAuthenticateUser_InvalidPassword() {
    	RegisterDTO registerDTO = new RegisterDTO();
        
    	registerDTO.setUsername("john_doe");
    	registerDTO.setEmail("john@example.com");
    	registerDTO.setPassword("wrongPassword");
        User user = new User();
        user.setPassword("encodedPass");

        when(userRepo.findByUsernameIgnoreCase("john_doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPass")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.authenticateUser(registerDTO);
        });

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void testAuthenticateUser_UserNotFound() {
    	RegisterDTO registerDto = new RegisterDTO();
    	registerDto.setUsername("nonexistent");
    	registerDto.setEmail("noemail@example.com");
    	registerDto.setPassword("password");
        when(userRepo.findByUsernameIgnoreCase("nonexistent")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.authenticateUser(registerDto);
        });

        assertEquals("Invalid credentials", exception.getMessage());
    }
}

