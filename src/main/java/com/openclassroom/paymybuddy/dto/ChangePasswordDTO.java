package com.openclassroom.paymybuddy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO pour gérer les données de modification de mot de passe.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordDTO {

    /**
     * Le mot de passe actuel.
     */
    @NotBlank(message = "Le mot de passe actuel est requis")
    private String currentPassword;

    /**
     * Le nouveau mot de passe.
     */
    @NotBlank(message = "Le nouveau mot de passe est requis")
    @Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).*$", message = "Le mot de passe doit contenir au moins une lettre et un chiffre")
    private String newPassword;

    /**
     * La confirmation du nouveau mot de passe.
     */
    @NotBlank(message = "La confirmation du nouveau mot de passe est requise")
    private String confirmPassword;

    /**
     * Vérifie si les nouveaux mots de passe correspondent.
     *
     * @return true si les mots de passe correspondent, false sinon.
     */
    public boolean isPasswordMatching() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }

    /**
     * Retourne une représentation textuelle de l'objet.
     *
     * @return Une chaîne de caractères représentant l'objet.
     */
    @Override
    public String toString() {
        return "ChangePasswordDTO{" +
                "currentPassword='[PROTECTED]'" +
                ", newPassword='[PROTECTED]'" +
                ", confirmPassword='[PROTECTED]'" +
                '}';
    }
}
