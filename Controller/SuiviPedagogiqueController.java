package Controller;

import Database.ApprenantDAO;
import Database.InscriptionDAO;
import Database.SessionDAO;
import Database.SuiviPedagogiqueDAO;
import FX.MainApp;
import metier.Apprenant;
import metier.Session;
import metier.SuiviPedagogique;
import metier.Utilisateur;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class SuiviPedagogiqueController {

    @FXML private TableView<SuiviPedagogique>          tableView;
    @FXML private TableColumn<SuiviPedagogique, Boolean> colPresence;
    @FXML private TableColumn<SuiviPedagogique, String>  colStatut;   // Réussi / Abandon / En cours
    @FXML private TableColumn<SuiviPedagogique, Double>  colNote;
    @FXML private TableColumn<SuiviPedagogique, String>  colFeedback;
    @FXML private TableColumn<SuiviPedagogique, String>  colApprenant;
    @FXML private TableColumn<SuiviPedagogique, String>  colSession;

    // Formulaire saisie (FORMATEUR + ADMIN + RESPONSABLE)
    @FXML private VBox       vboxFormulaire;
    @FXML private HBox       hboxBoutons;
    @FXML private ComboBox<Apprenant> cbApprenant;
    @FXML private ComboBox<Session>   cbSession;
    @FXML private CheckBox            chkPresence;
    @FXML private TextField           txtNote;
    @FXML private TextArea            txtFeedback;

    // Boutons RESPONSABLE uniquement (réussite / abandon)
    @FXML private HBox  hboxStatutBoutons;
    @FXML private Button btnReussi;
    @FXML private Button btnAbandon;
    @FXML private Button btnEnCours;

    // Stats taux (RESPONSABLE uniquement)
    @FXML private VBox   vboxTaux;
    @FXML private ComboBox<Session> cbFiltreSession;
    @FXML private Label  lblTauxPresence;
    @FXML private Label  lblTauxReussite;
    @FXML private Label  lblTauxAbandon;
    @FXML private Label  lblStatus;

    private SuiviPedagogiqueDAO dao          = new SuiviPedagogiqueDAO();
    private ApprenantDAO        apprenantDAO = new ApprenantDAO();
    private SessionDAO          sessionDAO   = new SessionDAO();
    private InscriptionDAO      inscriptionDAO = new InscriptionDAO();

    private ObservableList<SuiviPedagogique> data = FXCollections.observableArrayList();
    private SuiviPedagogique selectedSuivi = null;
    private String roleActuel;
    private Utilisateur userActuel;

    @FXML
    public void initialize() {
        colPresence.setCellValueFactory(cd -> new SimpleBooleanProperty(cd.getValue().isPresence()));
        colNote.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().getNote()).asObject());
        colFeedback.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getFeedback()));
        colApprenant.setCellValueFactory(cd -> {
            SuiviPedagogique sp = cd.getValue();
            String nom = sp.getApprenant() != null ? sp.getApprenant().getNomComplet()
                : (sp.getIdApprenant() != null ? "ID " + sp.getIdApprenant() : "–");
            return new SimpleStringProperty(nom);
        });
        colSession.setCellValueFactory(cd -> {
            SuiviPedagogique sp = cd.getValue();
            String lib = sp.getSession() != null ? sp.getSession().getLibelle()
                : (sp.getIdSession() != null ? "Session " + sp.getIdSession() : "–");
            return new SimpleStringProperty(lib);
        });
        if (colStatut != null)
            colStatut.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getStatutSuivi() != null ? cd.getValue().getStatutSuivi() : "En cours"));

        userActuel = MainApp.getCurrentUser();
        roleActuel = (userActuel != null) ? userActuel.getRole() : "";

        appliquerRole();
        loadData();

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) { selectedSuivi = selected; fillForm(selected); }
        });
    }

    private void appliquerRole() {
        boolean isFormateur    = roleActuel.equals("FORMATEUR");
        boolean isResponsable  = roleActuel.equals("RESPONSABLE");
        boolean isAdmin        = roleActuel.equals("ADMIN");
        boolean peutSaisir     = isFormateur || isResponsable || isAdmin;
        boolean voitTaux       = isResponsable || isAdmin;
        boolean peutSetStatut  = isResponsable || isAdmin;  // boutons réussi/abandon

        if (vboxFormulaire != null) { vboxFormulaire.setVisible(peutSaisir); vboxFormulaire.setManaged(peutSaisir); }
        if (hboxBoutons    != null) { hboxBoutons.setVisible(peutSaisir);    hboxBoutons.setManaged(peutSaisir); }
        if (vboxTaux       != null) { vboxTaux.setVisible(voitTaux);         vboxTaux.setManaged(voitTaux); }
        if (hboxStatutBoutons != null) { hboxStatutBoutons.setVisible(peutSetStatut); hboxStatutBoutons.setManaged(peutSetStatut); }

        List<Session> sessions = isFormateur && userActuel != null
            ? sessionDAO.findByFormateur(userActuel.getIdUtilisateur())
            : sessionDAO.findAll();

        List<Apprenant> tousApprenants = apprenantDAO.findAll();
        if (cbApprenant    != null) cbApprenant.setItems(FXCollections.observableArrayList(tousApprenants));
        if (cbSession      != null) {
            cbSession.setItems(FXCollections.observableArrayList(sessions));
            // Quand on choisit une session, filtrer cbApprenant aux inscrits de cette session
            cbSession.valueProperty().addListener((obs, oldS, newS) -> {
                if (cbApprenant == null) return;
                if (newS == null) {
                    cbApprenant.setItems(FXCollections.observableArrayList(tousApprenants));
                } else {
                    List<Integer> ids = inscriptionDAO.findBySession(newS.getIdSession())
                        .stream().map(i -> i.getIdApprenant()).filter(id -> id != null)
                        .collect(java.util.stream.Collectors.toList());
                    List<Apprenant> filtres = tousApprenants.stream()
                        .filter(a -> ids.contains(a.getIdUtilisateur()))
                        .collect(java.util.stream.Collectors.toList());
                    cbApprenant.setItems(FXCollections.observableArrayList(filtres));
                    cbApprenant.setValue(null);
                }
            });
        }
        if (cbFiltreSession!= null) cbFiltreSession.setItems(FXCollections.observableArrayList(sessions));
    }

    private void loadData() {
        if (roleActuel.equals("FORMATEUR") && userActuel != null)
            data.setAll(dao.findByFormateurAvecDetails(userActuel.getIdUtilisateur()));
        else
            data.setAll(dao.findAllAvecDetails());
        tableView.setItems(data);
    }

    private void fillForm(SuiviPedagogique s) {
        if (chkPresence != null) chkPresence.setSelected(s.isPresence());
        if (txtNote     != null) txtNote.setText(String.valueOf(s.getNote()));
        if (txtFeedback != null) txtFeedback.setText(s.getFeedback() != null ? s.getFeedback() : "");
    }

    // ── Actions statut (RESPONSABLE) ────────────────────────────────────
    @FXML public void handleReussi()  { setStatut("Réussi");  }
    @FXML public void handleAbandon() { setStatut("Abandon"); }
    @FXML public void handleEnCours() { setStatut("En cours"); }

    private void setStatut(String statut) {
        if (selectedSuivi == null) { setStatus("⚠️ Sélectionnez un apprenant dans la liste.", true); return; }
        selectedSuivi.setStatutSuivi(statut);
        if (dao.updateStatut(selectedSuivi.getIdSuivi(), statut)) {
            setStatus("✅ Statut mis à jour : " + statut, false);
            loadData();
        } else setStatus("❌ Erreur mise à jour statut.", true);
    }

    // ── CRUD suivi ───────────────────────────────────────────────────────
    @FXML public void handleAjouter() {
        SuiviPedagogique s = buildFromForm();
        if (s.getIdApprenant() == null || s.getIdSession() == null) {
            setStatus("⚠️ Apprenant et Session obligatoires.", true); return;
        }
        if (dao.insert(s)) { setStatus("✅ Suivi enregistré.", false); loadData(); clearForm(); }
        else setStatus("❌ Erreur enregistrement.", true);
    }

    @FXML public void handleModifier() {
        if (selectedSuivi == null) { setStatus("⚠️ Sélectionnez un suivi.", true); return; }
        SuiviPedagogique s = buildFromForm(); s.setIdSuivi(selectedSuivi.getIdSuivi());
        if (dao.update(s)) { setStatus("✅ Suivi modifié.", false); loadData(); clearForm(); }
        else setStatus("❌ Erreur modification.", true);
    }

    @FXML public void handleSupprimer() {
        if (selectedSuivi == null) { setStatus("⚠️ Sélectionnez un suivi.", true); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce suivi ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> r = a.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.YES) {
            if (dao.delete(selectedSuivi.getIdSuivi())) {
                setStatus("✅ Supprimé.", false); loadData(); clearForm(); selectedSuivi = null;
            } else setStatus("❌ Erreur suppression.", true);
        }
    }

    @FXML public void handleFiltrerParSession() {
        Session sel = cbFiltreSession != null ? cbFiltreSession.getValue() : null;
        if (sel == null) { loadData(); return; }
        data.setAll(dao.findBySessionAvecDetails(sel.getIdSession()));
        tableView.setItems(data);
        calculerTaux(sel.getIdSession());
    }

    private void calculerTaux(int idSession) {
        List<SuiviPedagogique> liste = dao.findBySessionAvecDetails(idSession);
        int total = liste.size();
        if (total == 0) {
            setText(lblTauxPresence, "Présence : –");
            setText(lblTauxReussite, "Réussite : –");
            setText(lblTauxAbandon,  "Abandon : –");
            return;
        }
        long presents  = liste.stream().filter(SuiviPedagogique::isPresence).count();
        long reussis   = liste.stream().filter(s -> "Réussi".equals(s.getStatutSuivi())).count();
        long abandons  = liste.stream().filter(s -> "Abandon".equals(s.getStatutSuivi())).count();
        setText(lblTauxPresence, String.format("Présence : %.1f%%", 100.0 * presents / total));
        setText(lblTauxReussite, String.format("Réussite : %.1f%%", 100.0 * reussis / total));
        setText(lblTauxAbandon,  String.format("Abandon  : %.1f%%", 100.0 * abandons / total));
    }

    @FXML public void handleNouveauForm() { clearForm(); selectedSuivi = null; }

    private SuiviPedagogique buildFromForm() {
        SuiviPedagogique s = new SuiviPedagogique();
        s.setPresence(chkPresence != null && chkPresence.isSelected());
        try { s.setNote(Double.parseDouble(txtNote != null ? txtNote.getText().trim() : "0")); }
        catch (Exception e) { s.setNote(0); }
        s.setFeedback(txtFeedback != null ? txtFeedback.getText().trim() : "");
        s.setIdApprenant(cbApprenant != null && cbApprenant.getValue() != null
            ? cbApprenant.getValue().getIdUtilisateur() : null);
        s.setIdSession(cbSession != null && cbSession.getValue() != null
            ? cbSession.getValue().getIdSession() : null);
        return s;
    }

    private void clearForm() {
        if (chkPresence != null) chkPresence.setSelected(false);
        if (txtNote     != null) txtNote.clear();
        if (txtFeedback != null) txtFeedback.clear();
    }

    private void setStatus(String msg, boolean error) {
        if (lblStatus != null) { lblStatus.setText(msg); lblStatus.setStyle(error ? "-fx-text-fill: red;" : "-fx-text-fill: green;"); }
    }

    private void setText(Label lbl, String text) { if (lbl != null) lbl.setText(text); }
}
