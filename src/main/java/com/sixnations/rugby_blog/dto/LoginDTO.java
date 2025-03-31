package com.sixnations.rugby_blog.dto;

public class LoginDTO {
	private String identifier;
	private String password;
	public LoginDTO(String identifier, String password) {
		this.identifier = identifier;
		this.password = password;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setEmail(String identifier) {
		this.identifier = identifier;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
