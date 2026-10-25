package Controller;

import Database.ApprenantDAO;
import Database.UtilisateurDAO;
import metier.Apprenant;
import metier.Utilisateur;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.Optional;

public class UtilisateurController {

    @FXML private TableView<Utilisateur> tableView;
    @FXML private TableColumn<Utilisateur, Integer> colId;
    @FXML private TableColumn<Utilisateur, String> colNom;
    @FXML private TableColumn<Utilisateur, String> colPrenom;
    @FXML private TableColumn<Utilisateur, String> colEmail;
    @FXML private TableColumn<Utilisateur, String> colRole;
    @FXML private TableColumn<Utilisateur, Boolean> colActif;

    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtMotDePasse;
    @FXML private ComboBox<String> cbRole;
    @FXML private CheckBox chkActif;
    @FXML private Label lblStatus;

    private UtilisateurDAO dao = new UtilisateurDAO();
    private ApprenantDAO apprenantDAO = new ApprenantDAO();
    private ObservableList<Utilisateur> data = FXCollections.observableArrayList();
    private Utilisateur selectedUser = null;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getIdUtilisateur()).asObject());
        colNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));
        colPrenom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrenom()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));
        colActif.setCellValueFactory(data -> new SimpleBooleanProperty(data.getValue().isActif()));

        if (cbRole != null)
            cbRole.setItems(FXCollections.observableArrayList(
                "ADMIN", "RESPONSABLE", "FORMATEUR", "APPRENANT", "FINANCIER", "ENTREPRISE", "SERVICE_FINANCIER"));

        loadData();

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) { selectedUser = selected; fillForm(selected); }
        });
    }

    private void loadData() {
        data.setAll(dao.findAll());
        tableView.setItems(data);
    }

    private void fillForm(Utilisateur u) {
        txtNom.setText(u.getNom());
        txtPrenom.setText(u.getPrenom());
        txtEmail.setText(u.getEmail());
        txtMotDePasse.setText(u.getMotDePasse());
        if (cbRole != null) cbRole.setValue(u.getRole());
        if (chkActif != null) chkActif.setSelected(u.isActif());
    }

    @FXML
    public void handleAjouter() {
        if (!validateForm()) return;
        Utilisateur u = buildFromForm();

        if ("APPRENANT".equals(u.getRole())) {
            // Insérer dans utilisateurs + table apprenant
            Apprenant a = new Apprenant();
            a.setNom(u.getNom());
            a.setPrenom(u.getPrenom());
            a.setEmail(u.getEmail());
            a.setMotDePasse(u.getMotDePasse());
            a.setDateCreation(u.getDateCreation());
            a.setActif(u.isActif());
            a.setMatricule("MAT" + System.currentTimeMillis() % 10000);
            a.setTelephone("");
            a.setAdresse("");
            a.setDateInscription(LocalDate.now());
            a.setIdEntreprise(null);
            if (apprenantDAO.insert(a)) {
                setStatus("✅ Apprenant ajouté (visible dans l'onglet Apprenants).", false);
                loadData(); clearForm();
            } else setStatus("❌ Erreur lors de l'ajout (email déjà utilisé?).", true);
        } else {
            if (dao.insert(u)) {
                setStatus("✅ Utilisateur ajouté.", false);
                loadData(); clearForm();
            } else setStatus("❌ Erreur lors de l'ajout (email déjà utilisé?).", true);
        }
    }

    @FXML
    public void handleModifier() {
        if (selectedUser == null) { setStatus("⚠️ Sélectionnez un utilisateur.", true); return; }
        if (!validateForm()) return;
        Utilisateur u = buildFromForm();
        u.setIdUtilisateur(selectedUser.getIdUtilisateur());
        if (dao.update(u)) {
            setStatus("✅ Utilisateur modifié.", false);
            loadData(); clearForm();
        } else setStatus("❌ Erreur modification.", true);
    }

    @FXML
    public void handleSupprimer() {
        if (selectedUser == null) { setStatus("⚠️ Sélectionnez un utilisateur.", true); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer " + selectedUser.getNomComplet() + " ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.YES) {
            if (dao.delete(selectedUser.getIdUtilisateur())) {
                setStatus("✅ Utilisateur supprimé.", false);
                loadData(); clearForm(); selectedUser = null;
            } else setStatus("❌ Erreur suppression.", true);
        }
    }

    @FXML public void handleNouveauForm() { clearForm(); selectedUser = null; }

    private boolean validateForm() {
        if (txtNom.getText().isEmpty() || txtEmail.getText().isEmpty()) {
            setStatus("⚠️ Nom et Email obligatoires.", true); return false;
        }
        return true;
    }

    private Utilisateur buildFromForm() {
        Utilisateur u = new Utilisateur();
        u.setNom(txtNom.getText().trim());
        u.setPrenom(txtPrenom.getText().trim());
        u.setEmail(txtEmail.getText().trim());
        u.setMotDePasse(txtMotDePasse.getText().trim());
        u.setRole(cbRole != null && cbRole.getValue() != null ? cbRole.getValue() : "APPRENANT");
        u.setDateCreation(LocalDate.now());
        u.setActif(chkActif == null || chkActif.isSelected());
        return u;
    }

    private void clearForm() {
        txtNom.clear(); txtPrenom.clear(); txtEmail.clear(); txtMotDePasse.clear();
        if (cbRole != null) cbRole.setValue(null);
        if (chkActif != null) chkActif.setSelected(true);
    }

    private void setStatus(String msg, boolean error) {
        if (lblStatus != null) {
            lblStatus.setText(msg);
            lblStatus.setStyle(error ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
        }
    }
}