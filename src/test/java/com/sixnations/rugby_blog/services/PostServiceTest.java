package com.sixnations.rugby_blog.services;

import com.sixnations.rugby_blog.dao.PostRepo;
import com.sixnations.rugby_blog.dao.UserRepo;
import com.sixnations.rugby_blog.models.Post;
import com.sixnations.rugby_blog.models.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class PostServiceTest {

    private PostRepo postRepo;
    private UserRepo userRepo;
    private PostService postService;

    @BeforeEach
    void setUp() {
        postRepo = mock(PostRepo.class);
        userRepo = mock(UserRepo.class);
        postService = new PostService(postRepo, userRepo);
    }

    @Test
    void testGetAllPosts() {
        List<Post> mockPosts = Arrays.asList(new Post(), new Post());
        when(postRepo.findAll()).thenReturn(mockPosts);

        List<Post> result = postService.getAllPosts();
        assertEquals(2, result.size());
        verify(postRepo, times(1)).findAll();
    }

    @Test
    void testGetPostById() {
        Post post = new Post();
        post.setId(1L);
        when(postRepo.findById(1L)).thenReturn(Optional.of(post));

        Optional<Post> result = postService.getPostById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void testGetPostById_NotFound() {
        when(postRepo.findById(1L)).thenReturn(Optional.empty());

        Optional<Post> result = postService.getPostById(1L);
        assertFalse(result.isPresent());
    }

    @Test
    void testGetPostsByUser() {
        Post post1 = new Post();
        Post post2 = new Post();
        when(postRepo.findByUserUsername("testuser")).thenReturn(List.of(post1, post2));

        List<Post> posts = postService.getPostsByUser("testuser");
        assertEquals(2, posts.size());
        verify(postRepo).findByUserUsername("testuser");
    }

    @Test
    void testCreatePost() {
        Post post = new Post();
        when(postRepo.save(post)).thenReturn(post);

        Post saved = postService.createPost(post);
        assertEquals(post, saved);
        verify(postRepo).save(post);
    }

    @Test
    void testGetUserByIdentifier_Email() {
        User user = new User();
        user.setEmail("user@example.com");
        when(userRepo.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));

        User result = postService.getUserByIdentifier("user@example.com");
        assertEquals("user@example.com", result.getEmail());
    }

    @Test
    void testGetUserByIdentifier_Username() {
        User user = new User();
        user.setUsername("username");
        when(userRepo.findByUsernameIgnoreCase("username")).thenReturn(Optional.of(user));

        User result = postService.getUserByIdentifier("username");
        assertEquals("username", result.getUsername());
    }

    @Test
    void testGetUserByIdentifier_NotFound() {
        when(userRepo.findByEmailIgnoreCase("notfound@example.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                postService.getUserByIdentifier("notfound@example.com"));

        assertTrue(exception.getMessage().contains("User not found"));
    }
}

