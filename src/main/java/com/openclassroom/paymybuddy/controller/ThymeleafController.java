package com.openclassroom.paymybuddy.controller;

import com.openclassroom.paymybuddy.model.Transaction;
import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.service.TransactionService;
import com.openclassroom.paymybuddy.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur pour gérer les opérations liées à l'affichage de la page d'accueil.
 */
@Controller
public class ThymeleafController {

    /**
     * Logger pour enregistrer les événements liés à l'affichage de la page d'accueil.
     */
    private static final Logger logger = LoggerFactory.getLogger(ThymeleafController.class);

    /**
     * Service pour gérer les transactions.
     */
    private final TransactionService transactionService;

    /**
     * Service d'authentification pour récupérer l'utilisateur connecté.
     */
    private final AuthService authService;

    /**
     * Constructeur pour injecter les services nécessaires.
     *
     * @param transactionService Service pour gérer les transactions.
     * @param authService Service d'authentification.
     */
    public ThymeleafController(TransactionService transactionService, AuthService authService) {
        this.transactionService = transactionService;
        this.authService = authService;
    }

    /**
     * Affiche la page d'accueil.
     *
     * @param model Le modèle utilisé pour transmettre des données à la vue.
     * @return Le nom de la vue de la page d'accueil ou une redirection vers la page de connexion si l'utilisateur n'est pas connecté.
     */
    @GetMapping("/")
    public String index(Model model) {
        logger.info("Affichage de la page d'accueil");
        
        // Vérifier si l'utilisateur est connecté
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            logger.debug("Utilisateur non connecté, redirection vers la page de connexion");
            return "redirect:/login";
        }
        
        logger.debug("Récupération des données pour l'utilisateur connecté ID: {}", currentUser.getId());
        
        try {
            List<Transaction> transactions = transactionService.getAllTransactions()
                    .stream()
                    .limit(10)
                    .collect(Collectors.toList());
            
            logger.debug("Nombre de transactions récupérées pour l'accueil: {}", transactions.size());
            model.addAttribute("transactions", transactions);
            
            var relations = transactionService.getRelations(currentUser.getId());
            logger.debug("Nombre de relations récupérées: {}", relations.size());
            model.addAttribute("relations", relations);
            
            // Ajouter l'utilisateur connecté au modèle
            model.addAttribute("currentUser", currentUser);
            
            logger.info("Page d'accueil chargée avec succès - {} transactions et {} relations", 
                       transactions.size(), relations.size());
            
        } catch (Exception e) {
            logger.error("Erreur lors du chargement de la page d'accueil", e);
            // Ajouter des attributs vides pour éviter les erreurs d'affichage
            model.addAttribute("transactions", List.of());
            model.addAttribute("relations", List.of());
            model.addAttribute("currentUser", currentUser);
        }
        
        return "index";
    }
}