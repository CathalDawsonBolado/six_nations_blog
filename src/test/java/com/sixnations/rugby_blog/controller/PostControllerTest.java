package com.sixnations.rugby_blog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixnations.rugby_blog.models.Post;
import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.security.CustomUserDetails;
import com.sixnations.rugby_blog.security.JwtService;
import com.sixnations.rugby_blog.services.CustomUserDetailsService;
import com.sixnations.rugby_blog.services.PostService;
import com.sixnations.rugby_blog.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @MockBean
    private UserService userService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    public void testGetAllPosts() throws Exception {
        Post post = new Post();
        post.setId(1L);
        post.setTitle("Test Post");
        post.setContent("This is a test post");
        User user = new User();
        user.setUsername("testUser");
        post.setUser(user);
        when(postService.getAllPosts()).thenReturn(List.of(post));

        mockMvc.perform(get("/api/posts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.hashMapList[0].title").value("Test Post"))
        .andExpect(jsonPath("$._embedded.hashMapList[0].content").value("This is a test post"));
    }

    @Test
    public void testGetPostById() throws Exception {
        Post post = new Post();
        post.setId(1L);
        post.setTitle("Test Post");
        post.setContent("This is a test post");
        User user = new User();
        user.setUsername("testUser");
        post.setUser(user);

        when(postService.getPostById(1L)).thenReturn(Optional.of(post));

        mockMvc.perform(get("/api/posts/{postId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Post"))
                .andExpect(jsonPath("$.content").value("This is a test post"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @Test
    public void testGetPostByIdNotFound() throws Exception {
        when(postService.getPostById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/posts/{postId}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreatePostUnauthorized() throws Exception {
        mockMvc.perform(post("/api/posts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New Post\", \"content\":\"Content for the new post\"}"))
                .andExpect(status().isForbidden());
    }
}
