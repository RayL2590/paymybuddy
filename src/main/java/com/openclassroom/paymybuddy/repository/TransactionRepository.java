package com.openclassroom.paymybuddy.repository;

import com.openclassroom.paymybuddy.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository pour gérer les opérations de persistance des transactions.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Récupère les transactions où l'utilisateur est soit l'expéditeur soit le destinataire.
     *
     * @param senderId L'identifiant de l'expéditeur.
     * @param receiverId L'identifiant du destinataire.
     * @return Une liste de transactions correspondant aux critères.
     */
    List<Transaction> findBySenderIdOrReceiverId(Long senderId, Long receiverId);
}
