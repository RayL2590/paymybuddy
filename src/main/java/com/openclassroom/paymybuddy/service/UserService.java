package com.openclassroom.paymybuddy.service;

import com.openclassroom.paymybuddy.dto.RegisterDTO;
import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.model.UserConnection;
import com.openclassroom.paymybuddy.repository.UserConnectionRepository;
import com.openclassroom.paymybuddy.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service pour gérer les utilisateurs et leurs connexions.
 */
@Service
public class UserService {

    /**
     * Logger pour enregistrer les informations de débogage.
     */
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * Repository pour accéder aux données des utilisateurs.
     */
    private final UserRepository userRepository;

    /**
     * Repository pour accéder aux connexions entre utilisateurs.
     */
    private final UserConnectionRepository userConnectionRepository;

    /**
     * Encodeur pour gérer les mots de passe des utilisateurs.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructeur pour initialiser les repositories nécessaires.
     *
     * @param userRepository Repository des utilisateurs
     * @param userConnectionRepository Repository des connexions entre utilisateurs
     * @param passwordEncoder Encodeur de mots de passe
     */
    public UserService(UserRepository userRepository, UserConnectionRepository userConnectionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userConnectionRepository = userConnectionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Récupère un utilisateur par son identifiant.
     *
     * @param id Identifiant de l'utilisateur
     * @return Un Optional contenant l'utilisateur si trouvé, sinon vide
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Ajoute une connexion entre deux utilisateurs.
     *
     * @param userId ID de l'utilisateur courant
     * @param targetEmail Email de l'utilisateur cible
     */
    public void addUserConnection(Long userId, String targetEmail) {
        logger.info("Tentative d'ajout d'une connexion - UserId: {}, Email cible: {}", userId, targetEmail);

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur courant non trouvé"));

        User targetUser = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur cible non trouvé"));

        if (userId.equals(targetUser.getId())) {
            throw new IllegalArgumentException("Vous ne pouvez pas vous ajouter vous-même");
        }

        // Vérifier si la connexion existe déjà
        boolean connectionExists = userConnectionRepository.existsByUserAndConnection(currentUser, targetUser);
        if (connectionExists) {
            throw new IllegalArgumentException("Cette connexion existe déjà");
        }

        UserConnection connection = new UserConnection(currentUser, targetUser);
        userConnectionRepository.save(connection);

        logger.info("Connexion ajoutée avec succès entre {} et {}", userId, targetUser.getId());
    }
    
    /**
     * Inscription d'un nouvel utilisateur
     *
     * @param registerDTO DTO contenant les informations d'inscription
     * @return L'utilisateur inscrit
     */
    public User registerUser(RegisterDTO registerDTO) {
        logger.info("Tentative d'inscription pour l'email: {}", registerDTO.getEmail());
        
        // Vérifier si l'email existe déjà
        if (userRepository.findByEmail(registerDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
        }
        
        // Vérifier que les mots de passe correspondent
        if (!registerDTO.isPasswordMatching()) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas");
        }
        
        // Créer le nouvel utilisateur
        User newUser = User.builder()
                .username(registerDTO.getUsername()) // Utilise getUsername() qui retourne firstName
                .email(registerDTO.getEmail())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .balance(BigDecimal.ZERO)
                .role("USER")
                .build();
        
        User savedUser = userRepository.save(newUser);
        logger.info("Utilisateur inscrit avec succès - ID: {}, Email: {}", savedUser.getId(), savedUser.getEmail());
        
        return savedUser;
    }
    
    /**
     * Recherche d'utilisateurs par email ou nom d'utilisateur
     *
     * @param searchTerm Terme de recherche
     * @param currentUserId ID de l'utilisateur courant
     * @return Une liste des utilisateurs correspondant au terme de recherche
     */
    public List<User> searchUsers(String searchTerm, Long currentUserId) {
        logger.info("Recherche d'utilisateurs avec le terme: {} pour l'utilisateur ID: {}", searchTerm, currentUserId);
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return List.of();
        }
        
        List<User> users = userRepository.searchUsersExcludingCurrent(currentUserId, searchTerm.trim());
        logger.info("Trouvé {} utilisateurs correspondant au terme: {}", users.size(), searchTerm);
        
        return users;
    }
    
    /**
     * Recherche d'un utilisateur par email ou nom d'utilisateur
     *
     * @param identifier Identifiant (email ou nom d'utilisateur)
     * @return Un Optional contenant l'utilisateur si trouvé, sinon vide
     */
    public Optional<User> findUserByEmailOrUsername(String identifier) {
        logger.info("Recherche d'utilisateur avec l'identifiant: {}", identifier);
        
        // Essayer d'abord par email
        Optional<User> userByEmail = userRepository.findByEmail(identifier);
        if (userByEmail.isPresent()) {
            logger.info("Utilisateur trouvé par email: {}", identifier);
            return userByEmail;
        }
        
        // Ensuite par nom d'utilisateur
        Optional<User> userByUsername = userRepository.findByUsername(identifier);
        if (userByUsername.isPresent()) {
            logger.info("Utilisateur trouvé par nom d'utilisateur: {}", identifier);
        } else {
            logger.info("Aucun utilisateur trouvé avec l'identifiant: {}", identifier);
        }
        
        return userByUsername;
    }
    
    /**
     * Ajouter une connexion utilisateur avec recherche par email ou nom d'utilisateur
     *
     * @param userId ID de l'utilisateur courant
     * @param identifier Identifiant de l'utilisateur cible
     */
    public void addUserConnectionByIdentifier(Long userId, String identifier) {
        logger.info("Tentative d'ajout d'une connexion - UserId: {}, Identifiant: {}", userId, identifier);

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur courant non trouvé"));

        User targetUser = findUserByEmailOrUsername(identifier)
                .orElseThrow(() -> new EntityNotFoundException("Cet utilisateur n'existe pas"));

        if (userId.equals(targetUser.getId())) {
            throw new IllegalArgumentException("Vous ne pouvez pas vous ajouter vous-même");
        }

        // Vérifier si la connexion existe déjà
        boolean connectionExists = userConnectionRepository.existsByUserAndConnection(currentUser, targetUser);
        if (connectionExists) {
            throw new IllegalArgumentException("Cette connexion existe déjà");
        }

        UserConnection connection = new UserConnection(currentUser, targetUser);
        userConnectionRepository.save(connection);

        logger.info("Connexion ajoutée avec succès entre {} et {}", userId, targetUser.getId());
    }
    
    /**
     * Modifier la balance d'un utilisateur
     *
     * @param userId ID de l'utilisateur
     * @param newBalance Nouveau montant de la balance
     */
    public void updateUserBalance(Long userId, BigDecimal newBalance) {
        logger.info("Tentative de modification de balance - UserId: {}, Nouveau montant: {}", userId, newBalance);
        
        // Validation des limites
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La balance ne peut pas être inférieure à 0€");
        }
        
        if (newBalance.compareTo(BigDecimal.valueOf(10000)) > 0) {
            throw new IllegalArgumentException("La balance ne peut pas être supérieure à 10 000€");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));
        
        BigDecimal oldBalance = user.getBalance();
        user.setBalance(newBalance);
        userRepository.save(user);
        
        logger.info("Balance modifiée avec succès - UserId: {}, Ancien montant: {}, Nouveau montant: {}", 
                   userId, oldBalance, newBalance);
    }
    
    /**
     * Ajouter ou retirer de l'argent à la balance d'un utilisateur
     *
     * @param userId ID de l'utilisateur
     * @param amount Montant à ajuster
     * @param operation Type d'opération ("ADD" ou "SUBTRACT")
     */
    public void adjustUserBalance(Long userId, BigDecimal amount, String operation) {
        logger.info("Tentative d'ajustement de balance - UserId: {}, Montant: {}, Opération: {}", 
                   userId, amount, operation);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));
        
        BigDecimal currentBalance = user.getBalance();
        BigDecimal newBalance;
        
        if ("ADD".equals(operation)) {
            newBalance = currentBalance.add(amount);
        } else if ("SUBTRACT".equals(operation)) {
            newBalance = currentBalance.subtract(amount);
        } else {
            throw new IllegalArgumentException("Opération invalide. Utilisez 'ADD' ou 'SUBTRACT'");
        }
        
        // Utiliser la méthode de validation existante
        updateUserBalance(userId, newBalance);
        
        logger.info("Balance ajustée avec succès - UserId: {}, Opération: {}, Montant: {}, Nouvelle balance: {}", 
                   userId, operation, amount, newBalance);
    }
    
    /**
     * Modifier le mot de passe d'un utilisateur
     *
     * @param userId ID de l'utilisateur
     * @param newPassword Nouveau mot de passe
     */
    public void changePassword(Long userId, String newPassword) {
        logger.info("Tentative de modification de mot de passe - UserId: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));
        
        // Encoder le nouveau mot de passe
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        
        userRepository.save(user);
        
        logger.info("Mot de passe modifié avec succès pour l'utilisateur ID: {}", userId);
    }
}
