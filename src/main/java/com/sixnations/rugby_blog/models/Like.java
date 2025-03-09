package com.sixnations.rugby_blog.models;

import jakarta.persistence.*;

@Entity
@Table(name = "likes")
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = true) // ✅ Post can be NULL (if this is a comment like)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "comment_id", nullable = true) // ✅ Comment can be NULL (if this is a post like)
    private Comment comment;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // ✅ User must always be present
    private User user;

    public Like() {}

    // ✅ Constructor for liking a post
    public Like(User user, Post post) {
        this.user = user;
        this.post = post;
        this.comment = null; // Ensures this like is not linked to a comment
    }

    // ✅ Constructor for liking a comment
    public Like(User user, Comment comment) {
        this.user = user;
        this.comment = comment;
        this.post = null; // Ensures this like is not linked to a post
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

