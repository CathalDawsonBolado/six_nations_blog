package com.sixnations.rugby_blog.services;

import com.sixnations.rugby_blog.models.Post;
import com.sixnations.rugby_blog.dao.PostRepo;

import java.util.List;

import org.springframework.stereotype.Service;
@Service
public class PostService {
	private final PostRepo postRepo;
	
	public PostService(PostRepo postRepo) {
		this.postRepo = postRepo;
	}
	
	public List<Post> getAllPosts(){
		return postRepo.findAll();
	}
	
	public Post createPost(Post post) {
		return postRepo.save(post);
	}

}
