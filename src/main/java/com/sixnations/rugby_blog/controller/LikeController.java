package com.sixnations.rugby_blog.controller;

import com.sixnations.rugby_blog.models.Like;
import com.sixnations.rugby_blog.services.LikeService;
import com.sixnations.rugby_blog.security.JwtService;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/likes")
public class LikeController {
    private final LikeService likeService;
    private final JwtService jwtService;

    public LikeController(LikeService likeService, JwtService jwtService) {
        this.likeService = likeService;
        this.jwtService = jwtService;
    }

   
    @PostMapping("/post/{postId}")
    public ResponseEntity<EntityModel<Map<String, Object>>> toggleLikePost(
            @PathVariable Long postId, HttpServletRequest request) {

        String username = extractUsernameFromToken(request);
        if (username == null) return ResponseEntity.status(401).body(null);

        Optional<Like> like = likeService.toggleLikePost(postId, username);
        int updatedLikeCount = likeService.getLikeCount(postId); // ✅ Get updated like count

        // ✅ Create response map
        Map<String, Object> response = new HashMap<>();
        response.put("likesCount", updatedLikeCount); // ✅ Updated like count
        like.ifPresent(value -> response.put("id", value.getId())); // ✅ Include like ID if exists

        // ✅ Wrap in HATEOAS entity model
        EntityModel<Map<String, Object>> hateoasResponse = EntityModel.of(response);
        hateoasResponse.add(linkTo(methodOn(LikeController.class).toggleLikePost(postId, request)).withSelfRel());

        return ResponseEntity.ok(hateoasResponse);
    }


   
    @PostMapping("/comment/{commentId}")
    public ResponseEntity<EntityModel<Like>> toggleLikeComment(@PathVariable Long commentId, HttpServletRequest request) {
        String username = extractUsernameFromToken(request);
        if (username == null) return ResponseEntity.status(401).body(null);

        Optional<Like> like = likeService.toggleLikeComment(commentId, username);
        return like.map(value -> ResponseEntity.ok(convertToHateoasModel(value)))
                   .orElse(ResponseEntity.status(404).body(null));
    }

    
    @DeleteMapping("/post/{postId}")
    public ResponseEntity<EntityModel<Map<String, Object>>> unlike(@PathVariable Long postId, HttpServletRequest request) {
        String username = extractUsernameFromToken(request);
        if (username == null) return ResponseEntity.status(401).body(null);

        boolean removed = likeService.unlike(postId, username);
        if (!removed) return ResponseEntity.status(404).body(null);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Like removed successfully.");

        EntityModel<Map<String, Object>> hateoasResponse = EntityModel.of(response);
        hateoasResponse.add(linkTo(methodOn(LikeController.class).unlike(postId, request)).withSelfRel());

        return ResponseEntity.ok(hateoasResponse);
    }



    
    @GetMapping("/{likeId}")
    public ResponseEntity<EntityModel<Like>> getLikeById(@PathVariable Long likeId) {
        Optional<Like> likeOpt = likeService.getLikeById(likeId);
        if (likeOpt.isEmpty()) return ResponseEntity.status(404).body(null);

        return ResponseEntity.ok(convertToHateoasModel(likeOpt.get()));
    }

    
    public ResponseEntity<CollectionModel<EntityModel<Like>>> getLikesForPost(@PathVariable Long postId) {
        List<Like> likes = likeService.getLikesForPost(postId);
        List<EntityModel<Like>> likeModels = likes.stream().map(this::convertToHateoasModel).collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(likeModels, linkTo(methodOn(LikeController.class).getLikesForPost(postId)).withSelfRel()));
    }

    
    private EntityModel<Like> convertToHateoasModel(Like like) {
        EntityModel<Like> likeModel = EntityModel.of(like);
        if (like.getPost() != null) {
            likeModel.add(linkTo(methodOn(PostController.class).getPostById(like.getPost().getId())).withRel("post"));
        }
        likeModel.add(linkTo(methodOn(LikeController.class).getLikeById(like.getId())).withSelfRel());
        return likeModel;
    }

    
    private String extractUsernameFromToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
            return jwtService.extractUsername(authHeader.substring(7));
        } catch (Exception e) {
            return null;
        }
    }
}

