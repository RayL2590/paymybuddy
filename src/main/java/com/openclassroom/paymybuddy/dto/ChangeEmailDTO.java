package com.openclassroom.paymybuddy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO pour gérer les données de modification d'email.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeEmailDTO {

    /**
     * Le nouvel email.
     */
    @NotBlank(message = "L'email est requis")
    @Email(message = "L'email doit avoir un format valide")
    private String newEmail;
}
