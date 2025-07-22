package com.openclassroom.paymybuddy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO pour gérer les données du formulaire d'inscription.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {

    /**
     * Le nom d'utilisateur.
     */
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom d'utilisateur doit contenir entre 2 et 50 caractères")
    private String username;

    /**
     * L'email de l'utilisateur.
     */
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    /**
     * Le mot de passe de l'utilisateur.
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).*$", message = "Le mot de passe doit contenir au moins une lettre et un chiffre")
    private String password;

    /**
     * La confirmation du mot de passe.
     */
    @NotBlank(message = "La confirmation du mot de passe est obligatoire")
    private String confirmPassword;

    /**
     * Vérifie si les mots de passe correspondent.
     *
     * @return true si les mots de passe correspondent, false sinon.
     */
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }

    /**
     * Retourne une représentation textuelle de l'objet.
     *
     * @return Une chaîne de caractères représentant l'objet.
     */
    @Override
    public String toString() {
        return "RegisterDTO{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                ", confirmPassword='[PROTECTED]'" +
                '}';
    }
}
