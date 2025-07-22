package com.openclassroom.paymybuddy.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Classe représentant l'identifiant composite pour une connexion entre deux utilisateurs.
 */
@Setter
@Getter
@EqualsAndHashCode
public class UserConnectionId implements Serializable {

    /**
     * L'identifiant de l'utilisateur principal.
     */
    private Long user;

    /**
     * L'identifiant de l'utilisateur secondaire.
     */
    private Long connection;

    /**
     * Constructeur par défaut.
     */
    public UserConnectionId() {
    }

    /**
     * Constructeur avec paramètres.
     *
     * @param user L'identifiant de l'utilisateur principal.
     * @param connection L'identifiant de l'utilisateur secondaire.
     */
    public UserConnectionId(Long user, Long connection) {
        this.user = user;
        this.connection = connection;
    }

}