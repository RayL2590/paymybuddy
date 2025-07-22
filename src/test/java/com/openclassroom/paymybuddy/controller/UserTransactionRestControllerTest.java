package com.openclassroom.paymybuddy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

/**
 * Tests unitaires pour UserTransactionRestController.
 * Teste tous les endpoints REST pour la gestion des transactions des utilisateurs.
 */
@ExtendWith(MockitoExtension.class)
class UserTransactionRestControllerTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private UserTransactionRestController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User mockCurrentUser;
    private User mockReceiver;
    private Transaction mockTransaction;
    private RelationDTO mockRelation;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();

        // Créer un utilisateur connecté mock
        mockCurrentUser = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .password("hashedPassword")
                .balance(BigDecimal.valueOf(100.0))
                .role("USER")
                .build();

        // Créer un utilisateur destinataire mock
        mockReceiver = User.builder()
                .id(2L)
                .username("jane_doe")
                .email("jane@example.com")
                .password("hashedPassword")
                .balance(BigDecimal.valueOf(50.0))
                .role("USER")
                .build();

        // Créer une transaction mock
        mockTransaction = Transaction.builder()
                .id(1L)
                .sender(mockCurrentUser)
                .receiver(mockReceiver)
                .amount(BigDecimal.valueOf(25.0))
                .description("Test transfer")
                .createdAt(LocalDateTime.now())
                .build();

        // Créer une relation mock
        mockRelation = new RelationDTO(2L, "jane_doe");
    }

    // ========== TESTS POUR getUserTransactions() ==========

    @Test
    void getUserTransactions_WithValidUserIdAndAuthenticatedUser_ShouldReturnTransactions() throws Exception {
        // Given
        Long userId = 1L;
        List<Transaction> mockTransactions = Arrays.asList(mockTransaction);

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        when(transactionService.getTransactionsByUserId(userId)).thenReturn(mockTransactions);

        // When & Then
        mockMvc.perform(get("/api/users/{userId}/transactions", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(authService).getCurrentUser();
        verify(transactionService).getTransactionsByUserId(userId);
    }

    @Test
    void getUserTransactions_WithUnauthenticatedUser_ShouldReturn401() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/users/{userId}/transactions", 1L))
                .andExpect(status().isUnauthorized());

        verify(authService).getCurrentUser();
        verify(transactionService, never()).getTransactionsByUserId(any());
    }

    @Test
    void getUserTransactions_WithDifferentUserId_ShouldReturn403() throws Exception {
        // Given
        Long differentUserId = 999L;
        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);

        // When & Then
        mockMvc.perform(get("/api/users/{userId}/transactions", differentUserId))
                .andExpect(status().isForbidden());

        verify(authService).getCurrentUser();
        verify(transactionService, never()).getTransactionsByUserId(any());
    }

    // ========== TESTS POUR getUserRelations() ==========

    @Test
    void getUserRelations_WithValidUserIdAndAuthenticatedUser_ShouldReturnRelations() throws Exception {
        // Given
        Long userId = 1L;
        List<RelationDTO> mockRelations = Arrays.asList(mockRelation);

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        when(transactionService.getRelations(userId)).thenReturn(mockRelations);

        // When & Then
        mockMvc.perform(get("/api/users/{userId}/transactions/relations", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("jane_doe"));

        verify(authService).getCurrentUser();
        verify(transactionService).getRelations(userId);
    }

    @Test
    void getUserRelations_WithUnauthenticatedUser_ShouldReturn401() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/users/{userId}/transactions/relations", 1L))
                .andExpect(status().isUnauthorized());

        verify(authService).getCurrentUser();
        verify(transactionService, never()).getRelations(any());
    }

    @Test
    void getUserRelations_WithDifferentUserId_ShouldReturn403() throws Exception {
        // Given
        Long differentUserId = 999L;
        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);

        // When & Then
        mockMvc.perform(get("/api/users/{userId}/transactions/relations", differentUserId))
                .andExpect(status().isForbidden());

        verify(authService).getCurrentUser();
        verify(transactionService, never()).getRelations(any());
    }

    // ========== TESTS POUR processTransfer() ==========

    @Test
    void processTransfer_WithValidDataAndAuthenticatedUser_ShouldReturnTransaction() throws Exception {
        // Given
        Long userId = 1L;
        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setSenderId(1L);  // Set sender ID
        transferDTO.setReceiverId(2L);
        transferDTO.setAmount(BigDecimal.valueOf(25.0));
        transferDTO.setDescription("Test transfer");

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        when(transactionService.createTransfer(any())).thenReturn(mockTransaction);

        // When & Then
        mockMvc.perform(post("/api/users/{userId}/transactions/transfer", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(authService).getCurrentUser();
        verify(transactionService).createTransfer(any());
    }

    @Test
    void processTransfer_WithUnauthenticatedUser_ShouldReturn401() throws Exception {
        // Given
        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setSenderId(1L);  // Set sender ID
        transferDTO.setReceiverId(2L);
        transferDTO.setAmount(BigDecimal.valueOf(25.0));
        transferDTO.setDescription("Test transfer");

        when(authService.getCurrentUser()).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/users/{userId}/transactions/transfer", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDTO)))
                .andExpect(status().isUnauthorized());

        verify(authService).getCurrentUser();
        verify(transactionService, never()).createTransfer(any());
    }

    @Test
    void processTransfer_WithDifferentUserId_ShouldReturn403() throws Exception {
        // Given
        Long differentUserId = 999L;
        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setSenderId(1L);  // Set sender ID
        transferDTO.setReceiverId(2L);
        transferDTO.setAmount(BigDecimal.valueOf(25.0));
        transferDTO.setDescription("Test transfer");

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);

        // When & Then
        mockMvc.perform(post("/api/users/{userId}/transactions/transfer", differentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDTO)))
                .andExpect(status().isForbidden());

        verify(authService).getCurrentUser();
        verify(transactionService, never()).createTransfer(any());
    }

    @Test
    void processTransfer_WithInvalidData_ShouldReturn400() throws Exception {
        // Given
        Long userId = 1L;
        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setSenderId(1L);  // Set sender ID
        transferDTO.setReceiverId(2L);
        transferDTO.setAmount(BigDecimal.valueOf(25.0));
        transferDTO.setDescription("Test transfer");

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        when(transactionService.createTransfer(any()))
                .thenThrow(new IllegalArgumentException("Solde insuffisant"));

        // When & Then
        mockMvc.perform(post("/api/users/{userId}/transactions/transfer", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDTO)))
                .andExpect(status().isBadRequest());

        verify(authService).getCurrentUser();
        verify(transactionService).createTransfer(any());
    }

    @Test
    void processTransfer_WithEmptyBody_ShouldReturn400() throws Exception {
        // Given
        Long userId = 1L;

        // When & Then - Spring should return 400 for invalid JSON before reaching the controller
        mockMvc.perform(post("/api/users/{userId}/transactions/transfer", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        // Note: Spring handles validation before reaching the controller, 
        // so authService and transactionService are not called
        verifyNoInteractions(transactionService);
    }

    // ========== TESTS POUR addBalance() ==========

    @Test
    void addBalance_WithValidDataAndAuthenticatedUser_ShouldReturnSuccess() throws Exception {
        // Given
        Long userId = 1L;
        BigDecimal amount = BigDecimal.valueOf(50.0);

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        doNothing().when(userService).adjustUserBalance(userId, amount, "ADD");

        // When & Then
        mockMvc.perform(post("/api/users/{userId}/transactions/balance/add", userId)
                        .param("amount", amount.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("50")))
                .andExpect(content().string(containsString("ajoutés à votre balance avec succès")));

        verify(authService).getCurrentUser();
        verify(userService).adjustUserBalance(userId, amount, "ADD");
    }

    @Test
    void addBalance_WithUnauthenticatedUser_ShouldReturn401() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/users/{userId}/transactions/balance/add", 1L)
                        .param("amount", "50.0"))
                .andExpect(status().isUnauthorized());

        verify(authService).getCurrentUser();
        verify(userService, never()).adjustUserBalance(any(), any(), any());
    }

    @Test
    void addBalance_WithDifferentUserId_ShouldReturn403() throws Exception {
        // Given
        Long differentUserId = 999L;
        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);

        // When & Then
        mockMvc.perform(post("/api/users/{userId}/transactions/balance/add", differentUserId)
                        .param("amount", "50.0"))
                .andExpect(status().isForbidden());

        verify(authService).getCurrentUser();
        verify(userService, never()).adjustUserBalance(any(), any(), any());
    }

    @Test
    void addBalance_WithServiceException_ShouldReturn400() throws Exception {
        // Given
        Long userId = 1L;
        BigDecimal amount = BigDecimal.valueOf(50.0);

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        doThrow(new IllegalArgumentException("Montant invalide"))
                .when(userService).adjustUserBalance(userId, amount, "ADD");

        // When & Then
        mockMvc.perform(post("/api/users/{userId}/transactions/balance/add", userId)
                        .param("amount", amount.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Erreur : Montant invalide"));

        verify(authService).getCurrentUser();
        verify(userService).adjustUserBalance(userId, amount, "ADD");
    }

    // ========== TESTS POUR subtractBalance() ==========

    @Test
    void subtractBalance_WithValidDataAndAuthenticatedUser_ShouldReturnSuccess() throws Exception {
        // Given
        Long userId = 1L;
        BigDecimal amount = BigDecimal.valueOf(30.0);

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        doNothing().when(userService).adjustUserBalance(userId, amount, "SUBTRACT");

        // When & Then
        mockMvc.perform(post("/api/users/{userId}/transactions/balance/subtract", userId)
                        .param("amount", amount.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("30")))
                .andExpect(content().string(containsString("retirés de votre balance avec succès")));

        verify(authService).getCurrentUser();
        verify(userService).adjustUserBalance(userId, amount, "SUBTRACT");
    }

    @Test
    void subtractBalance_WithUnauthenticatedUser_ShouldReturn401() throws Exception {
        // Given
        when(authService.getCurrentUser()).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/users/{userId}/transactions/balance/subtract", 1L)
                        .param("amount", "30.0"))
                .andExpect(status().isUnauthorized());

        verify(authService).getCurrentUser();
        verify(userService, never()).adjustUserBalance(any(), any(), any());
    }

    @Test
    void subtractBalance_WithDifferentUserId_ShouldReturn403() throws Exception {
        // Given
        Long differentUserId = 999L;
        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);

        // When & Then
        mockMvc.perform(post("/api/users/{userId}/transactions/balance/subtract", differentUserId)
                        .param("amount", "30.0"))
                .andExpect(status().isForbidden());

        verify(authService).getCurrentUser();
        verify(userService, never()).adjustUserBalance(any(), any(), any());
    }

    @Test
    void subtractBalance_WithInsufficientFunds_ShouldReturn400() throws Exception {
        // Given
        Long userId = 1L;
        BigDecimal amount = BigDecimal.valueOf(30.0);

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        doThrow(new IllegalArgumentException("Solde insuffisant"))
                .when(userService).adjustUserBalance(userId, amount, "SUBTRACT");

        // When & Then
        mockMvc.perform(post("/api/users/{userId}/transactions/balance/subtract", userId)
                        .param("amount", amount.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Erreur : Solde insuffisant"));

        verify(authService).getCurrentUser();
        verify(userService).adjustUserBalance(userId, amount, "SUBTRACT");
    }

    // ========== TESTS DE VALIDATION DES PARAMÈTRES ==========

    @Test
    void addBalance_WithNegativeAmount_ShouldReturn400() throws Exception {
        // Given
        Long userId = 1L;
        BigDecimal negativeAmount = BigDecimal.valueOf(-10.0);

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        doThrow(new IllegalArgumentException("Le montant doit être positif"))
                .when(userService).adjustUserBalance(userId, negativeAmount, "ADD");

        // When & Then
        mockMvc.perform(post("/api/users/{userId}/transactions/balance/add", userId)
                        .param("amount", negativeAmount.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Le montant doit être positif")));

        verify(authService).getCurrentUser();
        verify(userService).adjustUserBalance(userId, negativeAmount, "ADD");
    }

    @Test
    void subtractBalance_WithNegativeAmount_ShouldReturn400() throws Exception {
        // Given
        Long userId = 1L;
        BigDecimal negativeAmount = BigDecimal.valueOf(-10.0);

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        doThrow(new IllegalArgumentException("Le montant doit être positif"))
                .when(userService).adjustUserBalance(userId, negativeAmount, "SUBTRACT");

        // When & Then
        mockMvc.perform(post("/api/users/{userId}/transactions/balance/subtract", userId)
                        .param("amount", negativeAmount.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Le montant doit être positif")));

        verify(authService).getCurrentUser();
        verify(userService).adjustUserBalance(userId, negativeAmount, "SUBTRACT");
    }

    // ========== TESTS D'INTÉGRATION ==========

    @Test
    void userTransactionWorkflow_GetTransactionsThenTransfer_ShouldWork() throws Exception {
        // Given
        Long userId = 1L;
        TransferDTO transferDTO = new TransferDTO();
        transferDTO.setSenderId(1L);  // Set sender ID
        transferDTO.setReceiverId(2L);
        transferDTO.setAmount(BigDecimal.valueOf(25.0));
        transferDTO.setDescription("Test workflow transfer");

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        when(transactionService.getTransactionsByUserId(userId)).thenReturn(Arrays.asList());
        when(transactionService.createTransfer(any())).thenReturn(mockTransaction);

        // When & Then - Get initial transactions
        mockMvc.perform(get("/api/users/{userId}/transactions", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // When & Then - Make a transfer
        mockMvc.perform(post("/api/users/{userId}/transactions/transfer", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDTO)))
                .andExpect(status().isOk());

        verify(authService, times(2)).getCurrentUser();
        verify(transactionService).getTransactionsByUserId(userId);
        verify(transactionService).createTransfer(any());
    }

    @Test
    void balanceManagement_AddThenSubtract_ShouldWork() throws Exception {
        // Given
        Long userId = 1L;
        BigDecimal addAmount = BigDecimal.valueOf(100.0);
        BigDecimal subtractAmount = BigDecimal.valueOf(50.0);

        when(authService.getCurrentUser()).thenReturn(mockCurrentUser);
        doNothing().when(userService).adjustUserBalance(userId, addAmount, "ADD");
        doNothing().when(userService).adjustUserBalance(userId, subtractAmount, "SUBTRACT");

        // When & Then - Add balance
        mockMvc.perform(post("/api/users/{userId}/transactions/balance/add", userId)
                        .param("amount", addAmount.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("100")))
                .andExpect(content().string(containsString("ajoutés à votre balance avec succès")));

        // When & Then - Subtract balance
        mockMvc.perform(post("/api/users/{userId}/transactions/balance/subtract", userId)
                        .param("amount", subtractAmount.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("50")))
                .andExpect(content().string(containsString("retirés de votre balance avec succès")));

        verify(authService, times(2)).getCurrentUser();
        verify(userService).adjustUserBalance(userId, addAmount, "ADD");
        verify(userService).adjustUserBalance(userId, subtractAmount, "SUBTRACT");
    }
}
