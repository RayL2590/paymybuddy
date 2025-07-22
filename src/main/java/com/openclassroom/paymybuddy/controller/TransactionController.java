package com.openclassroom.paymybuddy.controller;

import com.openclassroom.paymybuddy.model.Transaction;
import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.service.AuthService;
import com.openclassroom.paymybuddy.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour gérer les opérations liées aux transactions.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    /**
     * Logger pour enregistrer les événements liés aux transactions.
     */
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    /**
     * Service pour gérer les transactions.
     */
    private final TransactionService transactionService;

    /**
     * Service pour gérer l'authentification.
     */
    private final AuthService authService;

    /**
     * Constructeur pour injecter les services nécessaires.
     *
     * @param transactionService Service pour gérer les transactions.
     * @param authService Service pour gérer l'authentification.
     */
    public TransactionController(TransactionService transactionService, AuthService authService) {
        this.transactionService = transactionService;
        this.authService = authService;
    }

    /**
     * Récupère toutes les transactions.
     *
     * @return Une liste de toutes les transactions.
     */
    @GetMapping
    public List<Transaction> getAllTransactions() {
        logger.info("GET /api/transactions - Récupération de toutes les transactions");
        return transactionService.getAllTransactions();
    }

    /**
     * Récupère une transaction par son ID.
     *
     * @param id L'ID de la transaction à récupérer.
     * @return La transaction correspondante ou une réponse 404 si elle n'est pas trouvée.
     */
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

    /**
     * Crée une nouvelle transaction.
     *
     * @param transaction Les détails de la transaction à créer.
     * @return La transaction créée.
     */
    @PostMapping
    public Transaction createTransaction(@RequestBody Transaction transaction) {
        logger.info("POST /api/transactions - Création d'une nouvelle transaction : {}", transaction);
        return transactionService.saveTransaction(transaction);
    }

    /**
     * Met à jour une transaction existante.
     *
     * @param id L'ID de la transaction à mettre à jour.
     * @param transaction Les nouveaux détails de la transaction.
     * @return La transaction mise à jour ou une réponse 404 si elle n'est pas trouvée.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @RequestBody Transaction transaction) {
        logger.info("PUT /api/transactions/{} - Mise à jour de la transaction avec ID {}", id, id);
        
        // Vérifier que l'utilisateur est connecté
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            logger.warn("Tentative de modification de transaction par un utilisateur non connecté");
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        
        // Récupérer la transaction existante
        var existingTransactionOpt = transactionService.getTransactionById(id);
        if (existingTransactionOpt.isEmpty()) {
            logger.warn("Transaction avec ID {} non trouvée pour mise à jour", id);
            return ResponseEntity.notFound().build();
        }
        
        Transaction existingTransaction = existingTransactionOpt.get();
        
        // Vérifier que l'utilisateur connecté est autorisé à modifier cette transaction
        // (doit être soit l'expéditeur soit le destinataire)
        boolean isAuthorized = existingTransaction.getSender().getId().equals(currentUser.getId()) ||
                             existingTransaction.getReceiver().getId().equals(currentUser.getId());
        
        if (!isAuthorized) {
            logger.warn("L'utilisateur {} tente de modifier la transaction {} qui ne lui appartient pas", 
                       currentUser.getId(), id);
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        // Préserver les informations critiques de la transaction originale
        transaction.setId(existingTransaction.getId());
        transaction.setSender(existingTransaction.getSender());
        transaction.setReceiver(existingTransaction.getReceiver());
        transaction.setCreatedAt(existingTransaction.getCreatedAt());
        
        logger.info("Transaction mise à jour par l'utilisateur autorisé {}: {}", currentUser.getId(), transaction);
        return ResponseEntity.ok(transactionService.saveTransaction(transaction));
    }

    /**
     * Supprime une transaction par son ID.
     *
     * @param id L'ID de la transaction à supprimer.
     * @return Une réponse 204 si la suppression est réussie ou une réponse 404 si elle n'est pas trouvée.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        logger.info("DELETE /api/transactions/{} - Suppression de la transaction avec ID {}", id, id);
        
        // Vérifier que l'utilisateur est connecté
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            logger.warn("Tentative de suppression de transaction par un utilisateur non connecté");
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        
        var existingTransactionOpt = transactionService.getTransactionById(id);
        if (existingTransactionOpt.isEmpty()) {
            logger.warn("Transaction avec ID {} non trouvée pour suppression", id);
            return ResponseEntity.notFound().build();
        }
        
        Transaction existingTransaction = existingTransactionOpt.get();
        
        // Vérifier que l'utilisateur connecté est autorisé à supprimer cette transaction
        // (doit être soit l'expéditeur soit le destinataire)
        boolean isAuthorized = existingTransaction.getSender().getId().equals(currentUser.getId()) ||
                             existingTransaction.getReceiver().getId().equals(currentUser.getId());
        
        if (!isAuthorized) {
            logger.warn("L'utilisateur {} tente de supprimer la transaction {} qui ne lui appartient pas", 
                       currentUser.getId(), id);
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        transactionService.deleteTransaction(id);
        logger.info("Transaction avec ID {} supprimée par l'utilisateur autorisé {}", id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}