package com.openclassroom.paymybuddy.controller;

import com.openclassroom.paymybuddy.dto.RegisterDTO;
import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour RegisterController avec MockMvc standalone configuré
 * Alternative si on veut rester en tests unitaires purs
 */
@ExtendWith(MockitoExtension.class)
class RegisterControllerUnitTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private RegisterController registerController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Configuration d'un ViewResolver pour éviter l'erreur "Circular view path"
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(registerController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    void showRegisterForm_ShouldDisplayRegisterPage() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerDTO"));
    }

    @Test
    void processRegistration_WithValidData_ShouldRedirectToLogin() throws Exception {
        // Given
        User mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .balance(BigDecimal.ZERO)
                .role("USER")
                .build();

        when(userService.registerUser(any(RegisterDTO.class))).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(post("/register")
                .param("username", "testuser")
                .param("email", "test@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService).registerUser(any(RegisterDTO.class));
    }

    @Test
    void processRegistration_WithExistingEmail_ShouldReturnFormWithError() throws Exception {
        // Given
        when(userService.registerUser(any(RegisterDTO.class)))
                .thenThrow(new IllegalArgumentException("Un utilisateur avec cet email existe déjà"));

        // When & Then
        mockMvc.perform(post("/register")
                .param("username", "testuser")
                .param("email", "existing@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(userService).registerUser(any(RegisterDTO.class));
    }

    @Test
    void processRegistration_WithUnexpectedError_ShouldReturnFormWithGenericError() throws Exception {
        // Given
        when(userService.registerUser(any(RegisterDTO.class)))
                .thenThrow(new RuntimeException("Erreur inattendue"));

        // When & Then
        mockMvc.perform(post("/register")
                .param("username", "testuser")
                .param("email", "test@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("errorMessage", 
                        "Une erreur inattendue s'est produite. Veuillez réessayer."));

        verify(userService).registerUser(any(RegisterDTO.class));
    }
}
