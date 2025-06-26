Voici comment tester chaque endpoint de `TransactionController` sur Postman :

### 1. **Récupérer toutes les transactions**
- **Méthode HTTP** : `GET`
- **URL** : `http://localhost:8080/api/transactions`
- **Body** : Aucun
- **Résultat attendu** : Liste des transactions au format JSON.

---

### 2. **Récupérer une transaction par son identifiant**
- **Méthode HTTP** : `GET`
- **URL** : `http://localhost:8080/api/transactions/{id}`
- Remplacez `{id}` par l'identifiant de la transaction (exemple : `1`).
- **Body** : Aucun
- **Résultat attendu** : Détails de la transaction au format JSON ou une réponse `404` si elle n'existe pas.

---

### 3. **Créer une nouvelle transaction**
- **Méthode HTTP** : `POST`
- **URL** : `http://localhost:8080/api/transactions`
- **Body** : JSON
  ```json
  {
    "sender": {
      "id": 1
    },
    "receiver": {
      "id": 2
    },
    "description": "Paiement pour le déjeuner",
    "amount": 15.50
  }
  ```
- **Résultat attendu** : La transaction créée au format JSON.

---

### 4. **Mettre à jour une transaction existante**
- **Méthode HTTP** : `PUT`
- **URL** : `http://localhost:8080/api/transactions/{id}`
- Remplacez `{id}` par l'identifiant de la transaction à mettre à jour.
- **Body** : JSON
  ```json
  {
    "sender": {
      "id": 1
    },
    "receiver": {
      "id": 2
    },
    "description": "Paiement modifié",
    "amount": 20.00
  }
  ```
- **Résultat attendu** : La transaction mise à jour au format JSON ou une réponse `404` si elle n'existe pas.

---

### 5. **Supprimer une transaction par son identifiant**
- **Méthode HTTP** : `DELETE`
- **URL** : `http://localhost:8080/api/transactions/{id}`
- Remplacez `{id}` par l'identifiant de la transaction à supprimer.
- **Body** : Aucun
- **Résultat attendu** : Réponse `204 No Content` si la suppression est réussie ou `404` si la transaction n'existe pas.