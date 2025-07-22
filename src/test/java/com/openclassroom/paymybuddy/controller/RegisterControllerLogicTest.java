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
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour RegisterController - logique métier uniquement
 * Cette approche teste la logique sans les aspects HTTP/MVC
 */
@ExtendWith(MockitoExtension.class)
class RegisterControllerLogicTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private RegisterController registerController;

    private RegisterDTO validRegisterDTO;
    private User mockUser;

    @BeforeEach
    void setUp() {
        validRegisterDTO = new RegisterDTO();
        validRegisterDTO.setUsername("testuser");
        validRegisterDTO.setEmail("test@example.com");
        validRegisterDTO.setPassword("password123");
        validRegisterDTO.setConfirmPassword("password123");

        mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .balance(BigDecimal.ZERO)
                .role("USER")
                .build();
    }

    @Test
    void showRegisterForm_ShouldAddRegisterDTOToModel() {
        // When
        String viewName = registerController.showRegisterForm(model);

        // Then
        assertEquals("register", viewName);
        verify(model).addAttribute(eq("registerDTO"), any(RegisterDTO.class));
    }

    @Test
    void processRegistration_WithValidData_ShouldRedirectToLogin() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.registerUser(any(RegisterDTO.class))).thenReturn(mockUser);

        // When
        String viewName = registerController.processRegistration(validRegisterDTO, bindingResult, redirectAttributes, model);

        // Then
        assertEquals("redirect:/login", viewName);
        verify(userService).registerUser(validRegisterDTO);
        verify(redirectAttributes).addFlashAttribute("successMessage", 
                "Inscription réussie ! Vous pouvez maintenant vous connecter.");
    }

    @Test
    void processRegistration_WithValidationErrors_ShouldReturnRegisterForm() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        String viewName = registerController.processRegistration(validRegisterDTO, bindingResult, redirectAttributes, model);

        // Then
        assertEquals("register", viewName);
        verify(userService, never()).registerUser(any(RegisterDTO.class));
        verify(model).addAttribute("registerDTO", validRegisterDTO);
    }

    @Test
    void processRegistration_WithExistingEmail_ShouldReturnRegisterFormWithError() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.registerUser(any(RegisterDTO.class)))
                .thenThrow(new IllegalArgumentException("Un utilisateur avec cet email existe déjà"));

        // When
        String viewName = registerController.processRegistration(validRegisterDTO, bindingResult, redirectAttributes, model);

        // Then
        assertEquals("register", viewName);
        verify(userService).registerUser(validRegisterDTO);
        verify(model).addAttribute("registerDTO", validRegisterDTO);
        verify(model).addAttribute("errorMessage", "Un utilisateur avec cet email existe déjà");
    }

    @Test
    void processRegistration_WithUnexpectedError_ShouldReturnRegisterFormWithGenericError() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.registerUser(any(RegisterDTO.class)))
                .thenThrow(new RuntimeException("Erreur inattendue"));

        // When
        String viewName = registerController.processRegistration(validRegisterDTO, bindingResult, redirectAttributes, model);

        // Then
        assertEquals("register", viewName);
        verify(userService).registerUser(validRegisterDTO);
        verify(model).addAttribute("registerDTO", validRegisterDTO);
        verify(model).addAttribute("errorMessage", "Une erreur inattendue s'est produite. Veuillez réessayer.");
    }
}
