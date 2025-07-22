package com.openclassroom.paymybuddy.service;

import com.openclassroom.paymybuddy.dto.RelationDTO;
import com.openclassroom.paymybuddy.dto.TransferDTO;
import com.openclassroom.paymybuddy.model.Transaction;
import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.repository.TransactionRepository;
import com.openclassroom.paymybuddy.repository.UserConnectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.openclassroom.paymybuddy.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service pour gérer les transactions entre utilisateurs.
 */
@Service
public class TransactionService {

    /**
     * Repository pour accéder aux données des transactions.
     */
    private final TransactionRepository transactionRepository;

    /**
     * Repository pour accéder aux connexions entre utilisateurs.
     */
    private final UserConnectionRepository userConnectionRepository;

    /**
     * Repository pour accéder aux données des utilisateurs.
     */
    private final UserRepository userRepository;

    /**
     * Constructeur pour initialiser les repositories nécessaires.
     *
     * @param transactionRepository Repository des transactions
     * @param userConnectionRepository Repository des connexions entre utilisateurs
     * @param userRepository Repository des utilisateurs
     */
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
     * @param userId ID de l'utilisateur
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

    /**
     * Récupère les transactions associées à un utilisateur.
     *
     * @param userId ID de l'utilisateur
     * @return une liste des transactions de l'utilisateur
     */
    public List<Transaction> getTransactionsByUserId(Long userId) {
        return transactionRepository.findBySenderIdOrReceiverId(userId, userId);
    }

    /**
     * Crée une transaction de transfert entre deux utilisateurs.
     *
     * @param transferDTO DTO contenant les informations de transfert
     * @return la transaction créée
     */
    @Transactional
    public Transaction createTransfer(TransferDTO transferDTO) {
        Logger logger = LoggerFactory.getLogger(TransactionService.class);

        logger.info("Début de la transaction : {} envoie {}€ à {}",
                transferDTO.getSenderId(), transferDTO.getAmount(), transferDTO.getReceiverId());

        User sender = userRepository.findById(transferDTO.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("Expéditeur introuvable"));
        User receiver = userRepository.findById(transferDTO.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("Destinataire introuvable"));

        boolean isConnected = userConnectionRepository.existsByUserIdAndConnectionId(
        sender.getId(), receiver.getId());

        if (!isConnected) {
            throw new IllegalArgumentException("Vous ne pouvez envoyer de l'argent qu'à vos connexions");
        }


        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Vous ne pouvez pas effectuer un transfert vers vous-même");
        }

        if (sender.getBalance().compareTo(transferDTO.getAmount()) < 0) {
            logger.warn("Solde insuffisant pour l'utilisateur {}", sender.getUsername());
            throw new IllegalArgumentException("Balance insuffisante pour effectuer la transaction");
        }

        sender.setBalance(sender.getBalance().subtract(transferDTO.getAmount()));
        logger.info("Nouveau solde de {} : {}€", sender.getUsername(), sender.getBalance());

        receiver.setBalance(receiver.getBalance().add(transferDTO.getAmount()));
        logger.info("Nouveau solde de {} : {}€", receiver.getUsername(), receiver.getBalance());

        userRepository.save(sender);
        userRepository.save(receiver);

        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(transferDTO.getAmount())
                .description(transferDTO.getDescription())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        logger.info("Transaction enregistrée avec l'id {}", savedTransaction.getId());

        return savedTransaction;
    }

}