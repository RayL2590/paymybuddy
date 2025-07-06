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
     * Constructeur pour injecter le service des transactions.
     *
     * @param transactionService Service pour gérer les transactions.
     */
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
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

    /**
     * Effectue un transfert d'argent entre utilisateurs.
     *
     * @param senderId L'ID de l'utilisateur envoyant l'argent.
     * @param transferDTO Les détails du transfert.
     * @return La transaction créée ou une réponse 400 en cas d'erreur.
     */
    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transferMoney(
            @RequestParam Long senderId,
            @Valid @RequestBody TransferDTO transferDTO) {
        try {
            transferDTO.setSenderId(senderId);
            Transaction transaction = transactionService.createTransfer(transferDTO);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
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
        if (transactionService.getTransactionById(id).isPresent()) {
            transactionService.deleteTransaction(id);
            logger.info("Transaction avec ID {} supprimée", id);
            return ResponseEntity.noContent().build();
        }
        logger.warn("Transaction avec ID {} non trouvée pour suppression", id);
        return ResponseEntity.notFound().build();
    }
}