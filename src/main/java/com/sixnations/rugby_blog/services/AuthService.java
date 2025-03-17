package com.sixnations.rugby_blog.services;

import com.sixnations.rugby_blog.dto.RegisterDTO;
import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.models.User.Role;
import com.sixnations.rugby_blog.dao.UserRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    
    public void registerUser(RegisterDTO request) {
        if (userRepo.findByUsernameIgnoreCase(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already taken");
        }

        if (userRepo.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword())); // Encrypt password
        newUser.setRole(Role.USER); // Default role

        userRepo.save(newUser);
    }

    
    public String authenticateUser(RegisterDTO request) {
        Optional<User> userOpt = userRepo.findByUsernameIgnoreCase(request.getUsername());

        if (userOpt.isPresent() && passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            return "JWT-TOKEN-GENERATED-HERE"; // Replace with JWT generation logic
        } else {
            throw new IllegalArgumentException("Invalid credentials");
        }
    }
}


