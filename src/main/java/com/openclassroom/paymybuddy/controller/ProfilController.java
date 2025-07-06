package com.openclassroom.paymybuddy.controller;

import com.openclassroom.paymybuddy.service.AuthService;
import com.openclassroom.paymybuddy.service.UserService;
import com.openclassroom.paymybuddy.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Contrôleur pour gérer les opérations liées au profil utilisateur.
 */
@Controller
@RequestMapping("/profil")
public class ProfilController {

    /**
     * Logger pour enregistrer les événements liés au profil utilisateur.
     */
    private static final Logger logger = LoggerFactory.getLogger(ProfilController.class);

    /**
     * Service d'authentification pour récupérer l'utilisateur connecté.
     */
    private final AuthService authService;

    /**
     * Service utilisateur pour gérer les opérations liées aux utilisateurs.
     */
    private final UserService userService;

    /**
     * Encodeur de mot de passe pour vérifier et encoder les mots de passe.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructeur pour injecter les dépendances nécessaires.
     *
     * @param authService Service d'authentification.
     * @param userService Service utilisateur.
     * @param passwordEncoder Encodeur de mot de passe.
     */
    public ProfilController(AuthService authService, UserService userService, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Affiche le profil de l'utilisateur connecté.
     *
     * @param model Le modèle utilisé pour transmettre des données à la vue.
     * @return Le nom de la vue du profil ou une redirection vers la page de connexion si l'utilisateur n'est pas connecté.
     */
    @GetMapping
    public String showProfile(Model model) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            logger.warn("Utilisateur non connecté tentant d'accéder au profil");
            return "redirect:/login";
        }

        logger.info("Affichage du profil pour l'utilisateur ID: {}", currentUser.getId());
        model.addAttribute("user", currentUser);

        return "profil";
    }

    /**
     * Permet à l'utilisateur de changer son mot de passe.
     *
     * @param currentPassword Le mot de passe actuel de l'utilisateur.
     * @param newPassword Le nouveau mot de passe souhaité.
     * @param confirmPassword La confirmation du nouveau mot de passe.
     * @param redirectAttributes Les attributs pour transmettre des messages à la vue.
     * @return Une redirection vers la vue du profil ou la page de connexion si l'utilisateur n'est pas connecté.
     */
    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            logger.warn("Utilisateur non connecté tentant de modifier le mot de passe");
            return "redirect:/login";
        }

        logger.info("Tentative de modification de mot de passe pour l'utilisateur ID: {}", currentUser.getId());

        // Validation des paramètres
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Le mot de passe actuel est requis");
            return "redirect:/profil";
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Le nouveau mot de passe est requis");
            return "redirect:/profil";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Les mots de passe ne correspondent pas");
            return "redirect:/profil";
        }

        // Validation de la force du mot de passe
        if (!isPasswordValid(newPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Le mot de passe doit contenir au moins 8 caractères, incluant des lettres et des chiffres");
            return "redirect:/profil";
        }

        // Vérification de l'ancien mot de passe
        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            logger.warn("Tentative de modification avec un mauvais mot de passe actuel pour l'utilisateur ID: {}", 
                       currentUser.getId());
            redirectAttributes.addFlashAttribute("errorMessage", "Mot de passe actuel incorrect");
            return "redirect:/profil";
        }

        // Vérification que le nouveau mot de passe est différent
        if (passwordEncoder.matches(newPassword, currentUser.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Le nouveau mot de passe doit être différent de l'ancien");
            return "redirect:/profil";
        }

        try {
            // Modifier le mot de passe
            userService.changePassword(currentUser.getId(), newPassword);
            logger.info("Mot de passe modifié avec succès pour l'utilisateur ID: {}", currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Mot de passe modifié avec succès");
        } catch (Exception e) {
            logger.error("Erreur lors de la modification du mot de passe pour l'utilisateur ID: {}", 
                        currentUser.getId(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la modification du mot de passe");
        }

        return "redirect:/profil";
    }

    /**
     * Valide la force du mot de passe.
     *
     * @param password Le mot de passe à valider.
     * @return true si le mot de passe est valide, false sinon.
     */
    private boolean isPasswordValid(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");

        return hasLetter && hasDigit;
    }
}
