package com.openclassroom.paymybuddy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité représentant une transaction entre deux utilisateurs.
 */
@Setter
@Getter
@Entity
@Builder
@Table(name = "transaction")
public class Transaction {

    /**
     * L'identifiant unique de la transaction.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * L'utilisateur qui envoie l'argent.
     */
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonIgnore
    private User sender;

    /**
     * L'utilisateur qui reçoit l'argent.
     */
    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    @JsonIgnore
    private User receiver;

    /**
     * La description de la transaction.
     */
    @Column(length = 255)
    private String description;

    /**
     * Le montant de la transaction.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * La date et l'heure de création de la transaction.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Méthode exécutée avant la persistance pour définir la date de création si elle est absente.
     */
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    /**
     * Constructeur par défaut.
     */
    public Transaction() {
    }

    /**
     * Constructeur avec paramètres.
     *
     * @param id L'identifiant de la transaction.
     * @param sender L'utilisateur qui envoie l'argent.
     * @param receiver L'utilisateur qui reçoit l'argent.
     * @param description La description de la transaction.
     * @param amount Le montant de la transaction.
     * @param createdAt La date et l'heure de création de la transaction.
     */
    public Transaction(Long id, User sender, User receiver, String description, BigDecimal amount, LocalDateTime createdAt) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.description = description;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    /**
     * Retourne une chaîne représentant la relation entre l'expéditeur et le destinataire.
     *
     * @return Une chaîne au format "expéditeur -> destinataire".
     */
    public String getRelationName() {
        return sender.getUsername() + " -> " + receiver.getUsername();
    }
}