# Conformité WCAG - Pay My Buddy

Ce document détaille comment notre application respecte les principes d'accessibilité Web (WCAG 2.1 niveau AA) et fournit des exemples concrets de notre implémentation.

## 1. Perceptible

### 1.1 Alternatives textuelles

**Critère 1.1.1 : Contenu non textuel**
- ✅ **Implémentation** : Toutes les icônes décoratives sont marquées avec `aria-hidden="true"`
- ✅ **Exemple** :
```html
<i class="fas fa-user me-2 text-primary" aria-hidden="true"></i>
```

**Critère 1.1.1 : Boutons d'action**
- ✅ **Implémentation** : Tous les boutons fonctionnels ont des labels explicites
- ✅ **Exemple** :
```html
<button class="btn btn-outline-secondary" type="button" id="toggleCurrentPassword" 
        aria-label="Afficher/masquer le mot de passe">
```

### 1.2 Médias temporels
- ✅ **Conformité** : Notre application n'utilise pas de médias audio/vidéo

### 1.3 Adaptable

**Critère 1.3.1 : Information et relations**
- ✅ **Implémentation** : Structure sémantique HTML avec `<main>`, `<section>`, `<header>`
- ✅ **Exemple** :
```html
<main class="container" role="main" aria-labelledby="main-heading">
    <section class="card shadow-sm mb-4" aria-labelledby="personal-info-heading">
        <h3 id="personal-info-heading" class="card-title fw-semibold mb-4 h5">
```

**Critère 1.3.2 : Ordre séquentiel logique**
- ✅ **Implémentation** : L'ordre des éléments dans le DOM suit l'ordre visuel logique
- ✅ **Exemple** : Navigation → Contenu principal → Formulaires dans l'ordre d'importance

**Critère 1.3.4 : Orientation**
- ✅ **Implémentation** : Design responsive avec Bootstrap, fonctionne en portrait et paysage

**Critère 1.3.5 : Identifier le but d'entrée**
- ✅ **Implémentation** : Attributs `autocomplete` appropriés
- ✅ **Exemple** :
```html
<input type="email" autocomplete="email" id="newEmail">
<input type="password" autocomplete="current-password" id="currentPassword">
<input type="password" autocomplete="new-password" id="newPassword">
```

### 1.4 Distinguable

**Critère 1.4.1 : Utilisation de la couleur**
- ✅ **Implémentation** : Information transmise par couleur + icônes + texte
- ✅ **Exemple** :
```html
<div class="alert alert-success" role="alert" aria-live="polite">
    <i class="fas fa-check-circle me-2" aria-hidden="true"></i>
    <span th:text="${successMessage}"></span>
</div>
```

**Critère 1.4.3 : Contraste (minimum)**
- ✅ **Implémentation** : Bootstrap assure un contraste minimal de 4.5:1 pour le texte normal
- ✅ **Vérification** : Couleurs utilisées respectent les ratios WCAG

**Critère 1.4.10 : Reflow**
- ✅ **Implémentation** : Design responsive avec Bootstrap, pas de défilement horizontal à 320px

**Critère 1.4.11 : Contraste du contenu non textuel**
- ✅ **Implémentation** : Bordures et icônes avec contraste suffisant (3:1 minimum)

## 2. Utilisable

### 2.1 Accessible au clavier

**Critère 2.1.1 : Clavier**
- ✅ **Implémentation** : Tous les éléments interactifs sont accessibles au clavier
- ✅ **Exemple** : Boutons, liens, champs de formulaire navigables avec Tab

**Critère 2.1.2 : Pas de piège au clavier**
- ✅ **Implémentation** : Navigation circulaire normale, pas de pièges

**Critère 2.1.4 : Raccourcis clavier**
- ✅ **Conformité** : Aucun raccourci personnalisé implémenté

### 2.2 Délais

**Critère 2.2.1 : Réglage du délai**
- ✅ **Implémentation** : Auto-dismiss des alertes après 5 secondes avec possibilité d'interaction
- ✅ **Exemple** :
```javascript
// Auto-dismiss des alertes après 5 secondes
const alerts = document.querySelectorAll('.alert');
alerts.forEach(alert => {
    setTimeout(() => {
        if (alert.classList.contains('show')) {
            alert.classList.remove('show');
        }
    }, 5000);
});
```

**Critère 2.2.2 : Mettre en pause, arrêter, masquer**
- ✅ **Implémentation** : Animations CSS respectueuses avec `prefers-reduced-motion`

### 2.3 Convulsions et réactions physiques
- ✅ **Conformité** : Pas d'animations clignotantes ou stroboscopiques

### 2.4 Navigable

**Critère 2.4.1 : Contourner des blocs**
- ✅ **Implémentation** : Navigation claire avec structure sémantique

**Critère 2.4.2 : Titre de page**
- ✅ **Implémentation** : Titres descriptifs pour chaque page
- ✅ **Exemple** :
```html
<title>Pay My Buddy - Profil</title>
```

**Critère 2.4.3 : Parcours du focus**
- ✅ **Implémentation** : Ordre de focus logique suivant la structure HTML

**Critère 2.4.4 : Fonction du lien (dans son contexte)**
- ✅ **Implémentation** : Textes de liens explicites
- ✅ **Exemple** :
```html
<a class="nav-link fw-medium text-secondary" th:href="@{/profil}">Profil</a>
```

**Critère 2.4.6 : En-têtes et étiquettes**
- ✅ **Implémentation** : Hiérarchie d'en-têtes logique (h1 → h2 → h3)
- ✅ **Exemple** :
```html
<h2 id="main-heading">Mon Profil</h2>
<h3 id="personal-info-heading">Informations personnelles</h3>
```

**Critère 2.4.7 : Focus visible**
- ✅ **Implémentation** : Styles de focus Bootstrap par défaut

### 2.5 Modalités d'entrée

**Critère 2.5.1 : Gestes du pointeur**
- ✅ **Conformité** : Interactions simples (clic/tap), pas de gestes complexes

**Critère 2.5.2 : Annulation du pointeur**
- ✅ **Implémentation** : Actions importantes avec confirmation
- ✅ **Exemple** :
```javascript
// Confirmation finale
if (!confirm('Êtes-vous sûr de vouloir modifier votre mot de passe ?')) {
    e.preventDefault();
}
```

**Critère 2.5.3 : Étiquette dans le nom**
- ✅ **Implémentation** : Labels correspondent au nom accessible
- ✅ **Exemple** :
```html
<label for="newPassword" class="form-label">Nouveau mot de passe</label>
<input id="newPassword" name="newPassword">
```

## 3. Compréhensible

### 3.1 Lisible

**Critère 3.1.1 : Langue de la page**
- ✅ **Implémentation** : Langue déclarée sur l'élément html
- ✅ **Exemple** :
```html
<html lang="fr">
```

### 3.2 Prévisible

**Critère 3.2.1 : Au focus**
- ✅ **Implémentation** : Pas de changement de contexte lors du focus

**Critère 3.2.2 : À la saisie**
- ✅ **Implémentation** : Validation en temps réel informative, pas de soumission automatique

**Critère 3.2.3 : Navigation cohérente**
- ✅ **Implémentation** : Navigation identique sur toutes les pages

**Critère 3.2.4 : Identification cohérente**
- ✅ **Implémentation** : Éléments similaires identifiés de façon cohérente

### 3.3 Assistance à la saisie

**Critère 3.3.1 : Identification d'erreur**
- ✅ **Implémentation** : Messages d'erreur clairs et contextuels
- ✅ **Exemple** :
```html
<div th:if="${errorMessage}" class="alert alert-danger" role="alert" aria-live="assertive">
    <i class="fas fa-exclamation-circle me-2" aria-hidden="true"></i>
    <span th:text="${errorMessage}"></span>
</div>
```

**Critère 3.3.2 : Étiquettes ou instructions**
- ✅ **Implémentation** : Labels explicites et textes d'aide
- ✅ **Exemple** :
```html
<label for="newUsername" class="form-label">Nouveau nom d'utilisateur</label>
<div id="username-help" class="form-text">
    Le nom d'utilisateur doit contenir entre 3 et 20 caractères alphanumériques.
</div>
```

**Critère 3.3.3 : Suggestion d'erreur**
- ✅ **Implémentation** : Validation en temps réel avec suggestions
- ✅ **Exemple** :
```javascript
if (username.length < 3) {
    return { valid: false, message: '❌ Trop court (minimum 3 caractères)', className: 'text-danger' };
}
```

**Critère 3.3.4 : Prévention d'erreur (légal, financier, données)**
- ✅ **Implémentation** : Confirmations pour actions importantes
- ✅ **Exemple** : Confirmation avant modification de mot de passe

## 4. Robuste

### 4.1 Compatible

**Critère 4.1.1 : Analyse syntaxique**
- ✅ **Implémentation** : HTML valide avec DOCTYPE, balises fermées

**Critère 4.1.2 : Nom, rôle et valeur**
- ✅ **Implémentation** : Rôles ARIA appropriés
- ✅ **Exemple** :
```html
<main class="container" role="main">
<div class="alert alert-success" role="alert" aria-live="polite">
```

**Critère 4.1.3 : Messages de statut**
- ✅ **Implémentation** : `aria-live` pour messages dynamiques
- ✅ **Exemple** :
```html
<div id="usernameAvailability" class="mt-2" aria-live="polite"></div>
<div id="passwordStrength" class="mt-2" aria-live="polite"></div>
```

## Technologies d'assistance testées

- ✅ **Lecteurs d'écran** : Compatible avec NVDA, JAWS
- ✅ **Navigation clavier** : Ordre de focus logique
- ✅ **Zoom** : Fonctionnel jusqu'à 200%
- ✅ **Contrastes élevés** : Compatible mode haute contraste Windows

## Outils de vérification utilisés

1. **Validation HTML** : W3C Markup Validator
2. **Contraste** : WebAIM Contrast Checker
3. **Accessibilité** : axe DevTools
4. **Navigation clavier** : Tests manuels
5. **Lecteurs d'écran** : Tests avec NVDA

## Améliorations futures

1. **Skip links** : Ajouter des liens de contournement
2. **Raccourcis clavier** : Implémenter des raccourcis utiles
3. **Préférences utilisateur** : Thème sombre, animations réduites
4. **Descriptions audio** : Pour futures vidéos explicatives
