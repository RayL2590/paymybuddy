package com.openclassroom.paymybuddy.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour AuthController.
 * Teste la gestion de la page de connexion et ses différents paramètres.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Configuration d'un ViewResolver pour éviter l'erreur de vue circulaire
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".html");
        
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setViewResolvers(viewResolver)
                .build();
    }

    // ========== TESTS D'AFFICHAGE NORMAL ==========

    @Test
    void login_WithoutParameters_ShouldDisplayLoginPage() throws Exception {
        // When & Then
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeDoesNotExist("errorMessage"))
                .andExpect(model().attributeDoesNotExist("logoutMessage"));
    }

    // ========== TESTS DE GESTION D'ERREURS ==========

    @Test
    void login_WithErrorParameter_ShouldDisplayErrorMessage() throws Exception {
        // When & Then
        mockMvc.perform(get("/login").param("error", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("errorMessage", "Email ou mot de passe incorrect"))
                .andExpect(model().attributeDoesNotExist("logoutMessage"));
    }

    @Test
    void login_WithErrorParameterTrue_ShouldDisplayErrorMessage() throws Exception {
        // When & Then
        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("errorMessage", "Email ou mot de passe incorrect"))
                .andExpect(model().attributeDoesNotExist("logoutMessage"));
    }

    @Test
    void login_WithErrorParameterCustomValue_ShouldDisplayErrorMessage() throws Exception {
        // When & Then
        mockMvc.perform(get("/login").param("error", "invalid_credentials"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("errorMessage", "Email ou mot de passe incorrect"))
                .andExpect(model().attributeDoesNotExist("logoutMessage"));
    }

    // ========== TESTS DE GESTION DE DÉCONNEXION ==========

    @Test
    void login_WithLogoutParameter_ShouldDisplayLogoutMessage() throws Exception {
        // When & Then
        mockMvc.perform(get("/login").param("logout", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("logoutMessage", "Vous avez été déconnecté avec succès"))
                .andExpect(model().attributeDoesNotExist("errorMessage"));
    }

    @Test
    void login_WithLogoutParameterTrue_ShouldDisplayLogoutMessage() throws Exception {
        // When & Then
        mockMvc.perform(get("/login").param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("logoutMessage", "Vous avez été déconnecté avec succès"))
                .andExpect(model().attributeDoesNotExist("errorMessage"));
    }

    @Test
    void login_WithLogoutParameterSuccess_ShouldDisplayLogoutMessage() throws Exception {
        // When & Then
        mockMvc.perform(get("/login").param("logout", "success"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("logoutMessage", "Vous avez été déconnecté avec succès"))
                .andExpect(model().attributeDoesNotExist("errorMessage"));
    }

    // ========== TESTS DE CAS COMBINÉS ==========

    @Test
    void login_WithBothErrorAndLogoutParameters_ShouldDisplayBothMessages() throws Exception {
        // When & Then
        mockMvc.perform(get("/login")
                .param("error", "true")
                .param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("errorMessage", "Email ou mot de passe incorrect"))
                .andExpect(model().attribute("logoutMessage", "Vous avez été déconnecté avec succès"));
    }

    @Test
    void login_WithMultipleErrorParameters_ShouldStillDisplayErrorMessage() throws Exception {
        // When & Then
        mockMvc.perform(get("/login")
                .param("error", "first")
                .param("error", "second"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("errorMessage", "Email ou mot de passe incorrect"))
                .andExpect(model().attributeDoesNotExist("logoutMessage"));
    }

    // ========== TESTS DE CAS LIMITES ==========

    @Test
    void login_WithEmptyParameters_ShouldDisplayBothMessages() throws Exception {
        // When & Then
        mockMvc.perform(get("/login")
                .param("error", "")
                .param("logout", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("errorMessage", "Email ou mot de passe incorrect"))
                .andExpect(model().attribute("logoutMessage", "Vous avez été déconnecté avec succès"));
    }

    @Test
    void login_WithNullValueParameters_ShouldDisplayBothMessages() throws Exception {
        // When & Then
        mockMvc.perform(get("/login")
                .param("error", "null")
                .param("logout", "null"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("errorMessage", "Email ou mot de passe incorrect"))
                .andExpect(model().attribute("logoutMessage", "Vous avez été déconnecté avec succès"));
    }

    // ========== TESTS D'INTÉGRATION ==========

    @Test
    void login_SimulateLoginErrorWorkflow_ShouldWork() throws Exception {
        // Simuler un workflow complet : affichage normal → erreur → logout

        // 1. Page normale
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeDoesNotExist("errorMessage"))
                .andExpect(model().attributeDoesNotExist("logoutMessage"));

        // 2. Erreur de connexion
        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("errorMessage", "Email ou mot de passe incorrect"));

        // 3. Déconnexion réussie
        mockMvc.perform(get("/login").param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("logoutMessage", "Vous avez été déconnecté avec succès"));
    }

    // ========== TESTS DE COMPORTEMENT ==========

    @Test
    void login_ParameterValidation_ShouldWork() throws Exception {
        // Test que n'importe quelle valeur de paramètre active la condition
        
        // Paramètre avec valeur "false" → condition vraie quand même
        mockMvc.perform(get("/login").param("error", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("errorMessage"));

        // Paramètre avec valeur "0" → condition vraie quand même
        mockMvc.perform(get("/login").param("logout", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("logoutMessage"));
    }
}
