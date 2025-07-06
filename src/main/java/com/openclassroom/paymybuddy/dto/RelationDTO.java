package com.openclassroom.paymybuddy.dto;

import lombok.Getter;

/**
 * DTO pour représenter une relation entre utilisateurs.
 */
@Getter
public class RelationDTO {

    /**
     * L'identifiant de la relation.
     */
    private Long id;

    /**
     * Le nom de l'utilisateur lié par cette relation.
     */
    private String name;

    /**
     * Constructeur avec paramètres.
     *
     * @param id L'identifiant de la relation.
     * @param name Le nom de l'utilisateur lié par cette relation.
     */
    public RelationDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

}