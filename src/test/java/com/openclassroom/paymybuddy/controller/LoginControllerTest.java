package com.openclassroom.paymybuddy.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.openclassroom.paymybuddy.configuration.CustomUserDetailsService;
import com.openclassroom.paymybuddy.configuration.SpringSecurityConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour LoginController.
 * Teste l'accès aux endpoints en fonction des rôles utilisateur.
 */
@WebMvcTest(LoginController.class)
@Import(SpringSecurityConfig.class)
public class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // ========== TESTS ENDPOINT /user ==========

    @Test
    @WithMockUser(roles = "USER")
    void getUser_WithUserRole_ShouldReturnWelcomeMessage() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome, User"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUser_WithAdminRole_ShouldReturnWelcomeMessage() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome, User"));
    }

    @Test
    void getUser_WithoutAuthentication_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // ========== TESTS ENDPOINT /admin ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAdmin_WithAdminRole_ShouldReturnWelcomeMessage() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome, Admin"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAdmin_WithUserRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAdmin_WithoutAuthentication_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // ========== TESTS DE SÉCURITÉ SUPPLÉMENTAIRES ==========

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getUser_WithSpecificUser_ShouldReturnCorrectMessage() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome, User"))
                .andExpect(content().contentType("text/plain;charset=UTF-8"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAdmin_WithSpecificAdmin_ShouldReturnCorrectMessage() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome, Admin"))
                .andExpect(content().contentType("text/plain;charset=UTF-8"));
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void getUser_WithMultipleRoles_ShouldReturnWelcomeMessage() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome, User"));
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void getAdmin_WithMultipleRoles_ShouldReturnWelcomeMessage() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome, Admin"));
    }

    // ========== TESTS DE CAS LIMITES ==========

    @Test
    @WithMockUser(roles = "MODERATOR")
    void getUser_WithCustomRole_ShouldReturnWelcomeMessage() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome, User"));
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void getAdmin_WithCustomRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "", roles = "USER")
    void getUser_WithEmptyUsername_ShouldReturnWelcomeMessage() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome, User"));
    }

    // ========== TESTS DE MÉTHODES HTTP ==========

    @Test
    @WithMockUser(roles = "USER")
    void userEndpoint_WithGetMethod_ShouldBeSupported() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEndpoint_WithGetMethod_ShouldBeSupported() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk());
    }
}
