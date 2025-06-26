package com.openclassroom.paymybuddy.controller;

import com.openclassroom.paymybuddy.dto.TransferDTO;
import com.openclassroom.paymybuddy.model.Transaction;
import com.openclassroom.paymybuddy.service.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<Transaction> getAllTransactions() {
        logger.info("GET /api/transactions - Récupération de toutes les transactions");
        return transactionService.getAllTransactions();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        logger.info("GET /api/transactions/{} - Récupération de la transaction avec ID {}", id, id);
        return transactionService.getTransactionById(id)
                .map(transaction -> {
                    logger.info("Transaction trouvée : {}", transaction);
                    return ResponseEntity.ok(transaction);
                })
                .orElseGet(() -> {
                    logger.warn("Transaction avec ID {} non trouvée", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PostMapping
    public Transaction createTransaction(@RequestBody Transaction transaction) {
        logger.info("POST /api/transactions - Création d'une nouvelle transaction : {}", transaction);
        return transactionService.saveTransaction(transaction);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @RequestBody Transaction transaction) {
        logger.info("PUT /api/transactions/{} - Mise à jour de la transaction avec ID {}", id, id);
        return transactionService.getTransactionById(id)
                .map(existingTransaction -> {
                    transaction.setId(existingTransaction.getId());
                    logger.info("Transaction mise à jour : {}", transaction);
                    return ResponseEntity.ok(transactionService.saveTransaction(transaction));
                })
                .orElseGet(() -> {
                    logger.warn("Transaction avec ID {} non trouvée pour mise à jour", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transferMoney(
            @RequestParam Long senderId,
            @Valid @RequestBody TransferDTO transferDTO) {
        try {
            Transaction transaction = transactionService.createTransfer(senderId, transferDTO);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        logger.info("DELETE /api/transactions/{} - Suppression de la transaction avec ID {}", id, id);
        if (transactionService.getTransactionById(id).isPresent()) {
            transactionService.deleteTransaction(id);
            logger.info("Transaction avec ID {} supprimée", id);
            return ResponseEntity.noContent().build();
        }
        logger.warn("Transaction avec ID {} non trouvée pour suppression", id);
        return ResponseEntity.notFound().build();
    }
}