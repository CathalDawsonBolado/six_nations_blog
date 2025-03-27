package com.sixnations.rugby_blog.controller;

import com.sixnations.rugby_blog.dto.LoginDTO;
import com.sixnations.rugby_blog.dto.RegisterDTO;
import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.security.CustomAuthenticationManager;
import com.sixnations.rugby_blog.security.CustomPasswordEncoder;
import com.sixnations.rugby_blog.security.CustomUserDetails;
import com.sixnations.rugby_blog.services.AuthService;
import com.sixnations.rugby_blog.services.CustomUserDetailsService;
import com.sixnations.rugby_blog.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthService authService;

    @MockBean
    private CustomAuthenticationManager authenticationManager;

    @Test
    void testLogin_Success() throws Exception {
        // Mock the PasswordEncoder to simulate encoding
        CustomPasswordEncoder mockPasswordEncoder = mock(CustomPasswordEncoder.class);
        
        // Encode the password as it would be in the system
        String encodedPassword = new BCryptPasswordEncoder().encode("password123");
        when(mockPasswordEncoder.encode(anyString())).thenReturn(encodedPassword);
        when(mockPasswordEncoder.matches(eq("password123"), eq(encodedPassword))).thenReturn(true);  // Ensure the matching works

        // Create a mocked User object
        User mockUser = mock(User.class);
        when(mockUser.getUsername()).thenReturn("user1");
        when(mockUser.getPassword()).thenReturn(encodedPassword);  // Store the encoded password here
        when(mockUser.getRole()).thenReturn(User.Role.USER);  // Assuming User has an enum Role

        // Create a CustomUserDetails object using the mocked User
        CustomUserDetails mockUserDetails = new CustomUserDetails(mockUser);

        // Mock the service to return the CustomUserDetails for the "user1"
        when(userDetailsService.loadUserByUsername("user1")).thenReturn(mockUserDetails);

        // Mock JWT token generation
        when(jwtService.generateToken(mockUserDetails)).thenReturn("mock-jwt-token");

        // Create the authentication manager using the mocked password encoder
        CustomAuthenticationManager mockAuthenticationManager = new CustomAuthenticationManager(userDetailsService, mockPasswordEncoder);

        // Perform the login and assert that the response is successful with the mocked JWT token
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"identifier\":\"user1\", \"password\":\"password123\"}"))
                .andExpect(status().isOk())  // Expecting 200 OK
                .andExpect(content().string("mock-jwt-token"));  // Expecting the mocked JWT token
    }

    @Test
    void testLogin_UserNotFound() throws Exception {
        LoginDTO loginDTO = new LoginDTO("user1", "password");

        // Mock the service to throw UsernameNotFoundException
        when(userDetailsService.loadUserByUsername("user1")).thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"identifier\":\"user1\", \"password\":\"password\"}"))
                .andExpect(status().isUnauthorized())  // Expecting 401 Unauthorized
                .andExpect(content().string("User not found!"));  // Message from the controller
    }

    
    @Test
    void testRegister_Success() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("user1");
        registerDTO.setPassword("password");
        registerDTO.setEmail("email@example.com");

        doNothing().when(authService).registerUser(registerDTO);

        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content("{\"username\":\"user1\", \"password\":\"password\", \"email\":\"email@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully."));
    }


}
