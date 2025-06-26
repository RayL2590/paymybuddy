package com.openclassroom.paymybuddy.dto;

import lombok.Data;
import java.math.BigDecimal;

import jakarta.validation.constraints.*;

@Data
public class TransferDTO {

    @NotNull(message = "Le destinataire est obligatoire")
    private Long receiverId;

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères")
    private String description;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à zéro")
    private BigDecimal amount;
}
