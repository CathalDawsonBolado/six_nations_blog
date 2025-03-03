package com.sixnations.rugby_blog.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name= "posts")
public class Post {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String title;
	
	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;
	
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	private LocalDateTime createdAt = LocalDateTime.now();
	
	public Post() {}
	public Post(String title, String content, User user) {
		this.title = title;
		this.content = content;
		this.user = user;
		this.createdAt = LocalDateTime.now();
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

}
