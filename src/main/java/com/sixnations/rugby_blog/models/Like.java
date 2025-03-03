package com.sixnations.rugby_blog.models;

import jakarta.persistence.*;
@Entity
@Table(name = "likes")
public class Like {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name="post_id", nullable = false)
	private Post post;
	
	@ManyToOne
	@JoinColumn(name="user_id", nullable = false)
	private User user;
	
	public Like() {}
	
	public Like(Post post, User user) {
		this.post = post;
		this.user =user;
	}

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
