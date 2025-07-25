package com.openclassroom.paymybuddy.controller;

import com.openclassroom.paymybuddy.dto.ChangePasswordDTO;
import com.openclassroom.paymybuddy.dto.ChangeUsernameDTO;
import com.openclassroom.paymybuddy.dto.ChangeEmailDTO;
import com.openclassroom.paymybuddy.service.AuthService;
import com.openclassroom.paymybuddy.service.UserService;
import com.openclassroom.paymybuddy.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
     * @param changePasswordDTO Le DTO contenant les données de changement de mot de passe.
     * @param bindingResult Le résultat de la validation.
     * @param redirectAttributes Les attributs pour transmettre des messages à la vue.
     * @return Une redirection vers la vue du profil ou la page de connexion si l'utilisateur n'est pas connecté.
     */
    @PostMapping("/change-password")
    public String changePassword(
            @Valid @ModelAttribute ChangePasswordDTO changePasswordDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            logger.warn("Utilisateur non connecté tentant de modifier le mot de passe");
            return "redirect:/login";
        }

        logger.info("Tentative de modification de mot de passe pour l'utilisateur ID: {}", currentUser.getId());

        // Vérification des erreurs de validation
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .findFirst()
                    .orElse("Données invalides");
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/profil";
        }

        // Vérification que les mots de passe correspondent
        if (!changePasswordDTO.isPasswordMatching()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Les mots de passe ne correspondent pas");
            return "redirect:/profil";
        }

        // Vérification de l'ancien mot de passe
        if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), currentUser.getPassword())) {
            logger.warn("Tentative de modification avec un mauvais mot de passe actuel pour l'utilisateur ID: {}", 
                       currentUser.getId());
            redirectAttributes.addFlashAttribute("errorMessage", "Mot de passe actuel incorrect");
            return "redirect:/profil";
        }

        // Vérification que le nouveau mot de passe est différent
        if (passwordEncoder.matches(changePasswordDTO.getNewPassword(), currentUser.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Le nouveau mot de passe doit être différent de l'ancien");
            return "redirect:/profil";
        }

        try {
            // Modifier le mot de passe
            userService.changePassword(currentUser.getId(), changePasswordDTO.getNewPassword());
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
     * Vérifie si un nom d'utilisateur est disponible (endpoint AJAX).
     *
     * @param username Le nom d'utilisateur à vérifier.
     * @return true si le nom d'utilisateur est disponible, false sinon.
     */
    @GetMapping("/check-username")
    @ResponseBody
    public boolean checkUsernameAvailability(@RequestParam String username) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        return userService.isUsernameAvailable(username, currentUser.getId());
    }
    
    /**
     * Vérifie si une adresse email est disponible (endpoint AJAX).
     *
     * @param email L'adresse email à vérifier.
     * @return true si l'adresse email est disponible, false sinon.
     */
    @GetMapping("/check-email")
    @ResponseBody
    public boolean checkEmailAvailability(@RequestParam String email) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        return userService.isEmailAvailable(email, currentUser.getId());
    }

    /**
     * Permet à l'utilisateur de changer son nom d'utilisateur.
     *
     * @param changeUsernameDTO Le DTO contenant le nouveau nom d'utilisateur.
     * @param currentPassword Le mot de passe actuel pour vérification.
     * @param bindingResult Le résultat de la validation.
     * @param redirectAttributes Les attributs pour transmettre des messages à la vue.
     * @return Une redirection vers la vue du profil ou la page de connexion si l'utilisateur n'est pas connecté.
     */
    @PostMapping("/change-username")
    public String changeUsername(
            @Valid @ModelAttribute ChangeUsernameDTO changeUsernameDTO,
            @RequestParam String currentPassword,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            logger.warn("Utilisateur non connecté tentant de modifier le nom d'utilisateur");
            return "redirect:/login";
        }

        logger.info("Tentative de modification du nom d'utilisateur pour l'utilisateur ID: {}", currentUser.getId());

        // Vérification des erreurs de validation
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .findFirst()
                    .orElse("Données invalides");
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/profil";
        }

        // Validation du mot de passe actuel
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Le mot de passe actuel est requis pour cette modification");
            return "redirect:/profil";
        }

        // Vérification du mot de passe actuel
        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            logger.warn("Tentative de modification du nom d'utilisateur avec un mauvais mot de passe pour l'utilisateur ID: {}", 
                       currentUser.getId());
            redirectAttributes.addFlashAttribute("errorMessage", "Mot de passe incorrect");
            return "redirect:/profil";
        }

        // Vérifier si le nom d'utilisateur est identique à l'actuel
        if (changeUsernameDTO.getNewUsername().equals(currentUser.getUsername())) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Le nouveau nom d'utilisateur doit être différent de l'actuel");
            return "redirect:/profil";
        }

        try {
            // Modifier le nom d'utilisateur
            userService.changeUsername(currentUser.getId(), changeUsernameDTO.getNewUsername());
            logger.info("Nom d'utilisateur modifié avec succès pour l'utilisateur ID: {}", currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Nom d'utilisateur modifié avec succès");
        } catch (IllegalArgumentException e) {
            logger.warn("Tentative de modification avec un nom d'utilisateur déjà pris pour l'utilisateur ID: {}", 
                       currentUser.getId());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur lors de la modification du nom d'utilisateur pour l'utilisateur ID: {}", 
                        currentUser.getId(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la modification du nom d'utilisateur");
        }

        return "redirect:/profil";
    }
    
    /**
     * Permet à l'utilisateur de changer son adresse email.
     *
     * @param changeEmailDTO Le DTO contenant la nouvelle adresse email.
     * @param currentPassword Le mot de passe actuel pour vérification.
     * @param bindingResult Le résultat de la validation.
     * @param redirectAttributes Les attributs pour transmettre des messages à la vue.
     * @return Une redirection vers la vue du profil ou la page de connexion si l'utilisateur n'est pas connecté.
     */
    @PostMapping("/change-email")
    public String changeEmail(
            @Valid @ModelAttribute ChangeEmailDTO changeEmailDTO,
            @RequestParam String currentPassword,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes, HttpServletRequest request) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            logger.warn("Utilisateur non connecté tentant de modifier l'adresse email");
            return "redirect:/login";
        }

        logger.info("Tentative de modification de l'adresse email pour l'utilisateur ID: {}", currentUser.getId());

        // Vérification des erreurs de validation
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .findFirst()
                    .orElse("Données invalides");
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/profil";
        }

        // Validation du mot de passe actuel
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Le mot de passe actuel est requis pour cette modification");
            return "redirect:/profil";
        }

        // Vérification du mot de passe actuel
        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            logger.warn("Tentative de modification de l'adresse email avec un mauvais mot de passe pour l'utilisateur ID: {}", 
                       currentUser.getId());
            redirectAttributes.addFlashAttribute("errorMessage", "Mot de passe incorrect");
            return "redirect:/profil";
        }

        // Vérifier si l'adresse email est identique à l'actuelle
        if (changeEmailDTO.getNewEmail().equals(currentUser.getEmail())) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "La nouvelle adresse email doit être différente de l'actuelle");
            return "redirect:/profil";
        }

        try {
            // Modifier l'adresse email
             userService.changeEmail(currentUser.getId(), changeEmailDTO.getNewEmail());

            SecurityContextHolder.clearContext();
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            logger.info("Email modifié et utilisateur déconnecté - ID: {}", currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", 
                "Email modifié avec succès. Veuillez vous reconnecter avec votre nouvel email.");
            return "redirect:/login";
            
        } catch (IllegalArgumentException e) {
            logger.warn("Tentative de modification avec une adresse email déjà prise pour l'utilisateur ID: {}", 
                    currentUser.getId());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/profil"; 
        } catch (Exception e) {
            logger.error("Erreur lors de la modification de l'adresse email pour l'utilisateur ID: {}", 
                        currentUser.getId(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la modification de l'adresse email");
            return "redirect:/profil"; 
        }
    }
}