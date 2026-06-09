# Explication des Tests Unitaires

Dans le cadre du projet Ratatouille, trois tests unitaires ont été implémentés pour valider la robustesse de la logique métier (le `ProductRepository`).
Ces tests utilisent les librairies **JUnit 4** pour l'exécution, **MockK** pour simuler les dépendances (API et Base de données), et **Kotlinx Coroutines Test** pour tester de manière synchrone les fonctions suspendues (`suspend fun`).

Les tests sont situés dans le fichier `app/src/test/java/com/ratacorp/ratatouille/ProductRepositoryTest.kt`.

## 1. Test du mode Hors-ligne (US 7)
**Nom du test :** `getProduct returns local product if API fails`

**Objectif :** Vérifier que l'application est capable de fournir les données d'un produit même sans connexion internet, en se basant sur le cache.
**Fonctionnement :**
- On simule (Arrange) la présence d'un produit dans la base de données locale (Room) via le Mock du DAO.
- On force l'API réseau à échouer en jetant une `UnknownHostException` (qui simule une perte de réseau).
- On appelle (Act) la méthode `getProduct`.
- On vérifie (Assert) que le résultat est un succès, que le nom du produit correspond bien aux données locales, et surtout que l'attribut `isOffline` a bien été basculé à `true` pour afficher l'indicateur visuel à l'utilisateur.

## 2. Test du message d'erreur personnalisé
**Nom du test :** `getProduct returns 404 message when product is unknown online and offline`

**Objectif :** S'assurer que l'utilisateur reçoit un message compréhensible lorsqu'il scanne un code-barres qui n'existe pas.
**Fonctionnement :**
- On simule une base de données locale vide pour ce code.
- On simule une réponse de l'API (Retrofit) jetant une `HttpException` avec le code HTTP `404`.
- On vérifie que la fonction capte bien l'erreur et retourne un `Result.failure` contenant exactement le texte "Produit non trouvé", et non pas l'erreur technique brute.

## 3. Test de préservation des Favoris lors d'une suppression (US 5)
**Nom du test :** `deleteProduct resets scanDate to 0 if product is favorite`

**Objectif :** Valider la règle métier stipulant que supprimer un produit de l'historique ne doit pas le supprimer des favoris.
**Fonctionnement :**
- On crée une instance de `Product` où l'attribut `isFavorite` est défini sur `true`.
- On appelle la méthode `deleteProduct` du repository.
- À l'aide de MockK, on vérifie (`coVerify`) que la méthode appelée sur la base de données n'est **pas** `deleteProduct` (qui effacerait la ligne), mais bien `updateScanDate` avec la valeur `0L`. Cela garantit que le produit disparaît de l'historique (qui filtre les dates > 0) mais reste dans la table pour l'écran Favoris.
