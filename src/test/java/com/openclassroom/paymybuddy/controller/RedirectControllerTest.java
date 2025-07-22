package com.openclassroom.paymybuddy.controller;

import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour RedirectController.
 * Teste les redirections pour les utilisateurs authentifiés et non authentifiés.
 */
@ExtendWith(MockitoExtension.class)
class RedirectControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private RedirectController controller;

    private MockMvc mockMvc;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        // Créer un utilisateur mock
        mockUser = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .password("hashedPassword")
                .balance(BigDecimal.valueOf(100.0))
                .role("USER")
                .build();
    }

    // ========== TESTS POUR redirectToMyTransactions() ==========

    @Test
    void redirectToMyTransactions_WithAuthenticatedUser_ShouldRedirectToUserTransactions() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(get("/my-transactions"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user-transactions/1"));

        verify(authService).getCurrentUser();
    }

    @Test
    void redirectToMyTransactions_WithUnauthenticatedUser_ShouldRedirectToLogin() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/my-transactions"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(authService).getCurrentUser();
    }

    @Test
    void redirectToMyTransactions_WithDifferentUserId_ShouldRedirectCorrectly() throws Exception {
        // Given
        User userWithDifferentId = User.builder()
                .id(999L)
                .username("jane_doe")
                .email("jane@example.com")
                .build();
        when(authService.getCurrentUser()).thenReturn(userWithDifferentId);

        // When & Then
        mockMvc.perform(get("/my-transactions"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user-transactions/999"));

        verify(authService).getCurrentUser();
    }

    // ========== TESTS POUR redirectToAddRelation() ==========

    @Test
    void redirectToAddRelation_WithAuthenticatedUser_ShouldRedirectToAddRelations() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(get("/add-relation"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user-relations/add"));

        verify(authService).getCurrentUser();
    }

    @Test
    void redirectToAddRelation_WithUnauthenticatedUser_ShouldRedirectToLogin() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/add-relation"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(authService).getCurrentUser();
    }

    // ========== TESTS DE ROBUSTESSE ==========

    @Test
    void redirectToMyTransactions_WithAuthServiceException_ShouldPropagateException() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenThrow(new RuntimeException("Service indisponible"));

        // When & Then
        try {
            mockMvc.perform(get("/my-transactions"));
            // Le test ne doit pas arriver ici
            assert false : "Une exception aurait dû être levée";
        } catch (Exception e) {
            // Vérifier que l'exception contient le message attendu
            assert e.getCause().getMessage().contains("Service indisponible");
        }

        verify(authService).getCurrentUser();
    }

    @Test
    void redirectToAddRelation_WithAuthServiceException_ShouldPropagateException() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenThrow(new RuntimeException("Service indisponible"));

        // When & Then
        try {
            mockMvc.perform(get("/add-relation"));
            // Le test ne doit pas arriver ici
            assert false : "Une exception aurait dû être levée";
        } catch (Exception e) {
            // Vérifier que l'exception contient le message attendu
            assert e.getCause().getMessage().contains("Service indisponible");
        }

        verify(authService).getCurrentUser();
    }

    // ========== TESTS D'INTÉGRATION ==========

    @Test
    void redirectionWorkflow_AuthenticatedUserAccessBothEndpoints_ShouldWork() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // When & Then - Test my-transactions
        mockMvc.perform(get("/my-transactions"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user-transactions/1"));

        // When & Then - Test add-relation
        mockMvc.perform(get("/add-relation"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user-relations/add"));

        // Verify both calls to authService
        verify(authService, times(2)).getCurrentUser();
    }

    @Test
    void redirectionWorkflow_UnauthenticatedUserAccessBothEndpoints_ShouldRedirectToLogin() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When & Then - Test my-transactions
        mockMvc.perform(get("/my-transactions"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        // When & Then - Test add-relation
        mockMvc.perform(get("/add-relation"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        // Verify both calls to authService
        verify(authService, times(2)).getCurrentUser();
    }

    // ========== TESTS DE VALIDATION DES URLS ==========

    @Test
    void redirectToMyTransactions_UrlConstruction_ShouldBeCorrect() throws Exception {
        // Given
        User userWithLongId = User.builder()
                .id(123456789L)
                .username("test_user")
                .email("test@example.com")
                .build();
        when(authService.getCurrentUser()).thenReturn(userWithLongId);

        // When & Then
        mockMvc.perform(get("/my-transactions"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user-transactions/123456789"));

        verify(authService).getCurrentUser();
    }

    @Test
    void redirectToAddRelation_MultipleCallsSameUser_ShouldBeConsistent() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // When & Then - First call
        mockMvc.perform(get("/add-relation"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user-relations/add"));

        // When & Then - Second call
        mockMvc.perform(get("/add-relation"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user-relations/add"));

        verify(authService, times(2)).getCurrentUser();
    }
}
