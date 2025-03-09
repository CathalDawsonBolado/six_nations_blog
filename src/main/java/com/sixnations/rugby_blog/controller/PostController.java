package com.sixnations.rugby_blog.controller;

import com.sixnations.rugby_blog.dao.UserRepo;
import com.sixnations.rugby_blog.models.Post;
import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.security.JwtService;
import com.sixnations.rugby_blog.services.PostService;
import io.jsonwebtoken.Claims;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;
    private final JwtService jwtService;
    private final UserRepo userRepo;

    public PostController(PostService postService, JwtService jwtService, UserRepo userRepo) {
        this.postService = postService;
        this.jwtService = jwtService;
        this.userRepo = userRepo;
    }

    // ✅ Get All Posts (HATEOAS)
    @GetMapping
    public CollectionModel<EntityModel<Post>> getAllPosts() {
        List<EntityModel<Post>> posts = postService.getAllPosts().stream()
                .map(post -> EntityModel.of(post,
                        linkTo(methodOn(PostController.class).getPostById(post.getId())).withSelfRel(),
                        linkTo(methodOn(PostController.class).getAllPosts()).withRel("all-posts")
                )).collect(Collectors.toList());

        return CollectionModel.of(posts, linkTo(methodOn(PostController.class).getAllPosts()).withSelfRel());
    }

    // ✅ Get a specific post by ID (HATEOAS)
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

    // ✅ Create a Post (HATEOAS)
    @PostMapping("/create")
    public ResponseEntity<?> createPost(@RequestBody Post post, HttpServletRequest request) {
        try {
            if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
                return ResponseEntity.status(400).body("Title is required!");
            }
            if (post.getContent() == null || post.getContent().trim().isEmpty()) {
                return ResponseEntity.status(400).body("Content is required!");
            }

            String identifier = extractUsernameFromToken(request);
            if (identifier == null) return ResponseEntity.status(401).body("Unauthorized: Missing or invalid token");

            User user = getUserByIdentifier(identifier);
            if (user == null) return ResponseEntity.status(404).body("User not found!");

            post.setUser(user);
            Post savedPost = postService.createPost(post);

            EntityModel<Post> postResource = EntityModel.of(savedPost,
                    linkTo(methodOn(PostController.class).getPostById(savedPost.getId())).withSelfRel(),
                    linkTo(methodOn(PostController.class).getAllPosts()).withRel("all-posts")
            );

            return ResponseEntity.ok(postResource);

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body("Forbidden: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }

    // ✅ Delete a Post (HATEOAS)
    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId, HttpServletRequest request) {
        try {
            String username = extractUsernameFromToken(request);
            if (username == null) return ResponseEntity.status(401).body("Unauthorized");

            boolean deleted = postService.deletePost(postId, username);
            if (!deleted) return ResponseEntity.status(404).body("Post not found.");

            return ResponseEntity.ok().body("Post deleted successfully.");
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body("Forbidden: " + e.getMessage());
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

    private User getUserByIdentifier(String identifier) throws AccessDeniedException {
        Optional<User> userOpt = identifier.contains("@") ?
                userRepo.findByEmailIgnoreCase(identifier) :
                userRepo.findByUsernameIgnoreCase(identifier);
        
        return userOpt.orElseThrow(() -> new IllegalArgumentException("User not found: " + identifier));
    }
}





