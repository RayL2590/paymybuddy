package com.openclassroom.paymybuddy.controller;

import com.openclassroom.paymybuddy.dto.RelationDTO;
import com.openclassroom.paymybuddy.dto.TransferDTO;
import com.openclassroom.paymybuddy.model.Transaction;
import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.service.TransactionService;
import com.openclassroom.paymybuddy.service.UserService;
import com.openclassroom.paymybuddy.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * Contrôleur pour gérer les transactions des utilisateurs via l'interface web.
 * Ce contrôleur gère les vues et redirige vers les endpoints REST standardisés.
 */
@Controller
@RequestMapping("/user-transactions")
public class UserTransactionController {

    /**
     * Logger pour enregistrer les événements liés aux transactions des utilisateurs.
     */
    private static final Logger logger = LoggerFactory.getLogger(UserTransactionController.class);

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
    public UserTransactionController(TransactionService transactionService, UserService userService, AuthService authService) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * Récupère les transactions d'un utilisateur.
     *
     * @param userId L'ID de l'utilisateur.
     * @param model Le modèle utilisé pour transmettre des données à la vue.
     * @return Le nom de la vue des transactions ou une redirection vers la page de connexion si l'utilisateur n'est pas connecté.
     */
    @GetMapping("/{userId}")
    public String getUserTransactions(@PathVariable Long userId, Model model) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            logger.warn("Utilisateur non connecté tentant d'accéder aux transactions");
            return "redirect:/login";
        }

        // Vérifier que l'utilisateur accède à ses propres transactions
        if (!currentUser.getId().equals(userId)) {
            logger.warn("L'utilisateur {} tente d'accéder aux transactions de l'utilisateur {}", 
                       currentUser.getId(), userId);
            return "redirect:/user-transactions/" + currentUser.getId();
        }

        logger.info("Récupération des transactions pour l'utilisateur ID: {}", userId);

        List<Transaction> transactions = transactionService.getTransactionsByUserId(userId);
        logger.debug("Nombre de transactions trouvées: {}", transactions.size());

        model.addAttribute("user", currentUser);

        // Ajouter la liste des relations
        List<RelationDTO> relations = transactionService.getRelations(userId);
        model.addAttribute("relations", relations);
        logger.debug("Nombre de relations trouvées: {}", relations.size());

        model.addAttribute("transactions", transactions);
        return "user-transactions";
    }

    /**
     * Affiche le formulaire d'ajout de relations.
     *
     * @param model Le modèle utilisé pour transmettre des données à la vue.
     * @return Le nom de la vue du formulaire d'ajout de relations ou une redirection vers la page de connexion si l'utilisateur n'est pas connecté.
     */
    @GetMapping("/add")
    public String showAddRelationForm(Model model) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            logger.warn("Utilisateur non connecté tentant d'accéder au formulaire d'ajout de relation");
            return "redirect:/login";
        }

        logger.info("Affichage du formulaire d'ajout de relation pour l'utilisateur ID: {}", currentUser.getId());
        model.addAttribute("user", currentUser);
        logger.debug("Utilisateur ajouté au modèle: {}", currentUser.getEmail());

        return "add-relations";
    }

    /**
     * Traite un transfert d'argent entre utilisateurs.
     *
     * @param userId L'ID de l'utilisateur effectuant le transfert.
     * @param transferDTO Les détails du transfert.
     * @param redirectAttributes Les attributs pour transmettre des messages à la vue.
     * @return Une redirection vers la vue des transactions ou une page d'erreur en cas de problème.
     */
    @PostMapping("/{userId}/transfer")
    public String processTransfer(
            @PathVariable Long userId,
            TransferDTO transferDTO,
            RedirectAttributes redirectAttributes) {

        logger.info("🔥 DÉBUT DU TRANSFERT - UserID: {}, TransferDTO reçu: {}", userId, transferDTO);

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            logger.warn("❌ Utilisateur non connecté tentant d'effectuer un transfert");
            return "redirect:/login";
        }

        // Vérifier que l'utilisateur effectue un transfert depuis son propre compte
        if (!currentUser.getId().equals(userId)) {
            logger.warn("⚠️ L'utilisateur {} tente d'effectuer un transfert depuis le compte {}", 
                       currentUser.getId(), userId);
            return "redirect:/user-transactions/" + currentUser.getId();
        }

        logger.info("✅ Utilisateur connecté vérifié: {}", currentUser.getEmail());
        // Assigner l'ID de l'utilisateur actuel à l'expéditeur du transfert
        transferDTO.setSenderId(currentUser.getId());

        logger.info("📝 Données reçues - Destinataire ID: {}, Montant: {}, Description: {}", 
                   transferDTO.getReceiverId(), transferDTO.getAmount(), transferDTO.getDescription());

        // Validation des données du transfert
        if (transferDTO.getReceiverId() == null) {
            logger.warn("❌ Destinataire manquant");
            redirectAttributes.addFlashAttribute("errorMessage", "Veuillez sélectionner un destinataire");
            return "redirect:/user-transactions/" + userId;
        }

        if (transferDTO.getAmount() == null || transferDTO.getAmount().compareTo(java.math.BigDecimal.valueOf(0.01)) < 0) {
            logger.warn("❌ Montant invalide: {}", transferDTO.getAmount());
            redirectAttributes.addFlashAttribute("errorMessage", "Le montant doit être supérieur à 0.01");
            return "redirect:/user-transactions/" + userId;
        }

        if (transferDTO.getDescription() == null || transferDTO.getDescription().trim().isEmpty()) {
            logger.warn("❌ Description manquante");
            redirectAttributes.addFlashAttribute("errorMessage", "Veuillez saisir une description");
            return "redirect:/user-transactions/" + userId;
        }

        try {
            logger.info("🚀 Appel du service de transfert avec: {}", transferDTO);
            transactionService.createTransfer(transferDTO);
            logger.info("✅ Transfert effectué avec succès - Utilisateur ID: {}, Montant: {}, Destinataire ID: {}", 
                       userId, transferDTO.getAmount(), transferDTO.getReceiverId());
            redirectAttributes.addFlashAttribute("successMessage", "Transfert effectué avec succès");
        } catch (Exception e) {
            logger.error("💥 Erreur lors du transfert - Utilisateur ID: {}, Montant: {}, Destinataire ID: {}", 
                        userId, transferDTO.getAmount(), transferDTO.getReceiverId(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors du transfert : " + e.getMessage());
        }

        return "redirect:/user-transactions/" + userId;
    }

    /**
     * Ajoute de l'argent à la balance de l'utilisateur.
     *
     * @param userId L'ID de l'utilisateur.
     * @param amount Le montant à ajouter.
     * @param redirectAttributes Les attributs pour transmettre des messages à la vue.
     * @return Une redirection vers la vue des transactions ou une page d'erreur en cas de problème.
     */
    @PostMapping("/{userId}/balance/add")
    public String addBalance(
            @PathVariable Long userId,
            @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            logger.warn("Utilisateur non connecté tentant d'ajouter de l'argent");
            return "redirect:/login";
        }

        if (!currentUser.getId().equals(userId)) {
            logger.warn("L'utilisateur {} tente de modifier la balance de l'utilisateur {}", 
                       currentUser.getId(), userId);
            return "redirect:/user-transactions/" + currentUser.getId();
        }

        try {
            userService.adjustUserBalance(userId, amount, "ADD");
            logger.info("Argent ajouté avec succès - Utilisateur ID: {}, Montant: {}", userId, amount);
            redirectAttributes.addFlashAttribute("successMessage", 
                String.format("%.2f€ ajoutés à votre balance avec succès", amount));
        } catch (Exception e) {
            logger.error("Erreur lors de l'ajout d'argent - Utilisateur ID: {}, Montant: {}", userId, amount, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur : " + e.getMessage());
        }

        return "redirect:/user-transactions/" + userId;
    }

    /**
     * Retire de l'argent de la balance de l'utilisateur.
     *
     * @param userId L'ID de l'utilisateur.
     * @param amount Le montant à retirer.
     * @param redirectAttributes Les attributs pour transmettre des messages à la vue.
     * @return Une redirection vers la vue des transactions ou une page d'erreur en cas de problème.
     */
    @PostMapping("/{userId}/balance/subtract")
    public String subtractBalance(
            @PathVariable Long userId,
            @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            logger.warn("Utilisateur non connecté tentant de retirer de l'argent");
            return "redirect:/login";
        }

        if (!currentUser.getId().equals(userId)) {
            logger.warn("L'utilisateur {} tente de modifier la balance de l'utilisateur {}", 
                       currentUser.getId(), userId);
            return "redirect:/user-transactions/" + currentUser.getId();
        }

        try {
            userService.adjustUserBalance(userId, amount, "SUBTRACT");
            logger.info("Argent retiré avec succès - Utilisateur ID: {}, Montant: {}", userId, amount);
            redirectAttributes.addFlashAttribute("successMessage", 
                String.format("%.2f€ retirés de votre balance avec succès", amount));
        } catch (Exception e) {
            logger.error("Erreur lors du retrait d'argent - Utilisateur ID: {}, Montant: {}", userId, amount, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur : " + e.getMessage());
        }

        return "redirect:/user-transactions/" + userId;
    }
}