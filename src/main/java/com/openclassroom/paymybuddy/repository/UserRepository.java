package com.openclassroom.paymybuddy.repository;

import com.openclassroom.paymybuddy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour gérer les opérations de persistance des utilisateurs.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Récupère un utilisateur par son email.
     *
     * @param email L'email de l'utilisateur.
     * @return Un utilisateur correspondant à l'email, ou un Optional vide si aucun utilisateur n'est trouvé.
     */
    Optional<User> findByEmail(String email);

    /**
     * Récupère un utilisateur par son nom d'utilisateur.
     *
     * @param username Le nom d'utilisateur.
     * @return Un utilisateur correspondant au nom d'utilisateur, ou un Optional vide si aucun utilisateur n'est trouvé.
     */
    Optional<User> findByUsername(String username);

    /**
     * Recherche des utilisateurs par email ou nom d'utilisateur.
     *
     * @param searchTerm Le terme de recherche.
     * @return Une liste d'utilisateurs correspondant au terme de recherche.
     */
    @Query("SELECT u FROM User u WHERE u.email LIKE %:searchTerm% OR u.username LIKE %:searchTerm%")
    List<User> searchUsersByEmailOrUsername(@Param("searchTerm") String searchTerm);

    /**
     * Recherche des utilisateurs par email ou nom d'utilisateur, en excluant l'utilisateur actuel.
     *
     * @param currentUserId L'identifiant de l'utilisateur actuel.
     * @param searchTerm Le terme de recherche.
     * @return Une liste d'utilisateurs correspondant au terme de recherche, excluant l'utilisateur actuel.
     */
    @Query("SELECT u FROM User u WHERE u.id != :currentUserId AND (u.email LIKE %:searchTerm% OR u.username LIKE %:searchTerm%)")
    List<User> searchUsersExcludingCurrent(@Param("currentUserId") Long currentUserId, @Param("searchTerm") String searchTerm);
}