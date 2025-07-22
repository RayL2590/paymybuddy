package com.openclassroom.paymybuddy.controller;

import com.openclassroom.paymybuddy.service.UserService;
import com.openclassroom.paymybuddy.service.AuthService;
import com.openclassroom.paymybuddy.model.User;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour gérer les relations entre utilisateurs.
 * Utilise la convention REST standard avec le préfixe /api.
 */
@RestController
@RequestMapping("/api/relations")
public class RelationController {

    /**
     * Logger pour enregistrer les événements liés aux relations entre utilisateurs.
     */
    private static final Logger logger = LoggerFactory.getLogger(RelationController.class);

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
    public RelationController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * API pour la recherche d'utilisateurs (pour l'autocomplétion).
     *
     * @param term Le terme de recherche.
     * @return Une liste d'utilisateurs correspondant au terme de recherche.
     */
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String term) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<User> users = userService.searchUsers(term, currentUser.getId());
        return ResponseEntity.ok(users);
    }

    /**
     * Ajoute une relation entre l'utilisateur connecté et un autre utilisateur.
     *
     * @param email L'email de l'utilisateur à ajouter comme relation.
     * @return Une réponse avec le statut de l'opération.
     */
    @PostMapping
    public ResponseEntity<String> addRelation(@RequestParam String email) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body("Utilisateur non connecté");
        }

        logger.info("Tentative d'ajout de relation - Utilisateur ID: {}, Identifiant du contact: {}", 
                   currentUser.getId(), email);

        try {
            userService.addUserConnectionByIdentifier(currentUser.getId(), email);
            logger.info("Relation ajoutée avec succès - Utilisateur ID: {}, Contact identifiant: {}", 
                       currentUser.getId(), email);
            return ResponseEntity.ok("Contact ajouté avec succès");
        } catch (EntityNotFoundException e) {
            logger.warn("Échec de l'ajout de relation - Utilisateur non trouvé avec identifiant: {}", email);
            return ResponseEntity.badRequest().body("Cet utilisateur n'existe pas");
        } catch (IllegalArgumentException e) {
            logger.warn("Échec de l'ajout de relation - Argument invalide: {} pour utilisateur ID: {}", 
                       e.getMessage(), currentUser.getId());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'ajout de relation - Utilisateur ID: {}, Identifiant: {}", 
                        currentUser.getId(), email, e);
            return ResponseEntity.internalServerError().body("Une erreur inattendue s'est produite");
        }
    }
}
