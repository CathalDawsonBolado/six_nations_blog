package com.sixnations.rugby_blog.services;

import com.sixnations.rugby_blog.models.Post;
import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.dao.PostRepo;
import com.sixnations.rugby_blog.dao.UserRepo;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    private final PostRepo postRepo;
    private final UserRepo userRepo;

    public PostService(PostRepo postRepo, UserRepo userRepo) {
        this.postRepo = postRepo;
        this.userRepo = userRepo;
    }

    
    public List<Post> getAllPosts() {
        return postRepo.findAll();
    }

    
    public Optional<Post> getPostById(Long postId) {
        return postRepo.findById(postId);
    }

    
    public List<Post> getPostsByUser(String username) {
        return postRepo.findByUserUsername(username);
    }

    
    public Post createPost(Post post) {
        return postRepo.save(post);
    }

   
    
    public User getUserByIdentifier(String identifier) {
        Optional<User> userOpt;

        if (identifier.contains("@")) {
           
            userOpt = userRepo.findByEmailIgnoreCase(identifier);
        } else {
            
            userOpt = userRepo.findByUsernameIgnoreCase(identifier);
        }

        return userOpt.orElseThrow(() -> new IllegalArgumentException("User not found: " + identifier));
    }
}


