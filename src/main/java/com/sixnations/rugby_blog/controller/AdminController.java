package com.sixnations.rugby_blog.controller;

import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.services.UserService;

import java.util.List;

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

   
    @PutMapping("/promote/{userId}")
    public ResponseEntity<String> promoteUser(@PathVariable Long userId) {
        boolean promoted = userService.promoteUserToAdmin(userId);
        if (promoted) {
            return ResponseEntity.ok("User promoted to Admin successfully.");
        }
        return ResponseEntity.badRequest().body("User not found or already an Admin.");
    }
 
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }


    
    @PutMapping("/suspend/{userId}")
    public ResponseEntity<String> suspendUser(@PathVariable Long userId) {
        boolean suspended = userService.suspendUser(userId);
        if (suspended) {
            return ResponseEntity.ok("User suspended successfully.");
        }
        return ResponseEntity.badRequest().body("User not found or already suspended.");
    }
    @PutMapping("/unsuspend/{userId}") 
    public ResponseEntity<String> unsuspendUser(@PathVariable Long userId) {

    	boolean isUnsuspended = userService.unsuspendedUser(userId);
    	if(isUnsuspended) {
    		return ResponseEntity.ok("User has been unsuspendedd successfully");
    	}else {
    		return ResponseEntity.status(400).body("Failed to unsuspend user. User must not success");
    	}
    }

    
    @DeleteMapping("/delete/post/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId) {
        boolean deleted = userService.deletePost(postId);
        if (deleted) {
            return ResponseEntity.ok("Post deleted successfully.");
        }
        return ResponseEntity.badRequest().body("Post not found.");
    }

    
    @DeleteMapping("/delete/comment/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId) {
        boolean deleted = userService.deleteComment(commentId);
        if (deleted) {
            return ResponseEntity.ok("Comment deleted successfully.");
        }
        return ResponseEntity.badRequest().body("Comment not found.");
    }
}

