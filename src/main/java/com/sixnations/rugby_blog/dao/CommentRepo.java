package com.sixnations.rugby_blog.dao;

import com.sixnations.rugby_blog.models.Comment;
import com.sixnations.rugby_blog.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepo extends JpaRepository<Comment, Long> {
    List<Comment> findByPost(Post post);  
}

