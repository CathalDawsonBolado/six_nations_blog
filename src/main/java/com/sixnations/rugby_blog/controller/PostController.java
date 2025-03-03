package com.sixnations.rugby_blog.controller;

import com.sixnations.rugby_blog.models.Post;
import com.sixnations.rugby_blog.services.PostService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {
	private final PostService postService;
	
	public PostController(PostService postService) {
		this.postService = postService;
	}
	
	@GetMapping
	
	public List<Post> getAllPosts(){
		return postService.getAllPosts();
	}
	
	@PostMapping
	public ResponseEntity<Post> createPost(@RequestBody Post post){
		return ResponseEntity.ok(postService.createPost(post));
	}

}
