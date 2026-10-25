# Centre de formation

Ce projet est une application de bureau qui permet de gérer un centre de formation de A à Z. L'idée de départ : remplacer les fichiers Excel et les papiers par un outil unique, où tout est centralisé au même endroit.

Il a été réalisé dans le cadre d'un projet scolaire à l'ESTA (filière SIR, L2, 2025-2026), avec JavaFX pour l'interface et MySQL pour la base de données.

## Ce que l'application permet de faire

- Créer des programmes de formation et organiser leurs modules
- Planifier des sessions : dates, lieu, nombre de places, formateur responsable
- Gérer les apprenants, les formateurs et les différents comptes utilisateurs
- Suivre les inscriptions, les validations et les paiements
- Faire l'appel, saisir les notes et recueillir les retours des apprenants
- Générer des certificats, attestations et relevés de notes
- Consulter des statistiques : chiffre d'affaires, taux de réussite, remplissage des sessions...

## Organisation du code

- `Controller/` : la logique derrière chaque écran
- `Database/` : la connexion à la base de données et l'accès aux données
- `FX/` : les interfaces JavaFX
- `metier/` : les classes qui représentent les éléments du centre (formations, sessions, apprenants...)

## Pour l'essayer chez soi

1. Récupérer le projet :
