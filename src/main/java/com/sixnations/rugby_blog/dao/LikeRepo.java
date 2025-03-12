package com.sixnations.rugby_blog.dao;

import com.sixnations.rugby_blog.models.Like;
import com.sixnations.rugby_blog.models.Post;
import com.sixnations.rugby_blog.models.Comment;
import com.sixnations.rugby_blog.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepo extends JpaRepository<Like, Long> {
    List<Like> findByPost(Post post);
    List<Like> findByComment(Comment comment);
    Optional<Like> findByUserAndPost(User user, Post post);
    Optional<Like> findByUserAndComment(User user, Comment comment);
    
    
    int countByPostId(Long postId);
    
    
    int countByCommentId(Long commentId);
}

