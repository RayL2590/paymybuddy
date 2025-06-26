# 🧾 Projet : Pay My Buddy - Application de transfert d'argent

## 🎯 Objectif
Créer un prototype d'application web permettant aux utilisateurs d'envoyer et recevoir de l'argent facilement entre amis, sans passer par des processus bancaires lourds.

## 🛠️ Tâches principales (développeur backend)

### Partie 1 - Base de données
- [ ] Concevoir le Modèle Physique de Données (MPD)
- [ ] Implémenter la base de données relationnelle
- [ ] Créer les scripts SQL pour :
  - Tables
  - Contraintes
  - Séquences (si nécessaire)
- [ ] Prévoir des mécanismes de sauvegarde/restauration
- [ ] Remplir avec des données de test réalistes
- [ ] Ajouter le MPD dans le `README.md` du dépôt GitHub

### Partie 2 - Application Web
#### Back-end
- [ ] Implémenter la couche DAL (Data Access Layer)
- [ ] Utiliser Spring Boot 3 + Java 17
- [ ] Connexion sécurisée à la BDD (identifiants dans un fichier `.env` ou `application.properties`)
- [ ] Gérer les transactions (commit/rollback)
- [ ] Implémenter :
  - Inscription (email unique)
  - Connexion
  - Ajout d'amis par email
  - Paiement entre utilisateurs
- [ ] Prévoir une structure évolutive (ajout futur de facturation, virements bancaires, etc.)

#### Front-end
- [ ] Créer une interface web avec Thymeleaf
- [ ] S'inspirer des maquettes Figma fournies
- [ ] Utiliser Bootstrap (au choix)
- [ ] Se concentrer sur :
  - Accessibilité (normes WCAG)
  - Simplicité, ergonomie, navigabilité
- [ ] Responsive non obligatoire (prototype)

### Partie 3 - Tests
- [ ] Utiliser JUnit, Mockito, Spring Boot Test
- [ ] Écrire des tests unitaires et d'intégration pour :
  - Inscription / connexion
  - Transactions (paiement + rollback)
  - Ajout d'amis
- [ ] Prévoir des tests d'erreurs, cas limites et sécurité


## 🔐 Sécurité & accessibilité
- Sécuriser les données sensibles (chiffrement, stockage sécurisé)
- Suivre les normes WCAG pour l'accessibilité
- Protéger l'app contre les vulnérabilités courantes (injections, XSS, CSRF...)

## 🧰 Outils recommandés
- Base de données : MySQL
- Backend : Java 17, Spring Boot 3, Maven
- Frontend : Thymeleaf, HTML5, CSS3, Bootstrap
- Tests : JUnit, Mockito
- IDE : IntelliJ ou Eclipse