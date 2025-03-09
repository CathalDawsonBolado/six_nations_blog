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
        return postOpt.map(commentRepo::findByPost).orElse(null);
    }

    // ✅ Create a comment
    public Comment createComment(Long postId, String content, String username) throws AccessDeniedException {
        Optional<Post> postOpt = postRepo.findById(postId);
        Optional<User> userOpt = userRepo.findByUsernameIgnoreCase(username);

        if (postOpt.isEmpty() || userOpt.isEmpty()) {
            throw new IllegalArgumentException("Post or User not found.");
        }

        // ❌ Check if user is suspended
        User user = userOpt.get();
        if (user.isSuspended()) {
            throw new AccessDeniedException("User is suspended and cannot comment.");
        }

        Comment comment = new Comment(content, postOpt.get(), user);
        return commentRepo.save(comment);
    }

    // ✅ Delete a comment (Only Admin or the author)
    public boolean deleteComment(Long commentId, String username) throws AccessDeniedException {
        Optional<Comment> commentOpt = commentRepo.findById(commentId);
        if (commentOpt.isPresent()) {
            Comment comment = commentOpt.get();
            User author = comment.getUser();
            Optional<User> userOpt = userRepo.findByUsernameIgnoreCase(username);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.equals(author) || user.getRole() == User.Role.ADMIN) {
                    commentRepo.deleteById(commentId);
                    return true;
                }
            }
            throw new AccessDeniedException("You are not allowed to delete this comment!");
        }
        return false;
    }
}
