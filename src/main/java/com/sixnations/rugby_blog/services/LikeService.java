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

    
    public Optional<Like> toggleLikePost(Long postId, String username) {
        User user = userRepo.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Optional<Like> existingLike = likeRepo.findByUserAndPost(user, post);
        if (existingLike.isPresent()) {
            likeRepo.delete(existingLike.get());
            return Optional.empty(); // Like removed
        }

        Like like = new Like(user, post);
        return Optional.of(likeRepo.save(like)); // Like added
    }

   
    public Optional<Like> toggleLikeComment(Long commentId, String username) {
        User user = userRepo.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        Optional<Like> existingLike = likeRepo.findByUserAndComment(user, comment);
        if (existingLike.isPresent()) {
            likeRepo.delete(existingLike.get());
            return Optional.empty(); // Like removed
        }

        Like like = new Like(user, comment);
        return Optional.of(likeRepo.save(like)); // Like added
    }

    
    public boolean unlike(Long likeId, String username) {
        Optional<Like> likeOpt = likeRepo.findById(likeId);
        if (likeOpt.isEmpty()) return false; // Like does not exist

        Like like = likeOpt.get();
        if (!like.getUser().getUsername().equalsIgnoreCase(username)) return false; // Not authorized

        likeRepo.deleteById(likeId);
        return true;
    }

    
    public List<Like> getLikesForPost(Long postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        return likeRepo.findByPost(post);
    }

    // 
    public List<Like> getLikesForComment(Long commentId) {
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        return likeRepo.findByComment(comment);
    }

    
    public Optional<Like> getLikeById(Long likeId) {
        return likeRepo.findById(likeId);
    }
}

