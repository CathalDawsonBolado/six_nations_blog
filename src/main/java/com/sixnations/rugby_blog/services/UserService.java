package com.sixnations.rugby_blog.services;

import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.security.CustomPasswordEncoder;
import com.sixnations.rugby_blog.dao.UserRepo;
import com.sixnations.rugby_blog.dao.PostRepo;
import com.sixnations.rugby_blog.dao.CommentRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepo userRepo;
    private final CustomPasswordEncoder passwordEncoder;
    private final PostRepo postRepo;
    private final CommentRepo commentRepo;

    public UserService(UserRepo userRepo, CustomPasswordEncoder passwordEncoder, PostRepo postRepo, CommentRepo commentRepo) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.postRepo = postRepo;
        this.commentRepo = commentRepo;
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

    
    public boolean suspendUser(Long userId) {
        Optional<User> userOpt = userRepo.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setSuspended(true);
            userRepo.save(user);
            return true;
        }
        return false;
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

    
    public boolean deletePost(Long postId) {
        if (postRepo.existsById(postId)) {
            postRepo.deleteById(postId);
            return true;
        }
        return false;
    }

    
    public boolean deleteComment(Long commentId) {
        if (commentRepo.existsById(commentId)) {
            commentRepo.deleteById(commentId);
            return true;
        }
        return false;
    }

    
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    
    public List<User> searchUsers(String query) {
        return userRepo.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
    }

    
    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }
}



