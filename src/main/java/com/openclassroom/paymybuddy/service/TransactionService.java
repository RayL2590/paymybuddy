package com.openclassroom.paymybuddy.service;

import com.openclassroom.paymybuddy.dto.RelationDTO;
import com.openclassroom.paymybuddy.dto.TransferDTO;
import com.openclassroom.paymybuddy.model.Transaction;
import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.repository.TransactionRepository;
import com.openclassroom.paymybuddy.repository.UserConnectionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.openclassroom.paymybuddy.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserConnectionRepository userConnectionRepository;
    private final UserRepository userRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            UserConnectionRepository userConnectionRepository,
            UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userConnectionRepository = userConnectionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Récupère toutes les relations (connexions entre utilisateurs).
     *
     * @return une liste des relations sous forme d'objets simplifiés
     */
    public List<RelationDTO> getRelations(Long userId) {
        return userConnectionRepository.findByUserId(userId).stream()
                .map(connection -> new RelationDTO(connection.getConnection().getId(), connection.getConnection().getUsername()))
                .collect(Collectors.toList());
    }

    /**
     * Récupère toutes les transactions.
     *
     * @return une liste de toutes les transactions
     */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /**
     * Récupère une transaction par son identifiant.
     *
     * @param id l'identifiant de la transaction
     * @return un Optional contenant la transaction si elle existe, sinon vide
     */
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    /**
     * Sauvegarde une transaction.
     *
     * @param transaction la transaction à sauvegarder
     * @return la transaction sauvegardée
     */
    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    /**
     * Supprime une transaction par son identifiant.
     *
     * @param id l'identifiant de la transaction à supprimer
     */
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }

    public List<Transaction> getTransactionsByUserId(Long userId) {
        return transactionRepository.findBySenderIdOrReceiverId(userId, userId);
    }

    @Transactional
    public Transaction createTransfer(Long senderId, TransferDTO transferDTO) {
        if (transferDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("Expéditeur non trouvé"));

        User receiver = userRepository.findById(transferDTO.getReceiverId())
                .orElseThrow(() -> new EntityNotFoundException("Destinataire non trouvé"));

        Transaction transaction = new Transaction();
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setDescription(transferDTO.getDescription());
        transaction.setAmount(transferDTO.getAmount());

        return transactionRepository.save(transaction);
    }


}