package Controller;

import Database.ApprenantDAO;
import Database.EntrepriseDAO;
import Database.InscriptionDAO;
import Database.ProgrammeDAO;
import Database.SessionDAO;
import Database.PaiementDAO;
import FX.MainApp;
import metier.Apprenant;
import metier.Entreprise;
import metier.Inscription;
import metier.Programme;
import metier.Session;
import metier.Utilisateur;
import javafx.beans.property.SimpleBooleanProperty;
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
import java.util.stream.Collectors;

public class InscriptionController {

    // ── Table principale (toutes inscriptions) ───────────────────────────
    @FXML private TableView<Inscription>              tableView;
    @FXML private TableColumn<Inscription, Integer>   colId;
    @FXML private TableColumn<Inscription, LocalDate> colDate;
    @FXML private TableColumn<Inscription, String>    colStatut;
    @FXML private TableColumn<Inscription, Boolean>   colValidation;
    @FXML private TableColumn<Inscription, String>    colApprenant;
    @FXML private TableColumn<Inscription, String>    colSession;
    @FXML private TableColumn<Inscription, String>    colPrixFormation;
    @FXML private TableColumn<Inscription, String>    colResultat;

    // ── Formulaire ADMIN/RESPONSABLE ────────────────────────────────────
    @FXML private ComboBox<Apprenant> cbApprenant;
    @FXML private ComboBox<Session>   cbSession;
    @FXML private ComboBox<String>    cbStatut;
    @FXML private CheckBox            chkValidation;
    @FXML private HBox                hboxBoutons;
    @FXML private VBox                vboxFormulaire;
    @FXML private Label               lblStatus;

    // ── Bande info APPRENANT ─────────────────────────────────────────────
    @FXML private VBox  vboxInfoApprenant;
    @FXML private Label lblResteAPayer;
    @FXML private Label lblMontantPaye;
    @FXML private Label lblTotalDu;

    // ── Satisfaction APPRENANT ───────────────────────────────────────────
    @FXML private VBox  vboxSatisfaction;
    @FXML private Label lblVoteActuel;

    // ── Panel ENTREPRISE : soumettre une demande ─────────────────────────
    @FXML private VBox                vboxEntrepriseInscription;
    @FXML private ComboBox<Apprenant> cbEntrepriseApprenant;
    @FXML private ComboBox<Session>   cbEntrepriseSession;
    @FXML private Label               lblEntrepriseStatus;

    // ── Panel RESPONSABLE/ADMIN : demandes EN_ATTENTE ────────────────────
    @FXML private VBox                               vboxDemandesAttente;
    @FXML private TableView<Inscription>             tableAttente;
    @FXML private TableColumn<Inscription, LocalDate> colAttDate;
    @FXML private TableColumn<Inscription, String>   colAttApprenant;
    @FXML private TableColumn<Inscription, String>   colAttEntreprise;
    @FXML private TableColumn<Inscription, String>   colAttSession;
    @FXML private TableColumn<Inscription, String>   colAttPrix;
    @FXML private TableColumn<Inscription, String>   colAttStatut;
    @FXML private Label                              lblNbAttente;
    @FXML private Label                              lblAttenteStatus;

    // ── DAOs ─────────────────────────────────────────────────────────────
    private final InscriptionDAO dao          = new InscriptionDAO();
    private final ApprenantDAO   apprenantDAO = new ApprenantDAO();
    private final SessionDAO     sessionDAO   = new SessionDAO();
    private final PaiementDAO    paiementDAO  = new PaiementDAO();
    private final EntrepriseDAO  entrepriseDAO = new EntrepriseDAO();
    private final ProgrammeDAO   programmeDAO  = new ProgrammeDAO();

    private ObservableList<Inscription> data = FXCollections.observableArrayList();
    private Inscription selectedInscription  = null;
    private Inscription selectedAttente      = null;

    @FXML
    public void initialize() {
        // Colonnes table principale
        colId.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getIdInscription()).asObject());
        colDate.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getDateInscription()));
        colStatut.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatut()));
        colValidation.setCellValueFactory(cd -> new SimpleBooleanProperty(cd.getValue().isValidationAdministrative()));
        colApprenant.setCellValueFactory(cd -> {
            Inscription i = cd.getValue();
            String nom = (i.getApprenant() != null) ? i.getApprenant().getNomComplet()
                : (i.getIdApprenant() != null ? "ID " + i.getIdApprenant() : "–");
            return new SimpleStringProperty(nom);
        });
        colSession.setCellValueFactory(cd -> {
            Inscription i = cd.getValue();
            String lib = (i.getSession() != null) ? i.getSession().getLibelle()
                : (i.getIdSession() != null ? "Session " + i.getIdSession() : "–");
            return new SimpleStringProperty(lib);
        });
        if (colResultat != null) colResultat.setCellValueFactory(cd -> {
            String r = cd.getValue().getResultSession();
            return new SimpleStringProperty(r != null ? r : "");
        });
        if (colPrixFormation != null) colPrixFormation.setCellValueFactory(cd -> {
            Inscription i = cd.getValue();
            if (i.getSession() != null && i.getSession().getIdProgramme() != null) {
                Programme p = programmeDAO.findById(i.getSession().getIdProgramme());
                if (p != null) return new SimpleStringProperty(String.format("%,.0f FCFA", p.getTarif()));
            }
            return new SimpleStringProperty("–");
        });

        // Colonnes table attente
        if (colAttDate      != null) colAttDate.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getDateInscription()));
        if (colAttApprenant != null) colAttApprenant.setCellValueFactory(cd -> {
            Inscription i = cd.getValue();
            String nom = (i.getApprenant() != null) ? i.getApprenant().getNomComplet()
                : (i.getIdApprenant() != null ? "ID " + i.getIdApprenant() : "–");
            return new SimpleStringProperty(nom);
        });
        if (colAttEntreprise != null) colAttEntreprise.setCellValueFactory(cd -> {
            // Retrouver l'entreprise de l'apprenant
            Inscription i = cd.getValue();
            if (i.getApprenant() != null && i.getApprenant() instanceof Apprenant) {
                Apprenant a = (Apprenant) i.getApprenant();
                if (a.getIdEntreprise() != null) {
                    Entreprise e = entrepriseDAO.findById(a.getIdEntreprise());
                    return new SimpleStringProperty(e != null ? e.getNom() : "–");
                }
            }
            return new SimpleStringProperty("–");
        });
        if (colAttSession   != null) colAttSession.setCellValueFactory(cd -> {
            Inscription i = cd.getValue();
            String lib = (i.getSession() != null) ? i.getSession().getLibelle()
                : (i.getIdSession() != null ? "Session " + i.getIdSession() : "–");
            return new SimpleStringProperty(lib);
        });
        if (colAttPrix != null) colAttPrix.setCellValueFactory(cd -> {
            Inscription i = cd.getValue();
            if (i.getSession() != null && i.getSession().getIdProgramme() != null) {
                Programme p = programmeDAO.findById(i.getSession().getIdProgramme());
                if (p != null) return new SimpleStringProperty(String.format("%,.0f FCFA", p.getTarif()));
            }
            return new SimpleStringProperty("–");
        });
        if (colAttStatut    != null) colAttStatut.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getStatut()));

        // Sélection dans la table attente
        if (tableAttente != null) {
            tableAttente.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
                if (sel != null) selectedAttente = sel;
            });
        }

        Utilisateur user = MainApp.getCurrentUser();
        String role = (user != null) ? user.getRole() : "";

        appliquerRole(role, user);
        loadData(role, user);

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectedInscription = selected;
                fillForm(selected);
                if ("APPRENANT".equals(role)) afficherSatisfaction(selected);
            }
        });
    }

    // ─── Permissions ─────────────────────────────────────────────────────
    private void appliquerRole(String role, Utilisateur user) {
        boolean isGestionnaire = role.equals("ADMIN") || role.equals("RESPONSABLE");
        boolean isApprenant    = role.equals("APPRENANT");
        boolean isEntreprise   = role.equals("ENTREPRISE");

        // Formulaire CRUD → ADMIN / RESPONSABLE uniquement
        setVisible(vboxFormulaire,           isGestionnaire);
        setVisible(hboxBoutons,              isGestionnaire);

        // Bande financière → APPRENANT uniquement
        setVisible(vboxInfoApprenant,        isApprenant);

        // Satisfaction → masquée par défaut, affichée à la sélection
        setVisible(vboxSatisfaction,         false);

        // Panel inscription entreprise → ENTREPRISE uniquement
        setVisible(vboxEntrepriseInscription, isEntreprise);

        // Panel demandes en attente → ADMIN / RESPONSABLE uniquement
        setVisible(vboxDemandesAttente,      isGestionnaire);

        if (isGestionnaire) {
            if (cbStatut    != null) cbStatut.setItems(FXCollections.observableArrayList(
                "EN_ATTENTE", "VALIDEE", "REFUSEE", "ANNULEE"));
            if (cbApprenant != null) cbApprenant.setItems(FXCollections.observableArrayList(apprenantDAO.findAll()));
            if (cbSession   != null) cbSession.setItems(FXCollections.observableArrayList(sessionDAO.findAll()));
        }

        if (isEntreprise) {
            chargerComboEntreprise(user);
        }
    }

    // ─── Chargement combos pour ENTREPRISE ───────────────────────────────
    private void chargerComboEntreprise(Utilisateur user) {
        // Trouver l'entreprise liée à cet utilisateur
        Entreprise entreprise = entrepriseDAO.findAll().stream()
            .filter(e -> e.getEmail() != null && e.getEmail().equalsIgnoreCase(user.getEmail()))
            .findFirst().orElseGet(() ->
                entrepriseDAO.findAll().stream()
                    .filter(e -> e.getNom() != null && e.getNom().equalsIgnoreCase(user.getNom()))
                    .findFirst().orElse(null));

        if (entreprise == null) {
            setStatus(lblEntrepriseStatus, "⚠️ Votre entreprise n'est pas trouvée en base.", true);
            return;
        }

        // Membres = apprenants liés à cette entreprise
        List<Apprenant> membres = apprenantDAO.findByEntrepriseId(entreprise.getIdEntreprise());
        if (cbEntrepriseApprenant != null)
            cbEntrepriseApprenant.setItems(FXCollections.observableArrayList(membres));

        // Toutes les sessions sauf TERMINEE
        List<Session> sessionsDisponibles = sessionDAO.findAll().stream()
            .filter(s -> s.getStatut() == null ||
                !s.getStatut().equalsIgnoreCase("TERMINEE"))
            .collect(Collectors.toList());
        if (cbEntrepriseSession != null)
            cbEntrepriseSession.setItems(FXCollections.observableArrayList(sessionsDisponibles));
    }

    // ─── Action : ENTREPRISE soumet une demande d'inscription ────────────
    @FXML
    public void handleEntrepriseInscrire() {
        if (cbEntrepriseApprenant == null || cbEntrepriseApprenant.getValue() == null) {
            setStatus(lblEntrepriseStatus, "⚠️ Sélectionnez un membre.", true); return;
        }
        if (cbEntrepriseSession == null || cbEntrepriseSession.getValue() == null) {
            setStatus(lblEntrepriseStatus, "⚠️ Sélectionnez une session.", true); return;
        }

        Apprenant apprenant = cbEntrepriseApprenant.getValue();
        Session   session   = cbEntrepriseSession.getValue();

        // Vérifier que cet apprenant n'est pas déjà inscrit à cette session
        boolean dejaInscrit = dao.findBySession(session.getIdSession()).stream()
            .anyMatch(i -> i.getIdApprenant() != null &&
                          i.getIdApprenant().equals(apprenant.getIdUtilisateur()));
        if (dejaInscrit) {
            setStatus(lblEntrepriseStatus,
                "⚠️ " + apprenant.getNomComplet() + " est déjà inscrit(e) à cette session.", true);
            return;
        }

        // Créer l'inscription avec statut EN_ATTENTE (pas encore validée)
        Inscription insc = new Inscription();
        insc.setDateInscription(LocalDate.now());
        insc.setStatut("EN_ATTENTE");
        insc.setValidationAdministrative(false);
        insc.setIdApprenant(apprenant.getIdUtilisateur());
        insc.setIdSession(session.getIdSession());

        if (dao.insert(insc)) {
            setStatus(lblEntrepriseStatus,
                "✅ Demande soumise pour " + apprenant.getNomComplet() +
                ". En attente de validation par le responsable.", false);
            cbEntrepriseApprenant.setValue(null);
            cbEntrepriseSession.setValue(null);
            // Recharger la table pour voir la nouvelle demande EN_ATTENTE
            Utilisateur user = MainApp.getCurrentUser();
            if (user != null) loadData(user.getRole(), user);
        } else {
            setStatus(lblEntrepriseStatus, "❌ Erreur lors de la soumission de la demande.", true);
        }
    }

    // ─── Action : RESPONSABLE valide une demande EN_ATTENTE ──────────────
    @FXML
    public void handleValiderAttente() {
        if (selectedAttente == null) {
            setStatus(lblAttenteStatus, "⚠️ Sélectionnez une demande dans la liste.", true); return;
        }
        selectedAttente.setStatut("VALIDEE");
        selectedAttente.setValidationAdministrative(true);
        if (dao.update(selectedAttente)) {
            setStatus(lblAttenteStatus, "✅ Inscription validée avec succès.", false);
            selectedAttente = null;
            reloadData();
        } else {
            setStatus(lblAttenteStatus, "❌ Erreur lors de la validation.", true);
        }
    }

    // ─── Action : RESPONSABLE refuse une demande EN_ATTENTE ──────────────
    @FXML
    public void handleRefuserAttente() {
        if (selectedAttente == null) {
            setStatus(lblAttenteStatus, "⚠️ Sélectionnez une demande dans la liste.", true); return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Refuser la demande de " +
            (selectedAttente.getApprenant() != null ? selectedAttente.getApprenant().getNomComplet() : "cet apprenant") +
            " ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.YES) {
            selectedAttente.setStatut("REFUSEE");
            selectedAttente.setValidationAdministrative(false);
            if (dao.update(selectedAttente)) {
                setStatus(lblAttenteStatus, "✅ Demande refusée.", false);
                selectedAttente = null;
                reloadData();
            } else {
                setStatus(lblAttenteStatus, "❌ Erreur lors du refus.", true);
            }
        }
    }

    // ─── Chargement des données ───────────────────────────────────────────
    private void loadData(String role, Utilisateur user) {
        if (role.equals("APPRENANT") && user != null) {
            // L'apprenant voit uniquement ses propres inscriptions
            data.setAll(dao.findByApprenantAvecDetails(user.getIdUtilisateur()));
            tableView.setItems(data);
            afficherResteAPayer(user.getIdUtilisateur());

        } else if (role.equals("ENTREPRISE") && user != null) {
            // L'entreprise voit les inscriptions de ses membres
            Entreprise entreprise = entrepriseDAO.findAll().stream()
                .filter(e -> e.getEmail() != null && e.getEmail().equalsIgnoreCase(user.getEmail()))
                .findFirst().orElseGet(() ->
                    entrepriseDAO.findAll().stream()
                        .filter(e -> e.getNom() != null && e.getNom().equalsIgnoreCase(user.getNom()))
                        .findFirst().orElse(null));

            if (entreprise != null) {
                List<Apprenant> membres = apprenantDAO.findByEntrepriseId(entreprise.getIdEntreprise());
                List<Integer> ids = membres.stream()
                    .map(Apprenant::getIdUtilisateur).collect(Collectors.toList());
                List<Inscription> inscEntreprise = dao.findAllAvecDetails().stream()
                    .filter(i -> i.getIdApprenant() != null && ids.contains(i.getIdApprenant()))
                    .collect(Collectors.toList());
                data.setAll(inscEntreprise);
            } else {
                data.clear();
            }
            tableView.setItems(data);

        } else {
            // ADMIN / RESPONSABLE / FINANCIER : toutes les inscriptions
            data.setAll(dao.findAllAvecDetails());
            tableView.setItems(data);
        }

        // Charger la table des demandes EN_ATTENTE (ADMIN/RESPONSABLE)
        chargerDemandesAttente(role);
    }

    // ─── Table demandes EN_ATTENTE ────────────────────────────────────────
    private void chargerDemandesAttente(String role) {
        if (tableAttente == null) return;
        boolean isGestionnaire = role.equals("ADMIN") || role.equals("RESPONSABLE");
        if (!isGestionnaire) return;

        // Filtrer uniquement les inscriptions EN_ATTENTE
        List<Inscription> attentes = dao.findAllAvecDetails().stream()
            .filter(i -> "EN_ATTENTE".equalsIgnoreCase(i.getStatut()))
            .collect(Collectors.toList());

        tableAttente.setItems(FXCollections.observableArrayList(attentes));

        // Mettre à jour le compteur
        if (lblNbAttente != null) {
            int nb = attentes.size();
            lblNbAttente.setText("(" + nb + ")");
            lblNbAttente.setStyle(nb > 0
                ? "-fx-text-fill:#e74c3c; -fx-font-weight:bold; -fx-font-size:14px;"
                : "-fx-text-fill:#27ae60; -fx-font-weight:bold; -fx-font-size:14px;");
        }
    }

    // ─── Résumé financier APPRENANT ──────────────────────────────────────
    private void afficherResteAPayer(int idApprenant) {
        double paye    = paiementDAO.getMontantPayeParApprenant(idApprenant);
        double reste   = paiementDAO.getMontantRestantParApprenant(idApprenant);
        double totalDu = paye + reste;
        if (lblMontantPaye != null) lblMontantPaye.setText(String.format("%,.0f FCFA", paye));
        if (lblTotalDu     != null) lblTotalDu.setText(String.format("%,.0f FCFA", totalDu));
        if (lblResteAPayer != null) {
            lblResteAPayer.setText(String.format("%,.0f FCFA", reste));
            lblResteAPayer.setStyle(reste > 0
                ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;"
                : "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        }
    }

    // ─── Satisfaction ─────────────────────────────────────────────────────
    private void afficherSatisfaction(Inscription inscr) {
        if (vboxSatisfaction == null) return;
        // Afficher uniquement pour les inscriptions VALIDEE
        boolean validee = "VALIDEE".equalsIgnoreCase(inscr.getStatut());
        setVisible(vboxSatisfaction, validee);
        if (!validee) return;

        String vote = paiementDAO.getVoteApprenant(inscr.getIdInscription());
        if (lblVoteActuel != null) {
            if ("SATISFAIT".equals(vote)) {
                lblVoteActuel.setText("Votre vote actuel : ✅ Satisfait");
                lblVoteActuel.setStyle("-fx-text-fill: #27ae60;");
            } else if ("NON_SATISFAIT".equals(vote)) {
                lblVoteActuel.setText("Votre vote actuel : ❌ Non satisfait");
                lblVoteActuel.setStyle("-fx-text-fill: #e74c3c;");
            } else {
                lblVoteActuel.setText("Vous n'avez pas encore voté.");
                lblVoteActuel.setStyle("-fx-text-fill: #7f8c8d;");
            }
        }
    }

    @FXML public void handleVoterSatisfait() {
        if (selectedInscription == null) { setStatus(lblStatus, "⚠️ Sélectionnez une inscription.", true); return; }
        Utilisateur user = MainApp.getCurrentUser();
        if (user == null) return;
        paiementDAO.enregistrerVoteSatisfaction(
            selectedInscription.getIdInscription(), user.getIdUtilisateur(), true);
        afficherSatisfaction(selectedInscription);
        setStatus(lblStatus, "✅ Vote enregistré : Satisfait.", false);
    }

    @FXML public void handleVoterNonSatisfait() {
        if (selectedInscription == null) { setStatus(lblStatus, "⚠️ Sélectionnez une inscription.", true); return; }
        Utilisateur user = MainApp.getCurrentUser();
        if (user == null) return;
        paiementDAO.enregistrerVoteSatisfaction(
            selectedInscription.getIdInscription(), user.getIdUtilisateur(), false);
        afficherSatisfaction(selectedInscription);
        setStatus(lblStatus, "✅ Vote enregistré : Non satisfait.", false);
    }

    // ─── CRUD standard (ADMIN/RESPONSABLE) ───────────────────────────────
    @FXML public void handleAjouter() {
        Inscription i = new Inscription();
        i.setDateInscription(LocalDate.now());
        i.setStatut(cbStatut != null && cbStatut.getValue() != null ? cbStatut.getValue() : "EN_ATTENTE");
        i.setValidationAdministrative(chkValidation != null && chkValidation.isSelected());
        i.setIdApprenant(cbApprenant != null && cbApprenant.getValue() != null
            ? cbApprenant.getValue().getIdUtilisateur() : null);
        i.setIdSession(cbSession != null && cbSession.getValue() != null
            ? cbSession.getValue().getIdSession() : null);
        if (i.getIdApprenant() == null || i.getIdSession() == null) {
            setStatus(lblStatus, "⚠️ Apprenant et Session obligatoires.", true); return;
        }
        if (dao.insert(i)) {
            setStatus(lblStatus, "✅ Inscription créée.", false); reloadData(); clearForm();
        } else setStatus(lblStatus, "❌ Erreur lors de l'inscription.", true);
    }

    @FXML public void handleValider() {
        if (selectedInscription == null) { setStatus(lblStatus, "⚠️ Sélectionnez une inscription.", true); return; }
        selectedInscription.setStatut("VALIDEE");
        selectedInscription.setValidationAdministrative(true);
        if (dao.update(selectedInscription)) {
            setStatus(lblStatus, "✅ Inscription validée.", false); reloadData();
        } else setStatus(lblStatus, "❌ Erreur lors de la validation.", true);
    }

    @FXML public void handleRefuser() {
        if (selectedInscription == null) { setStatus(lblStatus, "⚠️ Sélectionnez une inscription.", true); return; }
        selectedInscription.setStatut("REFUSEE");
        if (dao.update(selectedInscription)) {
            setStatus(lblStatus, "✅ Inscription refusée.", false); reloadData();
        } else setStatus(lblStatus, "❌ Erreur.", true);
    }

    @FXML public void handleSupprimer() {
        if (selectedInscription == null) { setStatus(lblStatus, "⚠️ Sélectionnez une inscription.", true); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer l'inscription ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.YES) {
            if (dao.delete(selectedInscription.getIdInscription())) {
                setStatus(lblStatus, "✅ Inscription supprimée.", false);
                reloadData(); clearForm(); selectedInscription = null;
            } else setStatus(lblStatus, "❌ Erreur suppression.", true);
        }
    }

    @FXML public void handleAdmettre() {
        if (selectedInscription == null) { setStatus(lblStatus, "⚠️ Sélectionnez une inscription.", true); return; }
        if (dao.updateResultSession(selectedInscription.getIdInscription(), "ADMIS")) {
            setStatus(lblStatus, "✅ Apprenant admis.", false); reloadData();
        } else setStatus(lblStatus, "❌ Erreur (vérifiez migration SQL).", true);
    }

    @FXML public void handleAjourner() {
        if (selectedInscription == null) { setStatus(lblStatus, "⚠️ Sélectionnez une inscription.", true); return; }
        if (dao.updateResultSession(selectedInscription.getIdInscription(), "AJOURNÉ")) {
            setStatus(lblStatus, "✅ Apprenant ajourné.", false); reloadData();
        } else setStatus(lblStatus, "❌ Erreur (vérifiez migration SQL).", true);
    }

    @FXML public void handleNouveauForm() { clearForm(); selectedInscription = null; }

    // ─── Helpers ──────────────────────────────────────────────────────────
    private void reloadData() {
        Utilisateur user = MainApp.getCurrentUser();
        if (user != null) loadData(user.getRole(), user);
    }

    private void fillForm(Inscription i) {
        if (cbStatut      != null) cbStatut.setValue(i.getStatut());
        if (chkValidation != null) chkValidation.setSelected(i.isValidationAdministrative());
    }

    private void clearForm() {
        if (cbApprenant   != null) cbApprenant.setValue(null);
        if (cbSession     != null) cbSession.setValue(null);
        if (cbStatut      != null) cbStatut.setValue(null);
        if (chkValidation != null) chkValidation.setSelected(false);
    }

    private void setVisible(javafx.scene.Node node, boolean visible) {
        if (node != null) { node.setVisible(visible); node.setManaged(visible); }
    }

    private void setStatus(Label lbl, String msg, boolean error) {
        if (lbl != null) {
            lbl.setText(msg);
            lbl.setStyle(error ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
        }
    }

    // ─── Génération attestation / certificat ──────────────────────────────
    @FXML public void handleGenererAttestation() {
        Inscription selected = tableView != null ? tableView.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            setStatus(lblStatus, "⚠️ Sélectionnez d'abord une inscription dans la liste.", true);
            return;
        }
        try {
            String nomApprenant = selected.getApprenant() != null
                ? selected.getApprenant().getNomComplet() : "Apprenant";

            String formation = "–";
            String session   = "–";
            java.time.LocalDate dateFin = java.time.LocalDate.now();

            if (selected.getSession() != null) {
                session = selected.getSession().getLibelle() != null ? selected.getSession().getLibelle() : "–";
                dateFin = selected.getSession().getDateFin() != null ? selected.getSession().getDateFin() : dateFin;
                if (selected.getSession().getIdProgramme() != null) {
                    Programme prog = programmeDAO.findById(selected.getSession().getIdProgramme());
                    if (prog != null) formation = prog.getIntitule();
                }
            }

            String resultat = selected.getResultSession() != null ? selected.getResultSession() : "PARTICIPATION";

            java.io.File doc = DocumentGenerator.genererAttestationFormation(
                nomApprenant, formation, resultat, session, dateFin);
            DocumentGenerator.ouvrirDocument(doc);

            setStatus(lblStatus, "✅ Attestation générée : " + doc.getName(), false);
            Alert info = new Alert(Alert.AlertType.INFORMATION,
                "✅ Attestation / certificat généré !\n\nFichier : " + doc.getAbsolutePath() +
                "\n\nLe document s'ouvre dans votre navigateur.\nVous pouvez l'imprimer (Ctrl+P).",
                ButtonType.OK);
            info.setHeaderText("Document généré");
            info.showAndWait();
        } catch (Exception e) {
            setStatus(lblStatus, "❌ Erreur génération : " + e.getMessage(), true);
        }
    }
}