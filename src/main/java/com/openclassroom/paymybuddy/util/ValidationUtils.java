package com.openclassroom.paymybuddy.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Classe utilitaire pour les validations communes.
 * Centralise les logiques de validation pour éviter la duplication de code.
 */
@Component
public class ValidationUtils {

    /**
     * Pattern pour la validation des noms d'utilisateur.
     * Le nom d'utilisateur ne peut contenir que des lettres et des chiffres.
     */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");

    /**
     * Pattern pour la validation des emails.
     * Utilise un pattern simple mais efficace pour la validation d'email.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    /**
     * Valide un mot de passe selon les critères définis.
     *
     * @param password Le mot de passe à valider.
     * @return true si le mot de passe est valide, false sinon.
     */
    public boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        
        // Vérifie la longueur (au moins 8 caractères)
        if (password.length() < 8) {
            return false;
        }
        
        // Vérifie qu'il contient au moins une lettre et un chiffre
        boolean hasLetter = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            }
            if (Character.isDigit(c)) {
                hasDigit = true;
            }
            if (hasLetter && hasDigit) {
                break;
            }
        }
        
        return hasLetter && hasDigit;
    }

    /**
     * Valide un nom d'utilisateur selon les critères définis.
     *
     * @param username Le nom d'utilisateur à valider.
     * @return true si le nom d'utilisateur est valide, false sinon.
     */
    public boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        // Vérifie la longueur (entre 3 et 20 caractères)
        if (username.length() < 3 || username.length() > 20) {
            return false;
        }
        
        // Vérifie qu'il ne contient que des lettres et des chiffres
        return USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Valide un email selon les critères définis.
     *
     * @param email L'email à valider.
     * @return true si l'email est valide, false sinon.
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Vérifie si deux chaînes de caractères sont identiques.
     *
     * @param first La première chaîne.
     * @param second La seconde chaîne.
     * @return true si les chaînes sont identiques, false sinon.
     */
    public boolean areEqual(String first, String second) {
        if (first == null && second == null) {
            return true;
        }
        
        return first != null && first.equals(second);
    }
}
