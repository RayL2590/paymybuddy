package com.openclassroom.paymybuddy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO pour gérer les données de modification de nom d'utilisateur.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeUsernameDTO {

    /**
     * Le nouveau nom d'utilisateur.
     */
    @NotBlank(message = "Le nom d'utilisateur est requis")
    @Size(min = 3, max = 20, message = "Le nom d'utilisateur doit contenir entre 3 et 20 caractères")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Le nom d'utilisateur ne peut contenir que des lettres et des chiffres")
    private String newUsername;
}
