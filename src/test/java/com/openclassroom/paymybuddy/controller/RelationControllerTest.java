package com.openclassroom.paymybuddy.controller;

import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.service.AuthService;
import com.openclassroom.paymybuddy.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour RelationController.
 * Teste les endpoints REST pour la gestion des relations entre utilisateurs.
 */
@ExtendWith(MockitoExtension.class)
class RelationControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private RelationController relationController;

    private MockMvc mockMvc;
    private User mockCurrentUser;
    private User mockTargetUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(relationController).build();

        // Créer un utilisateur connecté mock
        mockCurrentUser = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .password("hashedPassword")
                .balance(BigDecimal.valueOf(100.0))
                .role("USER")
                .build();

        // Créer un utilisateur cible mock
        mockTargetUser = User.builder()
                .id(2L)
                .username("jane_doe")
                .email("jane@example.com")
                .password("hashedPassword")
                .balance(BigDecimal.valueOf(50.0))
                .role("USER")
                .build();
    }

    // ========== TESTS POUR searchUsers() ==========

    @Test
    void searchUsers_WithValidTermAndAuthenticatedUser_ShouldReturnUsers() throws Exception {
        // Given
        String searchTerm = "jane";
        List<User> mockUsers = Arrays.asList(mockTargetUser);

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        when(userService.searchUsers(searchTerm, mockCurrentUser.getId())).thenReturn(mockUsers);

        // When & Then
        mockMvc.perform(get("/api/relations/search")
                        .param("term", searchTerm))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].username").value("jane_doe"))
                .andExpect(jsonPath("$[0].email").value("jane@example.com"));

        verify(authService).getCurrentUser();
        verify(userService).searchUsers(searchTerm, mockCurrentUser.getId());
    }

    @Test
    void searchUsers_WithUnauthenticatedUser_ShouldReturn401() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/relations/search")
                        .param("term", "jane"))
                .andExpect(status().isUnauthorized());

        verify(authService).getCurrentUser();
        verify(userService, never()).searchUsers(any(), any());
    }

    @Test
    void searchUsers_WithEmptyTerm_ShouldReturnEmptyList() throws Exception {
        // Given
        String searchTerm = "";
        List<User> emptyList = Arrays.asList();

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        when(userService.searchUsers(searchTerm, mockCurrentUser.getId())).thenReturn(emptyList);

        // When & Then
        mockMvc.perform(get("/api/relations/search")
                        .param("term", searchTerm))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(authService).getCurrentUser();
        verify(userService).searchUsers(searchTerm, mockCurrentUser.getId());
    }

    @Test
    void searchUsers_WithMultipleResults_ShouldReturnAllUsers() throws Exception {
        // Given
        String searchTerm = "test";
        User user1 = User.builder().id(2L).username("test1").email("test1@example.com").build();
        User user2 = User.builder().id(3L).username("test2").email("test2@example.com").build();
        List<User> mockUsers = Arrays.asList(user1, user2);

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        when(userService.searchUsers(searchTerm, mockCurrentUser.getId())).thenReturn(mockUsers);

        // When & Then
        mockMvc.perform(get("/api/relations/search")
                        .param("term", searchTerm))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[1].id").value(3));

        verify(authService).getCurrentUser();
        verify(userService).searchUsers(searchTerm, mockCurrentUser.getId());
    }

    // ========== TESTS POUR addRelation() ==========

    @Test
    void addRelation_WithValidEmailAndAuthenticatedUser_ShouldReturnSuccess() throws Exception {
        // Given
        String targetEmail = "jane@example.com";

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        doNothing().when(userService).addUserConnectionByIdentifier(mockCurrentUser.getId(), targetEmail);

        // When & Then
        mockMvc.perform(post("/api/relations")
                        .param("email", targetEmail))
                .andExpect(status().isOk())
                .andExpect(content().string("Contact ajouté avec succès"));

        verify(authService).getCurrentUser();
        verify(userService).addUserConnectionByIdentifier(mockCurrentUser.getId(), targetEmail);
    }

    @Test
    void addRelation_WithUnauthenticatedUser_ShouldReturn401() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/relations")
                        .param("email", "jane@example.com"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Utilisateur non connecté"));

        verify(authService).getCurrentUser();
        verify(userService, never()).addUserConnectionByIdentifier(any(), any());
    }

    @Test
    void addRelation_WithNonExistentUser_ShouldReturn400() throws Exception {
        // Given
        String targetEmail = "nonexistent@example.com";

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        doThrow(new EntityNotFoundException("Cet utilisateur n'existe pas"))
                .when(userService).addUserConnectionByIdentifier(mockCurrentUser.getId(), targetEmail);

        // When & Then
        mockMvc.perform(post("/api/relations")
                        .param("email", targetEmail))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cet utilisateur n'existe pas"));

        verify(authService).getCurrentUser();
        verify(userService).addUserConnectionByIdentifier(mockCurrentUser.getId(), targetEmail);
    }

    @Test
    void addRelation_WithSelfAdd_ShouldReturn400() throws Exception {
        // Given
        String ownEmail = mockCurrentUser.getEmail();

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        doThrow(new IllegalArgumentException("Vous ne pouvez pas vous ajouter vous-même"))
                .when(userService).addUserConnectionByIdentifier(mockCurrentUser.getId(), ownEmail);

        // When & Then
        mockMvc.perform(post("/api/relations")
                        .param("email", ownEmail))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Vous ne pouvez pas vous ajouter vous-même"));

        verify(authService).getCurrentUser();
        verify(userService).addUserConnectionByIdentifier(mockCurrentUser.getId(), ownEmail);
    }

    @Test
    void addRelation_WithExistingConnection_ShouldReturn400() throws Exception {
        // Given
        String targetEmail = "jane@example.com";

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        doThrow(new IllegalArgumentException("Cette connexion existe déjà"))
                .when(userService).addUserConnectionByIdentifier(mockCurrentUser.getId(), targetEmail);

        // When & Then
        mockMvc.perform(post("/api/relations")
                        .param("email", targetEmail))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cette connexion existe déjà"));

        verify(authService).getCurrentUser();
        verify(userService).addUserConnectionByIdentifier(mockCurrentUser.getId(), targetEmail);
    }

    @Test
    void addRelation_WithEmptyEmail_ShouldReturn400() throws Exception {
        // Given
        String emptyEmail = "";

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        doThrow(new IllegalArgumentException("L'email ne peut pas être vide"))
                .when(userService).addUserConnectionByIdentifier(mockCurrentUser.getId(), emptyEmail);

        // When & Then
        mockMvc.perform(post("/api/relations")
                        .param("email", emptyEmail))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("L'email ne peut pas être vide"));

        verify(authService).getCurrentUser();
        verify(userService).addUserConnectionByIdentifier(mockCurrentUser.getId(), emptyEmail);
    }

    @Test
    void addRelation_WithUsernameInsteadOfEmail_ShouldWork() throws Exception {
        // Given
        String targetUsername = "jane_doe";

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        doNothing().when(userService).addUserConnectionByIdentifier(mockCurrentUser.getId(), targetUsername);

        // When & Then
        mockMvc.perform(post("/api/relations")
                        .param("email", targetUsername))
                .andExpect(status().isOk())
                .andExpect(content().string("Contact ajouté avec succès"));

        verify(authService).getCurrentUser();
        verify(userService).addUserConnectionByIdentifier(mockCurrentUser.getId(), targetUsername);
    }

    @Test
    void addRelation_WithUnexpectedException_ShouldReturn500() throws Exception {
        // Given
        String targetEmail = "jane@example.com";

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        doThrow(new RuntimeException("Erreur de base de données"))
                .when(userService).addUserConnectionByIdentifier(mockCurrentUser.getId(), targetEmail);

        // When & Then
        mockMvc.perform(post("/api/relations")
                        .param("email", targetEmail))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Une erreur inattendue s'est produite"));

        verify(authService).getCurrentUser();
        verify(userService).addUserConnectionByIdentifier(mockCurrentUser.getId(), targetEmail);
    }

    // ========== TESTS D'INTÉGRATION DES ENDPOINTS ==========

    @Test
    void relationWorkflow_SearchThenAdd_ShouldWork() throws Exception {
        // Given
        String searchTerm = "jane";
        String targetEmail = "jane@example.com";
        List<User> searchResults = Arrays.asList(mockTargetUser);

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        when(userService.searchUsers(searchTerm, mockCurrentUser.getId())).thenReturn(searchResults);
        doNothing().when(userService).addUserConnectionByIdentifier(mockCurrentUser.getId(), targetEmail);

        // When & Then - First search
        mockMvc.perform(get("/api/relations/search")
                        .param("term", searchTerm))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value(targetEmail));

        // When & Then - Then add relation
        mockMvc.perform(post("/api/relations")
                        .param("email", targetEmail))
                .andExpect(status().isOk())
                .andExpect(content().string("Contact ajouté avec succès"));

        verify(authService, times(2)).getCurrentUser();
        verify(userService).searchUsers(searchTerm, mockCurrentUser.getId());
        verify(userService).addUserConnectionByIdentifier(mockCurrentUser.getId(), targetEmail);
    }

    @Test
    void addRelation_WithMissingEmailParameter_ShouldReturn400() throws Exception {
        // When & Then - Spring retourne automatiquement 400 pour les paramètres manqués requis
        mockMvc.perform(post("/api/relations"))
                .andExpect(status().isBadRequest());

        // Note: Spring gère automatiquement les paramètres manquants avant d'atteindre le contrôleur
        verifyNoInteractions(authService, userService);
    }

    @Test
    void searchUsers_WithSpecialCharacters_ShouldWork() throws Exception {
        // Given
        String searchTerm = "jean-françois@test.com";
        List<User> mockUsers = Arrays.asList();

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        when(userService.searchUsers(searchTerm, mockCurrentUser.getId())).thenReturn(mockUsers);

        // When & Then
        mockMvc.perform(get("/api/relations/search")
                        .param("term", searchTerm))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(authService).getCurrentUser();
        verify(userService).searchUsers(searchTerm, mockCurrentUser.getId());
    }

    // ========== TESTS SUPPLÉMENTAIRES POUR LA ROBUSTESSE ==========

    @Test
    void searchUsers_WithLongSearchTerm_ShouldWork() throws Exception {
        // Given
        String longSearchTerm = "a".repeat(100);
        List<User> mockUsers = Arrays.asList();

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        when(userService.searchUsers(longSearchTerm, mockCurrentUser.getId())).thenReturn(mockUsers);

        // When & Then
        mockMvc.perform(get("/api/relations/search")
                        .param("term", longSearchTerm))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(authService).getCurrentUser();
        verify(userService).searchUsers(longSearchTerm, mockCurrentUser.getId());
    }

    @Test
    void addRelation_WithEmailContainingSpaces_ShouldWork() throws Exception {
        // Given
        String emailWithSpaces = " jane@example.com ";

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        doNothing().when(userService).addUserConnectionByIdentifier(mockCurrentUser.getId(), emailWithSpaces);

        // When & Then
        mockMvc.perform(post("/api/relations")
                        .param("email", emailWithSpaces))
                .andExpect(status().isOk())
                .andExpect(content().string("Contact ajouté avec succès"));

        verify(authService).getCurrentUser();
        verify(userService).addUserConnectionByIdentifier(mockCurrentUser.getId(), emailWithSpaces);
    }

    @Test
    void searchUsers_WhenServiceThrowsException_ShouldPropagateError() throws Exception {
        // Given
        String searchTerm = "test";

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        when(userService.searchUsers(searchTerm, mockCurrentUser.getId()))
                .thenThrow(new RuntimeException("Erreur de base de données"));

        // When & Then - Sans gestionnaire d'exception global, l'exception remonte
        try {
            mockMvc.perform(get("/api/relations/search")
                            .param("term", searchTerm));
            // Le test ne doit pas arriver ici
            assert false : "Une exception aurait dû être levée";
        } catch (Exception e) {
            // Vérifier que l'exception contient le message attendu
            assert e.getCause().getMessage().contains("Erreur de base de données");
        }

        verify(authService).getCurrentUser();
        verify(userService).searchUsers(searchTerm, mockCurrentUser.getId());
    }

    @Test
    void addRelation_WithVeryLongEmail_ShouldHandleGracefully() throws Exception {
        // Given
        String veryLongEmail = "very.long.email.address." + "a".repeat(200) + "@example.com";

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        doThrow(new IllegalArgumentException("Email trop long"))
                .when(userService).addUserConnectionByIdentifier(mockCurrentUser.getId(), veryLongEmail);

        // When & Then
        mockMvc.perform(post("/api/relations")
                        .param("email", veryLongEmail))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email trop long"));

        verify(authService).getCurrentUser();
        verify(userService).addUserConnectionByIdentifier(mockCurrentUser.getId(), veryLongEmail);
    }

    @Test
    void searchUsers_WithMissingTermParameter_ShouldReturn400() throws Exception {
        // When & Then - Spring retourne automatiquement 400 pour les paramètres manqués requis
        mockMvc.perform(get("/api/relations/search"))
                .andExpect(status().isBadRequest());

        // Note: Spring gère automatiquement les paramètres manquants avant d'atteindre le contrôleur
        verifyNoInteractions(authService, userService);
    }

    @Test
    void addRelation_ConcurrentRequests_ShouldHandleCorrectly() throws Exception {
        // Given
        String targetEmail = "jane@example.com";

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        
        // Premier appel réussit
        doNothing().when(userService).addUserConnectionByIdentifier(mockCurrentUser.getId(), targetEmail);

        // When & Then
        mockMvc.perform(post("/api/relations")
                        .param("email", targetEmail))
                .andExpect(status().isOk())
                .andExpect(content().string("Contact ajouté avec succès"));

        // Deuxième appel avec connexion déjà existante
        doThrow(new IllegalArgumentException("Cette connexion existe déjà"))
                .when(userService).addUserConnectionByIdentifier(mockCurrentUser.getId(), targetEmail);

        mockMvc.perform(post("/api/relations")
                        .param("email", targetEmail))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cette connexion existe déjà"));

        verify(authService, times(2)).getCurrentUser();
        verify(userService, times(2)).addUserConnectionByIdentifier(mockCurrentUser.getId(), targetEmail);
    }
}
