package com.sixnations.rugby_blog.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // ✅ Extract Token from Header
        String token = request.getHeader("Authorization");
        System.out.println("📢 Extracted Token from Request: " + token);

        if (token == null || !token.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ Remove "Bearer " prefix
        token = token.substring(7);

        try {
            // ✅ Extract Username from Token
            String username = jwtService.extractUsername(token);
            System.out.println("🔍 Extracted Username: " + username);

            // ✅ Ensure User is not already authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // ✅ Load User from Database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // ✅ Validate Token
                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("✅ User Authenticated: " + username);
                }
            }

        } catch (Exception e) {
            System.out.println("❌ JWT Validation Failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}

