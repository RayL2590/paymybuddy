package com.openclassroom.paymybuddy.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Contrôleur pour gérer les opérations liées à l'authentification.
 */
@Controller
public class AuthController {

    /**
     * Logger pour enregistrer les événements liés à l'authentification.
     */
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    /**
     * Gère l'affichage de la page de connexion.
     *
     * @param model Le modèle utilisé pour transmettre des données à la vue.
     * @param error Indique si une erreur d'authentification s'est produite.
     * @param logout Indique si l'utilisateur s'est déconnecté.
     * @return Le nom de la vue de connexion.
     */
    @GetMapping("/login")
    public String login(Model model, 
                       @RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout) {

        logger.debug("Affichage de la page de connexion");

        if (error != null) {
            model.addAttribute("errorMessage", "Email ou mot de passe incorrect");
            logger.warn("Tentative de connexion échouée");
        }

        if (logout != null) {
            model.addAttribute("logoutMessage", "Vous avez été déconnecté avec succès");
            logger.info("Utilisateur déconnecté");
        }

        return "login";
    }
}
