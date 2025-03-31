package com.sixnations.rugby_blog.services;

import com.sixnations.rugby_blog.dao.CommentRepo;
import com.sixnations.rugby_blog.dao.PostRepo;
import com.sixnations.rugby_blog.dao.UserRepo;
import com.sixnations.rugby_blog.models.Comment;
import com.sixnations.rugby_blog.models.Post;
import com.sixnations.rugby_blog.models.User;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {
    private final CommentRepo commentRepo;
    private final PostRepo postRepo;
    private final UserRepo userRepo;

    public CommentService(CommentRepo commentRepo, PostRepo postRepo, UserRepo userRepo) {
        this.commentRepo = commentRepo;
        this.postRepo = postRepo;
        this.userRepo = userRepo;
    }

    //  Get comments for a post
    public List<Comment> getCommentsByPost(Long postId) {
        Optional<Post> postOpt = postRepo.findById(postId);
        
        if (postOpt.isEmpty()) {
           
            return List.of(); // Return empty list instead of null
        }
        
        return commentRepo.findByPost(postOpt.get());
    }

    
    public boolean postExists(Long postId) {
        return postRepo.existsById(postId);
    }

    
    public Comment createComment(Long postId, String content, String username) throws AccessDeniedException {
        Optional<Post> postOpt = postRepo.findById(postId);
        
        if (postOpt.isEmpty()) {
            throw new IllegalArgumentException("Post not found.");
        }

        System.out.println("📢 Checking if user exists: " + username);
        Optional<User> userOpt = userRepo.findByUsernameIgnoreCase(username);
        
        if (userOpt.isEmpty()) {
            System.out.println("❌ ERROR: User not found: " + username);
            throw new IllegalArgumentException("User not found.");
        }

        
        User user = userOpt.get();
        if (user.isSuspended()) {
            
            throw new AccessDeniedException("User is suspended and cannot comment.");
        }

        
        Comment comment = new Comment(content, postOpt.get(), user);
        Comment savedComment = commentRepo.save(comment);
        

        return savedComment;
    }
}

