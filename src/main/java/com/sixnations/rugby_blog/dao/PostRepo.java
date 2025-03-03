package com.sixnations.rugby_blog.dao;
import com.sixnations.rugby_blog.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface PostRepo extends JpaRepository <Post, Long>{

}
