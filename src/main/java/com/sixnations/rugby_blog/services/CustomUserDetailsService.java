package com.sixnations.rugby_blog.services;
import com.sixnations.rugby_blog.dao.UserRepo;
import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.models.User.Role;
import com.sixnations.rugby_blog.security.CustomUserDetails;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
	
	private final UserRepo userRepo;
	public CustomUserDetailsService(UserRepo userRepo) {
		this.userRepo = userRepo;
	}
	@Override
	public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
	    Optional<User> userOpt = userRepo.findByEmailIgnoreCase(identifier);
	    
	    if (userOpt.isEmpty()) {
	        userOpt = userRepo.findByUsernameIgnoreCase(identifier);
	    }

	    User user = userOpt.orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));

	    return new CustomUserDetails(user); 
	}

	
	private Collection<? extends GrantedAuthority> getAuthorities(Role role){
		return List.of(new SimpleGrantedAuthority("ROLE_" + role));
	}
	

}
