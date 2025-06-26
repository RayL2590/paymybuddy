package com.openclassroom.paymybuddy.service;

import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.model.UserConnection;
import com.openclassroom.paymybuddy.repository.UserConnectionRepository;
import com.openclassroom.paymybuddy.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserConnectionRepository userConnectionRepository;

    @Autowired
    public UserService(UserRepository userRepository, UserConnectionRepository userConnectionRepository) {
        this.userRepository = userRepository;
        this.userConnectionRepository = userConnectionRepository;
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
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
}
