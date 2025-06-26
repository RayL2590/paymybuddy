# Pay My Buddy - Application de transfert d'argent

## Description
Pay My Buddy est une application web permettant aux utilisateurs de transférer de l'argent facilement entre amis, sans passer par des processus bancaires complexes.

## Fonctionnalités
- Inscription avec un email unique
- Connexion utilisateur
- Ajout d'amis via email
- Paiement entre utilisateurs avec une commission de 0,5%
- Affichage du solde utilisateur

## Technologies utilisées
- **Backend** : Java 17, Spring Boot 3
- **Base de données** : MySQL
- **Frontend** : Thymeleaf, HTML5, CSS3
- **Tests** : JUnit, Mockito

## 🛠️ Configuration de la base de données

### Étapes pour configurer MySQL :
1. Assurez-vous que MySQL est installé et en cours d'exécution sur votre machine.
2. Connectez-vous à MySQL via un terminal ou un client MySQL :
   ```bash
   mysql -u <votre_nom_utilisateur> -p
   ```
3. Créez une base de données nommée `paymybuddy` :
   ```sql
   CREATE DATABASE paymybuddy CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
   ```
4. Créez un user :
   ```sql
   CREATE USER 'payuser'@'localhost' IDENTIFIED BY 'motdepassetresfort';
   GRANT ALL PRIVILEGES ON paymybuddy.* TO 'payuser'@'localhost';
   FLUSH PRIVILEGES;
   ```
5. Accédez à la base de données nouvellement créée :
   ```sql
   USE paymybuddy;
   ```
6. Importez le script SQL situé dans `src/main/resources/script.sql` :
   ```sql
   SOURCE src/main/resources/script.sql;
   ```
7. Configuration des identifiants dans `.env` :
   Créez votre fichier `.env` à la racine de votre projet pour correspondre à vos identifiants MySQL :
   ```env
   DB_URL=jdbc:mysql://localhost:3306/paymybuddy?useSSL=false&serverTimezone=UTC
   DB_USERNAME=<votre_nom_utilisateur>
   DB_PASSWORD=<votre_mot_de_passe>
   ```

Une fois ces étapes terminées, la base de données sera prête à être utilisée par l'application.