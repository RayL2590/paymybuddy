package com.openclassroom.paymybuddy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.repository.UserRepository;

/**
 * Service pour gérer l'authentification et les informations de l'utilisateur connecté.
 */
@Service
public class AuthService {

    /**
     * Logger pour enregistrer les informations de débogage.
     */
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    /**
     * Repository pour accéder aux données des utilisateurs.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Récupère l'utilisateur actuellement connecté.
     *
     * @return L'utilisateur connecté ou null si aucun utilisateur n'est connecté.
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            logger.debug("Aucun utilisateur connecté");
            return null;
        }

        String email = auth.getName();
        logger.debug("Utilisateur connecté: {}", email);

        return userRepository.findByEmail(email)
                .orElse(null);
    }

    /**
     * Récupère l'ID de l'utilisateur actuellement connecté.
     *
     * @return L'ID de l'utilisateur connecté ou null si aucun utilisateur n'est connecté.
     */
    public Long getCurrentUserId() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getId() : null;
    }

    /**
     * Vérifie si un utilisateur est actuellement connecté.
     *
     * @return true si un utilisateur est connecté, false sinon.
     */
    public boolean isAuthenticated() {
        return getCurrentUser() != null;
    }
}
