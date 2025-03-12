package com.sixnations.rugby_blog.controller;

import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    
    @PostMapping
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        User savedUser = userService.saveUser(user);
        return ResponseEntity.ok(savedUser);
    }

    
    @GetMapping("/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return userService.findByUserEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam("query") String query) {
        List<User> users = userService.searchUsers(query);
        return ResponseEntity.ok(users);
    }
}
