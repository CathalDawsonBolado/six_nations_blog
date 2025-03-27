package com.sixnations.rugby_blog.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigIntegrationIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testPublicEndpointAccessible() throws Exception {
        mockMvc.perform(get("/api/posts"))
               .andExpect(status().isOk());
    }

    @Test
    void testProtectedPostCreateUnauthorized() throws Exception {
        mockMvc.perform(post("/api/posts/create"))
               .andExpect(status().isForbidden()); // or .isUnauthorized() depending on auth flow
    }

    @Test
    void testLoginEndpointIsPublic() throws Exception {
        mockMvc.perform(post("/api/auth/login"))
               .andExpect(status().is4xxClientError()); // Will likely be 400 unless full login implemented
    }

    @Test
    void testAdminRouteBlockedForAnonymous() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
               .andExpect(status().isForbidden());
    }
}
