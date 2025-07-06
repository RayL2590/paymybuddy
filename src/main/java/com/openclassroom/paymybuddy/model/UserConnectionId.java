package com.openclassroom.paymybuddy.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * Classe représentant l'identifiant composite pour une connexion entre deux utilisateurs.
 */
@Setter
@Getter
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

    /**
     * Vérifie si deux objets UserConnectionId sont égaux.
     *
     * @param o L'objet à comparer.
     * @return true si les objets sont égaux, false sinon.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserConnectionId that)) return false;
        return Objects.equals(user, that.user) &&
                Objects.equals(connection, that.connection);
    }

    /**
     * Calcule le hashcode de l'objet.
     *
     * @return Le hashcode de l'objet.
     */
    @Override
    public int hashCode() {
        return Objects.hash(user, connection);
    }
}