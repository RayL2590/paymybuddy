package com.openclassroom.paymybuddy.controller;

import com.openclassroom.paymybuddy.model.Transaction;
import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.service.AuthService;
import com.openclassroom.paymybuddy.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;
    @Mock
    private AuthService authService;
    @InjectMocks
    private TransactionController controller;

    private User sender;
    private User receiver;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sender = new User();
        sender.setId(1L);
        receiver = new User();
        receiver.setId(2L);
        transaction = new Transaction();
        transaction.setId(10L);
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getAllTransactions_returnsList() {
        when(transactionService.getAllTransactions()).thenReturn(List.of(transaction));
        List<Transaction> result = controller.getAllTransactions();
        assertEquals(1, result.size());
        assertEquals(transaction, result.get(0));
    }

    @Test
    void getTransactionById_found_returnsTransaction() {
        when(transactionService.getTransactionById(10L)).thenReturn(Optional.of(transaction));
        ResponseEntity<Transaction> response = controller.getTransactionById(10L);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(transaction, response.getBody());
    }

    @Test
    void getTransactionById_notFound_returns404() {
        when(transactionService.getTransactionById(99L)).thenReturn(Optional.empty());
        ResponseEntity<Transaction> response = controller.getTransactionById(99L);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void createTransaction_callsServiceAndReturnsTransaction() {
        when(transactionService.saveTransaction(transaction)).thenReturn(transaction);
        Transaction result = controller.createTransaction(transaction);
        assertEquals(transaction, result);
    }

    @Test
    void updateTransaction_notAuthenticated_returns401() {
        when(authService.getCurrentUser()).thenReturn(null);
        ResponseEntity<Transaction> response = controller.updateTransaction(10L, transaction);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void updateTransaction_notFound_returns404() {
        when(authService.getCurrentUser()).thenReturn(sender);
        when(transactionService.getTransactionById(10L)).thenReturn(Optional.empty());
        ResponseEntity<Transaction> response = controller.updateTransaction(10L, transaction);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void updateTransaction_notAuthorized_returns403() {
        User other = new User();
        other.setId(99L);
        when(authService.getCurrentUser()).thenReturn(other);
        when(transactionService.getTransactionById(10L)).thenReturn(Optional.of(transaction));
        ResponseEntity<Transaction> response = controller.updateTransaction(10L, transaction);
        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void updateTransaction_authorized_updatesAndReturnsTransaction() {
        when(authService.getCurrentUser()).thenReturn(sender);
        when(transactionService.getTransactionById(10L)).thenReturn(Optional.of(transaction));
        when(transactionService.saveTransaction(any(Transaction.class))).thenReturn(transaction);
        ResponseEntity<Transaction> response = controller.updateTransaction(10L, transaction);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(transaction, response.getBody());
    }

    @Test
    void deleteTransaction_notAuthenticated_returns401() {
        when(authService.getCurrentUser()).thenReturn(null);
        ResponseEntity<Void> response = controller.deleteTransaction(10L);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void deleteTransaction_notFound_returns404() {
        when(authService.getCurrentUser()).thenReturn(sender);
        when(transactionService.getTransactionById(10L)).thenReturn(Optional.empty());
        ResponseEntity<Void> response = controller.deleteTransaction(10L);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void deleteTransaction_notAuthorized_returns403() {
        User other = new User();
        other.setId(99L);
        when(authService.getCurrentUser()).thenReturn(other);
        when(transactionService.getTransactionById(10L)).thenReturn(Optional.of(transaction));
        ResponseEntity<Void> response = controller.deleteTransaction(10L);
        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void deleteTransaction_authorized_deletesAndReturns204() {
        when(authService.getCurrentUser()).thenReturn(sender);
        when(transactionService.getTransactionById(10L)).thenReturn(Optional.of(transaction));
        doNothing().when(transactionService).deleteTransaction(10L);
        ResponseEntity<Void> response = controller.deleteTransaction(10L);
        assertEquals(204, response.getStatusCode().value());
    }
}
