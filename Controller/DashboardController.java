package Controller;

import Database.*;
import FX.MainApp;
import metier.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.net.URL;

public class DashboardController {

    @FXML private BorderPane mainPane;
    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private StackPane contentArea;

    @FXML private Label lblNbApprenants;
    @FXML private Label lblNbFormateurs;
    @FXML private Label lblNbSessions;
    @FXML private VBox  cardChiffreAffaires;
    @FXML private Label lblChiffreAffaires;

    @FXML private Button btnApprenants;
    @FXML private Button btnFormateurs;
    @FXML private Button btnProgrammes;
    @FXML private Button btnSessions;
    @FXML private Button btnInscriptions;
    @FXML private Button btnPaiements;
    @FXML private Button btnSuivi;
    @FXML private Button btnCertifications;
    @FXML private Button btnUtilisateurs;
    @FXML private Button btnStatistiques;
    @FXML private Button btnEntreprises;

    private ApprenantDAO apprenantDAO = new ApprenantDAO();
    private FormateurDAO formateurDAO = new FormateurDAO();
    private SessionDAO   sessionDAO   = new SessionDAO();
    private PaiementDAO  paiementDAO  = new PaiementDAO();

    @FXML
    public void initialize() {
        Utilisateur user = MainApp.getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Bienvenue, " + user.getNomComplet());
            roleLabel.setText("Rôle : " + user.getRole());
            appliquerPermissions(user.getRole());
            if ("ENTREPRISE".equals(user.getRole())) {
                loadView("entreprise.fxml");
                return;
            }
        }
        loadStats();
    }

    private void appliquerPermissions(String role) {
        setVisible(btnApprenants,    false);
        setVisible(btnFormateurs,    false);
        setVisible(btnProgrammes,    false);
        setVisible(btnSessions,      false);
        setVisible(btnInscriptions,  false);
        setVisible(btnPaiements,     false);
        setVisible(btnSuivi,         false);
        setVisible(btnCertifications,false);
        setVisible(btnUtilisateurs,  false);
        setVisible(btnStatistiques,  false);
        setVisible(btnEntreprises,   false);

        boolean voitArgent = role.equals("ADMIN") || role.equals("FINANCIER") || role.equals("SERVICE_FINANCIER");

        if (cardChiffreAffaires != null) {
            cardChiffreAffaires.setVisible(voitArgent);
            cardChiffreAffaires.setManaged(voitArgent);
        }

        switch (role) {
            case "ADMIN":
                setVisible(btnApprenants,    true);
                setVisible(btnFormateurs,    true);
                setVisible(btnProgrammes,    true);
                setVisible(btnSessions,      true);
                setVisible(btnInscriptions,  true);
                setVisible(btnPaiements,     true);
                setVisible(btnSuivi,         true);
                setVisible(btnCertifications,true);
                setVisible(btnUtilisateurs,  true);
                setVisible(btnStatistiques,  true);
                setVisible(btnEntreprises,   true);
                break;
            case "RESPONSABLE":
                setVisible(btnApprenants,    true);
                setVisible(btnFormateurs,    true);
                setVisible(btnProgrammes,    true);
                setVisible(btnSessions,      true);
                setVisible(btnInscriptions,  true);
                setVisible(btnSuivi,         true);
                setVisible(btnCertifications,true);
                setVisible(btnStatistiques,  true);
                setVisible(btnEntreprises,   true);
                break;
            case "FINANCIER":
            case "SERVICE_FINANCIER":
                setVisible(btnPaiements,    true);
                setVisible(btnStatistiques, true);
                setVisible(btnApprenants,   true);
                setVisible(btnFormateurs,   true);
                setVisible(btnEntreprises,  true);
                break;
            case "FORMATEUR":
                setVisible(btnSessions,   true);
                setVisible(btnSuivi,      true);
                setVisible(btnProgrammes, true);
                break;
            case "APPRENANT":
                setVisible(btnInscriptions,   true);
                setVisible(btnPaiements,      true);
                setVisible(btnCertifications, true);
                setVisible(btnProgrammes,     true);
                break;
            case "ENTREPRISE":
                setVisible(btnProgrammes,   true);
                setVisible(btnSessions,     true);
                setVisible(btnInscriptions, true);
                setVisible(btnStatistiques, true);
                break;
            default:
                setVisible(btnProgrammes, true);
                break;
        }
    }

    private void setVisible(Button btn, boolean visible) {
        if (btn != null) { btn.setVisible(visible); btn.setManaged(visible); }
    }

    private void loadStats() {
        try {
            if (lblNbApprenants != null) lblNbApprenants.setText(String.valueOf(apprenantDAO.findAll().size()));
            if (lblNbFormateurs != null) lblNbFormateurs.setText(String.valueOf(formateurDAO.findAll().size()));
            if (lblNbSessions   != null) lblNbSessions.setText(String.valueOf(sessionDAO.findAll().size()));
            if (lblChiffreAffaires != null) lblChiffreAffaires.setText(String.format("%,.0f FCFA", paiementDAO.getTotalChiffreAffaires()));
        } catch (Exception e) {
            System.err.println("Erreur chargement stats : " + e.getMessage());
        }
    }

    @FXML public void showApprenants()       { loadView("apprenants.fxml"); }
    @FXML public void showEntreprises()      { loadView("entreprise.fxml"); }
    @FXML public void showFormateurs()       { loadView("formateurs.fxml"); }
    @FXML public void showProgrammes()       { loadView("programmes.fxml"); }
    @FXML public void showSessions()         { loadView("sessions.fxml"); }
    @FXML public void showInscriptions()     { loadView("inscriptions.fxml"); }
    @FXML public void showPaiements()        { loadView("paiements.fxml"); }
    @FXML public void showSuiviPedagogique() { loadView("suivi_pedagogique.fxml"); }
    @FXML public void showCertifications()   { loadView("certifications.fxml"); }
    @FXML public void showUtilisateurs()     { loadView("utilisateurs.fxml"); }
    @FXML public void showStatistiques()     { loadView("statistiques.fxml"); }

    private void loadView(String fxmlName) {
        try {
            // Cherche le FXML dans le même package que MainApp (FX/)
            URL url = MainApp.class.getResource(fxmlName);

            // LOG de diagnostic — à supprimer une fois que ça marche
            if (url == null) {
                System.err.println("[ERREUR] Ressource introuvable : " + fxmlName);
                System.err.println("  Chemin cherché : " + MainApp.class.getPackageName().replace('.', '/') + "/" + fxmlName);
                System.err.println("  Classpath root URL : " + MainApp.class.getResource("/"));
                System.err.println("  → Vérifiez que le fichier est bien dans src/FX/ et que le projet a été rebuild (Project > Clean).");
                return;
            }

            System.out.println("[OK] Chargement : " + url);
            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();
            if (contentArea != null) contentArea.getChildren().setAll(view);

        } catch (Exception e) {
            System.err.println("Erreur chargement vue " + fxmlName + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML public void handleLogout() { MainApp.setCurrentUser(null); MainApp.loadLogin(); }
}