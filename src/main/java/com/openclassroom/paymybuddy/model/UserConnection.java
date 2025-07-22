package com.openclassroom.paymybuddy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entité représentant une connexion entre deux utilisateurs.
 */
@Setter
@Getter
@Entity
@Table(name = "user_connections")
@IdClass(UserConnectionId.class)
public class UserConnection {

    /**
     * L'utilisateur principal dans la connexion.
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * L'utilisateur secondaire dans la connexion.
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connection_id", nullable = false)
    private User connection;

    /**
     * Constructeur par défaut.
     */
    public UserConnection() {
    }

    /**
     * Constructeur avec paramètres.
     *
     * @param user L'utilisateur principal.
     * @param connection L'utilisateur secondaire.
     */
    public UserConnection(User user, User connection) {
        this.user = user;
        this.connection = connection;
    }
}