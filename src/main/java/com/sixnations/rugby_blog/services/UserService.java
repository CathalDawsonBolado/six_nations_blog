package com.sixnations.rugby_blog.services;

import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.models.User.Role;
import com.sixnations.rugby_blog.security.CustomPasswordEncoder;
import com.sixnations.rugby_blog.dao.UserRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepo userRepo;
    private final CustomPasswordEncoder passwordEncoder;


    public UserService(UserRepo userRepo, CustomPasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        
    }

    
    public Optional<User> findByUserEmail(String email) {
        return userRepo.findByEmailIgnoreCase(email);
    }

   
    public Optional<User> findByUsername(String username) {
        return userRepo.findByUsernameIgnoreCase(username);
    }

    
    public boolean promoteUserToAdmin(Long userId) {
        Optional<User> userOpt = userRepo.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getRole() == User.Role.ADMIN) {
                return false; // Already an admin
            }
            user.setRole(User.Role.ADMIN);
            userRepo.save(user);
            return true;
        }
        return false;
    }

    
    public String suspendUser(Long userId) {
        Optional<User> userOpt = userRepo.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Debugging: Print user role
            System.out.println("Attempting to suspend user: " + user.getUsername() + " | Role: " + user.getRole());

            //  Prevent suspending an admin (since Role is an ENUM, compare it properly)
            if (user.getRole() == Role.ADMIN) { 
                return "Cannot suspend an admin."; // Send response message directly
            }

            if (!user.isSuspended()) {
                user.setSuspended(true);
                userRepo.save(user);
                return "success"; // Indicate success to the controller
            } else {
                return "User is already suspended.";
            }
        }
        return "User not found.";
    }



    
    public boolean unsuspendedUser(Long userId) {
        Optional<User> userOpt = userRepo.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isSuspended()) {
                user.setSuspended(false);
                userRepo.save(user);
                return true;
            }
        }
        return false;
    }


    
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    
    
    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }
}



