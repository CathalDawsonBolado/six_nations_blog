package com.sixnations.rugby_blog.security;

import com.sixnations.rugby_blog.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    
    
	private static final long serialVersionUID = 1L;
	private final User user;

    public CustomUserDetails(User user) {
    	 System.out.println("💡 CustomUserDetails constructor called with user: " + user);
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
    	System.out.println("💥 Calling getUsername() on user: " + user);
        return user.getUsername(); 
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.isSuspended();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !user.isSuspended();
    }

    public User getUser() {
        return user;
    }
}