package Controller;

import Database.FormateurDAO;
import Database.PaiementDAO;
import Database.ProgrammeDAO;
import Database.SessionDAO;
import Database.SuiviPedagogiqueDAO;
import FX.MainApp;
import metier.Formateur;
import metier.Paiement;
import metier.Programme;
import metier.Session;
import metier.SuiviPedagogique;
import metier.Utilisateur;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class FormateurController {

    // ── Table principale ────────────────────────────────────────────────
    @FXML private TableView<Formateur>         tableView;
    @FXML private TableColumn<Formateur, Integer> colId;
    @FXML private TableColumn<Formateur, String>  colNom;
    @FXML private TableColumn<Formateur, String>  colPrenom;
    @FXML private TableColumn<Formateur, String>  colEmail;
    @FXML private TableColumn<Formateur, String>  colSpecialite;
    @FXML private TableColumn<Formateur, Double>  colTauxHoraire;

    // ── Formulaire (ADMIN / RESPONSABLE uniquement) ──────────────────────
    @FXML private VBox      vboxFormulaire;
    @FXML private HBox      hboxBoutons;
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtMotDePasse;
    @FXML private TextField txtSpecialite;
    @FXML private TextField txtTauxHoraire;
    @FXML private TextField txtDisponibilite;
    @FXML private TextArea  txtCv;
    @FXML private Label     lblStatus;

    // ── Onglets profil / historique / calcul montant ─────────────────────
    @FXML private TabPane  tabPaneProfil;
    @FXML private Tab      tabMontant;

    // Onglet Profil détaillé
    @FXML private Label    lblProfilNom;
    @FXML private Label    lblProfilEmail;
    @FXML private Label    lblProfilSpecialite;
    @FXML private Label    lblProfilTaux;
    @FXML private Label    lblProfilDispo;
    @FXML private TextArea txtProfilCv;

    // Onglet Historique sessions
    @FXML private TableView<Session>              tableHistorique;
    @FXML private TableColumn<Session, String>     colHSession;
    @FXML private TableColumn<Session, LocalDate>  colHDateDebut;
    @FXML private TableColumn<Session, LocalDate>  colHDateFin;
    @FXML private TableColumn<Session, String>     colHLieu;
    @FXML private TableColumn<Session, Integer>    colHInscrits;

    // Onglet Calcul montant par programme (SERVICE_FINANCIER)
    @FXML private VBox     vboxMontantFinancier;
    @FXML private TableView<LigneCalcul>           tableCalcul;
    @FXML private TableColumn<LigneCalcul, String>  colCProgramme;
    @FXML private TableColumn<LigneCalcul, Integer> colCHeures;
    @FXML private TableColumn<LigneCalcul, Double>  colCTaux;
    @FXML private TableColumn<LigneCalcul, Double>  colCMontant;
    @FXML private Label    lblTotalAVerser;

    // ── DAOs ────────────────────────────────────────────────────────────
    private FormateurDAO         dao          = new FormateurDAO();
    private SessionDAO           sessionDAO   = new SessionDAO();
    private ProgrammeDAO         programmeDAO = new ProgrammeDAO();
    private PaiementDAO          paiementDAO  = new PaiementDAO();
    private SuiviPedagogiqueDAO  suiviDAO     = new SuiviPedagogiqueDAO();

    private ObservableList<Formateur> data = FXCollections.observableArrayList();
    private Formateur selectedFormateur = null;
    private String    roleActuel;

    @FXML
    public void initialize() {
        Utilisateur user = MainApp.getCurrentUser();
        roleActuel = (user != null) ? user.getRole() : "";

        // Colonnes table principale
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getIdUtilisateur()).asObject());
        colNom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNom()));
        colPrenom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPrenom()));
        if (colEmail != null) colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colSpecialite.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSpecialite()));
        colTauxHoraire.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getTauxHoraire()).asObject());

        // Colonnes historique
        if (colHSession  != null) colHSession.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLibelle()));
        if (colHDateDebut!= null) colHDateDebut.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDateDebut()));
        if (colHDateFin  != null) colHDateFin.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDateFin()));
        if (colHLieu     != null) colHLieu.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLieu()));
        if (colHInscrits != null) colHInscrits.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getCapaciteMax()).asObject());

        // Colonnes calcul montant
        if (colCProgramme!= null) colCProgramme.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().programme));
        if (colCHeures   != null) colCHeures.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().heures).asObject());
        if (colCTaux     != null) colCTaux.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().taux).asObject());
        if (colCMontant  != null) {
            colCMontant.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().montant).asObject());
            colCMontant.setCellFactory(col -> new javafx.scene.control.TableCell<LigneCalcul, Double>() {
                @Override protected void updateItem(Double v, boolean empty) {
                    super.updateItem(v, empty);
                    setText((empty || v == null) ? "" : String.format("%,.0f FCFA", v));
                }
            });
        }

        // Permissions
        boolean peutEditer = roleActuel.equals("ADMIN") || roleActuel.equals("RESPONSABLE");
        boolean estFinancier = roleActuel.equals("SERVICE_FINANCIER") || roleActuel.equals("FINANCIER");

        if (vboxFormulaire != null) { vboxFormulaire.setVisible(peutEditer); vboxFormulaire.setManaged(peutEditer); }
        if (hboxBoutons    != null) { hboxBoutons.setVisible(peutEditer);    hboxBoutons.setManaged(peutEditer); }
        if (vboxMontantFinancier != null) {
            vboxMontantFinancier.setVisible(estFinancier || roleActuel.equals("ADMIN"));
            vboxMontantFinancier.setManaged(estFinancier || roleActuel.equals("ADMIN"));
        }
        // Masquer l'onglet "Montant à Verser" pour le RESPONSABLE
        if (tabMontant != null && tabPaneProfil != null) {
            boolean voitMontant = estFinancier || roleActuel.equals("ADMIN");
            if (!voitMontant) {
                tabPaneProfil.getTabs().remove(tabMontant);
            }
        }

        loadData();

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) { selectedFormateur = selected; afficherProfil(selected); if (peutEditer) fillForm(selected); }
        });
    }

    private void loadData() {
        data.setAll(dao.findAll());
        tableView.setItems(data);
    }

    // ── Affichage du profil et historique à la sélection ─────────────────
    private void afficherProfil(Formateur f) {
        if (lblProfilNom        != null) lblProfilNom.setText(f.getNomComplet());
        if (lblProfilEmail      != null) lblProfilEmail.setText(f.getEmail());
        if (lblProfilSpecialite != null) lblProfilSpecialite.setText(f.getSpecialite());
        if (lblProfilTaux       != null) lblProfilTaux.setText(String.format("%,.0f FCFA/h", f.getTauxHoraire()));
        if (lblProfilDispo      != null) lblProfilDispo.setText(f.getDisponibilite() != null ? f.getDisponibilite() : "–");
        if (txtProfilCv         != null) txtProfilCv.setText(f.getCv() != null ? f.getCv() : "");

        // Historique des sessions
        if (tableHistorique != null) {
            List<Session> sessions = sessionDAO.findByFormateur(f.getIdUtilisateur());
            tableHistorique.setItems(FXCollections.observableArrayList(sessions));
        }

        // Calcul montant à verser par programme
        if (tableCalcul != null) {
            ObservableList<LigneCalcul> lignes = FXCollections.observableArrayList();
            double totalGeneral = 0;
            List<Session> sessions = sessionDAO.findByFormateur(f.getIdUtilisateur());
            for (Session s : sessions) {
                if (s.getIdProgramme() == null) continue;
                Programme p = programmeDAO.findById(s.getIdProgramme());
                String nomProg = (p != null) ? p.getIntitule() : "Programme " + s.getIdProgramme();
                // Durée du programme en heures
                int heures = (p != null) ? p.getDuree() : 0;
                double montant = heures * f.getTauxHoraire();
                totalGeneral += montant;
                lignes.add(new LigneCalcul(nomProg, heures, f.getTauxHoraire(), montant));
            }
            tableCalcul.setItems(lignes);
            if (lblTotalAVerser != null)
                lblTotalAVerser.setText(String.format("Total à verser : %,.0f FCFA", totalGeneral));
        }
    }

    private void fillForm(Formateur f) {
        if (txtNom          != null) txtNom.setText(f.getNom());
        if (txtPrenom       != null) txtPrenom.setText(f.getPrenom());
        if (txtEmail        != null) txtEmail.setText(f.getEmail());
        if (txtMotDePasse   != null) txtMotDePasse.setText(f.getMotDePasse());
        if (txtSpecialite   != null) txtSpecialite.setText(f.getSpecialite());
        if (txtTauxHoraire  != null) txtTauxHoraire.setText(String.valueOf(f.getTauxHoraire()));
        if (txtDisponibilite!= null) txtDisponibilite.setText(f.getDisponibilite() != null ? f.getDisponibilite() : "");
        if (txtCv           != null) txtCv.setText(f.getCv() != null ? f.getCv() : "");
    }

    @FXML public void handleAjouter() {
        if (!validateForm()) return;
        Formateur f = buildFromForm(); f.setActif(true);
        if (dao.insert(f)) { setStatus("✅ Formateur ajouté.", false); loadData(); clearForm(); }
        else setStatus("❌ Erreur lors de l'ajout.", true);
    }

    @FXML public void handleModifier() {
        if (selectedFormateur == null) { setStatus("⚠️ Sélectionnez un formateur.", true); return; }
        if (!validateForm()) return;
        Formateur f = buildFromForm();
        f.setIdUtilisateur(selectedFormateur.getIdUtilisateur());
        f.setActif(selectedFormateur.isActif());
        if (dao.update(f)) { setStatus("✅ Modifié.", false); loadData(); clearForm(); }
        else setStatus("❌ Erreur modification.", true);
    }

    @FXML public void handleSupprimer() {
        if (selectedFormateur == null) { setStatus("⚠️ Sélectionnez un formateur.", true); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer " + selectedFormateur.getNomComplet() + " ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> r = a.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.YES) {
            if (dao.delete(selectedFormateur.getIdUtilisateur())) {
                setStatus("✅ Supprimé.", false); loadData(); clearForm(); selectedFormateur = null;
            } else setStatus("❌ Erreur suppression.", true);
        }
    }

    @FXML public void handleNouveauForm() { clearForm(); selectedFormateur = null; }

    private boolean validateForm() {
        if (txtNom == null || txtNom.getText().isEmpty()) { setStatus("⚠️ Nom obligatoire.", true); return false; }
        return true;
    }

    private Formateur buildFromForm() {
        Formateur f = new Formateur();
        f.setNom(txtNom.getText().trim());
        f.setPrenom(txtPrenom != null ? txtPrenom.getText().trim() : "");
        f.setEmail(txtEmail != null ? txtEmail.getText().trim() : "");
        f.setMotDePasse(txtMotDePasse != null ? txtMotDePasse.getText().trim() : "");
        f.setSpecialite(txtSpecialite != null ? txtSpecialite.getText().trim() : "");
        try { f.setTauxHoraire(Double.parseDouble(txtTauxHoraire.getText().trim())); } catch (Exception e) { f.setTauxHoraire(0); }
        f.setDisponibilite(txtDisponibilite != null ? txtDisponibilite.getText().trim() : "");
        if (txtCv != null) f.setCv(txtCv.getText().trim());
        return f;
    }

    private void clearForm() {
        if (txtNom          != null) txtNom.clear();
        if (txtPrenom       != null) txtPrenom.clear();
        if (txtEmail        != null) txtEmail.clear();
        if (txtMotDePasse   != null) txtMotDePasse.clear();
        if (txtSpecialite   != null) txtSpecialite.clear();
        if (txtTauxHoraire  != null) txtTauxHoraire.clear();
        if (txtDisponibilite!= null) txtDisponibilite.clear();
        if (txtCv           != null) txtCv.clear();
    }

    private void setStatus(String msg, boolean error) {
        if (lblStatus != null) { lblStatus.setText(msg); lblStatus.setStyle(error ? "-fx-text-fill: red;" : "-fx-text-fill: green;"); }
    }

    // ── Classe interne pour le tableau de calcul ─────────────────────────
    public static class LigneCalcul {
        String programme; int heures; double taux; double montant;
        LigneCalcul(String p, int h, double t, double m) { programme=p; heures=h; taux=t; montant=m; }
        public String getProgramme() { return programme; }
        public int    getHeures()    { return heures; }
        public double getTaux()      { return taux; }
        public double getMontant()   { return montant; }
    }
}
