package com.sixnations.rugby_blog.services;
import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.security.CustomPasswordEncoder;
import com.sixnations.rugby_blog.dao.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


import org.springframework.stereotype.Service;
import java.util.Optional;
@Service
public class UserService {
	private final UserRepo userRepo;
	private final CustomPasswordEncoder passwordEncoder;
	private final PostRepo postRepo;
	private final CommentRepo commentRepo;
	
	public UserService(UserRepo userRepo, CustomPasswordEncoder passwordEncoder, PostRepo postRepo, CommentRepo commentRepo) {
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
		this.postRepo = postRepo;
		this.commentRepo = commentRepo;
	}

	public Optional<User> findByUserEmail(String email) {
		return userRepo.findByEmailIgnoreCase(email);
	}
	public boolean promoteUserToAdmin(Long userId) {
	    Optional<User> userOpt = userRepo.findById(userId);
	    if (userOpt.isPresent()) {
	        User user = userOpt.get();
	        if (user.getRole() == User.Role.ADMIN) {  // Prevent duplicate promotions
	            return false;  // User is already an admin
	        }
	        user.setRole(User.Role.ADMIN);
	        userRepo.save(user);
	        return true;
	    }
	    return false;
	}

	public boolean suspendUser(Long userId) {
		Optional<User> userOpt = userRepo.findById(userId);
		if(userOpt.isPresent()) {
			User user = userOpt.get();
			user.setSuspended(true);
			userRepo.save(user);
			return true;
		}
		return false;
	}
	public boolean unsuspendedUser(Long userId) {
		Optional<User> userOpt = userRepo.findById(userId);
		if(userOpt.isPresent()) {
			User user = userOpt.get();
			if(user.isSuspended()) {
				user.setSuspended(false);
				userRepo.save(user);
				return true;
			}
		}
		return false;
	}
	
	public boolean deletePost(Long postId) {
		if(postRepo.existsById(postId)){
			postRepo.deleteById(postId);
			return true;
		}
		return false;
	}
	
	public boolean deleteComment(Long commentId) {
		if(commentRepo.existsById(commentId)){
			postRepo.deleteById(commentId);
			return true;
		}
		return false;
	}
	
	public User saveUser(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		return userRepo.save(user);
	}
}
