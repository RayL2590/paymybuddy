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
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour UserTransactionController - logique métier uniquement
 * Cette approche teste la logique sans les aspects HTTP/MVC
 */
@ExtendWith(MockitoExtension.class)
class UserTransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private UserTransactionController userTransactionController;

    private User mockUser;
    private User otherUser;
    private List<Transaction> mockTransactions;
    private List<RelationDTO> mockRelations;
    private TransferDTO validTransferDTO;

    @BeforeEach
    void setUp() {
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

        // Configuration du transfert de test
        validTransferDTO = new TransferDTO();
        validTransferDTO.setReceiverId(2L);
        validTransferDTO.setAmount(BigDecimal.valueOf(20.00));
        validTransferDTO.setDescription("Test transfer");
    }

    // ========== TESTS POUR getUserTransactions() ==========

    @Test
    void getUserTransactions_WithValidUser_ShouldReturnUserTransactionsView() {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(transactionService.getTransactionsByUserId(1L)).thenReturn(mockTransactions);
        when(transactionService.getRelations(1L)).thenReturn(mockRelations);

        // When
        String viewName = userTransactionController.getUserTransactions(1L, model);

        // Then
        assertEquals("user-transactions", viewName);
        verify(model).addAttribute("user", mockUser);
        verify(model).addAttribute("transactions", mockTransactions);
        verify(model).addAttribute("relations", mockRelations);
        verify(transactionService).getTransactionsByUserId(1L);
        verify(transactionService).getRelations(1L);
    }

    @Test
    void getUserTransactions_WithNullUser_ShouldRedirectToLogin() {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When
        String viewName = userTransactionController.getUserTransactions(1L, model);

        // Then
        assertEquals("redirect:/login", viewName);
        verify(transactionService, never()).getTransactionsByUserId(any());
        verify(model, never()).addAttribute(any(), any());
    }

    @Test
    void getUserTransactions_WithDifferentUserId_ShouldRedirectToOwnTransactions() {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // When
        String viewName = userTransactionController.getUserTransactions(999L, model);

        // Then
        assertEquals("redirect:/user-transactions/1", viewName);
        verify(transactionService, never()).getTransactionsByUserId(any());
        verify(model, never()).addAttribute(any(), any());
    }

    // ========== TESTS POUR showAddRelationForm() ==========

    @Test
    void showAddRelationForm_WithValidUser_ShouldReturnAddRelationsView() {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // When
        String viewName = userTransactionController.showAddRelationForm(model);

        // Then
        assertEquals("add-relations", viewName);
        verify(model).addAttribute("user", mockUser);
    }

    @Test
    void showAddRelationForm_WithNullUser_ShouldRedirectToLogin() {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When
        String viewName = userTransactionController.showAddRelationForm(model);

        // Then
        assertEquals("redirect:/login", viewName);
        verify(model, never()).addAttribute(any(), any());
    }

    // ========== TESTS POUR processTransfer() ==========

    @Test
    void processTransfer_WithValidData_ShouldRedirectWithSuccessMessage() {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(transactionService.createTransfer(any(TransferDTO.class))).thenReturn(new Transaction());

        // When
        String viewName = userTransactionController.processTransfer(1L, validTransferDTO, redirectAttributes);

        // Then
        assertEquals("redirect:/user-transactions/1", viewName);
        assertEquals(1L, validTransferDTO.getSenderId()); // Vérifie que le senderId a été défini
        verify(transactionService).createTransfer(validTransferDTO);
        verify(redirectAttributes).addFlashAttribute("successMessage", "Transfert effectué avec succès");
    }

    @Test
    void processTransfer_WithNullUser_ShouldRedirectToLogin() {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When
        String viewName = userTransactionController.processTransfer(1L, validTransferDTO, redirectAttributes);

        // Then
        assertEquals("redirect:/login", viewName);
        verify(transactionService, never()).createTransfer(any());
    }

    @Test
    void processTransfer_WithDifferentUserId_ShouldRedirectToOwnTransactions() {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // When
        String viewName = userTransactionController.processTransfer(999L, validTransferDTO, redirectAttributes);

        // Then
        assertEquals("redirect:/user-transactions/1", viewName);
        verify(transactionService, never()).createTransfer(any());
    }

    @Test
    void processTransfer_WithNullReceiverId_ShouldRedirectWithError() {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        validTransferDTO.setReceiverId(null);

        // When
        String viewName = userTransactionController.processTransfer(1L, validTransferDTO, redirectAttributes);

        // Then
        assertEquals("redirect:/user-transactions/1", viewName);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Veuillez sélectionner un destinataire");
        verify(transactionService, never()).createTransfer(any());
    }

    @Test
    void processTransfer_WithNullAmount_ShouldRedirectWithError() {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        validTransferDTO.setAmount(null);

        // When
        String viewName = userTransactionController.processTransfer(1L, validTransferDTO, redirectAttributes);

        // Then
        assertEquals("redirect:/user-transactions/1", viewName);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Le montant doit être supérieur à 0.01");
        verify(transactionService, never()).createTransfer(any());
    }

    @Test
    void processTransfer_WithTooSmallAmount_ShouldRedirectWithError() {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        validTransferDTO.setAmount(BigDecimal.valueOf(0.005)); // Moins de 0.01

        // When
        String viewName = userTransactionController.processTransfer(1L, validTransferDTO, redirectAttributes);

        // Then
        assertEquals("redirect:/user-transactions/1", viewName);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Le montant doit être supérieur à 0.01");
        verify(transactionService, never()).createTransfer(any());
    }

    @Test
    void processTransfer_WithEmptyDescription_ShouldRedirectWithError() {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        validTransferDTO.setDescription("");

        // When
        String viewName = userTransactionController.processTransfer(1L, validTransferDTO, redirectAttributes);

        // Then
        assertEquals("redirect:/user-transactions/1", viewName);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Veuillez saisir une description");
        verify(transactionService, never()).createTransfer(any());
    }

    @Test
    void processTransfer_WithNullDescription_ShouldRedirectWithError() {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        validTransferDTO.setDescription(null);

        // When
        String viewName = userTransactionController.processTransfer(1L, validTransferDTO, redirectAttributes);

        // Then
        assertEquals("redirect:/user-transactions/1", viewName);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Veuillez saisir une description");
        verify(transactionService, never()).createTransfer(any());
    }

    @Test
    void processTransfer_WithServiceException_ShouldRedirectWithError() {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(transactionService.createTransfer(any(TransferDTO.class)))
                .thenThrow(new RuntimeException("Solde insuffisant"));

        // When
        String viewName = userTransactionController.processTransfer(1L, validTransferDTO, redirectAttributes);

        // Then
        assertEquals("redirect:/user-transactions/1", viewName);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Erreur lors du transfert : Solde insuffisant");
    }

    // ========== TESTS POUR addBalance() ==========

    @Test
    void addBalance_WithValidData_ShouldRedirectWithSuccessMessage() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(50.00);
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // When
        String viewName = userTransactionController.addBalance(1L, amount, redirectAttributes);

        // Then
        assertEquals("redirect:/user-transactions/1", viewName);
        verify(userService).adjustUserBalance(1L, amount, "ADD");
        verify(redirectAttributes).addFlashAttribute("successMessage", "50,00€ ajoutés à votre balance avec succès");
    }

    @Test
    void addBalance_WithNullUser_ShouldRedirectToLogin() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(50.00);
        when(authService.getCurrentUser()).thenReturn(null);

        // When
        String viewName = userTransactionController.addBalance(1L, amount, redirectAttributes);

        // Then
        assertEquals("redirect:/login", viewName);
        verify(userService, never()).adjustUserBalance(any(), any(), any());
    }

    @Test
    void addBalance_WithDifferentUserId_ShouldRedirectToOwnTransactions() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(50.00);
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // When
        String viewName = userTransactionController.addBalance(999L, amount, redirectAttributes);

        // Then
        assertEquals("redirect:/user-transactions/1", viewName);
        verify(userService, never()).adjustUserBalance(any(), any(), any());
    }

    @Test
    void addBalance_WithServiceException_ShouldRedirectWithError() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(50.00);
        when(authService.getCurrentUser()).thenReturn(mockUser);
        doThrow(new RuntimeException("Erreur de service"))
                .when(userService).adjustUserBalance(1L, amount, "ADD");

        // When
        String viewName = userTransactionController.addBalance(1L, amount, redirectAttributes);

        // Then
        assertEquals("redirect:/user-transactions/1", viewName);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Erreur : Erreur de service");
    }

    // ========== TESTS POUR subtractBalance() ==========

    @Test
    void subtractBalance_WithValidData_ShouldRedirectWithSuccessMessage() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(30.00);
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // When
        String viewName = userTransactionController.subtractBalance(1L, amount, redirectAttributes);

        // Then
        assertEquals("redirect:/user-transactions/1", viewName);
        verify(userService).adjustUserBalance(1L, amount, "SUBTRACT");
        verify(redirectAttributes).addFlashAttribute("successMessage", "30,00€ retirés de votre balance avec succès");
    }

    @Test
    void subtractBalance_WithNullUser_ShouldRedirectToLogin() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(30.00);
        when(authService.getCurrentUser()).thenReturn(null);

        // When
        String viewName = userTransactionController.subtractBalance(1L, amount, redirectAttributes);

        // Then
        assertEquals("redirect:/login", viewName);
        verify(userService, never()).adjustUserBalance(any(), any(), any());
    }

    @Test
    void subtractBalance_WithDifferentUserId_ShouldRedirectToOwnTransactions() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(30.00);
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // When
        String viewName = userTransactionController.subtractBalance(999L, amount, redirectAttributes);

        // Then
        assertEquals("redirect:/user-transactions/1", viewName);
        verify(userService, never()).adjustUserBalance(any(), any(), any());
    }

    @Test
    void subtractBalance_WithServiceException_ShouldRedirectWithError() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(30.00);
        when(authService.getCurrentUser()).thenReturn(mockUser);
        doThrow(new RuntimeException("Solde insuffisant"))
                .when(userService).adjustUserBalance(1L, amount, "SUBTRACT");

        // When
        String viewName = userTransactionController.subtractBalance(1L, amount, redirectAttributes);

        // Then
        assertEquals("redirect:/user-transactions/1", viewName);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Erreur : Solde insuffisant");
    }

    // ========== TESTS DE VALIDATION DES PARAMÈTRES ==========

    @Test
    void processTransfer_WithWhitespaceDescription_ShouldRedirectWithError() {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        validTransferDTO.setDescription("   "); // Espaces uniquement

        // When
        String viewName = userTransactionController.processTransfer(1L, validTransferDTO, redirectAttributes);

        // Then
        assertEquals("redirect:/user-transactions/1", viewName);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Veuillez saisir une description");
        verify(transactionService, never()).createTransfer(any());
    }

    @Test
    void processTransfer_WithExactMinimumAmount_ShouldSucceed() {
        // Given
        when(authService.getCurrentUser()).thenReturn(mockUser);
        validTransferDTO.setAmount(BigDecimal.valueOf(0.01)); // Montant minimum exact
        when(transactionService.createTransfer(any(TransferDTO.class))).thenReturn(new Transaction());

        // When
        String viewName = userTransactionController.processTransfer(1L, validTransferDTO, redirectAttributes);

        // Then
        assertEquals("redirect:/user-transactions/1", viewName);
        verify(transactionService).createTransfer(validTransferDTO);
        verify(redirectAttributes).addFlashAttribute("successMessage", "Transfert effectué avec succès");
    }
}
