package com.openclassroom.paymybuddy.controller;

import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Contrôleur pour gérer les redirections des utilisateurs connectés.
 */
@Controller
public class RedirectController {

    /**
     * Logger pour enregistrer les événements liés aux redirections.
     */
    private static final Logger logger = LoggerFactory.getLogger(RedirectController.class);

    /**
     * Service d'authentification pour récupérer l'utilisateur connecté.
     */
    @Autowired
    private AuthService authService;

    /**
     * Redirige vers les transactions de l'utilisateur connecté.
     *
     * @return Une redirection vers la vue des transactions de l'utilisateur ou vers la page de connexion si l'utilisateur n'est pas connecté.
     */
    @GetMapping("/my-transactions")
    public String redirectToMyTransactions() {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            logger.warn("Utilisateur non connecté tentant d'accéder à ses transactions");
            return "redirect:/login";
        }

        logger.info("Redirection vers les transactions de l'utilisateur ID: {}", currentUser.getId());
        return "redirect:/user-transactions/" + currentUser.getId();
    }

    /**
     * Redirige vers le formulaire d'ajout de relations.
     *
     * @return Une redirection vers la vue d'ajout de relations ou vers la page de connexion si l'utilisateur n'est pas connecté.
     */
    @GetMapping("/add-relation")
    public String redirectToAddRelation() {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            logger.warn("Utilisateur non connecté tentant d'accéder au formulaire d'ajout de relation");
            return "redirect:/login";
        }

        logger.info("Redirection vers le formulaire d'ajout de relation pour l'utilisateur ID: {}", currentUser.getId());
        return "redirect:/user-relations/add";
    }
}
