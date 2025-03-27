package com.sixnations.rugby_blog.security;

import com.sixnations.rugby_blog.services.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration


public class SecurityConfig {

    private final CustomAuthenticationManager authenticationManager;

    public SecurityConfig(CustomUserDetailsService userDetailsService, CustomPasswordEncoder passwordEncoder) {
        this.authenticationManager = new CustomAuthenticationManager(userDetailsService, passwordEncoder);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService,
                                                   CustomUserDetailsService userDetailsService) throws Exception {
        http.csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/styles.css", "/script.js").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/comments/**").permitAll()
                .requestMatchers("/api/auth/login", "/api/users", "/api/auth/register").permitAll()
                .requestMatchers("/user-dashboard.css", "/user-dashboard.js", "/user-dashboard.html").permitAll()
                .requestMatchers("/admin-dashboard.css", "/admin-dashboard.js", "/admin-dashboard.html").permitAll()
                .requestMatchers("/images/SixNationsTrophy.PNG").permitAll()
                .requestMatchers("/images/TripleCrown.PNG").permitAll()
                .requestMatchers("/images/RugbyBall.png").permitAll()

                
                
                
                .requestMatchers(HttpMethod.POST, "/api/comments/create/**").hasAnyAuthority("USER", "ADMIN")

                // ✅ Ensure posting works
                .requestMatchers(HttpMethod.POST, "/api/posts/create").hasAnyAuthority("USER", "ADMIN") 
               
                // ✅ Admin routes
                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(new JwtAuthenticationFilter(jwtService, userDetailsService), 
                             UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}







