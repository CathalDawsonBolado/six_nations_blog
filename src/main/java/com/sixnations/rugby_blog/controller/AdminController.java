package com.sixnations.rugby_blog.controller;



import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.services.UserService;

import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // Ensures only admins can access
public class AdminController {
    
    private final UserService userService;
  

    public AdminController(UserService userService) {
        this.userService = userService;
        
    }

   
    @PutMapping("/promote/{userId}")// Promote user to admin
    public ResponseEntity<String> promoteUser(@PathVariable Long userId) {
        boolean promoted = userService.promoteUserToAdmin(userId);
        if (promoted) {
            return ResponseEntity.ok("User promoted to Admin successfully.");
        }
        return ResponseEntity.badRequest().body("User not found or already an Admin.");
    }
 
    @GetMapping("/users")//Get list of all user
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }


    
    @PutMapping("/suspend/{userId}") // Suspend user
    public ResponseEntity<String> suspendUser(@PathVariable Long userId) {
        String result = userService.suspendUser(userId); // Let the service handle all checks

        if ("success".equals(result)) {
            return ResponseEntity.ok("User suspended successfully.");
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }


    @PutMapping("/unsuspend/{userId}") //unsuspend user
    public ResponseEntity<String> unsuspendUser(@PathVariable Long userId) {

    	boolean isUnsuspended = userService.unsuspendedUser(userId);
    	if(isUnsuspended) {
    		return ResponseEntity.ok("User has been unsuspended successfully");
    	}else {
    		return ResponseEntity.status(400).body("Failed to unsuspend user. User must not success");
    	}
    }

    
    
}

