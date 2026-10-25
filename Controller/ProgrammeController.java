package Controller;

import Database.ProgrammeDAO;
import FX.MainApp;
import metier.Programme;
import metier.Utilisateur;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class ProgrammeController {

    @FXML private TableView<Programme> tableView;
    @FXML private TableColumn<Programme, Integer> colId;
    @FXML private TableColumn<Programme, String>  colIntitule;
    @FXML private TableColumn<Programme, String>  colModalite;
    @FXML private TableColumn<Programme, Integer> colDuree;
    @FXML private TableColumn<Programme, Double>  colTarif;
    @FXML private TableColumn<Programme, Boolean> colPublie;

    // Éléments du formulaire (peuvent être null si masqués)
    @FXML private VBox      vboxFormulaire;
    @FXML private TextField txtIntitule;
    @FXML private TextArea  txtObjectifs;
    @FXML private TextArea  txtPrerequis;
    @FXML private TextField txtDuree;
    @FXML private ComboBox<String> cbModalite;
    @FXML private TextField txtTarif;
    @FXML private CheckBox  chkPublie;
    @FXML private Label     lblStatus;
    @FXML private HBox      hboxBoutons;

    private ProgrammeDAO dao = new ProgrammeDAO();
    private ObservableList<Programme> data = FXCollections.observableArrayList();
    private Programme selectedProgramme = null;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getIdProgramme()).asObject());
        colIntitule.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIntitule()));
        colModalite.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getModalite()));
        colDuree.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getDuree()).asObject());
        colTarif.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getTarif()).asObject());
        colPublie.setCellValueFactory(data -> new SimpleBooleanProperty(data.getValue().isPublie()));

        if (cbModalite != null)
            cbModalite.setItems(FXCollections.observableArrayList("présentiel", "distanciel", "hybride"));

        // Masquer le formulaire pour APPRENANT et FORMATEUR
        Utilisateur user = MainApp.getCurrentUser();
        String role = (user != null) ? user.getRole() : "";
        boolean lectureSeule = role.equals("APPRENANT") || role.equals("FORMATEUR") || role.equals("ENTREPRISE");
        if (lectureSeule) {
            if (vboxFormulaire != null) { vboxFormulaire.setVisible(false); vboxFormulaire.setManaged(false); }
            if (hboxBoutons    != null) { hboxBoutons.setVisible(false);    hboxBoutons.setManaged(false); }
        }

        loadData();

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) { selectedProgramme = selected; fillForm(selected); }
        });
    }

    private void loadData() {
        data.setAll(dao.findAll());
        tableView.setItems(data);
    }

    private void fillForm(Programme p) {
        if (txtIntitule != null) txtIntitule.setText(p.getIntitule());
        if (txtObjectifs != null) txtObjectifs.setText(p.getObjectifs());
        if (txtPrerequis != null) txtPrerequis.setText(p.getPrerequis());
        if (txtDuree != null) txtDuree.setText(String.valueOf(p.getDuree()));
        if (cbModalite != null) cbModalite.setValue(p.getModalite());
        if (txtTarif != null) txtTarif.setText(String.valueOf(p.getTarif()));
        if (chkPublie != null) chkPublie.setSelected(p.isPublie());
    }

    @FXML
    public void handleAjouter() {
        if (!validateForm()) return;
        Programme p = buildFromForm();
        if (dao.insert(p)) {
            setStatus("✅ Programme ajouté.", false);
            loadData(); clearForm();
        } else setStatus("❌ Erreur lors de l'ajout.", true);
    }

    @FXML
    public void handleModifier() {
        if (selectedProgramme == null) { setStatus("⚠️ Sélectionnez un programme.", true); return; }
        if (!validateForm()) return;
        Programme p = buildFromForm();
        p.setIdProgramme(selectedProgramme.getIdProgramme());
        if (dao.update(p)) {
            setStatus("✅ Programme modifié.", false);
            loadData(); clearForm();
        } else setStatus("❌ Erreur lors de la modification.", true);
    }

    @FXML
    public void handleSupprimer() {
        if (selectedProgramme == null) { setStatus("⚠️ Sélectionnez un programme.", true); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer \"" + selectedProgramme.getIntitule() + "\" ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.YES) {
            if (dao.delete(selectedProgramme.getIdProgramme())) {
                setStatus("✅ Programme supprimé.", false);
                loadData(); clearForm(); selectedProgramme = null;
            } else setStatus("❌ Erreur lors de la suppression.", true);
        }
    }

    @FXML public void handleNouveauForm() { clearForm(); selectedProgramme = null; }

    private boolean validateForm() {
        if (txtIntitule == null || txtIntitule.getText().isEmpty()) {
            setStatus("⚠️ L'intitulé est obligatoire.", true); return false;
        }
        return true;
    }

    private Programme buildFromForm() {
        Programme p = new Programme();
        p.setIntitule(txtIntitule.getText().trim());
        p.setObjectifs(txtObjectifs != null ? txtObjectifs.getText().trim() : "");
        p.setPrerequis(txtPrerequis != null ? txtPrerequis.getText().trim() : "");
        try { p.setDuree(Integer.parseInt(txtDuree.getText().trim())); } catch (Exception e) { p.setDuree(0); }
        p.setModalite(cbModalite != null ? cbModalite.getValue() : "présentiel");
        try { p.setTarif(Double.parseDouble(txtTarif.getText().trim())); } catch (Exception e) { p.setTarif(0); }
        p.setTotalAPayer(p.getTarif());
        p.setPublie(chkPublie != null && chkPublie.isSelected());
        p.setArchive(false);
        return p;
    }

    private void clearForm() {
        if (txtIntitule != null) txtIntitule.clear();
        if (txtDuree    != null) txtDuree.clear();
        if (txtTarif    != null) txtTarif.clear();
        if (txtObjectifs != null) txtObjectifs.clear();
        if (txtPrerequis != null) txtPrerequis.clear();
        if (cbModalite  != null) cbModalite.setValue(null);
        if (chkPublie   != null) chkPublie.setSelected(false);
    }

    private void setStatus(String msg, boolean error) {
        if (lblStatus != null) {
            lblStatus.setText(msg);
            lblStatus.setStyle(error ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
        }
    }
}
