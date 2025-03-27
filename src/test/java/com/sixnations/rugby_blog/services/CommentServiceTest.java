package com.sixnations.rugby_blog.services;

import com.sixnations.rugby_blog.dao.CommentRepo;
import com.sixnations.rugby_blog.dao.PostRepo;
import com.sixnations.rugby_blog.dao.UserRepo;
import com.sixnations.rugby_blog.models.Comment;
import com.sixnations.rugby_blog.models.Post;
import com.sixnations.rugby_blog.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommentServiceTest {

    private CommentRepo commentRepo;
    private PostRepo postRepo;
    private UserRepo userRepo;
    private CommentService commentService;

    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        commentRepo = mock(CommentRepo.class);
        postRepo = mock(PostRepo.class);
        userRepo = mock(UserRepo.class);
        commentService = new CommentService(commentRepo, postRepo, userRepo);

        // Create shared test user and post
        testUser = new User();
        testUser.setUsername("john_doe");
        testUser.setSuspended(false);

        testPost = new Post("Test Title", "Test Content", testUser);
        testPost.setId(1L);
    }

    @Test
    void testGetCommentsByPost_PostExists() {
        List<Comment> mockComments = List.of(new Comment(), new Comment());
        when(postRepo.findById(1L)).thenReturn(Optional.of(testPost));
        when(commentRepo.findByPost(testPost)).thenReturn(mockComments);

        List<Comment> result = commentService.getCommentsByPost(1L);

        assertEquals(2, result.size());
        verify(commentRepo).findByPost(testPost);
    }

    @Test
    void testGetCommentsByPost_PostDoesNotExist() {
        when(postRepo.findById(999L)).thenReturn(Optional.empty());

        List<Comment> result = commentService.getCommentsByPost(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(commentRepo, never()).findByPost(any());
    }

    @Test
    void testPostExists_True() {
        when(postRepo.existsById(1L)).thenReturn(true);

        assertTrue(commentService.postExists(1L));
    }

    @Test
    void testPostExists_False() {
        when(postRepo.existsById(999L)).thenReturn(false);

        assertFalse(commentService.postExists(999L));
    }

    @Test
    void testCreateComment_Success() throws Exception {
        when(postRepo.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepo.findByUsernameIgnoreCase("john_doe")).thenReturn(Optional.of(testUser));

        Comment savedComment = new Comment("Nice post!", testPost, testUser);
        when(commentRepo.save(any(Comment.class))).thenReturn(savedComment);

        Comment result = commentService.createComment(1L, "Nice post!", "john_doe");

        assertNotNull(result);
        assertEquals("Nice post!", result.getContent());
        assertEquals(testPost, result.getPost());
        assertEquals(testUser, result.getUser());
    }

    @Test
    void testCreateComment_PostNotFound() {
        when(postRepo.findById(2L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                commentService.createComment(2L, "Hello", "john_doe")
        );

        assertEquals("Post not found.", ex.getMessage());
    }

    @Test
    void testCreateComment_UserNotFound() {
        when(postRepo.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepo.findByUsernameIgnoreCase("john_doe")).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                commentService.createComment(1L, "Hi!", "john_doe")
        );

        assertEquals("User not found.", ex.getMessage());
    }

    @Test
    void testCreateComment_UserSuspended() {
        testUser.setSuspended(true);
        when(postRepo.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepo.findByUsernameIgnoreCase("john_doe")).thenReturn(Optional.of(testUser));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                commentService.createComment(1L, "Forbidden", "john_doe")
        );

        assertEquals("User is suspended and cannot comment.", ex.getMessage());
    }
}


