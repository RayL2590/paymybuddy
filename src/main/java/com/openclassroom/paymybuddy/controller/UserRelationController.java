package com.openclassroom.paymybuddy.controller;

import com.openclassroom.paymybuddy.service.UserService;
import com.openclassroom.paymybuddy.service.AuthService;
import com.openclassroom.paymybuddy.model.User;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Contrôleur pour gérer les relations entre utilisateurs.
 */
@Controller
@RequestMapping("/user-relations")
public class UserRelationController {

    /**
     * Logger pour enregistrer les événements liés aux relations entre utilisateurs.
     */
    private static final Logger logger = LoggerFactory.getLogger(UserRelationController.class);

    /**
     * Service utilisateur pour gérer les opérations liées aux utilisateurs.
     */
    private final UserService userService;

    /**
     * Service d'authentification pour récupérer l'utilisateur connecté.
     */
    private final AuthService authService;

    /**
     * Constructeur pour injecter les services nécessaires.
     *
     * @param userService Service utilisateur.
     * @param authService Service d'authentification.
     */
    public UserRelationController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
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
        logger.debug("Utilisateur trouvé et ajouté au modèle: {}", currentUser.getEmail());

        return "add-relations";
    }

    /**
     * Ajoute une relation entre l'utilisateur connecté et un autre utilisateur.
     *
     * @param email L'email de l'utilisateur à ajouter comme relation.
     * @param redirectAttributes Les attributs pour transmettre des messages à la vue.
     * @return Une redirection vers la vue des transactions ou le formulaire d'ajout de relations en cas d'erreur.
     */
    @PostMapping("/add")
    public String addRelation(
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            logger.warn("Utilisateur non connecté tentant d'ajouter une relation");
            return "redirect:/login";
        }

        logger.info("Tentative d'ajout de relation - Utilisateur ID: {}, Identifiant du contact: {}", 
                   currentUser.getId(), email);

        try {
            userService.addUserConnectionByIdentifier(currentUser.getId(), email);
            logger.info("Relation ajoutée avec succès - Utilisateur ID: {}, Contact identifiant: {}", 
                       currentUser.getId(), email);
            redirectAttributes.addFlashAttribute("successMessage", "Contact ajouté avec succès");
            return "redirect:/user-transactions/" + currentUser.getId();
        } catch (EntityNotFoundException e) {
            logger.warn("Échec de l'ajout de relation - Utilisateur non trouvé avec identifiant: {}", email);
            redirectAttributes.addFlashAttribute("errorMessage", "Cet utilisateur n'existe pas");
            return "redirect:/user-relations/add";
        } catch (IllegalArgumentException e) {
            logger.warn("Échec de l'ajout de relation - Argument invalide: {} pour utilisateur ID: {}", 
                       e.getMessage(), currentUser.getId());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/user-relations/add";
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'ajout de relation - Utilisateur ID: {}, Identifiant: {}", 
                        currentUser.getId(), email, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Une erreur inattendue s'est produite");
            return "redirect:/user-relations/add";
        }
    }

    /**
     * API pour la recherche d'utilisateurs (pour l'autocomplétion).
     *
     * @param term Le terme de recherche.
     * @return Une liste d'utilisateurs correspondant au terme de recherche.
     */
    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<List<User>> searchUsers(@RequestParam String term) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<User> users = userService.searchUsers(term, currentUser.getId());
        return ResponseEntity.ok(users);
    }
}
