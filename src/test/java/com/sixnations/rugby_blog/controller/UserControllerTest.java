package com.sixnations.rugby_blog.controller;

import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_ReturnsSavedUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");

        when(userService.saveUser(user)).thenReturn(user);

        ResponseEntity<User> response = userController.registerUser(user);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(user);
        verify(userService, times(1)).saveUser(user);
    }

    @Test
    void testGetUserByEmail_UserFound() {
        String email = "found@example.com";
        User user = new User();
        user.setEmail(email);
        user.setUsername("founduser");

        when(userService.findByUserEmail(email)).thenReturn(Optional.of(user));

        ResponseEntity<User> response = userController.getUserByEmail(email);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(user);
        verify(userService).findByUserEmail(email);
    }

    @Test
    void testGetUserByEmail_UserNotFound() {
        String email = "missing@example.com";

        when(userService.findByUserEmail(email)).thenReturn(Optional.empty());

        ResponseEntity<User> response = userController.getUserByEmail(email);

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        assertThat(response.getBody()).isNull();
        verify(userService).findByUserEmail(email);
    }
}
