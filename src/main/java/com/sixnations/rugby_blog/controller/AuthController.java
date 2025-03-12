package com.sixnations.rugby_blog.controller;

import com.sixnations.rugby_blog.dto.LoginDTO;
import com.sixnations.rugby_blog.dto.RegisterRequest;
import com.sixnations.rugby_blog.security.CustomAuthenticationManager;
import com.sixnations.rugby_blog.security.CustomPasswordEncoder;
import com.sixnations.rugby_blog.security.CustomUserDetails;
import com.sixnations.rugby_blog.security.JwtService;
import com.sixnations.rugby_blog.services.AuthService;
import com.sixnations.rugby_blog.services.CustomUserDetailsService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final CustomAuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AuthService authService;


    public AuthController(CustomUserDetailsService userDetailsService, CustomPasswordEncoder passwordEncoder, JwtService jwtService, AuthService authService) {
        this.authenticationManager = new CustomAuthenticationManager(userDetailsService, passwordEncoder);
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getIdentifier(), loginDTO.getPassword())
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(loginDTO.getIdentifier());

            if (!(userDetails instanceof CustomUserDetails)) {
                return ResponseEntity.status(500).body("User authentication failed: Invalid user type");
            }

            CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
            String jwtToken = jwtService.generateToken(customUserDetails); 

            return ResponseEntity.ok(jwtToken);

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(401).body("User not found!");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(e.getMessage());  
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.registerUser(request);
            return ResponseEntity.ok("User registered successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
