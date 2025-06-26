package com.openclassroom.paymybuddy.controller;

import com.openclassroom.paymybuddy.model.Transaction;
import com.openclassroom.paymybuddy.service.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ThymeleafController {

    private final TransactionService transactionService;

    public ThymeleafController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/")
    public String index(Model model) {
        Long userId = 1L;
        List<Transaction> transactions = transactionService.getAllTransactions()
                .stream()
                .limit(10)
                .collect(Collectors.toList());

        model.addAttribute("transactions", transactions);
        model.addAttribute("relations", transactionService.getRelations(userId));
        return "index";
    }
}