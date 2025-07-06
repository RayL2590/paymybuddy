package com.openclassroom.paymybuddy.dto;

import lombok.Data;
import java.math.BigDecimal;

import jakarta.validation.constraints.*;

/**
 * DTO pour représenter les données d'un transfert d'argent entre utilisateurs.
 */
@Data
public class TransferDTO {

    /**
     * L'identifiant de l'expéditeur.
     */
    @NotNull(message = "L'expéditeur est obligatoire")
    private Long senderId;

    /**
     * L'identifiant du destinataire.
     */
    @NotNull(message = "Le destinataire est obligatoire")
    private Long receiverId;

    /**
     * La description du transfert.
     */
    @NotBlank(message = "La description est obligatoire")
    @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères")
    private String description;

    /**
     * Le montant du transfert.
     */
    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à zéro")
    private BigDecimal amount;

    /**
     * Retourne une représentation textuelle de l'objet.
     *
     * @return Une chaîne de caractères représentant l'objet.
     */
    @Override
    public String toString() {
        return String.format("TransferDTO{senderId=%d, receiverId=%d, description='%s', amount=%s}", 
                           senderId, receiverId, description, amount);
    }
}
