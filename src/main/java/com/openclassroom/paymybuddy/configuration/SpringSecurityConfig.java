package com.openclassroom.paymybuddy.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité Spring Security pour l'application PayMyBuddy.
 * 
 * Cette classe configure l'authentification, l'autorisation, et la gestion des sessions
 * pour sécuriser l'application de transfert d'argent entre amis.
 * 
 * <p>Fonctionnalités configurées :</p>
 * <ul>
 *   <li>Authentification par email et mot de passe</li>
 *   <li>Autorisation basée sur les rôles (USER, ADMIN)</li>
 *   <li>Protection des endpoints sensibles</li>
 *   <li>Gestion des pages de connexion et déconnexion</li>
 *   <li>Encodage sécurisé des mots de passe avec BCrypt</li>
 * </ul>
 * 
 * <h3>Architecture de sécurité :</h3>
 * <p>L'application utilise une architecture de sécurité en couches :</p>
 * <ol>
 *   <li><strong>Couche Web :</strong> Filtrage des requêtes HTTP et gestion des sessions</li>
 *   <li><strong>Couche Authentification :</strong> Vérification des identifiants utilisateur</li>
 *   <li><strong>Couche Autorisation :</strong> Contrôle d'accès basé sur les rôles</li>
 *   <li><strong>Couche Données :</strong> Protection des informations sensibles en base</li>
 * </ol>
 * 
 * <h3>Niveaux d'accès :</h3>
 * <ul>
 *   <li><strong>Public :</strong> Pages d'accueil, connexion, inscription</li>
 *   <li><strong>Authentifié :</strong> Transactions, relations, profil utilisateur</li>
 *   <li><strong>Administrateur :</strong> Gestion système et utilisateurs</li>
 * </ul>
 * 
 * <h3>Sécurisation des mots de passe :</h3>
 * <p>Les mots de passe sont protégés par l'algorithme BCrypt qui offre :</p>
 * <ul>
 *   <li>Hachage irréversible avec salt automatique</li>
 *   <li>Résistance aux attaques par dictionnaire et force brute</li>
 *   <li>Coût de calcul adaptatif face à l'évolution technologique</li>
 * </ul>
 * 
 * @author PayMyBuddy Team
 * @version 1.0
 * @since 1.0
 * @see CustomUserDetailsService
 * @see org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 * @see org.springframework.security.web.SecurityFilterChain
 */
@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

    /**
     * Service personnalisé pour la gestion des détails utilisateur.
     * Utilisé pour charger les informations d'authentification depuis la base de données.
     */
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    /**
     * Configure la chaîne de filtres de sécurité pour l'application.
     * 
     * <p>Cette méthode définit :</p>
     * <ul>
     *   <li><strong>Autorisations HTTP :</strong>
     *     <ul>
     *       <li>Accès libre : pages publiques (/, /login, /register) et ressources statiques</li>
     *       <li>Accès authentifié : transactions et relations utilisateur</li>
     *       <li>Accès admin : fonctionnalités d'administration</li>
     *     </ul>
     *   </li>
     *   <li><strong>Authentification par formulaire :</strong>
     *     <ul>
     *       <li>Page de connexion personnalisée (/login)</li>
     *       <li>Utilisation de l'email comme identifiant</li>
     *       <li>Redirection vers /my-transactions après connexion réussie</li>
     *       <li>Gestion des erreurs de connexion</li>
     *     </ul>
     *   </li>
     *   <li><strong>Gestion de déconnexion :</strong>
     *     <ul>
     *       <li>URL de déconnexion : /logout</li>
     *       <li>Redirection vers la page de connexion après déconnexion</li>
     *     </ul>
     *   </li>
     * </ul>
     * 
     * @param http l'objet HttpSecurity pour configurer les règles de sécurité web
     * @return SecurityFilterChain la chaîne de filtres de sécurité configurée
     * @throws Exception si une erreur survient lors de la configuration
     * 
     * @see HttpSecurity
     * @see SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> {
                // Accès libre pour les pages publiques et ressources statiques
                auth.requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**").permitAll();
                // Accès authentifié requis pour les fonctionnalités utilisateur
                auth.requestMatchers("/user-transactions/**", "/user-relations/**", "/profil/**").authenticated();
                // Accès admin requis pour les fonctionnalités d'administration
                auth.requestMatchers("/admin/**").hasRole("ADMIN");
                // Toute autre requête nécessite une authentification
                auth.anyRequest().authenticated();
            })
            .formLogin(form -> form
                .loginPage("/login")                    // Page de connexion personnalisée
                .usernameParameter("email")             // Utilise l'email comme identifiant
                .defaultSuccessUrl("/my-transactions", true) // Redirection après connexion réussie
                .failureUrl("/login?error=true")        // Redirection en cas d'échec
                .permitAll()                            // Accès libre à la page de connexion
            )
            .logout(logout -> logout
                .logoutUrl("/logout")                   // URL pour déclencher la déconnexion
                .logoutSuccessUrl("/login?logout=true") // Redirection après déconnexion
                .permitAll()                            // Accès libre à la déconnexion
            )
            .build();
    }

    /**
     * Configure l'encodeur de mots de passe BCrypt.
     * 
     * <p>BCrypt est un algorithme de hachage adaptatif spécialement conçu pour les mots de passe.
     * Il intègre un "salt" automatique et permet d'ajuster le coût de calcul pour résister
     * aux attaques par force brute même avec l'évolution de la puissance de calcul.</p>
     * 
     * <p>Avantages de BCrypt :</p>
     * <ul>
     *   <li>Résistant aux attaques temporelles</li>
     *   <li>Salt automatique pour chaque mot de passe</li>
     *   <li>Coût de calcul ajustable</li>
     *   <li>Largement testé et approuvé par la communauté sécurité</li>
     * </ul>
     * 
     * @return BCryptPasswordEncoder l'encodeur de mots de passe configuré
     * 
     * @see BCryptPasswordEncoder
     * @see org.springframework.security.crypto.password.PasswordEncoder
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configure le gestionnaire d'authentification pour l'application.
     * 
     * <p>Cette méthode configure l'AuthenticationManager en définissant :</p>
     * <ul>
     *   <li><strong>Service de détails utilisateur :</strong> {@link CustomUserDetailsService}
     *       pour charger les informations utilisateur depuis la base de données</li>
     *   <li><strong>Encodeur de mot de passe :</strong> {@link BCryptPasswordEncoder}
     *       pour vérifier les mots de passe de manière sécurisée</li>
     * </ul>
     * 
     * <p>L'AuthenticationManager est le composant central de Spring Security
     * responsable de l'authentification des utilisateurs. Il coordonne les
     * différents fournisseurs d'authentification et détermine si un utilisateur
     * est authentifié ou non.</p>
     * 
     * @param http l'objet HttpSecurity pour accéder au contexte de sécurité
     * @return AuthenticationManager le gestionnaire d'authentification configuré
     * @throws Exception si une erreur survient lors de la configuration
     * 
     * @see AuthenticationManager
     * @see AuthenticationManagerBuilder
     * @see CustomUserDetailsService
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = 
            http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
            .userDetailsService(customUserDetailsService)    // Service de chargement des utilisateurs
            .passwordEncoder(passwordEncoder());             // Encodeur pour la vérification des mots de passe
        return authenticationManagerBuilder.build();
    }
}