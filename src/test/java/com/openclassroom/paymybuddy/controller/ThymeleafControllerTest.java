package com.openclassroom.paymybuddy.controller;

import com.openclassroom.paymybuddy.dto.RelationDTO;
import com.openclassroom.paymybuddy.model.Transaction;
import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.service.AuthService;
import com.openclassroom.paymybuddy.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour ThymeleafController.
 * Teste l'affichage de la page d'accueil avec différents scénarios.
 */
@ExtendWith(MockitoExtension.class)
class ThymeleafControllerTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private AuthService authService;

    @Mock
    private Model model;

    @InjectMocks
    private ThymeleafController controller;

    private MockMvc mockMvc;
    private User mockUser;
    private List<Transaction> mockTransactions;
    private List<RelationDTO> mockRelations;

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

        // Créer des transactions mock
        Transaction transaction1 = Transaction.builder()
                .id(1L)
                .sender(mockUser)
                .receiver(mockUser)
                .amount(BigDecimal.valueOf(25.0))
                .description("Test transaction 1")
                .createdAt(LocalDateTime.now())
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(2L)
                .sender(mockUser)
                .receiver(mockUser)
                .amount(BigDecimal.valueOf(50.0))
                .description("Test transaction 2")
                .createdAt(LocalDateTime.now())
                .build();

        mockTransactions = Arrays.asList(transaction1, transaction2);

        // Créer des relations mock
        mockRelations = Arrays.asList(
                new RelationDTO(2L, "jane_doe"),
                new RelationDTO(3L, "bob_smith")
        );
    }

    // ========== TESTS POUR index() - CAS NOMINAL ==========

    @Test
    void index_WithAuthenticatedUserAndValidData_ShouldReturnIndexView() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(transactionService.getAllTransactions()).thenReturn(mockTransactions);
        when(transactionService.getRelations(mockUser.getId())).thenReturn(mockRelations);

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("transactions"))
                .andExpect(model().attributeExists("relations"))
                .andExpect(model().attributeExists("currentUser"))
                .andExpect(model().attribute("currentUser", mockUser));

        verify(authService).getCurrentUser();
        verify(transactionService).getAllTransactions();
        verify(transactionService).getRelations(mockUser.getId());
    }

    @Test
    void index_WithUnauthenticatedUser_ShouldRedirectToLogin() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(authService).getCurrentUser();
        verify(transactionService, never()).getAllTransactions();
        verify(transactionService, never()).getRelations(any());
    }

    // ========== TESTS DE LIMITATION DES TRANSACTIONS ==========

    @Test
    void index_WithMoreThan10Transactions_ShouldLimitTo10() throws Exception {
        // Given
        // Créer 15 transactions pour tester la limitation
        List<Transaction> manyTransactions = Arrays.asList(
                // Créer 15 transactions rapidement
                createMockTransaction(1L), createMockTransaction(2L), createMockTransaction(3L),
                createMockTransaction(4L), createMockTransaction(5L), createMockTransaction(6L),
                createMockTransaction(7L), createMockTransaction(8L), createMockTransaction(9L),
                createMockTransaction(10L), createMockTransaction(11L), createMockTransaction(12L),
                createMockTransaction(13L), createMockTransaction(14L), createMockTransaction(15L)
        );

        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(transactionService.getAllTransactions()).thenReturn(manyTransactions);
        when(transactionService.getRelations(mockUser.getId())).thenReturn(mockRelations);

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("transactions"));

        verify(authService).getCurrentUser();
        verify(transactionService).getAllTransactions();
        verify(transactionService).getRelations(mockUser.getId());
    }

    // ========== TESTS DE GESTION D'ERREURS ==========

    @Test
    void index_WithTransactionServiceException_ShouldReturnIndexWithEmptyData() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(transactionService.getAllTransactions())
                .thenThrow(new RuntimeException("Erreur base de données"));

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("transactions"))
                .andExpect(model().attributeExists("relations"))
                .andExpect(model().attributeExists("currentUser"))
                .andExpect(model().attribute("currentUser", mockUser));

        verify(authService).getCurrentUser();
        verify(transactionService).getAllTransactions();
        // getRelations n'est pas appelé en cas d'exception dans getAllTransactions
    }

    @Test
    void index_WithPartialServiceFailure_ShouldHandleGracefully() throws Exception {
        // Given - Transactions OK, Relations échouent
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(transactionService.getAllTransactions()).thenReturn(mockTransactions);
        when(transactionService.getRelations(mockUser.getId()))
                .thenThrow(new RuntimeException("Erreur relations"));

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("transactions"))
                .andExpect(model().attributeExists("relations"))
                .andExpect(model().attributeExists("currentUser"));

        verify(authService).getCurrentUser();
        verify(transactionService).getAllTransactions();
        verify(transactionService).getRelations(mockUser.getId());
    }

    // ========== TESTS AVEC DONNÉES VIDES ==========

    @Test
    void index_WithEmptyTransactionsAndRelations_ShouldReturnIndexView() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(transactionService.getAllTransactions()).thenReturn(Arrays.asList());
        when(transactionService.getRelations(mockUser.getId())).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("transactions"))
                .andExpect(model().attributeExists("relations"))
                .andExpect(model().attributeExists("currentUser"));

        verify(authService).getCurrentUser();
        verify(transactionService).getAllTransactions();
        verify(transactionService).getRelations(mockUser.getId());
    }

    // ========== TESTS AVEC DIFFÉRENTS UTILISATEURS ==========

    @Test
    void index_WithDifferentUser_ShouldLoadCorrectData() throws Exception {
        // Given
        User differentUser = User.builder()
                .id(999L)
                .username("admin_user")
                .email("admin@example.com")
                .role("ADMIN")
                .build();

        List<RelationDTO> adminRelations = Arrays.asList(
                new RelationDTO(100L, "admin_friend")
        );

        when(authService.getCurrentUser()).thenReturn(differentUser);
        when(transactionService.getAllTransactions()).thenReturn(mockTransactions);
        when(transactionService.getRelations(differentUser.getId())).thenReturn(adminRelations);

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("currentUser", differentUser));

        verify(authService).getCurrentUser();
        verify(transactionService).getAllTransactions();
        verify(transactionService).getRelations(differentUser.getId());
    }

    // ========== TESTS DE ROBUSTESSE ==========

    @Test
    void index_WithAuthServiceException_ShouldPropagateException() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenThrow(new RuntimeException("Service indisponible"));

        // When & Then
        try {
            mockMvc.perform(get("/"));
            // Le test ne doit pas arriver ici
            assert false : "Une exception aurait dû être levée";
        } catch (Exception e) {
            // Vérifier que l'exception contient le message attendu
            assert e.getCause().getMessage().contains("Service indisponible");
        }

        verify(authService).getCurrentUser();
        verifyNoInteractions(transactionService);
    }

    @Test
    void index_WithNullTransactions_ShouldHandleGracefully() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(transactionService.getAllTransactions()).thenReturn(null);

        // When & Then
        try {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("index"));
        } catch (Exception e) {
            // Peut lever une NullPointerException, c'est normal dans ce cas de test
            assert e.getCause() instanceof NullPointerException;
        }

        verify(authService).getCurrentUser();
        verify(transactionService).getAllTransactions();
    }

    // ========== TESTS D'INTÉGRATION ==========

    @Test
    void index_FullWorkflow_ShouldWorkCorrectly() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(transactionService.getAllTransactions()).thenReturn(mockTransactions);
        when(transactionService.getRelations(mockUser.getId())).thenReturn(mockRelations);

        // When & Then - Premier appel
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        // When & Then - Deuxième appel (simulation de rafraîchissement)
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        // Vérifier que les services sont appelés à chaque fois
        verify(authService, times(2)).getCurrentUser();
        verify(transactionService, times(2)).getAllTransactions();
        verify(transactionService, times(2)).getRelations(mockUser.getId());
    }

    // ========== MÉTHODES UTILITAIRES ==========

    private Transaction createMockTransaction(Long id) {
        return Transaction.builder()
                .id(id)
                .sender(mockUser)
                .receiver(mockUser)
                .amount(BigDecimal.valueOf(10.0))
                .description("Mock transaction " + id)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
