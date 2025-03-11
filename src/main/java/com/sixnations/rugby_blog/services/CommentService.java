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

    // ✅ Get comments for a post
    public List<Comment> getCommentsByPost(Long postId) {
        Optional<Post> postOpt = postRepo.findById(postId);
        
        if (postOpt.isEmpty()) {
            System.out.println("❌ ERROR: Post ID " + postId + " not found.");
            return List.of(); // Return empty list instead of null
        }
        
        return commentRepo.findByPost(postOpt.get());
    }

    // ✅ Check if post exists (Missing method in your service, now added!)
    public boolean postExists(Long postId) {
        return postRepo.existsById(postId);
    }

    // ✅ Create a comment
    public Comment createComment(Long postId, String content, String username) throws AccessDeniedException {
        System.out.println("📢 Checking if post exists for ID: " + postId);
        Optional<Post> postOpt = postRepo.findById(postId);
        
        if (postOpt.isEmpty()) {
            System.out.println("❌ ERROR: Post not found with ID: " + postId);
            throw new IllegalArgumentException("Post not found.");
        }

        System.out.println("📢 Checking if user exists: " + username);
        Optional<User> userOpt = userRepo.findByUsernameIgnoreCase(username);
        
        if (userOpt.isEmpty()) {
            System.out.println("❌ ERROR: User not found: " + username);
            throw new IllegalArgumentException("User not found.");
        }

        // ❌ Check if user is suspended
        User user = userOpt.get();
        if (user.isSuspended()) {
            System.out.println("❌ ERROR: User " + username + " is suspended and cannot comment.");
            throw new AccessDeniedException("User is suspended and cannot comment.");
        }

        // ✅ Save comment
        Comment comment = new Comment(content, postOpt.get(), user);
        Comment savedComment = commentRepo.save(comment);
        System.out.println("✅ Comment successfully created with ID: " + savedComment.getId());

        return savedComment;
    }

    // ✅ Delete a comment (Only Admin or the author)
    public boolean deleteComment(Long commentId, String username) throws AccessDeniedException {
        System.out.println("📢 Attempting to delete comment with ID: " + commentId);
        Optional<Comment> commentOpt = commentRepo.findById(commentId);
        
        if (commentOpt.isEmpty()) {
            System.out.println("❌ ERROR: Comment ID " + commentId + " not found.");
            return false; // Comment not found
        }

        Comment comment = commentOpt.get();
        User author = comment.getUser();
        Optional<User> userOpt = userRepo.findByUsernameIgnoreCase(username);

        if (userOpt.isEmpty()) {
            System.out.println("❌ ERROR: User " + username + " not found.");
            return false;
        }

        User user = userOpt.get();
        if (user.equals(author) || user.getRole() == User.Role.ADMIN) {
            commentRepo.deleteById(commentId);
            System.out.println("✅ Comment ID " + commentId + " deleted successfully.");
            return true;
        }

        System.out.println("❌ ERROR: User " + username + " is not allowed to delete comment ID " + commentId);
        throw new AccessDeniedException("You are not allowed to delete this comment!");
    }
}

