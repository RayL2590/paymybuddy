package com.openclassroom.paymybuddy.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * DTO pour représenter une relation entre utilisateurs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelationDTO {

    /**
     * L'identifiant de l'utilisateur connecté (ami).
     */
    private Long id;

    /**
     * Le nom de l'utilisateur lié par cette relation.
     */
    private String name;

}