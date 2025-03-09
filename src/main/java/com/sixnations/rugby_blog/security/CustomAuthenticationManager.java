package com.sixnations.rugby_blog.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.services.CustomUserDetailsService;

public class CustomAuthenticationManager implements AuthenticationManager{
	private final CustomUserDetailsService userDetailsService;
	private final CustomPasswordEncoder passwordEncoder;
	
	public CustomAuthenticationManager(CustomUserDetailsService userDetailsService, CustomPasswordEncoder passwordEncoder) {
		this.userDetailsService = userDetailsService;
		this.passwordEncoder = passwordEncoder;
	}
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
	    String identifier = authentication.getName();
	    String password = authentication.getCredentials().toString();

	    UserDetails userDetails = userDetailsService.loadUserByUsername(identifier);

	    if (!(userDetails instanceof CustomUserDetails)) {
	        throw new BadCredentialsException("User authentication failed: Invalid user type");
	    }

	    CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;

	    // 🚨 **CHECK IF USER IS SUSPENDED**
	    if (customUserDetails.getUser().isSuspended()) {
	        throw new BadCredentialsException("User is suspended and cannot log in.");
	    }

	    // ✅ Check if password is correct
	    if (!passwordEncoder.matches(password, userDetails.getPassword())) {
	        throw new BadCredentialsException("Invalid password!");
	    }

	    return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
	}



}
