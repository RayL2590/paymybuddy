package com.openclassroom.paymybuddy.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Entité représentant un utilisateur de l'application.
 */
@Setter
@Getter
@Builder
@ToString(exclude = {"connections", "connectedTo"})
@Entity
@Table(name = "app_user")
@AllArgsConstructor
@NoArgsConstructor
public class User {

    /**
     * L'identifiant unique de l'utilisateur.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Le nom d'utilisateur.
     */
    @Column(unique = true, nullable = false, length = 100)
    private String username;

    /**
     * L'email de l'utilisateur.
     */
    @Column(unique = true, nullable = false, length = 255)
    private String email;

    /**
     * Le mot de passe de l'utilisateur.
     */
    @Column(nullable = false, length = 255)
    private String password;

    /**
     * Le solde du compte de l'utilisateur.
     */
    @Column(nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * Le rôle de l'utilisateur pour Spring Security.
     */
    @Column(nullable = false, length = 50)
    @Builder.Default
    private String role = "USER";

    /**
     * Les connexions de l'utilisateur (relations où l'utilisateur est le principal).
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserConnection> connections;

    /**
     * Les connexions de l'utilisateur (relations où l'utilisateur est le secondaire).
     */
    @OneToMany(mappedBy = "connection", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserConnection> connectedTo;
}