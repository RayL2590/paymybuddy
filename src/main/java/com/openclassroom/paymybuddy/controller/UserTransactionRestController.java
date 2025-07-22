package com.openclassroom.paymybuddy.controller;

import com.openclassroom.paymybuddy.dto.RelationDTO;
import com.openclassroom.paymybuddy.dto.TransferDTO;
import com.openclassroom.paymybuddy.model.Transaction;
import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.service.TransactionService;
import com.openclassroom.paymybuddy.service.UserService;
import com.openclassroom.paymybuddy.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Contrôleur REST pour gérer les transactions des utilisateurs.
 * Utilise la convention REST standard avec le préfixe /api.
 */
@RestController
@RequestMapping("/api/users/{userId}/transactions")
public class UserTransactionRestController {

    /**
     * Logger pour enregistrer les événements liés aux transactions des utilisateurs.
     */
    private static final Logger logger = LoggerFactory.getLogger(UserTransactionRestController.class);

    /**
     * Service pour gérer les transactions.
     */
    private final TransactionService transactionService;

    /**
     * Service pour gérer les utilisateurs.
     */
    private final UserService userService;

    /**
     * Service d'authentification pour récupérer l'utilisateur connecté.
     */
    private final AuthService authService;

    /**
     * Constructeur pour injecter les services nécessaires.
     *
     * @param transactionService Service pour gérer les transactions.
     * @param userService Service pour gérer les utilisateurs.
     * @param authService Service d'authentification.
     */
    public UserTransactionRestController(TransactionService transactionService, UserService userService, AuthService authService) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * Récupère les transactions d'un utilisateur.
     *
     * @param userId L'ID de l'utilisateur.
     * @return Une liste des transactions de l'utilisateur.
     */
    @GetMapping
    public ResponseEntity<List<Transaction>> getUserTransactions(@PathVariable Long userId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        // Vérifier que l'utilisateur accède à ses propres transactions
        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        logger.info("Récupération des transactions pour l'utilisateur ID: {}", userId);
        List<Transaction> transactions = transactionService.getTransactionsByUserId(userId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Récupère les relations d'un utilisateur.
     *
     * @param userId L'ID de l'utilisateur.
     * @return Une liste des relations de l'utilisateur.
     */
    @GetMapping("/relations")
    public ResponseEntity<List<RelationDTO>> getUserRelations(@PathVariable Long userId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        // Vérifier que l'utilisateur accède à ses propres relations
        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        logger.info("Récupération des relations pour l'utilisateur ID: {}", userId);
        List<RelationDTO> relations = transactionService.getRelations(userId);
        return ResponseEntity.ok(relations);
    }

    /**
     * Traite un transfert d'argent entre utilisateurs.
     *
     * @param userId L'ID de l'utilisateur effectuant le transfert.
     * @param transferDTO Les détails du transfert.
     * @return La transaction créée ou une erreur en cas de problème.
     */
    @PostMapping("/transfer")
    public ResponseEntity<Transaction> processTransfer(
            @PathVariable Long userId,
            @Valid @RequestBody TransferDTO transferDTO) {

        logger.info("🔥 DÉBUT DU TRANSFERT - UserID: {}, TransferDTO reçu: {}", userId, transferDTO);

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            logger.warn("❌ Utilisateur non connecté tentant d'effectuer un transfert");
            return ResponseEntity.status(401).build();
        }

        // Vérifier que l'utilisateur effectue un transfert depuis son propre compte
        if (!currentUser.getId().equals(userId)) {
            logger.warn("⚠️ L'utilisateur {} tente d'effectuer un transfert depuis le compte {}", 
                       currentUser.getId(), userId);
            return ResponseEntity.status(403).build();
        }

        logger.info("✅ Utilisateur connecté vérifié: {}", currentUser.getEmail());

        // ✅ DÉFINIR LE SENDER AVANT LA VALIDATION
        transferDTO.setSenderId(currentUser.getId());

        try {
            logger.info("🚀 Appel du service de transfert avec: {}", transferDTO);
            Transaction transaction = transactionService.createTransfer(transferDTO);
            logger.info("✅ Transfert effectué avec succès - Utilisateur ID: {}, Montant: {}, Destinataire ID: {}", 
                       userId, transferDTO.getAmount(), transferDTO.getReceiverId());
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            logger.error("💥 Erreur lors du transfert - Utilisateur ID: {}, Montant: {}, Destinataire ID: {}", 
                        userId, transferDTO.getAmount(), transferDTO.getReceiverId(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Ajoute de l'argent à la balance de l'utilisateur.
     *
     * @param userId L'ID de l'utilisateur.
     * @param amount Le montant à ajouter.
     * @return Une réponse avec le statut de l'opération.
     */
    @PostMapping("/balance/add")
    public ResponseEntity<String> addBalance(
            @PathVariable Long userId,
            @RequestParam BigDecimal amount) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        try {
            userService.adjustUserBalance(userId, amount, "ADD");
            logger.info("Argent ajouté avec succès - Utilisateur ID: {}, Montant: {}", userId, amount);
            return ResponseEntity.ok(String.format("%.2f€ ajoutés à votre balance avec succès", amount));
        } catch (Exception e) {
            logger.error("Erreur lors de l'ajout d'argent - Utilisateur ID: {}, Montant: {}", userId, amount, e);
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }

    /**
     * Retire de l'argent de la balance de l'utilisateur.
     *
     * @param userId L'ID de l'utilisateur.
     * @param amount Le montant à retirer.
     * @return Une réponse avec le statut de l'opération.
     */
    @PostMapping("/balance/subtract")
    public ResponseEntity<String> subtractBalance(
            @PathVariable Long userId,
            @RequestParam BigDecimal amount) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        try {
            userService.adjustUserBalance(userId, amount, "SUBTRACT");
            logger.info("Argent retiré avec succès - Utilisateur ID: {}, Montant: {}", userId, amount);
            return ResponseEntity.ok(String.format("%.2f€ retirés de votre balance avec succès", amount));
        } catch (Exception e) {
            logger.error("Erreur lors du retrait d'argent - Utilisateur ID: {}, Montant: {}", userId, amount, e);
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }
}
