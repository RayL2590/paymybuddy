package com.openclassroom.paymybuddy.configuration;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.repository.UserRepository;

/**
 * Service personnalisé pour la gestion des détails utilisateur dans le cadre de l'authentification.
 * Implémente l'interface {@link UserDetailsService} pour fournir les détails utilisateur
 * nécessaires à Spring Security.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * Logger pour enregistrer les événements liés à l'authentification.
     */
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    /**
     * Référentiel utilisateur pour accéder aux données des utilisateurs.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Charge les détails utilisateur en fonction du nom d'utilisateur (email).
     *
     * @param username Le nom d'utilisateur (email) utilisé pour l'authentification.
     * @return Les détails utilisateur nécessaires à l'authentification.
     * @throws UsernameNotFoundException Si l'utilisateur n'est pas trouvé.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Tentative de connexion pour l'utilisateur: {}", username);

        // Recherche par email (utilisé comme username dans PayMyBuddy)
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    logger.warn("Utilisateur non trouvé: {}", username);
                    return new UsernameNotFoundException("Utilisateur non trouvé: " + username);
                });

        logger.debug("Utilisateur trouvé: {} (ID: {})", user.getEmail(), user.getId());

        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            getGrantedAuthorities(user.getRole())
        );
    }

    /**
     * Génère une liste d'autorités accordées à l'utilisateur en fonction de son rôle.
     *
     * @param role Le rôle de l'utilisateur.
     * @return Une liste d'autorités accordées.
     */
    private List<GrantedAuthority> getGrantedAuthorities(String role) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        return authorities;
    }
}