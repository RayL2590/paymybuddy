package com.openclassroom.paymybuddy.controller;

import com.openclassroom.paymybuddy.dto.RelationDTO;
import com.openclassroom.paymybuddy.dto.TransferDTO;
import com.openclassroom.paymybuddy.model.Transaction;
import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.service.UserService;
import com.openclassroom.paymybuddy.service.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user-transactions")
public class UserTransactionController {

    private static final Logger logger = LoggerFactory.getLogger(UserTransactionController.class);
    private final TransactionService transactionService;
    private final UserService userService;

    @Autowired
    public UserTransactionController(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public String getUserTransactions(@PathVariable Long userId, Model model) {
        List<Transaction> transactions = transactionService.getTransactionsByUserId(userId);

        // Obtenir l'utilisateur à partir des transactions
        if (!transactions.isEmpty()) {
            Transaction firstTransaction = transactions.get(0);
            User user = firstTransaction.getSender().getId().equals(userId) ?
                    firstTransaction.getSender() : firstTransaction.getReceiver();
            model.addAttribute("user", user);

            // Ajouter la liste des relations
            List<RelationDTO> relations = transactionService.getRelations(userId);
            model.addAttribute("relations", relations);
        }

        model.addAttribute("transactions", transactions);
        return "user-transactions";
    }

    @GetMapping("/add")
    public String showAddRelationForm(Model model) {
        // Simulation de l'utilisateur connecté (à remplacer par Spring Security plus tard)
        Long userId = 1L;
        Optional<User> optionalUser = userService.getUserById(userId);
        if (optionalUser.isPresent()) {
            model.addAttribute("user", optionalUser.get());
        }
        return "add-relations";
    }


    @PostMapping("/{userId}/transfer")
    public String processTransfer(
            @PathVariable Long userId,
            @Valid TransferDTO transferDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Données invalides : " +
                    bindingResult.getAllErrors().stream()
                            .map(e -> e.getDefaultMessage())
                            .findFirst().orElse("Erreur de validation"));
            return "redirect:/user-transactions/" + userId;
        }

        try {
            Transaction transaction = transactionService.createTransfer(userId, transferDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Transfert effectué avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors du transfert : " + e.getMessage());
        }

        return "redirect:/user-transactions/" + userId;
    }

}