package com.sixnations.rugby_blog.controller;

import com.sixnations.rugby_blog.models.Post;
import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.services.PostService;
import com.sixnations.rugby_blog.services.UserService;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;
    private final UserService userService;

    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    // ✅ Get All Posts
    @GetMapping
    public CollectionModel<EntityModel<Post>> getAllPosts() {
        List<EntityModel<Post>> posts = postService.getAllPosts().stream()
                .map(post -> EntityModel.of(post,
                        linkTo(methodOn(PostController.class).getPostById(post.getId())).withSelfRel(),
                        linkTo(methodOn(PostController.class).getAllPosts()).withRel("all-posts")
                )).collect(Collectors.toList());

        return CollectionModel.of(posts, linkTo(methodOn(PostController.class).getAllPosts()).withSelfRel());
    }

    // ✅ Get Post by ID
    @GetMapping("/{postId}")
    public ResponseEntity<EntityModel<Post>> getPostById(@PathVariable Long postId) {
        Optional<Post> postOpt = postService.getPostById(postId);
        if (postOpt.isEmpty()) return ResponseEntity.status(404).build();

        Post post = postOpt.get();
        EntityModel<Post> postResource = EntityModel.of(post,
                linkTo(methodOn(PostController.class).getPostById(postId)).withSelfRel(),
                linkTo(methodOn(PostController.class).getAllPosts()).withRel("all-posts")
        );

        return ResponseEntity.ok(postResource);
    }

    // ✅ Search Posts
    @GetMapping("/search")
    public ResponseEntity<List<Post>> searchPosts(@RequestParam("query") String query) {
        List<Post> posts = postService.searchPosts(query);
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Post> createPost(@RequestBody Post post, Principal principal) {
        if (principal == null) {
            System.out.println("❌ ERROR: Principal is NULL - User might not be authenticated");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden
        }

        // ✅ Fetch the logged-in user from the database
        Optional<User> optionalUser = userService.findByUsername(principal.getName());
        if (optionalUser.isEmpty()) {
            System.out.println("❌ ERROR: User not found for username: " + principal.getName());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
        }

        // ✅ Assign the user to the post before saving
        User user = optionalUser.get();
        post.setUser(user);
        System.out.println("✅ User: " + user.getUsername() + " is creating a post");

        Post savedPost = postService.createPost(post);
        return ResponseEntity.ok(savedPost);
    }



}
