package com.openclassroom.paymybuddy.repository;

import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.model.UserConnection;
import com.openclassroom.paymybuddy.model.UserConnectionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository pour gérer les opérations de persistance des connexions entre utilisateurs.
 */
public interface UserConnectionRepository extends JpaRepository<UserConnection, UserConnectionId> {

    /**
     * Récupère les connexions d'un utilisateur donné.
     *
     * @param userId L'identifiant de l'utilisateur.
     * @return Une liste de connexions associées à l'utilisateur.
     */
    List<UserConnection> findByUserId(Long userId);

    /**
     * Vérifie si une connexion existe entre deux utilisateurs.
     *
     * @param user L'utilisateur principal.
     * @param connection L'utilisateur secondaire.
     * @return true si la connexion existe, false sinon.
     */
    boolean existsByUserAndConnection(User user, User connection);
}