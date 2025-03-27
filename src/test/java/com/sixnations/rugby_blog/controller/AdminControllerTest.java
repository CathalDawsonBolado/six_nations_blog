package com.sixnations.rugby_blog.controller;

import com.sixnations.rugby_blog.models.User;
import com.sixnations.rugby_blog.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(authorities = "ADMIN") // ✅ FIXED
    void testPromoteUser_Success() throws Exception {
        when(userService.promoteUserToAdmin(1L)).thenReturn(true);

        mockMvc.perform(put("/api/admin/promote/1"))
               .andExpect(status().isOk())
               .andExpect(content().string("User promoted to Admin successfully."));
    }

    @Test
    @WithMockUser(authorities = "ADMIN") // ✅ FIXED
    void testPromoteUser_Failure() throws Exception {
        when(userService.promoteUserToAdmin(2L)).thenReturn(false);

        mockMvc.perform(put("/api/admin/promote/2"))
               .andExpect(status().isBadRequest())
               .andExpect(content().string("User not found or already an Admin."));
    }

    @Test
    @WithMockUser(authorities = "ADMIN") // ✅ FIXED
    void testSuspendUser_Success() throws Exception {
        when(userService.suspendUser(3L)).thenReturn("success");

        mockMvc.perform(put("/api/admin/suspend/3"))
               .andExpect(status().isOk())
               .andExpect(content().string("User suspended successfully."));
    }

    @Test
    @WithMockUser(authorities = "ADMIN") // ✅ FIXED
    void testSuspendUser_Failure() throws Exception {
        when(userService.suspendUser(4L)).thenReturn("User already suspended");

        mockMvc.perform(put("/api/admin/suspend/4"))
               .andExpect(status().isBadRequest())
               .andExpect(content().string("User already suspended"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN") // ✅ FIXED
    void testUnsuspendUser_Success() throws Exception {
        when(userService.unsuspendedUser(5L)).thenReturn(true);

        mockMvc.perform(put("/api/admin/unsuspend/5"))
               .andExpect(status().isOk())
               .andExpect(content().string("User has been unsuspended successfully"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN") // ✅ FIXED
    void testUnsuspendUser_Failure() throws Exception {
        when(userService.unsuspendedUser(6L)).thenReturn(false);

        mockMvc.perform(put("/api/admin/unsuspend/6"))
               .andExpect(status().isBadRequest())
               .andExpect(content().string("Failed to unsuspend user. User is not suspended"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN") // ✅ FIXED
    void testGetAllUsers() throws Exception {
        User user1 = new User(); user1.setUsername("user1");
        User user2 = new User(); user2.setUsername("user2");

        when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/admin/users"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    @WithMockUser(authorities = "USER") // ✅ USER only, should be forbidden
    void testAccessDeniedForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
               .andExpect(status().isForbidden());
    }
}

