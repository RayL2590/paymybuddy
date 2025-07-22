package com.openclassroom.paymybuddy.controller;

import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.service.AuthService;
import com.openclassroom.paymybuddy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour ProfilController.
 * Teste toutes les fonctionnalités de gestion du profil utilisateur.
 */
@ExtendWith(MockitoExtension.class)
class ProfilControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfilController profilController;

    private MockMvc mockMvc;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // Configuration de MockMvc avec validation Bean
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        SpringValidatorAdapter springValidator = new SpringValidatorAdapter(validator);
        
        mockMvc = MockMvcBuilders.standaloneSetup(profilController)
                .setValidator(springValidator)
                .build();

        // Créer un utilisateur mock
        mockUser = User.builder()
                .id(1L)
                .username("johndoe123") // nom valide selon le pattern du DTO
                .email("john@example.com")
                .password("$2a$12$hashedPassword")
                .balance(BigDecimal.valueOf(100.0))
                .role("USER")
                .build();
    }

    // ========== TESTS POUR showProfile() ==========


    @Test
    void showProfile_WithUnauthenticatedUser_ShouldRedirectToLogin() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/profil"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(authService).getCurrentUser();
    }

    // ========== TESTS POUR changePassword() ==========

    @Test
    void changePassword_WithValidData_ShouldSucceed() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(passwordEncoder.matches("currentPassword", mockUser.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("newPassword123", mockUser.getPassword())).thenReturn(false);
        doNothing().when(userService).changePassword(mockUser.getId(), "newPassword123");

        // When & Then
        mockMvc.perform(post("/profil/change-password")
                        .param("currentPassword", "currentPassword")
                        .param("newPassword", "newPassword123")
                        .param("confirmPassword", "newPassword123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profil"))
                .andExpect(flash().attribute("successMessage", "Mot de passe modifié avec succès"));

        verify(authService).getCurrentUser();
        verify(passwordEncoder).matches("currentPassword", mockUser.getPassword());
        verify(passwordEncoder).matches("newPassword123", mockUser.getPassword());
        verify(userService).changePassword(mockUser.getId(), "newPassword123");
    }

    @Test
    void changePassword_WithUnauthenticatedUser_ShouldRedirectToLogin() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/profil/change-password")
                        .param("currentPassword", "currentPassword")
                        .param("newPassword", "newPassword123")
                        .param("confirmPassword", "newPassword123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(authService).getCurrentUser();
        verifyNoInteractions(userService, passwordEncoder);
    }

    @Test
    void changePassword_WithIncorrectCurrentPassword_ShouldReturnError() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(passwordEncoder.matches("wrongPassword", mockUser.getPassword())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/profil/change-password")
                        .param("currentPassword", "wrongPassword")
                        .param("newPassword", "newPassword123")
                        .param("confirmPassword", "newPassword123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profil"))
                .andExpect(flash().attribute("errorMessage", "Mot de passe actuel incorrect"));

        verify(authService).getCurrentUser();
        verify(passwordEncoder).matches("wrongPassword", mockUser.getPassword());
        verify(userService, never()).changePassword(any(), any());
    }

    @Test
    void changePassword_WithNonMatchingPasswords_ShouldReturnError() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(post("/profil/change-password")
                        .param("currentPassword", "currentPassword")
                        .param("newPassword", "newPassword123")
                        .param("confirmPassword", "differentPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profil"))
                .andExpect(flash().attribute("errorMessage", "Les mots de passe ne correspondent pas"));

        verify(authService).getCurrentUser();
        verifyNoInteractions(passwordEncoder);
        verify(userService, never()).changePassword(any(), any());
    }

    @Test
    void changePassword_WithSamePassword_ShouldReturnError() throws Exception {
        // Given
        String currentValidPassword = "currentPass123"; // Mot de passe valide qui respecte le pattern
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(passwordEncoder.matches(currentValidPassword, mockUser.getPassword())).thenReturn(true);
        when(passwordEncoder.matches(currentValidPassword, mockUser.getPassword())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/profil/change-password")
                        .param("currentPassword", currentValidPassword)
                        .param("newPassword", currentValidPassword)
                        .param("confirmPassword", currentValidPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profil"))
                .andExpect(flash().attribute("errorMessage", "Le nouveau mot de passe doit être différent de l'ancien"));

        verify(authService).getCurrentUser();
        verify(passwordEncoder, times(2)).matches(currentValidPassword, mockUser.getPassword());
        verify(userService, never()).changePassword(any(), any());
    }

    @Test
    void changePassword_WithServiceException_ShouldReturnError() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(passwordEncoder.matches("currentPassword", mockUser.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("newPassword123", mockUser.getPassword())).thenReturn(false);
        doThrow(new RuntimeException("Service error")).when(userService).changePassword(mockUser.getId(), "newPassword123");

        // When & Then
        mockMvc.perform(post("/profil/change-password")
                        .param("currentPassword", "currentPassword")
                        .param("newPassword", "newPassword123")
                        .param("confirmPassword", "newPassword123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profil"))
                .andExpect(flash().attribute("errorMessage", "Erreur lors de la modification du mot de passe"));

        verify(userService).changePassword(mockUser.getId(), "newPassword123");
    }

    // ========== TESTS POUR checkUsernameAvailability() ==========

    @Test
    void checkUsernameAvailability_WithAuthenticatedUser_ShouldReturnAvailability() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(userService.isUsernameAvailable("newUser123", mockUser.getId())).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/profil/check-username")
                        .param("username", "newUser123"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(authService).getCurrentUser();
        verify(userService).isUsernameAvailable("newUser123", mockUser.getId());
    }

    @Test
    void checkUsernameAvailability_WithUnauthenticatedUser_ShouldReturnFalse() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/profil/check-username")
                        .param("username", "newUser123"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(authService).getCurrentUser();
        verifyNoInteractions(userService);
    }

    @Test
    void checkUsernameAvailability_WithUnavailableUsername_ShouldReturnFalse() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(userService.isUsernameAvailable("taken123", mockUser.getId())).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/profil/check-username")
                        .param("username", "taken123"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(authService).getCurrentUser();
        verify(userService).isUsernameAvailable("taken123", mockUser.getId());
    }

    // ========== TESTS POUR checkEmailAvailability() ==========

    @Test
    void checkEmailAvailability_WithAuthenticatedUser_ShouldReturnAvailability() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(userService.isEmailAvailable("new@example.com", mockUser.getId())).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/profil/check-email")
                        .param("email", "new@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(authService).getCurrentUser();
        verify(userService).isEmailAvailable("new@example.com", mockUser.getId());
    }

    @Test
    void checkEmailAvailability_WithUnauthenticatedUser_ShouldReturnFalse() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/profil/check-email")
                        .param("email", "new@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(authService).getCurrentUser();
        verifyNoInteractions(userService);
    }

    // ========== TESTS POUR changeUsername() ==========

    @Test
    void changeUsername_WithValidData_ShouldSucceed() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(passwordEncoder.matches("currentPassword", mockUser.getPassword())).thenReturn(true);
        doNothing().when(userService).changeUsername(mockUser.getId(), "newUser123");

        // When & Then
        mockMvc.perform(post("/profil/change-username")
                        .param("newUsername", "newUser123")
                        .param("currentPassword", "currentPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profil"))
                .andExpect(flash().attribute("successMessage", "Nom d'utilisateur modifié avec succès"));

        verify(authService).getCurrentUser();
        verify(passwordEncoder).matches("currentPassword", mockUser.getPassword());
        verify(userService).changeUsername(mockUser.getId(), "newUser123");
    }

    @Test
    void changeUsername_WithUnauthenticatedUser_ShouldRedirectToLogin() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/profil/change-username")
                        .param("newUsername", "newUser123")
                        .param("currentPassword", "currentPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(authService).getCurrentUser();
        verifyNoInteractions(userService, passwordEncoder);
    }

    @Test
    void changeUsername_WithIncorrectPassword_ShouldReturnError() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(passwordEncoder.matches("wrongPassword", mockUser.getPassword())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/profil/change-username")
                        .param("newUsername", "newUser123")
                        .param("currentPassword", "wrongPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profil"))
                .andExpect(flash().attribute("errorMessage", "Mot de passe incorrect"));

        verify(authService).getCurrentUser();
        verify(passwordEncoder).matches("wrongPassword", mockUser.getPassword());
        verify(userService, never()).changeUsername(any(), any());
    }

    @Test
    void changeUsername_WithSameUsername_ShouldReturnError() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(passwordEncoder.matches("currentPassword", mockUser.getPassword())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/profil/change-username")
                        .param("newUsername", "johndoe123") // même nom que l'utilisateur actuel
                        .param("currentPassword", "currentPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profil"))
                .andExpect(flash().attribute("errorMessage", "Le nouveau nom d'utilisateur doit être différent de l'actuel"));

        verify(authService).getCurrentUser();
        verify(passwordEncoder).matches("currentPassword", mockUser.getPassword());
        verify(userService, never()).changeUsername(any(), any());
    }

    @Test
    void changeUsername_WithTakenUsername_ShouldReturnError() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(passwordEncoder.matches("currentPassword", mockUser.getPassword())).thenReturn(true);
        doThrow(new IllegalArgumentException("Ce nom d'utilisateur est déjà utilisé"))
                .when(userService).changeUsername(mockUser.getId(), "taken123");

        // When & Then
        mockMvc.perform(post("/profil/change-username")
                        .param("newUsername", "taken123")
                        .param("currentPassword", "currentPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profil"))
                .andExpect(flash().attribute("errorMessage", "Ce nom d'utilisateur est déjà utilisé"));

        verify(userService).changeUsername(mockUser.getId(), "taken123");
    }

    @Test
    void changeUsername_WithEmptyPassword_ShouldReturnError() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(post("/profil/change-username")
                        .param("newUsername", "newUser123")
                        .param("currentPassword", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profil"))
                .andExpect(flash().attribute("errorMessage", "Le mot de passe actuel est requis pour cette modification"));

        verify(authService).getCurrentUser();
        verifyNoInteractions(passwordEncoder);
        verify(userService, never()).changeUsername(any(), any());
    }

    // ========== TESTS POUR changeEmail() ==========

    @Test
    void changeEmail_WithValidData_ShouldSucceed() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(passwordEncoder.matches("currentPassword", mockUser.getPassword())).thenReturn(true);
        doNothing().when(userService).changeEmail(mockUser.getId(), "new@example.com");

        // When & Then
        mockMvc.perform(post("/profil/change-email")
                        .param("newEmail", "new@example.com")
                        .param("currentPassword", "currentPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profil"))
                .andExpect(flash().attribute("successMessage", "Adresse email modifiée avec succès"));

        verify(authService).getCurrentUser();
        verify(passwordEncoder).matches("currentPassword", mockUser.getPassword());
        verify(userService).changeEmail(mockUser.getId(), "new@example.com");
    }

    @Test
    void changeEmail_WithUnauthenticatedUser_ShouldRedirectToLogin() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/profil/change-email")
                        .param("newEmail", "new@example.com")
                        .param("currentPassword", "currentPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(authService).getCurrentUser();
        verifyNoInteractions(userService, passwordEncoder);
    }

    @Test
    void changeEmail_WithIncorrectPassword_ShouldReturnError() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(passwordEncoder.matches("wrongPassword", mockUser.getPassword())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/profil/change-email")
                        .param("newEmail", "new@example.com")
                        .param("currentPassword", "wrongPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profil"))
                .andExpect(flash().attribute("errorMessage", "Mot de passe incorrect"));

        verify(authService).getCurrentUser();
        verify(passwordEncoder).matches("wrongPassword", mockUser.getPassword());
        verify(userService, never()).changeEmail(any(), any());
    }

    @Test
    void changeEmail_WithSameEmail_ShouldReturnError() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(passwordEncoder.matches("currentPassword", mockUser.getPassword())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/profil/change-email")
                        .param("newEmail", "john@example.com") // même email que l'utilisateur actuel
                        .param("currentPassword", "currentPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profil"))
                .andExpect(flash().attribute("errorMessage", "La nouvelle adresse email doit être différente de l'actuelle"));

        verify(authService).getCurrentUser();
        verify(passwordEncoder).matches("currentPassword", mockUser.getPassword());
        verify(userService, never()).changeEmail(any(), any());
    }

    @Test
    void changeEmail_WithTakenEmail_ShouldReturnError() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(passwordEncoder.matches("currentPassword", mockUser.getPassword())).thenReturn(true);
        doThrow(new IllegalArgumentException("Cette adresse email est déjà utilisée"))
                .when(userService).changeEmail(mockUser.getId(), "taken@example.com");

        // When & Then
        mockMvc.perform(post("/profil/change-email")
                        .param("newEmail", "taken@example.com")
                        .param("currentPassword", "currentPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profil"))
                .andExpect(flash().attribute("errorMessage", "Cette adresse email est déjà utilisée"));

        verify(userService).changeEmail(mockUser.getId(), "taken@example.com");
    }

    @Test
    void changeEmail_WithEmptyPassword_ShouldReturnError() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(post("/profil/change-email")
                        .param("newEmail", "new@example.com")
                        .param("currentPassword", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profil"))
                .andExpect(flash().attribute("errorMessage", "Le mot de passe actuel est requis pour cette modification"));

        verify(authService).getCurrentUser();
        verifyNoInteractions(passwordEncoder);
        verify(userService, never()).changeEmail(any(), any());
    }

    @Test
    void changeEmail_WithServiceException_ShouldReturnError() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(passwordEncoder.matches("currentPassword", mockUser.getPassword())).thenReturn(true);
        doThrow(new RuntimeException("Service error")).when(userService).changeEmail(mockUser.getId(), "new@example.com");

        // When & Then
        mockMvc.perform(post("/profil/change-email")
                        .param("newEmail", "new@example.com")
                        .param("currentPassword", "currentPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profil"))
                .andExpect(flash().attribute("errorMessage", "Erreur lors de la modification de l'adresse email"));

        verify(userService).changeEmail(mockUser.getId(), "new@example.com");
    }

    // ========== TESTS DE VALIDATION ==========

    @Test
    void changePassword_WithInvalidPassword_ShouldHandleValidationErrors() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // When & Then - Teste avec un mot de passe trop court qui ne respecte pas le pattern
        mockMvc.perform(post("/profil/change-password")
                .param("currentPassword", "current123")
                .param("newPassword", "short")
                .param("confirmPassword", "short"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profil"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void changeUsername_WithInvalidUsername_ShouldHandleValidationErrors() throws Exception {
        // When & Then - Teste avec un nom d'utilisateur invalide (trop court)
        // Les erreurs de validation Bean retournent un statut 400 AVANT d'entrer dans le contrôleur
        mockMvc.perform(post("/profil/change-username")
                        .param("newUsername", "ab") // trop court
                        .param("currentPassword", "currentPassword"))
                .andExpect(status().isBadRequest()); // 400 pour les erreurs de validation

    }

    @Test
    void changeEmail_WithInvalidEmail_ShouldHandleValidationErrors() throws Exception {
        // When & Then - Teste avec un email invalide
        // Les erreurs de validation Bean retournent un statut 400 AVANT d'entrer dans le contrôleur
        mockMvc.perform(post("/profil/change-email")
                        .param("newEmail", "invalid-email")
                        .param("currentPassword", "currentPassword"))
                .andExpect(status().isBadRequest()); // 400 pour les erreurs de validation

        // Pas de verify car la méthode du contrôleur n'est jamais appelée
    }
}
