package com.sixnations.rugby_blog.services;
import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.dao.UserRepo;

import org.springframework.stereotype.Service;
import java.util.Optional;
@Service
public class UserService {
	private final UserRepo userRepo;
	
	
	public UserService(UserRepo userRepo) {
		this.userRepo = userRepo;
	}

	public Optional<User> findByUserEmail(String email) {
		return userRepo.findByEmail(email);
	}
	
	public User saveUser(User user) {
		return userRepo.save(user);
	}
}
