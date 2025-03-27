package com.sixnations.rugby_blog.controller;

import com.sixnations.rugby_blog.models.Comment;
import com.sixnations.rugby_blog.security.JwtService;
import com.sixnations.rugby_blog.services.CommentService;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;

import java.nio.file.AccessDeniedException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private CommentController commentController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCommentsByPost() {
        Long postId = 1L;

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setContent("Great match!");

        when(commentService.getCommentsByPost(postId)).thenReturn(List.of(comment));

        ResponseEntity<?> response = commentController.getCommentsByPost(postId);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertThat(body).containsKey("_embedded");
        Map<String, Object> embedded = (Map<String, Object>) body.get("_embedded");
        List<?> commentList = (List<?>) embedded.get("commentList");

        assertThat(commentList).hasSize(1);
    }

    @Test
    void testCreateComment_Success() throws AccessDeniedException {
        Long postId = 1L;
        Comment inputComment = new Comment();
        inputComment.setContent("Nice try!");

        String token = "Bearer test.token.here";
        String username = "user1";

        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtService.extractUsername("test.token.here")).thenReturn(username);
        when(commentService.postExists(postId)).thenReturn(true);

        Comment savedComment = new Comment();
        savedComment.setId(2L);
        savedComment.setContent("Nice try!");
        when(commentService.createComment(postId, "Nice try!", username)).thenReturn(savedComment);

        ResponseEntity<EntityModel<Comment>> response = commentController.createComment(postId, inputComment, request);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getContent()).isEqualTo(savedComment);
    }

    @Test
    void testCreateComment_Unauthorized() {
        Long postId = 1L;
        Comment comment = new Comment();
        comment.setContent("Nice!");

        when(request.getHeader("Authorization")).thenReturn(null); // No token

        ResponseEntity<EntityModel<Comment>> response = commentController.createComment(postId, comment, request);

        assertThat(response.getStatusCodeValue()).isEqualTo(401);
    }

    @Test
    void testCreateComment_PostNotFound() {
        Long postId = 100L;
        Comment comment = new Comment();
        comment.setContent("Doesn't matter");

        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtService.extractUsername("token")).thenReturn("user1");
        when(commentService.postExists(postId)).thenReturn(false);

        ResponseEntity<EntityModel<Comment>> response = commentController.createComment(postId, comment, request);

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    void testCreateComment_ExceptionHandling() {
        Long postId = 1L;
        Comment comment = new Comment();
        comment.setContent("Boom");

        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtService.extractUsername("token")).thenThrow(new RuntimeException("Token error"));

        ResponseEntity<EntityModel<Comment>> response = commentController.createComment(postId, comment, request);

        assertThat(response.getStatusCodeValue()).isEqualTo(401);
    }
}

