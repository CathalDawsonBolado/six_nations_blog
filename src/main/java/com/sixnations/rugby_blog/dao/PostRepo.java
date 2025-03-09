package com.sixnations.rugby_blog.dao;
import com.sixnations.rugby_blog.models.Post;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface PostRepo extends JpaRepository <Post, Long>{
	List<Post> findByUserUsername(String username);
}
