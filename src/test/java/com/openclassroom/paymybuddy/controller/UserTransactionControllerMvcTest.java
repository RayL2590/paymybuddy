package com.openclassroom.paymybuddy.controller;

import com.openclassroom.paymybuddy.dto.RelationDTO;
import com.openclassroom.paymybuddy.dto.TransferDTO;
import com.openclassroom.paymybuddy.model.Transaction;
import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.service.AuthService;
import com.openclassroom.paymybuddy.service.TransactionService;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour UserTransactionController avec MockMvc standalone configuré
 * Alternative si on veut tester l'intégration complète avec les aspects HTTP/MVC
 */
@ExtendWith(MockitoExtension.class)
class UserTransactionControllerMvcTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private UserTransactionController userTransactionController;

    private MockMvc mockMvc;
    private User mockUser;
    private User otherUser;
    private List<Transaction> mockTransactions;
    private List<RelationDTO> mockRelations;

    @BeforeEach
    void setUp() {
        // Configuration d'un ViewResolver pour éviter l'erreur "Circular view path"
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(userTransactionController)
                .setViewResolvers(viewResolver)
                .build();

        // Configuration des utilisateurs de test
        mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .balance(BigDecimal.valueOf(100.00))
                .role("USER")
                .build();

        otherUser = User.builder()
                .id(2L)
                .username("otheruser")
                .email("other@example.com")
                .password("encodedPassword")
                .balance(BigDecimal.valueOf(50.00))
                .role("USER")
                .build();

        // Configuration des transactions de test
        Transaction transaction1 = Transaction.builder()
                .id(1L)
                .sender(mockUser)
                .receiver(otherUser)
                .amount(BigDecimal.valueOf(25.00))
                .description("Test transaction 1")
                .createdAt(LocalDateTime.now())
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(2L)
                .sender(otherUser)
                .receiver(mockUser)
                .amount(BigDecimal.valueOf(15.00))
                .description("Test transaction 2")
                .createdAt(LocalDateTime.now())
                .build();

        mockTransactions = Arrays.asList(transaction1, transaction2);

        // Configuration des relations de test
        RelationDTO relation1 = new RelationDTO(2L, "otheruser");
        RelationDTO relation2 = new RelationDTO(3L, "friend");
        mockRelations = Arrays.asList(relation1, relation2);
    }

    // ========== TESTS POUR getUserTransactions() ==========

    @Test
    void getUserTransactions_WithValidUser_ShouldDisplayTransactionsPage() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(transactionService.getTransactionsByUserId(1L)).thenReturn(mockTransactions);
        when(transactionService.getRelations(1L)).thenReturn(mockRelations);

        // When & Then
        mockMvc.perform(get("/user-transactions/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-transactions"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("transactions"))
                .andExpect(model().attributeExists("relations"))
                .andExpect(model().attribute("user", mockUser))
                .andExpect(model().attribute("transactions", mockTransactions))
                .andExpect(model().attribute("relations", mockRelations));

        verify(transactionService).getTransactionsByUserId(1L);
        verify(transactionService).getRelations(1L);
    }

    @Test
    void getUserTransactions_WithNullUser_ShouldRedirectToLogin() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/user-transactions/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(transactionService, never()).getTransactionsByUserId(any());
    }

    @Test
    void getUserTransactions_WithDifferentUserId_ShouldRedirectToOwnTransactions() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(get("/user-transactions/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user-transactions/1"));

        verify(transactionService, never()).getTransactionsByUserId(any());
    }

    // ========== TESTS POUR showAddRelationForm() ==========

    @Test
    void showAddRelationForm_WithValidUser_ShouldDisplayAddRelationsPage() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(get("/user-transactions/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-relations"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", mockUser));
    }

    @Test
    void showAddRelationForm_WithNullUser_ShouldRedirectToLogin() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/user-transactions/add"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // ========== TESTS POUR processTransfer() ==========

    @Test
    void processTransfer_WithValidData_ShouldRedirectWithSuccessMessage() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(transactionService.createTransfer(any(TransferDTO.class))).thenReturn(new Transaction());

        // When & Then
        mockMvc.perform(post("/user-transactions/1/transfer")
                .param("receiverId", "2")
                .param("amount", "25.50")
                .param("description", "Test transfer"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user-transactions/1"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(transactionService).createTransfer(any(TransferDTO.class));
    }

    @Test
    void processTransfer_WithNullUser_ShouldRedirectToLogin() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/user-transactions/1/transfer")
                .param("receiverId", "2")
                .param("amount", "25.50")
                .param("description", "Test transfer"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(transactionService, never()).createTransfer(any());
    }

    @Test
    void processTransfer_WithInvalidAmount_ShouldRedirectWithError() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(post("/user-transactions/1/transfer")
                .param("receiverId", "2")
                .param("amount", "0.005") // Montant trop petit
                .param("description", "Test transfer"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user-transactions/1"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(transactionService, never()).createTransfer(any());
    }

    @Test
    void processTransfer_WithEmptyDescription_ShouldRedirectWithError() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(post("/user-transactions/1/transfer")
                .param("receiverId", "2")
                .param("amount", "25.50")
                .param("description", "")) // Description vide
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user-transactions/1"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(transactionService, never()).createTransfer(any());
    }

    @Test
    void processTransfer_WithServiceException_ShouldRedirectWithError() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(transactionService.createTransfer(any(TransferDTO.class)))
                .thenThrow(new RuntimeException("Solde insuffisant"));

        // When & Then
        mockMvc.perform(post("/user-transactions/1/transfer")
                .param("receiverId", "2")
                .param("amount", "25.50")
                .param("description", "Test transfer"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user-transactions/1"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    // ========== TESTS POUR addBalance() ==========

    @Test
    void addBalance_WithValidData_ShouldRedirectWithSuccessMessage() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(post("/user-transactions/1/balance/add")
                .param("amount", "50.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user-transactions/1"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService).adjustUserBalance(eq(1L), any(BigDecimal.class), eq("ADD"));
    }

    @Test
    void addBalance_WithNullUser_ShouldRedirectToLogin() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/user-transactions/1/balance/add")
                .param("amount", "50.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService, never()).adjustUserBalance(any(), any(), any());
    }

    @Test
    void addBalance_WithDifferentUserId_ShouldRedirectToOwnTransactions() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(post("/user-transactions/999/balance/add")
                .param("amount", "50.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user-transactions/1"));

        verify(userService, never()).adjustUserBalance(any(), any(), any());
    }

    @Test
    void addBalance_WithServiceException_ShouldRedirectWithError() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        doThrow(new RuntimeException("Erreur de service"))
                .when(userService).adjustUserBalance(eq(1L), any(BigDecimal.class), eq("ADD"));

        // When & Then
        mockMvc.perform(post("/user-transactions/1/balance/add")
                .param("amount", "50.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user-transactions/1"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    // ========== TESTS POUR subtractBalance() ==========

    @Test
    void subtractBalance_WithValidData_ShouldRedirectWithSuccessMessage() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(post("/user-transactions/1/balance/subtract")
                .param("amount", "30.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user-transactions/1"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService).adjustUserBalance(eq(1L), any(BigDecimal.class), eq("SUBTRACT"));
    }

    @Test
    void subtractBalance_WithNullUser_ShouldRedirectToLogin() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/user-transactions/1/balance/subtract")
                .param("amount", "30.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService, never()).adjustUserBalance(any(), any(), any());
    }

    @Test
    void subtractBalance_WithServiceException_ShouldRedirectWithError() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        doThrow(new RuntimeException("Solde insuffisant"))
                .when(userService).adjustUserBalance(eq(1L), any(BigDecimal.class), eq("SUBTRACT"));

        // When & Then
        mockMvc.perform(post("/user-transactions/1/balance/subtract")
                .param("amount", "30.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user-transactions/1"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    // ========== TESTS DE VALIDATION DES PARAMÈTRES ==========

    @Test
    void processTransfer_WithMissingReceiverId_ShouldRedirectWithError() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // When & Then
        mockMvc.perform(post("/user-transactions/1/transfer")
                .param("amount", "25.50")
                .param("description", "Test transfer"))
                // receiverId manquant
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user-transactions/1"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(transactionService, never()).createTransfer(any());
    }

    @Test
    void processTransfer_WithExactMinimumAmount_ShouldSucceed() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(transactionService.createTransfer(any(TransferDTO.class))).thenReturn(new Transaction());

        // When & Then
        mockMvc.perform(post("/user-transactions/1/transfer")
                .param("receiverId", "2")
                .param("amount", "0.01") // Montant minimum exact
                .param("description", "Test transfer"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user-transactions/1"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(transactionService).createTransfer(any(TransferDTO.class));
    }
}
