package com.sixnations.rugby_blog.services;

import com.sixnations.rugby_blog.models.Post;
import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.dao.PostRepo;
import com.sixnations.rugby_blog.dao.UserRepo;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
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

    // ✅ Get all posts (Public)
    public List<Post> getAllPosts() {
        return postRepo.findAll();
    }

    // ✅ Get a specific post by ID
    public Optional<Post> getPostById(Long postId) {
        return postRepo.findById(postId);
    }

    // ✅ Search posts by title or content
    public List<Post> searchPosts(String query) {
        return postRepo.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(query, query);
    }

    // ✅ Get all posts by a specific user
    public List<Post> getPostsByUser(String username) {
        return postRepo.findByUserUsername(username);
    }

    // ✅ Create a new post
    public Post createPost(Post post) {
        return postRepo.save(post);
    }

    // ✅ Update a post (Only the author can update)
    public Post updatePost(Long postId, Post updatedPost, String username) throws AccessDeniedException {
        Optional<Post> existingPostOpt = postRepo.findById(postId);
        if (existingPostOpt.isPresent()) {
            Post existingPost = existingPostOpt.get();

            // ✅ Ensure only the author can edit
            if (!existingPost.getUser().getUsername().equals(username)) {
                throw new AccessDeniedException("You are not allowed to edit this post!");
            }

            existingPost.setTitle(updatedPost.getTitle());
            existingPost.setContent(updatedPost.getContent());
            return postRepo.save(existingPost);
        }
        return null;
    }

    // ✅ Delete a post (Only the author or an admin can delete)
    public boolean deletePost(Long postId, String username) throws AccessDeniedException {
        Optional<Post> postOpt = postRepo.findById(postId);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            User author = post.getUser();
            Optional<User> userOpt = userRepo.findByUsernameIgnoreCase(username);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // ✅ Allow author or admin to delete
                if (user.equals(author) || user.getRole() == User.Role.ADMIN) {
                    postRepo.deleteById(postId);
                    return true;
                }
            }
            throw new AccessDeniedException("You are not allowed to delete this post!");
        }
        return false;
    }

    // ✅ Get user by identifier (email or username)
    public User getUserByIdentifier(String identifier) {
        Optional<User> userOpt;

        if (identifier.contains("@")) {
            System.out.println("🔍 Looking up user by email: " + identifier);
            userOpt = userRepo.findByEmailIgnoreCase(identifier);
        } else {
            System.out.println("🔍 Looking up user by username: " + identifier);
            userOpt = userRepo.findByUsernameIgnoreCase(identifier);
        }

        return userOpt.orElseThrow(() -> new IllegalArgumentException("User not found: " + identifier));
    }
}


