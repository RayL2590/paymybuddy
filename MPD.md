# MPD (Modèle Physique de Données) - PayMyBuddy

## Vue d'ensemble

Le MPD de PayMyBuddy est composé de 3 tables principales qui gèrent les utilisateurs, leurs connexions et leurs transactions.

## Tables

### 1. APP_USER
Table principale contenant les informations des utilisateurs de l'application.

```sql
CREATE TABLE app_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    balance DECIMAL(10,2) NOT NULL DEFAULT 1000.00,
    role VARCHAR(50) NOT NULL DEFAULT 'USER'
);
```

**Colonnes :**
- `id` : Clé primaire auto-incrémentée
- `username` : Nom d'utilisateur unique (100 caractères max)
- `email` : Adresse email unique (255 caractères max)
- `password` : Mot de passe chiffré (255 caractères max)
- `balance` : Solde du compte (décimal 10,2) avec valeur par défaut 1000.00
- `role` : Rôle de l'utilisateur (50 caractères max) avec valeur par défaut 'USER'

**Contraintes :**
- Clé primaire : `id`
- Contraintes d'unicité : `username`, `email`
- Contraintes NOT NULL : `username`, `email`, `password`, `balance`, `role`

### 2. USER_CONNECTIONS
Table d'association gérant les relations d'amitié entre utilisateurs (relation many-to-many).

```sql
CREATE TABLE user_connections (
    user_id BIGINT NOT NULL,
    connection_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, connection_id),
    FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    FOREIGN KEY (connection_id) REFERENCES app_user(id) ON DELETE CASCADE
);
```

**Colonnes :**
- `user_id` : Référence vers l'utilisateur principal
- `connection_id` : Référence vers l'utilisateur connecté/ami

**Contraintes :**
- Clé primaire composite : `(user_id, connection_id)`
- Clé étrangère : `user_id` → `app_user(id)` avec CASCADE DELETE
- Clé étrangère : `connection_id` → `app_user(id)` avec CASCADE DELETE

### 3. TRANSACTION
Table des transactions financières entre utilisateurs.

```sql
CREATE TABLE transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    description VARCHAR(255),
    amount DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES app_user(id),
    FOREIGN KEY (receiver_id) REFERENCES app_user(id)
);
```

**Colonnes :**
- `id` : Clé primaire auto-incrémentée
- `sender_id` : Référence vers l'utilisateur expéditeur
- `receiver_id` : Référence vers l'utilisateur destinataire
- `description` : Description de la transaction (255 caractères max, optionnel)
- `amount` : Montant de la transaction (décimal 10,2)
- `created_at` : Date/heure de création avec valeur par défaut CURRENT_TIMESTAMP

**Contraintes :**
- Clé primaire : `id`
- Clé étrangère : `sender_id` → `app_user(id)`
- Clé étrangère : `receiver_id` → `app_user(id)`
- Contraintes NOT NULL : `sender_id`, `receiver_id`, `amount`

## Relations

### Relations entre les tables :

1. **APP_USER ↔ USER_CONNECTIONS** (1:N)
   - Un utilisateur peut avoir plusieurs connexions
   - Relation bidirectionnelle via `user_id` et `connection_id`

2. **APP_USER ↔ TRANSACTION** (1:N)
   - Un utilisateur peut être expéditeur de plusieurs transactions (`sender_id`)
   - Un utilisateur peut être destinataire de plusieurs transactions (`receiver_id`)

## Diagramme MPD

```
┌─────────────────────────────────────┐
│            TRANSACTION              │
├─────────────────────────────────────┤
│ PK  id: BIGINT                      │
│ FK  sender_id: BIGINT               │
│ FK  receiver_id: BIGINT             │
│     description: VARCHAR(255)       │
│     amount: DECIMAL(10,2)           │
│     created_at: TIMESTAMP           │
│ FK  sender_id → APP_USER(id)        │
│ FK  receiver_id → APP_USER(id)      │
└─────────────────────────────────────┘
                    │
                    │ 1:N
                    │
┌─────────────────────────────────────┐
│              APP_USER               │
├─────────────────────────────────────┤
│ PK  id: BIGINT                      │
│ UK  username: VARCHAR(100)          │
│ UK  email: VARCHAR(255)             │
│     password: VARCHAR(255)          │
│     balance: DECIMAL(10,2)          │
│     role: VARCHAR(50)               │
└─────────────────────────────────────┘
                    │
                    │ 1:N
                    │
┌─────────────────────────────────────┐
│          USER_CONNECTIONS           │
├─────────────────────────────────────┤
│ PK  user_id: BIGINT                 │
│ PK  connection_id: BIGINT           │
│ FK  user_id → APP_USER(id)          │
│ FK  connection_id → APP_USER(id)    │
└─────────────────────────────────────┘
```

## Correspondance avec les modèles Java

### User.java
- Correspond à la table `APP_USER`
- Utilise `@OneToMany` pour les relations avec `UserConnection`
- Annotations JPA : `@Entity`, `@Table(name = "app_user")`

### UserConnection.java
- Correspond à la table `USER_CONNECTIONS`
- Utilise `@IdClass(UserConnectionId.class)` pour la clé composite
- Relations `@ManyToOne` vers `User`

### Transaction.java
- Correspond à la table `TRANSACTION`
- Relations `@ManyToOne` vers `User` pour sender et receiver
- Utilise `@PrePersist` pour la gestion automatique de `created_at`

## Intégrité des données

### Contraintes d'intégrité :
1. **Intégrité d'entité** : Chaque table a une clé primaire
2. **Intégrité référentielle** : Les clés étrangères garantissent la cohérence
3. **Intégrité de domaine** : Types de données et contraintes NOT NULL
4. **Intégrité utilisateur** : Contraintes d'unicité sur username et email

### Règles de gestion :
1. Un utilisateur ne peut pas se connecter à lui-même
2. Les transactions doivent avoir un montant positif
3. Les utilisateurs connectés peuvent effectuer des transactions entre eux
4. Le solde ne peut pas être négatif (à implémenter au niveau applicatif)

## Indexation recommandée

```sql
-- Index sur les colonnes fréquemment utilisées
CREATE INDEX idx_user_username ON app_user(username);
CREATE INDEX idx_user_email ON app_user(email);
CREATE INDEX idx_transaction_sender ON transaction(sender_id);
CREATE INDEX idx_transaction_receiver ON transaction(receiver_id);
CREATE INDEX idx_transaction_created_at ON transaction(created_at);
```

## Données de test

Le script contient des données de test pour :
- 10 utilisateurs (alice, bob, charlie, diana, eve, frank, grace, henry, isabel, jack)
- Relations d'amitié entre ces utilisateurs
- 15 transactions d'exemple avec différents montants et descriptions