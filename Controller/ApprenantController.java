package Controller;

import Database.ApprenantDAO;
import Database.EntrepriseDAO;
import Database.InscriptionDAO;
import Database.ProgrammeDAO;
import FX.MainApp;
import metier.Apprenant;
import metier.Entreprise;
import metier.Inscription;
import metier.Programme;
import metier.Utilisateur;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ApprenantController {

    // ── Table principale ─────────────────────────────────────────────────
    @FXML private TableView<Apprenant>            tableView;
    @FXML private TableColumn<Apprenant, Integer> colId;
    @FXML private TableColumn<Apprenant, String>  colNom;
    @FXML private TableColumn<Apprenant, String>  colPrenom;
    @FXML private TableColumn<Apprenant, String>  colEmail;
    @FXML private TableColumn<Apprenant, String>  colMatricule;
    @FXML private TableColumn<Apprenant, String>  colTelephone;
    @FXML private TableColumn<Apprenant, String>  colTypeApprenant;
    @FXML private TableColumn<Apprenant, String>  colEntreprise;

    // ── Formulaire ADMIN/RESPONSABLE ─────────────────────────────────────
    @FXML private VBox      vboxFormulaire;
    @FXML private HBox      hboxBoutons;
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtMotDePasse;
    @FXML private TextField txtMatricule;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtAdresse;
    @FXML private RadioButton rbApprenanLibre;
    @FXML private RadioButton rbApprenantEntreprise;
    @FXML private VBox        vboxEntrepriseSelection;
    @FXML private ComboBox<Entreprise> cbEntreprise;
    @FXML private TextField   txtRecherche;
    @FXML private Label       lblStatus;

    // ── Panneau ENTREPRISE : gestion des membres ─────────────────────────
    @FXML private VBox                   vboxEntrepriseMembers;
    @FXML private TableView<Apprenant>   tableMembers;
    @FXML private TableColumn<Apprenant, String> colMNom;
    @FXML private TableColumn<Apprenant, String> colMPrenom;
    @FXML private TableColumn<Apprenant, String> colMEmail;
    @FXML private TableColumn<Apprenant, String> colMTelephone;
    @FXML private TableColumn<Apprenant, String> colMMatricule;
    @FXML private TableColumn<Apprenant, String> colMPrixFormation;
    @FXML private TextField txtMembreNom;
    @FXML private TextField txtMembrePrenom;
    @FXML private TextField txtMembreEmail;
    @FXML private TextField txtMembreTelephone;
    @FXML private Label     lblMembreStatus;

    // ── DAOs ─────────────────────────────────────────────────────────────
    private final ApprenantDAO  dao          = new ApprenantDAO();
    private final EntrepriseDAO entrepriseDAO = new EntrepriseDAO();

    private final ToggleGroup tgType = new ToggleGroup();

    private ObservableList<Apprenant> data = FXCollections.observableArrayList();
    private Apprenant  selectedApprenant = null;
    private Apprenant  selectedMembre    = null;
    private Entreprise monEntreprise     = null; // entreprise de l'utilisateur connecté

    @FXML
    public void initialize() {
        // Colonnes table principale
        if (colId        != null) colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getIdUtilisateur()).asObject());
        if (colNom       != null) colNom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNom()));
        if (colPrenom    != null) colPrenom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPrenom()));
        if (colEmail     != null) colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        if (colMatricule != null) colMatricule.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMatricule()));
        if (colTelephone != null) colTelephone.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getTelephone() != null ? d.getValue().getTelephone() : ""));
        if (colTypeApprenant != null) colTypeApprenant.setCellValueFactory(d -> {
            Integer idE = d.getValue().getIdEntreprise();
            return new SimpleStringProperty(idE != null ? "🏢 Entreprise" : "🌟 Libre");
        });
        if (colEntreprise != null) colEntreprise.setCellValueFactory(d -> {
            Integer idE = d.getValue().getIdEntreprise();
            if (idE == null) return new SimpleStringProperty("—");
            Entreprise e = entrepriseDAO.findById(idE);
            return new SimpleStringProperty(e != null ? e.getNom() : "—");
        });

        // Colonnes table membres (panneau entreprise)
        if (colMNom       != null) colMNom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNom()));
        if (colMPrenom    != null) colMPrenom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPrenom()));
        if (colMEmail     != null) colMEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        if (colMTelephone != null) colMTelephone.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getTelephone() != null ? d.getValue().getTelephone() : ""));
        if (colMMatricule != null) colMMatricule.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getMatricule() != null ? d.getValue().getMatricule() : ""));

        // Sélection dans la table membres
        if (tableMembers != null) {
            tableMembers.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
                if (sel != null) selectedMembre = sel;
            });
        }

        // Toggle group type apprenant
        if (rbApprenanLibre       != null) rbApprenanLibre.setToggleGroup(tgType);
        if (rbApprenantEntreprise != null) rbApprenantEntreprise.setToggleGroup(tgType);
        if (tgType.getSelectedToggle() == null && rbApprenanLibre != null)
            rbApprenanLibre.setSelected(true);

        // Permissions selon le rôle
        Utilisateur user = MainApp.getCurrentUser();
        String role = (user != null) ? user.getRole() : "";
        appliquerRole(role, user);

        loadData(role, user);

        if (cbEntreprise != null)
            cbEntreprise.setItems(FXCollections.observableArrayList(entrepriseDAO.findAll()));

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) { selectedApprenant = selected; fillForm(selected); }
        });

        // Recherche filtrée
        if (txtRecherche != null) {
            FilteredList<Apprenant> filtered = new FilteredList<>(data, p -> true);
            txtRecherche.textProperty().addListener((obs, oldVal, newVal) ->
                filtered.setPredicate(a -> {
                    if (newVal == null || newVal.isEmpty()) return true;
                    String lower = newVal.toLowerCase();
                    return a.getNom().toLowerCase().contains(lower)
                        || (a.getPrenom()   != null && a.getPrenom().toLowerCase().contains(lower))
                        || (a.getEmail()    != null && a.getEmail().toLowerCase().contains(lower))
                        || (a.getMatricule()!= null && a.getMatricule().toLowerCase().contains(lower));
                }));
            tableView.setItems(filtered);
        } else {
            tableView.setItems(data);
        }
    }

    // ─── Permissions ─────────────────────────────────────────────────────
    private void appliquerRole(String role, Utilisateur user) {
        boolean isEntreprise   = role.equals("ENTREPRISE");
        boolean isGestionnaire = role.equals("ADMIN") || role.equals("RESPONSABLE");
        boolean lectureSeule   = role.equals("SERVICE_FINANCIER") || role.equals("FINANCIER");

        // Panneau membres → ENTREPRISE uniquement
        setVisible(vboxEntrepriseMembers, isEntreprise);

        // Formulaire CRUD → ADMIN / RESPONSABLE uniquement
        setVisible(vboxFormulaire, isGestionnaire);
        setVisible(hboxBoutons,    isGestionnaire);

        // Si ENTREPRISE : trouver et mémoriser l'entreprise liée
        if (isEntreprise && user != null) {
            monEntreprise = entrepriseDAO.findAll().stream()
                .filter(e -> e.getEmail() != null && e.getEmail().equalsIgnoreCase(user.getEmail()))
                .findFirst().orElseGet(() ->
                    entrepriseDAO.findAll().stream()
                        .filter(e -> e.getNom() != null && e.getNom().equalsIgnoreCase(user.getNom()))
                        .findFirst().orElse(null));

            if (monEntreprise == null) {
                setStatus(lblMembreStatus, "⚠️ Votre entreprise n'est pas trouvée en base.", true);
            }
        }
    }

    // ─── Chargement données ───────────────────────────────────────────────
    private void loadData(String role, Utilisateur user) {
        if (role.equals("ENTREPRISE")) {
            // L'entreprise voit uniquement ses membres dans la table principale
            if (monEntreprise != null) {
                data.setAll(dao.findByEntrepriseId(monEntreprise.getIdEntreprise()));
            } else {
                data.clear();
            }
            chargerTableMembres();
        } else {
            data.setAll(dao.findAll());
        }
    }

    // ─── Charger la table des membres ────────────────────────────────────
    private void chargerTableMembres() {
        if (tableMembers == null || monEntreprise == null) return;
        List<Apprenant> membres = dao.findByEntrepriseId(monEntreprise.getIdEntreprise());
        tableMembers.setItems(FXCollections.observableArrayList(membres));
    }

    // ─── ENTREPRISE : ajouter un membre ──────────────────────────────────
    @FXML
    public void handleAjouterMembre() {
        if (monEntreprise == null) {
            setStatus(lblMembreStatus, "⚠️ Entreprise non identifiée.", true); return;
        }
        String nom    = txtMembreNom    != null ? txtMembreNom.getText().trim()    : "";
        String prenom = txtMembrePrenom != null ? txtMembrePrenom.getText().trim() : "";
        String email  = txtMembreEmail  != null ? txtMembreEmail.getText().trim()  : "";
        String tel    = txtMembreTelephone != null ? txtMembreTelephone.getText().trim() : "";

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
            setStatus(lblMembreStatus, "⚠️ Nom, Prénom et Email sont obligatoires.", true); return;
        }

        // Vérifier que l'email n'existe pas déjà
        boolean emailExiste = dao.findAll().stream()
            .anyMatch(a -> a.getEmail() != null && a.getEmail().equalsIgnoreCase(email));
        if (emailExiste) {
            setStatus(lblMembreStatus, "⚠️ Cet email est déjà utilisé par un autre apprenant.", true); return;
        }

        Apprenant membre = new Apprenant();
        membre.setNom(nom);
        membre.setPrenom(prenom);
        membre.setEmail(email);
        membre.setTelephone(tel);
        // Mot de passe par défaut = prénom en minuscule + "123"
        membre.setMotDePasse(prenom.toLowerCase() + "123");
        membre.setMatricule("MAT" + monEntreprise.getIdEntreprise() + "_" + System.currentTimeMillis() % 10000);
        membre.setAdresse("");
        membre.setDateInscription(LocalDate.now());
        membre.setDateCreation(LocalDate.now());
        membre.setActif(true);
        // Lier à l'entreprise
        membre.setIdEntreprise(monEntreprise.getIdEntreprise());

        if (dao.insert(membre)) {
            setStatus(lblMembreStatus,
                "✅ Membre ajouté : " + prenom + " " + nom +
                " (mot de passe provisoire : " + prenom.toLowerCase() + "123)", false);
            clearFormMembre();
            chargerTableMembres();
            // Rafraîchir aussi la table principale
            data.setAll(dao.findByEntrepriseId(monEntreprise.getIdEntreprise()));
        } else {
            setStatus(lblMembreStatus, "❌ Erreur lors de l'ajout. Email déjà utilisé ?", true);
        }
    }

    // ─── ENTREPRISE : retirer un membre ──────────────────────────────────
    @FXML
    public void handleRetirerMembre() {
        if (selectedMembre == null) {
            setStatus(lblMembreStatus, "⚠️ Sélectionnez un membre dans la liste.", true); return;
        }
        if (monEntreprise == null) {
            setStatus(lblMembreStatus, "⚠️ Entreprise non identifiée.", true); return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Retirer " + selectedMembre.getNomComplet() + " de votre entreprise ?\n" +
            "Son compte apprenant sera conservé mais dissocié de votre entreprise.",
            ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.YES) {
            // Dissocier (setIdEntreprise = null) sans supprimer le compte
            selectedMembre.setIdEntreprise(null);
            if (dao.update(selectedMembre)) {
                setStatus(lblMembreStatus,
                    "✅ " + selectedMembre.getNomComplet() + " a été retiré de votre entreprise.", false);
                selectedMembre = null;
                chargerTableMembres();
                data.setAll(dao.findByEntrepriseId(monEntreprise.getIdEntreprise()));
            } else {
                setStatus(lblMembreStatus, "❌ Erreur lors du retrait.", true);
            }
        }
    }

    // ─── ENTREPRISE : actualiser ──────────────────────────────────────────
    @FXML
    public void handleActualiserMembres() {
        chargerTableMembres();
        if (monEntreprise != null)
            data.setAll(dao.findByEntrepriseId(monEntreprise.getIdEntreprise()));
        setStatus(lblMembreStatus, "✅ Liste actualisée.", false);
    }

    // ─── Toggle type apprenant (formulaire ADMIN) ─────────────────────────
    @FXML
    public void handleTypeApprenantChange() {
        boolean entreprise = rbApprenantEntreprise != null && rbApprenantEntreprise.isSelected();
        setVisible(vboxEntrepriseSelection, entreprise);
        if (!entreprise && cbEntreprise != null) cbEntreprise.setValue(null);
    }

    // ─── CRUD ADMIN/RESPONSABLE ───────────────────────────────────────────
    @FXML public void handleAjouter() {
        if (!validateForm()) return;
        Apprenant a = buildFromForm();
        a.setDateCreation(LocalDate.now());
        a.setDateInscription(LocalDate.now());
        a.setActif(true);
        a.setMatricule(txtMatricule != null && !txtMatricule.getText().isEmpty()
            ? txtMatricule.getText() : "MAT" + System.currentTimeMillis() % 10000);
        if (dao.insert(a)) {
            setStatus(lblStatus, "✅ Apprenant ajouté avec succès.", false);
            reloadAll(); clearForm();
        } else setStatus(lblStatus, "❌ Erreur lors de l'ajout.", true);
    }

    @FXML public void handleModifier() {
        if (selectedApprenant == null) { setStatus(lblStatus, "⚠️ Sélectionnez un apprenant.", true); return; }
        if (!validateForm()) return;
        Apprenant a = buildFromForm();
        a.setIdUtilisateur(selectedApprenant.getIdUtilisateur());
        a.setActif(selectedApprenant.isActif());
        if (dao.update(a)) {
            setStatus(lblStatus, "✅ Apprenant modifié avec succès.", false);
            reloadAll(); clearForm();
        } else setStatus(lblStatus, "❌ Erreur lors de la modification.", true);
    }

    @FXML public void handleSupprimer() {
        if (selectedApprenant == null) { setStatus(lblStatus, "⚠️ Sélectionnez un apprenant.", true); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer " + selectedApprenant.getNomComplet() + " ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.YES) {
            if (dao.delete(selectedApprenant.getIdUtilisateur())) {
                setStatus(lblStatus, "✅ Apprenant supprimé.", false);
                reloadAll(); clearForm(); selectedApprenant = null;
            } else setStatus(lblStatus, "❌ Erreur lors de la suppression.", true);
        }
    }

    @FXML public void handleNouveauForm() { clearForm(); selectedApprenant = null; }

    // ─── Helpers ──────────────────────────────────────────────────────────
    private void reloadAll() {
        Utilisateur user = MainApp.getCurrentUser();
        String role = (user != null) ? user.getRole() : "";
        loadData(role, user);
    }

    private boolean validateForm() {
        if (txtNom == null || txtNom.getText().isEmpty()
            || txtPrenom == null || txtPrenom.getText().isEmpty()
            || txtEmail == null || txtEmail.getText().isEmpty()) {
            setStatus(lblStatus, "⚠️ Nom, Prénom et Email sont obligatoires.", true); return false;
        }
        if (rbApprenantEntreprise != null && rbApprenantEntreprise.isSelected()
            && (cbEntreprise == null || cbEntreprise.getValue() == null)) {
            setStatus(lblStatus, "⚠️ Veuillez sélectionner une entreprise.", true); return false;
        }
        return true;
    }

    private Apprenant buildFromForm() {
        Apprenant a = new Apprenant();
        a.setNom(txtNom.getText().trim());
        a.setPrenom(txtPrenom       != null ? txtPrenom.getText().trim()       : "");
        a.setEmail(txtEmail         != null ? txtEmail.getText().trim()         : "");
        a.setMotDePasse(txtMotDePasse != null ? txtMotDePasse.getText().trim() : "");
        a.setMatricule(txtMatricule  != null ? txtMatricule.getText().trim()   : "");
        a.setTelephone(txtTelephone  != null ? txtTelephone.getText().trim()   : "");
        a.setAdresse(txtAdresse      != null ? txtAdresse.getText().trim()     : "");
        boolean isEnt = rbApprenantEntreprise != null && rbApprenantEntreprise.isSelected();
        a.setIdEntreprise(isEnt && cbEntreprise != null && cbEntreprise.getValue() != null
            ? cbEntreprise.getValue().getIdEntreprise() : null);
        a.setDateInscription(LocalDate.now());
        return a;
    }

    private void fillForm(Apprenant a) {
        if (txtNom        != null) txtNom.setText(a.getNom());
        if (txtPrenom     != null) txtPrenom.setText(a.getPrenom());
        if (txtEmail      != null) txtEmail.setText(a.getEmail());
        if (txtMotDePasse != null) txtMotDePasse.setText(a.getMotDePasse());
        if (txtMatricule  != null) txtMatricule.setText(a.getMatricule());
        if (txtTelephone  != null) txtTelephone.setText(a.getTelephone() != null ? a.getTelephone() : "");
        if (txtAdresse    != null) txtAdresse.setText(a.getAdresse()     != null ? a.getAdresse()    : "");
        boolean hasEnt = a.getIdEntreprise() != null;
        if (rbApprenanLibre       != null) rbApprenanLibre.setSelected(!hasEnt);
        if (rbApprenantEntreprise != null) rbApprenantEntreprise.setSelected(hasEnt);
        setVisible(vboxEntrepriseSelection, hasEnt);
        if (cbEntreprise != null && hasEnt) {
            cbEntreprise.getItems().stream()
                .filter(e -> e.getIdEntreprise() == a.getIdEntreprise())
                .findFirst().ifPresent(cbEntreprise::setValue);
        } else if (cbEntreprise != null) {
            cbEntreprise.setValue(null);
        }
    }

    private void clearForm() {
        if (txtNom        != null) txtNom.clear();
        if (txtPrenom     != null) txtPrenom.clear();
        if (txtEmail      != null) txtEmail.clear();
        if (txtMotDePasse != null) txtMotDePasse.clear();
        if (txtMatricule  != null) txtMatricule.clear();
        if (txtTelephone  != null) txtTelephone.clear();
        if (txtAdresse    != null) txtAdresse.clear();
        if (rbApprenanLibre != null) rbApprenanLibre.setSelected(true);
        setVisible(vboxEntrepriseSelection, false);
        if (cbEntreprise != null) cbEntreprise.setValue(null);
    }

    private void clearFormMembre() {
        if (txtMembreNom       != null) txtMembreNom.clear();
        if (txtMembrePrenom    != null) txtMembrePrenom.clear();
        if (txtMembreEmail     != null) txtMembreEmail.clear();
        if (txtMembreTelephone != null) txtMembreTelephone.clear();
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
}