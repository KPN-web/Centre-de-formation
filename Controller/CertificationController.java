package Controller;

import Database.ApprenantDAO;
import Database.CertificationDAO;
import Database.SessionDAO;
import FX.MainApp;
import metier.Apprenant;
import metier.Certification;
import metier.Session;
import metier.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class CertificationController {

    @FXML private TableView<Certification>              tableView;
    @FXML private TableColumn<Certification, String>    colApprenant;
    @FXML private TableColumn<Certification, String>    colType;
    @FXML private TableColumn<Certification, LocalDate> colDate;
    @FXML private TableColumn<Certification, String>    colSession;
    @FXML private TableColumn<Certification, String>    colPdf;

    // Ces colonnes sont déclarées mais optionnelles selon le FXML original
    @FXML private TableColumn<Certification, String>    colId;

    @FXML private VBox                vboxFormulaire;
    @FXML private HBox                hboxBoutons;
    @FXML private ComboBox<String>    cbTypeDocument;
    @FXML private TextField           txtFichierPdf;
    @FXML private ComboBox<Apprenant> cbApprenant;
    @FXML private ComboBox<Session>   cbSession;
    @FXML private Label               lblStatus;

    private CertificationDAO dao          = new CertificationDAO();
    private ApprenantDAO     apprenantDAO = new ApprenantDAO();
    private SessionDAO       sessionDAO   = new SessionDAO();
    private List<Apprenant>  tousApprenants;
    private ObservableList<Certification> data = FXCollections.observableArrayList();
    private Certification selectedCert = null;

    // Types de documents
    private static final String[] TYPES = {
        "Certificat de formation",
        "Attestation de présence",
        "Relevé de notes",
        "Convention de formation",
        "Archive numérique"
    };

    @FXML
    public void initialize() {
        tousApprenants = apprenantDAO.findAll();

        if (colApprenant != null)
            colApprenant.setCellValueFactory(cd -> {
                Integer idA = cd.getValue().getIdApprenant();
                if (idA == null) return new SimpleStringProperty("–");
                return new SimpleStringProperty(tousApprenants.stream()
                    .filter(a -> a.getIdUtilisateur() == idA)
                    .map(Apprenant::getNomComplet).findFirst().orElse("ID " + idA));
            });
        if (colType != null)
            colType.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTypeCertificat()));
        if (colDate != null)
            colDate.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getDateEmission()));
        if (colSession != null)
            colSession.setCellValueFactory(cd -> {
                Integer idS = cd.getValue().getIdSession();
                if (idS == null) return new SimpleStringProperty("–");
                Session s = sessionDAO.findById(idS);
                return new SimpleStringProperty(s != null ? s.getLibelle() : "Session " + idS);
            });
        if (colPdf != null)
            colPdf.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getFichierPDF()));
        if (colId != null)
            colId.setCellValueFactory(cd -> {
                Integer idA = cd.getValue().getIdApprenant();
                if (idA == null) return new SimpleStringProperty("–");
                return new SimpleStringProperty(tousApprenants.stream()
                    .filter(a -> a.getIdUtilisateur() == idA)
                    .map(Apprenant::getNomComplet).findFirst().orElse("ID " + idA));
            });

        // Types de documents dans le combo
        if (cbTypeDocument != null)
            cbTypeDocument.setItems(FXCollections.observableArrayList(TYPES));

        // Masquer formulaire pour APPRENANT
        Utilisateur user = MainApp.getCurrentUser();
        String role = (user != null) ? user.getRole() : "";
        boolean lectureSeule = role.equals("APPRENANT");
        if (lectureSeule) {
            if (vboxFormulaire != null) { vboxFormulaire.setVisible(false); vboxFormulaire.setManaged(false); }
            if (hboxBoutons    != null) { hboxBoutons.setVisible(false);    hboxBoutons.setManaged(false); }
        } else {
            if (cbApprenant != null) cbApprenant.setItems(FXCollections.observableArrayList(tousApprenants));
            if (cbSession   != null) cbSession.setItems(FXCollections.observableArrayList(sessionDAO.findAll()));
        }

        loadData();

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) { selectedCert = sel; fillForm(sel); }
        });
    }

    private void loadData() {
        data.setAll(dao.findAll());
        tableView.setItems(data);
    }

    private void fillForm(Certification c) {
        if (cbTypeDocument!= null) cbTypeDocument.setValue(c.getTypeCertificat());
        if (txtFichierPdf != null) txtFichierPdf.setText(c.getFichierPDF() != null ? c.getFichierPDF() : "");
    }

    @FXML public void handleAjouter() {
        if (cbTypeDocument == null || cbTypeDocument.getValue() == null) {
            setStatus("⚠️ Type de document obligatoire.", true); return;
        }
        Certification c = buildFromForm();
        if (dao.insert(c)) { setStatus("✅ Document délivré.", false); loadData(); clearForm(); }
        else setStatus("❌ Erreur lors de la délivrance.", true);
    }

    @FXML public void handleModifier() {
        if (selectedCert == null) { setStatus("⚠️ Sélectionnez un document.", true); return; }
        Certification c = buildFromForm(); c.setIdCertification(selectedCert.getIdCertification());
        if (dao.update(c)) { setStatus("✅ Document modifié.", false); loadData(); clearForm(); }
        else setStatus("❌ Erreur modification.", true);
    }

    @FXML public void handleSupprimer() {
        if (selectedCert == null) { setStatus("⚠️ Sélectionnez un document.", true); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce document ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> r = a.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.YES) {
            if (dao.delete(selectedCert.getIdCertification())) {
                setStatus("✅ Supprimé.", false); loadData(); clearForm(); selectedCert = null;
            } else setStatus("❌ Erreur suppression.", true);
        }
    }

    private Certification buildFromForm() {
        Certification c = new Certification();
        c.setTypeCertificat(cbTypeDocument != null ? cbTypeDocument.getValue() : "");
        c.setDateEmission(LocalDate.now());
        c.setFichierPDF(txtFichierPdf != null ? txtFichierPdf.getText().trim() : "");
        c.setIdApprenant(cbApprenant != null && cbApprenant.getValue() != null
            ? cbApprenant.getValue().getIdUtilisateur() : null);
        c.setIdSession(cbSession != null && cbSession.getValue() != null
            ? cbSession.getValue().getIdSession() : null);
        return c;
    }

    private void clearForm() {
        if (cbTypeDocument!= null) cbTypeDocument.setValue(null);
        if (txtFichierPdf != null) txtFichierPdf.clear();
        if (cbApprenant   != null) cbApprenant.setValue(null);
        if (cbSession     != null) cbSession.setValue(null);
    }

    @FXML public void handleGenererDocument() {
        if (selectedCert == null) { setStatus("⚠️ Sélectionnez un document dans la liste.", true); return; }
        try {
            Integer idA = selectedCert.getIdApprenant();
            String nomApprenant = tousApprenants.stream()
                .filter(a -> a.getIdUtilisateur() == (idA != null ? idA : -1))
                .map(metier.Apprenant::getNomComplet).findFirst().orElse("Apprenant");

            String formation = "–";
            String sessionLib = "–";
            LocalDate dateFin = LocalDate.now();
            if (selectedCert.getIdSession() != null) {
                metier.Session s = sessionDAO.findById(selectedCert.getIdSession());
                if (s != null) {
                    sessionLib = s.getLibelle();
                    if (s.getDateFin() != null) dateFin = s.getDateFin();
                    if (s.getIdProgramme() != null) {
                        Database.ProgrammeDAO progDAO = new Database.ProgrammeDAO();
                        metier.Programme p = progDAO.findById(s.getIdProgramme());
                        if (p != null) formation = p.getIntitule();
                    }
                }
            }
            String type = selectedCert.getTypeCertificat();
            String resultat = type != null && type.contains("Certificat") ? "ADMIS" : "PARTICIPATION";

            File doc = DocumentGenerator.genererAttestationFormation(
                nomApprenant, formation, resultat, sessionLib, dateFin);
            DocumentGenerator.ouvrirDocument(doc);
            setStatus("✅ Document généré : " + doc.getName(), false);

            javafx.scene.control.Alert info = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION,
                "✅ Document généré !\n\nFichier : " + doc.getAbsolutePath() +
                "\n\nLe document s'ouvre dans votre navigateur.\nVous pouvez l'imprimer (Ctrl+P).",
                javafx.scene.control.ButtonType.OK);
            info.setHeaderText("Document généré");
            info.showAndWait();
        } catch (Exception e) {
            setStatus("❌ Erreur génération : " + e.getMessage(), true);
        }
    }

    private void setStatus(String msg, boolean error) {
        if (lblStatus != null) { lblStatus.setText(msg); lblStatus.setStyle(error ? "-fx-text-fill: red;" : "-fx-text-fill: green;"); }
    }
}
