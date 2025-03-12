package com.sixnations.rugby_blog.controller;

import com.sixnations.rugby_blog.models.Comment;
import com.sixnations.rugby_blog.security.JwtService;
import com.sixnations.rugby_blog.services.CommentService;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentService commentService;
    private final JwtService jwtService;

    public CommentController(CommentService commentService, JwtService jwtService) {
        this.commentService = commentService;
        this.jwtService = jwtService;
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getCommentsByPost(@PathVariable Long postId) {
        System.out.println("📢 Fetching comments for post: " + postId);

        List<Comment> commentList = commentService.getCommentsByPost(postId);

        if (commentList.isEmpty()) {
            System.out.println("⚠️ No comments found for post: " + postId);
        }

        List<EntityModel<Comment>> comments = commentList.stream()
                .map(comment -> EntityModel.of(comment,
                        linkTo(methodOn(CommentController.class).getCommentsByPost(postId)).withSelfRel()
                )).collect(Collectors.toList());

       
        Map<String, Object> response = new HashMap<>();
        response.put("_embedded", Collections.singletonMap("commentList", comments));

        return ResponseEntity.ok(response);
    }



    
    @PostMapping("/create/{postId}") 
    public ResponseEntity<EntityModel<Comment>> createComment(
            @PathVariable Long postId, 
            @RequestBody Comment comment, 
            HttpServletRequest request) {
        try {
            System.out.println("📢 Received request to add a comment to post ID: " + postId);

           
            String username = extractUsernameFromToken(request);
            
            if (username == null) {
                System.out.println("❌ ERROR: Unauthorized request - No username extracted.");
                return ResponseEntity.status(401).build();
            }

            System.out.println("✅ User " + username + " is adding a comment to post ID " + postId);
            System.out.println("📝 Comment Content: " + comment.getContent());

            
            if (!commentService.postExists(postId)) {
                System.out.println("❌ ERROR: Post with ID " + postId + " does not exist.");
                return ResponseEntity.status(404).build(); // Post not found
            }

            
            Comment savedComment = commentService.createComment(postId, comment.getContent(), username);
            
            System.out.println("✅ Successfully saved comment with ID: " + savedComment.getId());

            EntityModel<Comment> commentResource = EntityModel.of(savedComment,
                    linkTo(methodOn(CommentController.class).getCommentsByPost(postId)).withRel("post-comments")
            );

            return ResponseEntity.ok(commentResource);

        } catch (Exception e) {
            System.out.println("❌ ERROR: Failed to create comment: " + e.getMessage());
            e.printStackTrace(); 
            return ResponseEntity.status(500).build();
        }
    }


    private String extractUsernameFromToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("❌ ERROR: No valid Authorization header found");
                return null;
            }
            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);
            System.out.println("🔍 Extracted Username from Token: " + username);
            return username;
        } catch (Exception e) {
            System.out.println("❌ ERROR extracting username: " + e.getMessage());
            return null;
        }
    }
}


