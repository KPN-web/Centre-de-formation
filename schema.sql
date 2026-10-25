-- Schema de la base de donnees du projet Centre de formation
-- Structure uniquement : aucune donnee personnelle.
-- Import : mysql -u root < schema.sql

CREATE DATABASE IF NOT EXISTS `agence_form` CHARACTER SET utf8mb4;
USE `agence_form`;
SET FOREIGN_KEY_CHECKS = 0;

-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1:3306
-- Généré le : dim. 25 oct. 2026 à 18:57
-- Version du serveur : 9.1.0
-- Version de PHP : 8.3.14

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `agence_form`
--

-- --------------------------------------------------------

--
-- Structure de la table `administrateur`
--

DROP TABLE IF EXISTS `administrateur`;
CREATE TABLE IF NOT EXISTS `administrateur` (
  `idUtilisateur` int NOT NULL,
  PRIMARY KEY (`idUtilisateur`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `administrateur`
--


-- --------------------------------------------------------

--
-- Structure de la table `apprenant`
--

DROP TABLE IF EXISTS `apprenant`;
CREATE TABLE IF NOT EXISTS `apprenant` (
  `idUtilisateur` int NOT NULL,
  `matricule` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `telephone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `adresse` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `dateInscription` date NOT NULL,
  `idEntreprise` int DEFAULT NULL,
  PRIMARY KEY (`idUtilisateur`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `apprenant`
--


-- --------------------------------------------------------

--
-- Structure de la table `certification`
--

DROP TABLE IF EXISTS `certification`;
CREATE TABLE IF NOT EXISTS `certification` (
  `idCertification` int NOT NULL AUTO_INCREMENT,
  `typeCertificat` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `dateEmission` date NOT NULL,
  `fichierPDF` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `idApprenant` int DEFAULT NULL,
  `idSession` int DEFAULT NULL,
  PRIMARY KEY (`idCertification`),
  KEY `fk_certif_apprenant` (`idApprenant`),
  KEY `fk_certif_session` (`idSession`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `certification`
--


-- --------------------------------------------------------

--
-- Structure de la table `entreprise`
--

DROP TABLE IF EXISTS `entreprise`;
CREATE TABLE IF NOT EXISTS `entreprise` (
  `idEntreprise` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(150) NOT NULL,
  `adresse` varchar(255) DEFAULT NULL,
  `telephone` varchar(50) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `contactReferent` varchar(150) DEFAULT NULL,
  PRIMARY KEY (`idEntreprise`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `entreprise`
--


-- --------------------------------------------------------

--
-- Structure de la table `entreprisecliente`
--

DROP TABLE IF EXISTS `entreprisecliente`;
CREATE TABLE IF NOT EXISTS `entreprisecliente` (
  `idUtilisateur` int NOT NULL,
  `nomEntreprise` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `adresseEntreprise` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `telephoneEntreprise` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`idUtilisateur`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `formateur`
--

DROP TABLE IF EXISTS `formateur`;
CREATE TABLE IF NOT EXISTS `formateur` (
  `idUtilisateur` int NOT NULL,
  `specialite` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cv` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `tauxHoraire` double NOT NULL DEFAULT '0',
  `disponibilite` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`idUtilisateur`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `formateur`
--


-- --------------------------------------------------------

--
-- Structure de la table `inscription`
--

DROP TABLE IF EXISTS `inscription`;
CREATE TABLE IF NOT EXISTS `inscription` (
  `idInscription` int NOT NULL AUTO_INCREMENT,
  `dateInscription` date NOT NULL,
  `statut` varchar(25) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'EN_ATTENTE',
  `validationAdministrative` tinyint(1) NOT NULL DEFAULT '0',
  `idApprenant` int DEFAULT NULL,
  `idSession` int DEFAULT NULL,
  `resultSession` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ADMIS, AJOURNE ou NULL',
  PRIMARY KEY (`idInscription`),
  KEY `fk_inscription_apprenant` (`idApprenant`),
  KEY `fk_inscription_session` (`idSession`),
  KEY `idx_inscription_result` (`resultSession`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `inscription`
--


-- --------------------------------------------------------

--
-- Structure de la table `module`
--

DROP TABLE IF EXISTS `module`;
CREATE TABLE IF NOT EXISTS `module` (
  `idModule` int NOT NULL AUTO_INCREMENT,
  `titre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `typeContenu` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `support` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `idProgramme` int DEFAULT NULL,
  PRIMARY KEY (`idModule`),
  KEY `fk_module_programme` (`idProgramme`)
) ENGINE=InnoDB AUTO_INCREMENT=761 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `module`
--


-- --------------------------------------------------------

--
-- Structure de la table `module_formateur`
--

DROP TABLE IF EXISTS `module_formateur`;
CREATE TABLE IF NOT EXISTS `module_formateur` (
  `idModule` int NOT NULL,
  `idFormateur` int NOT NULL,
  PRIMARY KEY (`idModule`,`idFormateur`),
  KEY `idFormateur` (`idFormateur`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `module_formateur`
--


-- --------------------------------------------------------

--
-- Structure de la table `paiement`
--

DROP TABLE IF EXISTS `paiement`;
CREATE TABLE IF NOT EXISTS `paiement` (
  `idPaiement` int NOT NULL AUTO_INCREMENT,
  `montant` double NOT NULL,
  `datePaiement` date NOT NULL,
  `modePaiement` varchar(25) COLLATE utf8mb4_unicode_ci NOT NULL,
  `statutPaiement` varchar(25) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'EN_ATTENTE',
  `emailPayeur` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `idInscription` int DEFAULT NULL,
  `idApprenant` int DEFAULT NULL,
  `idFormateur` int DEFAULT NULL,
  `typePaiement` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'APPRENANT',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '',
  PRIMARY KEY (`idPaiement`),
  KEY `fk_paiement_inscription` (`idInscription`),
  KEY `idx_paiement_apprenant` (`idApprenant`),
  KEY `idx_paiement_formateur` (`idFormateur`),
  KEY `idx_paiement_type` (`typePaiement`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `paiement`
--


-- --------------------------------------------------------

--
-- Structure de la table `programme`
--

DROP TABLE IF EXISTS `programme`;
CREATE TABLE IF NOT EXISTS `programme` (
  `idProgramme` int NOT NULL AUTO_INCREMENT,
  `intitule` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `objectifs` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `prerequis` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `duree` int NOT NULL DEFAULT '0',
  `modalite` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tarif` double NOT NULL DEFAULT '0',
  `totalAPayer` double DEFAULT '0',
  `publie` tinyint(1) NOT NULL DEFAULT '0',
  `archive` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`idProgramme`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `programme`
--


-- --------------------------------------------------------

--
-- Structure de la table `responsable_pedagogique`
--

DROP TABLE IF EXISTS `responsable_pedagogique`;
CREATE TABLE IF NOT EXISTS `responsable_pedagogique` (
  `idUtilisateur` int NOT NULL,
  PRIMARY KEY (`idUtilisateur`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `service_financier`
--

DROP TABLE IF EXISTS `service_financier`;
CREATE TABLE IF NOT EXISTS `service_financier` (
  `idUtilisateur` int NOT NULL,
  PRIMARY KEY (`idUtilisateur`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `session`
--

DROP TABLE IF EXISTS `session`;
CREATE TABLE IF NOT EXISTS `session` (
  `idSession` int NOT NULL AUTO_INCREMENT,
  `dateDebut` date NOT NULL,
  `dateFin` date NOT NULL,
  `lieu` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `capaciteMax` int NOT NULL DEFAULT '0',
  `statut` varchar(25) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PLANIFIEE',
  `idProgramme` int DEFAULT NULL,
  `idFormateur` int DEFAULT NULL,
  PRIMARY KEY (`idSession`),
  KEY `fk_session_programme` (`idProgramme`),
  KEY `fk_session_formateur` (`idFormateur`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `session`
--


-- --------------------------------------------------------

--
-- Structure de la table `suivipedagogique`
--

DROP TABLE IF EXISTS `suivipedagogique`;
CREATE TABLE IF NOT EXISTS `suivipedagogique` (
  `idSuivi` int NOT NULL AUTO_INCREMENT,
  `presence` tinyint(1) NOT NULL DEFAULT '0',
  `note` double NOT NULL DEFAULT '0',
  `feedback` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `idApprenant` int DEFAULT NULL,
  `idSession` int DEFAULT NULL,
  `statutSuivi` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'En cours',
  `avisFormateur` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`idSuivi`),
  KEY `fk_suivi_apprenant` (`idApprenant`),
  KEY `fk_suivi_session` (`idSession`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `suivipedagogique`
--


-- --------------------------------------------------------

--
-- Structure de la table `utilisateurs`
--

DROP TABLE IF EXISTS `utilisateurs`;
CREATE TABLE IF NOT EXISTS `utilisateurs` (
  `idUtilisateur` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `prenom` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `motDePasse` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'UTILISATEUR',
  `dateCreation` date NOT NULL,
  `actif` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`idUtilisateur`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `utilisateurs`
--


--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `administrateur`
--
ALTER TABLE `administrateur`
  ADD CONSTRAINT `fk_admin_utilisateur` FOREIGN KEY (`idUtilisateur`) REFERENCES `utilisateurs` (`idUtilisateur`) ON DELETE CASCADE;

--
-- Contraintes pour la table `apprenant`
--
ALTER TABLE `apprenant`
  ADD CONSTRAINT `fk_apprenant_utilisateur` FOREIGN KEY (`idUtilisateur`) REFERENCES `utilisateurs` (`idUtilisateur`) ON DELETE CASCADE;

--
-- Contraintes pour la table `certification`
--
ALTER TABLE `certification`
  ADD CONSTRAINT `fk_certif_apprenant` FOREIGN KEY (`idApprenant`) REFERENCES `apprenant` (`idUtilisateur`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_certif_session` FOREIGN KEY (`idSession`) REFERENCES `session` (`idSession`) ON DELETE SET NULL;

--
-- Contraintes pour la table `entreprisecliente`
--
ALTER TABLE `entreprisecliente`
  ADD CONSTRAINT `fk_entreprise_utilisateur` FOREIGN KEY (`idUtilisateur`) REFERENCES `utilisateurs` (`idUtilisateur`) ON DELETE CASCADE;

--
-- Contraintes pour la table `formateur`
--
ALTER TABLE `formateur`
  ADD CONSTRAINT `fk_formateur_utilisateur` FOREIGN KEY (`idUtilisateur`) REFERENCES `utilisateurs` (`idUtilisateur`) ON DELETE CASCADE;

--
-- Contraintes pour la table `inscription`
--
ALTER TABLE `inscription`
  ADD CONSTRAINT `fk_inscription_apprenant` FOREIGN KEY (`idApprenant`) REFERENCES `apprenant` (`idUtilisateur`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_inscription_session` FOREIGN KEY (`idSession`) REFERENCES `session` (`idSession`) ON DELETE SET NULL;

--
-- Contraintes pour la table `module`
--
ALTER TABLE `module`
  ADD CONSTRAINT `fk_module_programme` FOREIGN KEY (`idProgramme`) REFERENCES `programme` (`idProgramme`) ON DELETE SET NULL;

--
-- Contraintes pour la table `paiement`
--
ALTER TABLE `paiement`
  ADD CONSTRAINT `fk_paiement_apprenant` FOREIGN KEY (`idApprenant`) REFERENCES `utilisateurs` (`idUtilisateur`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_paiement_formateur` FOREIGN KEY (`idFormateur`) REFERENCES `utilisateurs` (`idUtilisateur`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_paiement_inscription` FOREIGN KEY (`idInscription`) REFERENCES `inscription` (`idInscription`) ON DELETE SET NULL;

--
-- Contraintes pour la table `responsable_pedagogique`
--
ALTER TABLE `responsable_pedagogique`
  ADD CONSTRAINT `fk_resp_utilisateur` FOREIGN KEY (`idUtilisateur`) REFERENCES `utilisateurs` (`idUtilisateur`) ON DELETE CASCADE;

--
-- Contraintes pour la table `service_financier`
--
ALTER TABLE `service_financier`
  ADD CONSTRAINT `fk_sf_utilisateur` FOREIGN KEY (`idUtilisateur`) REFERENCES `utilisateurs` (`idUtilisateur`) ON DELETE CASCADE;

--
-- Contraintes pour la table `session`
--
ALTER TABLE `session`
  ADD CONSTRAINT `fk_session_formateur` FOREIGN KEY (`idFormateur`) REFERENCES `formateur` (`idUtilisateur`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_session_programme` FOREIGN KEY (`idProgramme`) REFERENCES `programme` (`idProgramme`) ON DELETE SET NULL;

--
-- Contraintes pour la table `suivipedagogique`
--
ALTER TABLE `suivipedagogique`
  ADD CONSTRAINT `fk_suivi_apprenant` FOREIGN KEY (`idApprenant`) REFERENCES `apprenant` (`idUtilisateur`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_suivi_session` FOREIGN KEY (`idSession`) REFERENCES `session` (`idSession`) ON DELETE SET NULL;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

SET FOREIGN_KEY_CHECKS = 1;

-- Compte administrateur de demonstration (fictif) : admin@example.com / admin123
-- A supprimer ou modifier avant tout usage reel.
INSERT INTO `utilisateurs` (`idUtilisateur`, `nom`, `prenom`, `email`, `motDePasse`, `role`, `dateCreation`, `actif`) VALUES
(1, 'Admin', 'Demo', 'admin@example.com', 'admin123', 'ADMIN', CURDATE(), 1);
INSERT INTO `administrateur` (`idUtilisateur`) VALUES (1);
