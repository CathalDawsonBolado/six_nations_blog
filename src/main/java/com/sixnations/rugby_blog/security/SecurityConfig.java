package com.sixnations.rugby_blog.security;

import com.sixnations.rugby_blog.services.CustomUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAuthenticationManager authenticationManager;

    public SecurityConfig(CustomUserDetailsService userDetailsService, CustomPasswordEncoder passwordEncoder) {
        this.authenticationManager = new CustomAuthenticationManager(userDetailsService, passwordEncoder);
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, 
                                                   JwtService jwtService, 
                                                   CustomUserDetailsService userDetailsService, 
                                                   CustomPasswordEncoder passwordEncoder) throws Exception {
        http.csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/users").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/posts/create").authenticated() // ✅ Only logged-in users can post
                .requestMatchers("/api/posts/edit/**").authenticated()
                .requestMatchers("/api/posts/delete/**").authenticated()
                .requestMatchers("/api/posts").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(new JwtAuthenticationFilter(jwtService, userDetailsService), 
                             UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}


