# Ratatouille 🐭

## Description
**Ratatouille** est une application Android avancée permettant de scanner des produits alimentaires, cosmétiques et animaliers afin d'obtenir des informations détaillées (Nutri-Score, ingrédients, alternatives plus saines).

Développée dans le cadre du module **Android Avancé**, l'application utilise les API d'**OpenFoodFacts**, **Open Beauty Facts** et **Open Pet Food Facts**.

## 🚀 Fonctionnalités (User Stories)

### MVP & Engagement
- **US 1 : Fiche produit (scan)** - Scan de code-barres et affichage des détails (Nutri-Score coloré, image, marque).
- **US 2 : Historique des scans** - Liste locale des produits scannés, triée par date.
- **US 3 : Recommandations** - Suggestion d'alternatives plus saines pour les produits notés D ou E.
- **US 4-6 : Favoris & Gestion** - Ajout aux favoris et suppression de l'historique (swipe to delete).

### Robustesse & Finition
- **US 7 : Mode hors-ligne** - Consultation des produits déjà scannés sans connexion internet.
- **US 8 : Synchronisation en arrière-plan** - Mise à jour quotidienne des données via WorkManager.
- **US 9 : Recherche paginée** - Exploration par catégorie avec chargement infini.
- **US 10 : Partage par Deep Link** - Partage de fiches produits via des URLs personnalisées (`myapp://product/{id}`).
- **US 11 : Widget d'accueil** - Affichage du dernier produit scanné directement sur l'écran d'accueil.
- **US 12 : Préférences** - Thème sombre et options de tri (DataStore).

## 👥 Auteurs
Réalisé par **@Julien-QTX** & **@NathKaden**
