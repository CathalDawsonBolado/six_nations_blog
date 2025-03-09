package com.sixnations.rugby_blog.controller;

import com.sixnations.rugby_blog.models.Comment;
import com.sixnations.rugby_blog.security.JwtService;
import com.sixnations.rugby_blog.services.CommentService;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.AccessDeniedException;
import java.util.List;
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

    // ✅ Get Comments for a Post (HATEOAS)
    @GetMapping("/{postId}")
    public CollectionModel<EntityModel<Comment>> getCommentsByPost(@PathVariable Long postId) {
        List<EntityModel<Comment>> comments = commentService.getCommentsByPost(postId).stream()
                .map(comment -> EntityModel.of(comment,
                        linkTo(methodOn(CommentController.class).getCommentsByPost(postId)).withSelfRel()
                )).collect(Collectors.toList());

        return CollectionModel.of(comments, linkTo(methodOn(CommentController.class).getCommentsByPost(postId)).withSelfRel());
    }

    // ✅ Create a Comment (HATEOAS)
    @PostMapping("/create/{postId}")
    public ResponseEntity<EntityModel<Comment>> createComment(@PathVariable Long postId, @RequestBody Comment comment, HttpServletRequest request) {
        try {
            String username = extractUsernameFromToken(request);
            if (username == null) return ResponseEntity.status(401).body(null);

            Comment savedComment = commentService.createComment(postId, comment.getContent(), username);
            EntityModel<Comment> commentResource = EntityModel.of(savedComment,
                    linkTo(methodOn(CommentController.class).getCommentsByPost(postId)).withRel("post-comments")
            );

            return ResponseEntity.ok(commentResource);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    private String extractUsernameFromToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
            return jwtService.extractUsername(authHeader.substring(7));
        } catch (Exception e) {
            return null;
        }
    }
}

