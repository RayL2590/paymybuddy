package com.openclassroom.paymybuddy.controller;

import com.openclassroom.paymybuddy.repository.UserRepository;
import com.openclassroom.paymybuddy.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user-relations")
public class UserRelationController {
    private static final Logger logger = LoggerFactory.getLogger(UserRelationController.class);

    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public UserRelationController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/add")
    public String showAddRelationForm(Model model) {
        Long simulatedUserId = 1L;
        userRepository.findById(simulatedUserId).ifPresent(user -> model.addAttribute("user", user));
        return "add-relations";
    }


    @PostMapping("/add")
    public String addRelation(
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {

        Long simulatedUserId = 1L; // à remplacer plus tard par Authentication

        try {
            userService.addUserConnection(simulatedUserId, email);
            redirectAttributes.addFlashAttribute("successMessage", "Contact ajouté avec succès");
            return "redirect:/user-transactions/" + simulatedUserId;
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur non trouvé avec cet email");
            return "redirect:/user-relations/add";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/user-relations/add";
        }
    }

}
