package com.sixnations.rugby_blog.controller;

import com.sixnations.rugby_blog.models.Post;
import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.security.CustomUserDetails;
import com.sixnations.rugby_blog.services.PostService;
import com.sixnations.rugby_blog.services.UserService;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.validation.Valid;


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

    
    @GetMapping
    public CollectionModel<EntityModel<Map<String, Object>>> getAllPosts() {//get all the post
        List<EntityModel<Map<String, Object>>> posts = postService.getAllPosts().stream()
                .map(post -> {
                    Map<String, Object> postData = new HashMap<>();
                    postData.put("id", post.getId());
                    postData.put("title", post.getTitle());
                    postData.put("content", post.getContent());
                    postData.put("username", post.getUser().getUsername());
                    

                    return EntityModel.of(postData,
                            linkTo(methodOn(PostController.class).getPostById(post.getId())).withSelfRel(),
                            linkTo(methodOn(PostController.class).getAllPosts()).withRel("all-posts")
                    );
                })
                .collect(Collectors.toList());

        return CollectionModel.of(posts, linkTo(methodOn(PostController.class).getAllPosts()).withSelfRel());
    }



    
    @GetMapping("/{postId}")//get post for particular post id
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

    @PostMapping("/create")//create post
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> createPost(@Valid @RequestBody Post post, @AuthenticationPrincipal CustomUserDetails userDetails) {
    	if (userDetails == null) {
    	    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not authenticated");
    	}

    	Optional<User> optionalUser = userService.findByUsername(userDetails.getUsername());
    	if (optionalUser.isEmpty()) {
    	    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
    	}


        //  Assign the user to the post before saving
        User user = optionalUser.get();
        post.setUser(user);

        //  Save the post
        Post savedPost = postService.createPost(post);
        return ResponseEntity.ok(savedPost);
    }


}
