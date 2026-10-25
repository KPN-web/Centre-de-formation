package Controller;

import Database.FormateurDAO;
import Database.ProgrammeDAO;
import Database.SessionDAO;
import FX.MainApp;
import metier.Formateur;
import metier.Programme;
import metier.Session;
import metier.Utilisateur;
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
import java.util.Optional;

public class SessionController {

    @FXML private TableView<Session>              tableView;
    @FXML private TableColumn<Session, String>    colLibelle;   // remplace colId
    @FXML private TableColumn<Session, LocalDate> colDateDebut;
    @FXML private TableColumn<Session, LocalDate> colDateFin;
    @FXML private TableColumn<Session, String>    colLieu;
    @FXML private TableColumn<Session, Integer>   colCapacite;
    @FXML private TableColumn<Session, String>    colStatut;
    @FXML private TableColumn<Session, String>    colValide;

    // Formulaire (RESPONSABLE/ADMIN uniquement)
    @FXML private VBox        vboxFormulaire;
    @FXML private HBox        hboxBoutons;
    @FXML private DatePicker  dpDateDebut;
    @FXML private DatePicker  dpDateFin;
    @FXML private TextField   txtLieu;
    @FXML private TextField   txtCapacite;
    @FXML private ComboBox<String>      cbStatut;
    @FXML private ComboBox<Programme>   cbProgramme;
    @FXML private ComboBox<Formateur>   cbFormateur;
    @FXML private Label                 lblStatus;

    private SessionDAO    dao          = new SessionDAO();
    private ProgrammeDAO  programmeDAO = new ProgrammeDAO();
    private FormateurDAO  formateurDAO = new FormateurDAO();

    private ObservableList<Session> data = FXCollections.observableArrayList();
    private Session selectedSession = null;

    @FXML
    public void initialize() {
        // Identification par libellé "Programme – Lieu (Date)" au lieu de l'ID
        colLibelle.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getLibelle()));
        colDateDebut.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getDateDebut()));
        colDateFin.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getDateFin()));
        colLieu.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getLieu()));
        colCapacite.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getCapaciteMax()).asObject());
        colStatut.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatut()));
        if (colValide != null) colValide.setCellValueFactory(cd -> {
            Session s = cd.getValue();
            String statut = s.getStatut();
            if (statut == null) return new SimpleStringProperty("");
            String st = statut.toUpperCase().replace(" ", "_");
            // En cours → case vide
            if (st.startsWith("EN_COURS")) return new SimpleStringProperty("");
            // Terminée → En attente (le responsable doit statuer)
            if (st.startsWith("TERMIN")) return new SimpleStringProperty("En attente");
            return new SimpleStringProperty("");
        });

        Utilisateur user = MainApp.getCurrentUser();
        String role = (user != null) ? user.getRole() : "";

        appliquerRole(role);
        loadData(role, user);

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) { selectedSession = selected; fillForm(selected); }
        });
    }

    private void appliquerRole(String role) {
        boolean isGestionnaire = role.equals("ADMIN") || role.equals("RESPONSABLE");

        if (vboxFormulaire != null) {
            vboxFormulaire.setVisible(isGestionnaire);
            vboxFormulaire.setManaged(isGestionnaire);
        }
        if (hboxBoutons != null) {
            hboxBoutons.setVisible(isGestionnaire);
            hboxBoutons.setManaged(isGestionnaire);
        }

        if (isGestionnaire) {
            if (cbStatut    != null) cbStatut.setItems(FXCollections.observableArrayList("PLANIFIEE","OUVERTE","COMPLETE","CLOTUREE"));
            if (cbProgramme != null) cbProgramme.setItems(FXCollections.observableArrayList(programmeDAO.findAll()));
            if (cbFormateur != null) cbFormateur.setItems(FXCollections.observableArrayList(formateurDAO.findAll()));
        }
    }

    private void loadData(String role, Utilisateur user) {
        if (role.equals("FORMATEUR") && user != null) {
            // Le formateur voit seulement ses sessions
            data.setAll(dao.findByFormateur(user.getIdUtilisateur()));
        } else {
            data.setAll(dao.findAll());
        }
        tableView.setItems(data);
    }

    private void fillForm(Session s) {
        if (dpDateDebut != null) dpDateDebut.setValue(s.getDateDebut());
        if (dpDateFin   != null) dpDateFin.setValue(s.getDateFin());
        if (txtLieu     != null) txtLieu.setText(s.getLieu());
        if (txtCapacite != null) txtCapacite.setText(String.valueOf(s.getCapaciteMax()));
        if (cbStatut    != null) cbStatut.setValue(s.getStatut());
        if (cbProgramme != null && s.getIdProgramme() != null)
            cbProgramme.getItems().stream()
                .filter(p -> p.getIdProgramme() == s.getIdProgramme())
                .findFirst().ifPresent(cbProgramme::setValue);
        if (cbFormateur != null && s.getIdFormateur() != null)
            cbFormateur.getItems().stream()
                .filter(f -> f.getIdUtilisateur() == s.getIdFormateur())
                .findFirst().ifPresent(cbFormateur::setValue);
    }

    @FXML

    public void handleAjouter() {
        if (!validateForm()) return;
        Session s = buildFromForm();
        if (dao.insert(s)) {
            setStatus("✅ Session créée.", false); reloadData(); clearForm();
        } else setStatus("❌ Erreur lors de la création.", true);
    }

    @FXML

    public void handleModifier() {
        if (selectedSession == null) { setStatus("⚠️ Sélectionnez une session.", true); return; }
        if (!validateForm()) return;
        Session s = buildFromForm();
        s.setIdSession(selectedSession.getIdSession());
        if (dao.update(s)) {
            setStatus("✅ Session modifiée.", false); reloadData(); clearForm();
        } else setStatus("❌ Erreur lors de la modification.", true);
    }

    
@FXML
    public void handleSupprimer() {
        if (selectedSession == null) { setStatus("⚠️ Sélectionnez une session.", true); return; }
        String lib = selectedSession.getLibelle();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer la session : " + lib + " ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.YES) {
            if (dao.delete(selectedSession.getIdSession())) {
                setStatus("✅ Session supprimée.", false);
                reloadData(); clearForm(); selectedSession = null;
            } else setStatus("❌ Erreur suppression.", true);
        }
    }

    @FXML public void handleNouveauForm() { clearForm(); selectedSession = null; }

    @FXML

    public void handleTerminer() {
        if (selectedSession == null) { setStatus("⚠️ Sélectionnez une session à terminer.", true); return; }
        String statut = selectedSession.getStatut();
        if ("TERMINEE".equalsIgnoreCase(statut) || "CLOTUREE".equalsIgnoreCase(statut)) {
            setStatus("ℹ️ Cette session est déjà terminée.", false); return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Terminer la session : " + selectedSession.getLibelle() + " ?\nLe statut passera à TERMINEE.",
            ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.YES) {
            if (dao.terminerSession(selectedSession.getIdSession())) {
                setStatus("✅ Session terminée avec succès.", false);
                reloadData(); clearForm(); selectedSession = null;
            } else setStatus("❌ Erreur lors de la clôture.", true);
        }
    }

    private boolean validateForm() {
        if (dpDateDebut == null || dpDateDebut.getValue() == null || txtLieu == null || txtLieu.getText().isEmpty()) {
            setStatus("⚠️ Date début et Lieu obligatoires.", true);
            return false;
        }
        return true;
    }

    private Session buildFromForm() {
        Session s = new Session();
        s.setDateDebut(dpDateDebut != null ? dpDateDebut.getValue() : LocalDate.now());
        s.setDateFin(dpDateFin != null && dpDateFin.getValue() != null ? dpDateFin.getValue() : LocalDate.now().plusMonths(1));
        s.setLieu(txtLieu != null ? txtLieu.getText().trim() : "");
        try { s.setCapaciteMax(Integer.parseInt(txtCapacite != null ? txtCapacite.getText().trim() : "20")); }
        catch (Exception e) { s.setCapaciteMax(20); }
        s.setStatut(cbStatut != null && cbStatut.getValue() != null ? cbStatut.getValue() : "PLANIFIEE");
        s.setIdProgramme(cbProgramme != null && cbProgramme.getValue() != null ? cbProgramme.getValue().getIdProgramme() : null);
        s.setIdFormateur(cbFormateur != null && cbFormateur.getValue() != null ? cbFormateur.getValue().getIdUtilisateur() : null);
        return s;
    }

    private void reloadData() {
        Utilisateur user = MainApp.getCurrentUser();
        if (user != null) loadData(user.getRole(), user);
    }

    private void clearForm() {
        if (dpDateDebut != null) dpDateDebut.setValue(null);
        if (dpDateFin   != null) dpDateFin.setValue(null);
        if (txtLieu     != null) txtLieu.clear();
        if (txtCapacite != null) txtCapacite.clear();
        if (cbStatut    != null) cbStatut.setValue(null);
        if (cbProgramme != null) cbProgramme.setValue(null);
        if (cbFormateur != null) cbFormateur.setValue(null);
    }

    private void setStatus(String msg, boolean error) {
        if (lblStatus != null) {
            lblStatus.setText(msg);
            lblStatus.setStyle(error ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
        }
    }
}
