package com.openclassroom.paymybuddy.controller;

import com.openclassroom.paymybuddy.dto.RegisterDTO;
import com.openclassroom.paymybuddy.model.User;
import com.openclassroom.paymybuddy.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Contrôleur pour gérer les opérations liées à l'inscription des utilisateurs.
 */
@Controller
public class RegisterController {

    /**
     * Logger pour enregistrer les événements liés à l'inscription.
     */
    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    /**
     * Service utilisateur pour gérer les opérations liées aux utilisateurs.
     */
    private final UserService userService;

    /**
     * Constructeur pour injecter le service utilisateur.
     *
     * @param userService Service utilisateur.
     */
    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Affiche le formulaire d'inscription.
     *
     * @param model Le modèle utilisé pour transmettre des données à la vue.
     * @return Le nom de la vue du formulaire d'inscription.
     */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        logger.info("Affichage de la page d'inscription");
        model.addAttribute("registerDTO", new RegisterDTO());
        return "register";
    }

    /**
     * Traite les données du formulaire d'inscription.
     *
     * @param registerDTO Les données du formulaire d'inscription.
     * @param bindingResult Les résultats de la validation des données.
     * @param redirectAttributes Les attributs pour transmettre des messages à la vue.
     * @param model Le modèle utilisé pour transmettre des données à la vue.
     * @return Une redirection vers la page de connexion en cas de succès ou vers le formulaire d'inscription en cas d'erreur.
     */
    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute RegisterDTO registerDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        logger.info("Tentative d'inscription reçue pour: {}", registerDTO.getEmail());

        // Vérifier les erreurs de validation
        if (bindingResult.hasErrors()) {
            logger.warn("Erreurs de validation lors de l'inscription: {}", bindingResult.getAllErrors());
            model.addAttribute("registerDTO", registerDTO);
            return "register";
        }

        try {
            User newUser = userService.registerUser(registerDTO);
            logger.info("Inscription réussie pour l'utilisateur: {}", newUser.getEmail());

            redirectAttributes.addFlashAttribute("successMessage", 
                "Inscription réussie ! Vous pouvez maintenant vous connecter.");
            return "redirect:/login";

        } catch (IllegalArgumentException e) {
            logger.warn("Erreur lors de l'inscription: {}", e.getMessage());
            model.addAttribute("registerDTO", registerDTO);
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'inscription", e);
            model.addAttribute("registerDTO", registerDTO);
            model.addAttribute("errorMessage", "Une erreur inattendue s'est produite. Veuillez réessayer.");
            return "register";
        }
    }
}
