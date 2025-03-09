package com.sixnations.rugby_blog.services;

import com.sixnations.rugby_blog.models.Like;
import com.sixnations.rugby_blog.models.Post;
import com.sixnations.rugby_blog.models.Comment;
import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.dao.LikeRepo;
import com.sixnations.rugby_blog.dao.UserRepo;
import com.sixnations.rugby_blog.dao.PostRepo;
import com.sixnations.rugby_blog.dao.CommentRepo;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class LikeService {
    private final LikeRepo likeRepo;
    private final UserRepo userRepo;
    private final PostRepo postRepo;
    private final CommentRepo commentRepo;

    public LikeService(LikeRepo likeRepo, UserRepo userRepo, PostRepo postRepo, CommentRepo commentRepo) {
        this.likeRepo = likeRepo;
        this.userRepo = userRepo;
        this.postRepo = postRepo;
        this.commentRepo = commentRepo;
    }

    // ✅ Like a post
    public Like likePost(Long postId, String username) {
        // Find the user
        User user = userRepo.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Find the post
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // Check if the user already liked the post
        Optional<Like> existingLike = likeRepo.findByUserAndPost(user, post);
        if (existingLike.isPresent()) {
            return existingLike.get(); // Return existing like to prevent duplicates
        }

        // Save and return new like
        Like like = new Like(user, post);
        return likeRepo.save(like);
    }

    // ✅ Like a comment
    public Like likeComment(Long commentId, String username) {
        // Find the user
        User user = userRepo.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Find the comment
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        // Check if the user already liked the comment
        Optional<Like> existingLike = likeRepo.findByUserAndComment(user, comment);
        if (existingLike.isPresent()) {
            return existingLike.get(); // Return existing like to prevent duplicates
        }

        // Save and return new like
        Like like = new Like(user, comment);
        return likeRepo.save(like);
    }

    // ✅ Unlike a post or comment
    public boolean unlike(Long likeId, String username) {
        Optional<Like> likeOpt = likeRepo.findById(likeId);
        if (likeOpt.isEmpty()) {
            return false; // Like does not exist
        }

        Like like = likeOpt.get();
        if (!like.getUser().getUsername().equalsIgnoreCase(username)) {
            return false; // Can't unlike a like that doesn't belong to the user
        }

        likeRepo.deleteById(likeId);
        return true;
    }

    // ✅ Get likes for a post
    public List<Like> getLikesForPost(Long postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        return likeRepo.findByPost(post);
    }

    // ✅ Get likes for a comment
    public List<Like> getLikesForComment(Long commentId) {
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        return likeRepo.findByComment(comment);
    }

    // ✅ Get Like by ID
    public Optional<Like> getLikeById(Long likeId) {
        return likeRepo.findById(likeId);
    }
}


