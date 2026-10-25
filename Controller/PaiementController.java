package Controller;

import Database.*;
import FX.MainApp;
import metier.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PaiementController {

    @FXML private TabPane tabPane;
    @FXML private Tab tabApprenants, tabFormateurs, tabEntreprises;

    @FXML private TableView<LigneEntrepriseFinance> tableEntrepriseFinance;
    @FXML private TableColumn<LigneEntrepriseFinance, String> colEFEntreprise, colEFNom, colEFPrenom, colEFFormation, colEFPrix, colEFPaye, colEFReste;
    @FXML private ComboBox<Entreprise> cbFiltreEntrepriseF;
    @FXML private Label lblTotalEntreprise;

    @FXML private TableView<Paiement> tableApprenants, tableFormateurs;
    @FXML private TableColumn<Paiement, String>    colAId, colAMode, colAStatut, colAApprenant, colFId, colFMode, colFStatut, colFDesc, colFFormateur;
    @FXML private TableColumn<Paiement, Double>    colAMontant, colFMontant;
    @FXML private TableColumn<Paiement, LocalDate> colADate, colFDate;

    @FXML private VBox vboxFormA, vboxFormF;
    @FXML private HBox hboxBoutonsA, hboxBoutonsF;
    @FXML private ComboBox<Apprenant>  cbApprenant;
    @FXML private ComboBox<Inscription> cbInscription;
    @FXML private ComboBox<Entreprise> cbEntreprisePaiement;
    @FXML private javafx.scene.layout.VBox vboxEntreprisePaiement;
    @FXML private Label lblInfoPaieur;
    @FXML private ComboBox<Formateur>  cbFormateur;
    @FXML private ComboBox<String>     cbModeA, cbStatutA, cbModeF, cbStatutF, cbMotif;
    @FXML private TextField            txtMontantA, txtMontantF;
    @FXML private Label                lblStatusA, lblTotalA, lblStatusF, lblTotalF, lblStatutFormateur, lblTotalDuFormateur;

    private final PaiementDAO    dao            = new PaiementDAO();
    private final ApprenantDAO   apprenantDAO   = new ApprenantDAO();
    private final FormateurDAO   formateurDAO   = new FormateurDAO();
    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();
    private final EntrepriseDAO  entrepriseDAO  = new EntrepriseDAO();
    private final ProgrammeDAO   programmeDAO   = new ProgrammeDAO();

    private List<Apprenant> tousApprenants;
    private final ObservableList<Paiement> dataA = FXCollections.observableArrayList();
    private final ObservableList<Paiement> dataF = FXCollections.observableArrayList();
    private Paiement selectedA, selectedF;

    @FXML
    public void initialize() {
        Utilisateur user = MainApp.getCurrentUser();
        String role = user != null ? user.getRole() : "";
        tousApprenants = apprenantDAO.findAll();

        Apprenant ac = role.equals("APPRENANT") ? apprenantDAO.findById(user.getIdUtilisateur()) : null;
        boolean apprenantEntreprise = ac != null && ac.getIdEntreprise() != null;

        initColsApprenants();
        initColsFormateurs();

        if (apprenantEntreprise) {
            if (tabPane != null) {
                tabPane.getTabs().clear();
                Tab t = new Tab("Information"); t.setClosable(false);
                Label l = new Label("Votre formation est prise en charge par votre entreprise.\nVous ne pouvez pas effectuer de paiement vous-même.");
                l.setStyle("-fx-font-size:14px;-fx-text-fill:#555;-fx-padding:30;");
                t.setContent(l); tabPane.getTabs().add(t);
            }
        } else if (role.equals("APPRENANT")) {
            hide(vboxFormA); hide(hboxBoutonsA); hide(vboxFormF); hide(hboxBoutonsF);
            if (tabPane != null) tabPane.getTabs().remove(tabFormateurs);
        } else {
            initCombos();
            if (tableApprenants != null)
                tableApprenants.getSelectionModel().selectedItemProperty().addListener((o, v, s) -> { if (s != null) { selectedA = s; fillFormA(s); } });
            if (tableFormateurs != null)
                tableFormateurs.getSelectionModel().selectedItemProperty().addListener((o, v, s) -> { if (s != null) { selectedF = s; fillFormF(s); } });
            if (cbFormateur != null)
                cbFormateur.valueProperty().addListener((o, v, s) -> actualiserStatutFormateur());
        }

        loadDataA(); loadDataF();
        if (!apprenantEntreprise) initOngletEntreprises();
    }

    private void hide(javafx.scene.Node n) { if (n != null) { n.setVisible(false); n.setManaged(false); } }

    private String nomApprenant(Integer id) {
        if (id == null) return "–";
        return tousApprenants.stream().filter(a -> a.getIdUtilisateur() == id).map(Apprenant::getNomComplet).findFirst().orElse("ID " + id);
    }

    private String nomFormateur(Integer id) {
        if (id == null) return "–";
        return formateurDAO.findAll().stream().filter(f -> f.getIdUtilisateur() == id).map(Formateur::getNomComplet).findFirst().orElse("ID " + id);
    }

    private void initColsApprenants() {
        if (colAId       != null) colAId.setCellValueFactory(cd -> new SimpleStringProperty(nomApprenant(cd.getValue().getIdApprenant())));
        if (colAApprenant!= null) colAApprenant.setCellValueFactory(cd -> new SimpleStringProperty(nomApprenant(cd.getValue().getIdApprenant())));
        if (colAMontant  != null) colAMontant.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().getMontant()).asObject());
        if (colADate     != null) colADate.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getDatePaiement()));
        if (colAMode     != null) colAMode.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getModePaiement()));
        if (colAStatut   != null) colAStatut.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatutPaiement()));
    }

    private void initColsFormateurs() {
        if (colFId      != null) colFId.setCellValueFactory(cd -> new SimpleStringProperty(nomFormateur(cd.getValue().getIdFormateur())));
        if (colFFormateur!= null) colFFormateur.setCellValueFactory(cd -> new SimpleStringProperty(nomFormateur(cd.getValue().getIdFormateur())));
        if (colFMontant != null) colFMontant.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().getMontant()).asObject());
        if (colFDate    != null) colFDate.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getDatePaiement()));
        if (colFMode    != null) colFMode.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getModePaiement()));
        if (colFStatut  != null) colFStatut.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatutPaiement()));
        if (colFDesc    != null) colFDesc.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDescription()));
    }

    private void initCombos() {
        List<String> modes = List.of("ESPECES","CHEQUE","VIREMENT","MOBILE_MONEY","CARTE");
        List<String> stats = List.of("EN_ATTENTE","VALIDE","REFUSE","REMBOURSE");
        if (cbModeA    != null) cbModeA.setItems(FXCollections.observableArrayList(modes));
        if (cbStatutA  != null) cbStatutA.setItems(FXCollections.observableArrayList(stats));
        if (cbApprenant!= null) cbApprenant.setItems(FXCollections.observableArrayList(tousApprenants));
        // cbInscription : vide au départ, se remplit quand on choisit un apprenant
        if (cbInscription != null) cbInscription.setItems(FXCollections.observableArrayList());
        if (cbEntreprisePaiement != null) cbEntreprisePaiement.setItems(FXCollections.observableArrayList(entrepriseDAO.findAll()));
        // Listener : quand on change d'apprenant, filtrer cbInscription ET gérer section entreprise
        if (cbApprenant != null) cbApprenant.valueProperty().addListener((obs, oldV, newA) -> {
            mettreAJourSectionEntreprise(newA);
            if (cbInscription != null) {
                if (newA != null) {
                    List<metier.Inscription> inscs = inscriptionDAO.findByApprenantAvecDetails(newA.getIdUtilisateur());
                    cbInscription.setItems(FXCollections.observableArrayList(inscs));
                    if (inscs.size() == 1) cbInscription.setValue(inscs.get(0));
                } else {
                    cbInscription.setItems(FXCollections.observableArrayList());
                    cbInscription.setValue(null);
                }
            }
        });
        if (cbModeF    != null) cbModeF.setItems(FXCollections.observableArrayList(modes));
        if (cbStatutF  != null) cbStatutF.setItems(FXCollections.observableArrayList(stats));
        if (cbFormateur!= null) cbFormateur.setItems(FXCollections.observableArrayList(formateurDAO.findAll()));
        if (cbMotif    != null) cbMotif.setItems(FXCollections.observableArrayList("Salaire","Prime","Vacation","Remboursement","Autre"));
    }

    private void loadDataA() {
        if (tableApprenants == null) return;
        Utilisateur user = MainApp.getCurrentUser();
        String role = user != null ? user.getRole() : "";
        if (role.equals("APPRENANT")) {
            dataA.setAll(dao.findByApprenant(user.getIdUtilisateur()));
        } else if (role.equals("ENTREPRISE") && user instanceof EntrepriseCliente) {
            Entreprise e = entrepriseDAO.findAll().stream()
                .filter(x -> x.getNom() != null && x.getNom().equalsIgnoreCase(((EntrepriseCliente) user).getNomEntreprise()))
                .findFirst().orElse(null);
            if (e != null) {
                List<Integer> ids = apprenantDAO.findByEntrepriseId(e.getIdEntreprise()).stream().map(Apprenant::getIdUtilisateur).collect(Collectors.toList());
                dataA.setAll(dao.findPaiementsApprenants().stream().filter(p -> p.getIdApprenant() != null && ids.contains(p.getIdApprenant())).collect(Collectors.toList()));
            } else dataA.clear();
        } else {
            dataA.setAll(dao.findPaiementsApprenants());
        }
        tableApprenants.setItems(dataA);
        if (lblTotalA != null) lblTotalA.setText(String.format("Total encaissé : %,.0f FCFA", dao.getTotalChiffreAffaires()));
    }

    private void loadDataF() {
        if (tableFormateurs != null) { dataF.setAll(dao.findPaiementsFormateurs()); tableFormateurs.setItems(dataF); }
        if (lblTotalF != null) lblTotalF.setText(String.format("Total salaires versés : %,.0f FCFA", dao.getTotalSalairesFormateurs()));
        actualiserStatutFormateur();
    }

    private void actualiserStatutFormateur() {
        if (cbFormateur == null || cbFormateur.getValue() == null) {
            if (lblStatutFormateur  != null) { lblStatutFormateur.setText(""); lblStatutFormateur.setStyle(""); }
            if (lblTotalDuFormateur != null) lblTotalDuFormateur.setText("");
            return;
        }
        Formateur f = cbFormateur.getValue();
        if (lblTotalDuFormateur != null)
            lblTotalDuFormateur.setText(String.format("Total versé à ce formateur : %,.0f FCFA", dao.getTotalMontantFormateur(f.getIdUtilisateur())));
        List<Paiement> pF = dao.findByFormateur(f.getIdUtilisateur());
        if (lblStatutFormateur != null) {
            if      (pF.isEmpty())                                                        { lblStatutFormateur.setText("🔴 Formateur non payé");             lblStatutFormateur.setStyle("-fx-text-fill:#e74c3c;-fx-font-weight:bold;-fx-font-size:13;"); }
            else if (pF.stream().allMatch(p -> "VALIDE".equals(p.getStatutPaiement())))   { lblStatutFormateur.setText("✅ Formateur payé");                  lblStatutFormateur.setStyle("-fx-text-fill:#27ae60;-fx-font-weight:bold;-fx-font-size:13;"); }
            else                                                                          { lblStatutFormateur.setText("🟡 Paiement partiel / en attente");  lblStatutFormateur.setStyle("-fx-text-fill:#e67e22;-fx-font-weight:bold;-fx-font-size:13;"); }
        }
    }

    private void fillFormA(Paiement p) {
        if (txtMontantA != null) txtMontantA.setText(String.valueOf(p.getMontant()));
        if (cbModeA     != null) cbModeA.setValue(p.getModePaiement());
        if (cbStatutA   != null) cbStatutA.setValue(p.getStatutPaiement());
        if (p.getIdApprenant() != null && cbApprenant != null) {
            cbApprenant.getItems().stream().filter(a -> a.getIdUtilisateur() == p.getIdApprenant()).findFirst().ifPresent(a -> {
                cbApprenant.setValue(a);
                mettreAJourSectionEntreprise(a);
            });
        }
    }

    private void fillFormF(Paiement p) {
        if (txtMontantF != null) txtMontantF.setText(String.valueOf(p.getMontant()));
        if (cbModeF != null) cbModeF.setValue(p.getModePaiement());
        if (cbStatutF != null) cbStatutF.setValue(p.getStatutPaiement());
        if (cbMotif != null) cbMotif.setValue(p.getDescription());
        if (p.getIdFormateur() != null && cbFormateur != null)
            cbFormateur.getItems().stream().filter(f -> f.getIdUtilisateur() == p.getIdFormateur()).findFirst().ifPresent(cbFormateur::setValue);
    }

    @FXML public void handleAjouterA() {
        if (cbApprenant == null || cbApprenant.getValue() == null) { setStatusA("⚠️ Sélectionnez un apprenant.", true); return; }
        if (txtMontantA == null || txtMontantA.getText().isEmpty()) { setStatusA("⚠️ Montant obligatoire.", true); return; }
        if (dao.insert(buildPaiementA())) { setStatusA("✅ Paiement enregistré.", false); loadDataA(); clearFormA(); }
        else setStatusA("❌ Erreur lors de l'enregistrement.", true);
    }

    @FXML public void handleModifierA() {
        if (selectedA == null) { setStatusA("⚠️ Sélectionnez un paiement.", true); return; }
        Paiement p = buildPaiementA(); p.setIdPaiement(selectedA.getIdPaiement());
        if (dao.update(p)) { setStatusA("✅ Paiement modifié.", false); loadDataA(); clearFormA(); }
        else setStatusA("❌ Erreur modification.", true);
    }

    @FXML public void handleSupprimerA() {
        if (selectedA == null) { setStatusA("⚠️ Sélectionnez un paiement.", true); return; }
        if (confirmer() && dao.delete(selectedA.getIdPaiement())) { setStatusA("✅ Supprimé.", false); loadDataA(); clearFormA(); selectedA = null; }
        else if (selectedA != null) setStatusA("❌ Erreur suppression.", true);
    }

    @FXML public void handleNouveauA() { clearFormA(); selectedA = null; }

    @FXML public void handleAjouterF() {
        if (cbFormateur == null || cbFormateur.getValue() == null) { setStatusF("⚠️ Sélectionnez un formateur.", true); return; }
        if (txtMontantF == null || txtMontantF.getText().isEmpty()) { setStatusF("⚠️ Montant obligatoire.", true); return; }
        if (dao.insert(buildPaiementF())) { setStatusF("✅ Salaire enregistré.", false); loadDataF(); clearFormF(); }
        else setStatusF("❌ Erreur lors de l'enregistrement.", true);
    }

    @FXML public void handleModifierF() {
        if (selectedF == null) { setStatusF("⚠️ Sélectionnez un paiement.", true); return; }
        Paiement p = buildPaiementF(); p.setIdPaiement(selectedF.getIdPaiement());
        if (dao.update(p)) { setStatusF("✅ Paiement modifié.", false); loadDataF(); clearFormF(); }
        else setStatusF("❌ Erreur modification.", true);
    }

    @FXML public void handleSupprimerF() {
        if (selectedF == null) { setStatusF("⚠️ Sélectionnez un paiement.", true); return; }
        if (confirmer() && dao.delete(selectedF.getIdPaiement())) { setStatusF("✅ Supprimé.", false); loadDataF(); clearFormF(); selectedF = null; }
        else if (selectedF != null) setStatusF("❌ Erreur suppression.", true);
    }

    @FXML public void handleNouveauF() { clearFormF(); selectedF = null; }

    private boolean confirmer() {
        return new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce paiement ?", ButtonType.YES, ButtonType.NO)
            .showAndWait().filter(b -> b == ButtonType.YES).isPresent();
    }

    private Paiement buildPaiementA() {
        Paiement p = new Paiement();
        try { p.setMontant(Double.parseDouble(txtMontantA.getText().trim())); } catch (Exception e) { p.setMontant(0); }
        p.setDatePaiement(LocalDate.now());
        p.setModePaiement(val(cbModeA, "ESPECES")); p.setStatutPaiement(val(cbStatutA, "EN_ATTENTE")); p.setTypePaiement("APPRENANT");
        if (cbApprenant != null && cbApprenant.getValue() != null) {
            Apprenant a = cbApprenant.getValue();
            p.setIdApprenant(a.getIdUtilisateur());
            // Si apprenant d'entreprise : le payeur est l'entreprise
            if (a.getIdEntreprise() != null && cbEntreprisePaiement != null && cbEntreprisePaiement.getValue() != null) {
                Entreprise ent = cbEntreprisePaiement.getValue();
                p.setEmailPayeur(ent.getEmail() != null ? ent.getEmail() : a.getEmail());
                p.setDescription("Paiement entreprise : " + ent.getNom());
            } else {
                p.setEmailPayeur(a.getEmail());
            }
        }
        if (cbInscription != null && cbInscription.getValue() != null) p.setIdInscription(cbInscription.getValue().getIdInscription());
        return p;
    }

    private Paiement buildPaiementF() {
        Paiement p = new Paiement();
        try { p.setMontant(Double.parseDouble(txtMontantF.getText().trim())); } catch (Exception e) { p.setMontant(0); }
        p.setDatePaiement(LocalDate.now());
        p.setModePaiement(val(cbModeF, "VIREMENT")); p.setStatutPaiement(val(cbStatutF, "EN_ATTENTE")); p.setTypePaiement("FORMATEUR");
        if (cbFormateur != null && cbFormateur.getValue() != null) { p.setIdFormateur(cbFormateur.getValue().getIdUtilisateur()); p.setEmailPayeur(cbFormateur.getValue().getEmail()); }
        p.setDescription(val(cbMotif, ""));
        return p;
    }

    private <T> String val(ComboBox<T> cb, String def) { return (cb != null && cb.getValue() != null) ? cb.getValue().toString() : def; }

    private void clearFormA() {
        if (txtMontantA != null) txtMontantA.clear();
        if (cbModeA != null) cbModeA.setValue(null); if (cbStatutA != null) cbStatutA.setValue(null);
        if (cbApprenant != null) cbApprenant.setValue(null); if (cbInscription != null) cbInscription.setValue(null);
        if (cbEntreprisePaiement != null) cbEntreprisePaiement.setValue(null);
        mettreAJourSectionEntreprise(null);
    }

    /**
     * Affiche ou masque la section "Entreprise payeur" selon le type d'apprenant sélectionné.
     * - Apprenant libre (idEntreprise == null) : section masquée, paiement en son nom propre
     * - Apprenant entreprise : section affichée, ComboBox pré-rempli avec son entreprise
     */
    private void mettreAJourSectionEntreprise(Apprenant a) {
        if (vboxEntreprisePaiement == null) return;
        if (a != null && a.getIdEntreprise() != null) {
            // Apprenant lié à une entreprise → afficher la section
            vboxEntreprisePaiement.setVisible(true);
            vboxEntreprisePaiement.setManaged(true);
            if (lblInfoPaieur != null) {
                lblInfoPaieur.setText("🏢 Formation payée par l'entreprise");
                lblInfoPaieur.setStyle("-fx-text-fill:#1565c0;-fx-font-weight:bold;-fx-font-size:13;");
            }
            // Pré-sélectionner l'entreprise de l'apprenant
            if (cbEntreprisePaiement != null) {
                cbEntreprisePaiement.getItems().stream()
                    .filter(e -> e.getIdEntreprise() == a.getIdEntreprise())
                    .findFirst()
                    .ifPresent(cbEntreprisePaiement::setValue);
            }
        } else {
            // Apprenant libre → masquer la section
            vboxEntreprisePaiement.setVisible(false);
            vboxEntreprisePaiement.setManaged(false);
            if (lblInfoPaieur != null) {
                lblInfoPaieur.setText("🌟 Paiement personnel (apprenant libre)");
                lblInfoPaieur.setStyle("-fx-text-fill:#2e7d32;-fx-font-weight:bold;-fx-font-size:13;");
            }
            if (cbEntreprisePaiement != null) cbEntreprisePaiement.setValue(null);
        }
    }

    private void clearFormF() {
        if (txtMontantF != null) txtMontantF.clear();
        if (cbModeF != null) cbModeF.setValue(null); if (cbStatutF != null) cbStatutF.setValue(null);
        if (cbFormateur != null) cbFormateur.setValue(null); if (cbMotif != null) cbMotif.setValue(null);
    }

    private void setStatusA(String m, boolean e) { if (lblStatusA != null) { lblStatusA.setText(m); lblStatusA.setStyle(e ? "-fx-text-fill:red;" : "-fx-text-fill:green;"); } }
    private void setStatusF(String m, boolean e) { if (lblStatusF != null) { lblStatusF.setText(m); lblStatusF.setStyle(e ? "-fx-text-fill:red;" : "-fx-text-fill:green;"); } }

    private void initOngletEntreprises() {
        if (colEFEntreprise != null) colEFEntreprise.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().nomEntreprise));
        if (colEFNom        != null) colEFNom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().nom));
        if (colEFPrenom     != null) colEFPrenom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().prenom));
        if (colEFFormation  != null) colEFFormation.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().formation));
        if (colEFPrix  != null) colEFPrix.setCellValueFactory(d -> new SimpleStringProperty(String.format("%,.0f FCFA", d.getValue().prixFormation)));
        if (colEFPaye  != null) colEFPaye.setCellValueFactory(d -> new SimpleStringProperty(String.format("%,.0f FCFA", d.getValue().montantPaye)));
        if (colEFReste != null) colEFReste.setCellValueFactory(d -> new SimpleStringProperty(String.format("%,.0f FCFA", d.getValue().reste)));
        if (cbFiltreEntrepriseF != null) {
            List<Entreprise> toutes = entrepriseDAO.findAll();
            Entreprise tout = new Entreprise(); tout.setIdEntreprise(0); tout.setNom("— Toutes —");
            toutes.add(0, tout);
            cbFiltreEntrepriseF.setItems(FXCollections.observableArrayList(toutes));
            cbFiltreEntrepriseF.setValue(tout);
            cbFiltreEntrepriseF.setOnAction(e -> chargerEntreprisesFinance());
        }
        chargerEntreprisesFinance();
    }

    private void chargerEntreprisesFinance() {
        if (tableEntrepriseFinance == null) return;

        Integer idF = (cbFiltreEntrepriseF != null
                && cbFiltreEntrepriseF.getValue() != null
                && cbFiltreEntrepriseF.getValue().getIdEntreprise() != 0)
            ? cbFiltreEntrepriseF.getValue().getIdEntreprise() : null;

        List<Entreprise> liste = idF != null
            ? List.of(entrepriseDAO.findById(idF))
            : entrepriseDAO.findAll();

        ObservableList<LigneEntrepriseFinance> lignes = FXCollections.observableArrayList();

        // Totaux cohérents :
        //   totalFacture = paye + reste  (ce que l'apprenant doit au total)
        //   totalPaye    = paiements VALIDE uniquement
        //   totalReste   = totalFacture - totalPaye
        double totalFacture = 0, totalPaye = 0, totalReste = 0;

        for (Entreprise e : liste) {
            if (e == null) continue;
            List<Apprenant> apprenants = entrepriseDAO.findApprenantsByEntreprise(e.getIdEntreprise());

            for (Apprenant a : apprenants) {
                // Montants calculés une seule fois par apprenant (source unique de vérité)
                double payeA  = dao.getMontantPayeParApprenant(a.getIdUtilisateur());
                double resteA = dao.getMontantRestantParApprenant(a.getIdUtilisateur());
                double factureA = payeA + resteA;   // tarif total réel

                totalPaye    += payeA;
                totalReste   += resteA;
                totalFacture += factureA;

                List<Inscription> inscriptions = inscriptionDAO.findByApprenantAvecDetails(a.getIdUtilisateur());

                if (inscriptions.isEmpty()) {
                    // Apprenant sans inscription : on affiche quand même une ligne avec ses montants
                    lignes.add(new LigneEntrepriseFinance(
                        e.getNom(), a.getNom(),
                        a.getPrenom() != null ? a.getPrenom() : "",
                        "–", factureA, payeA, resteA));
                } else {
                    for (Inscription insc : inscriptions) {
                        // Prix formation = tarif du programme lié à la session
                        double prixFormation = 0;
                        if (insc.getSession() != null && insc.getSession().getIdProgramme() != null) {
                            Programme prog = programmeDAO.findById(insc.getSession().getIdProgramme());
                            if (prog != null) prixFormation = prog.getTarif();
                        }
                        // Si prix toujours à 0 (lien cassé), on utilise le montant réel comme fallback
                        if (prixFormation == 0) prixFormation = factureA;

                        String sessionLib = (insc.getSession() != null)
                            ? insc.getSession().getLibelle() : "–";

                        lignes.add(new LigneEntrepriseFinance(
                            e.getNom(), a.getNom(),
                            a.getPrenom() != null ? a.getPrenom() : "",
                            sessionLib, prixFormation, payeA, resteA));
                    }
                }
            }
        }

        tableEntrepriseFinance.setItems(lignes);

        // Total affiché = somme de ce qui est facturé (paye + reste), pas juste les tarifs
        if (lblTotalEntreprise != null)
            lblTotalEntreprise.setText(String.format(
                "Total facturé : %,.0f FCFA  |  Payé : %,.0f FCFA  |  Reste : %,.0f FCFA",
                totalFacture, totalPaye, totalReste));
    }

    public static class LigneEntrepriseFinance {
        String nomEntreprise, nom, prenom, formation;
        double prixFormation, montantPaye, reste;
        LigneEntrepriseFinance(String ne, String n, String p, String f, double pf, double mp, double r) {
            nomEntreprise=ne; nom=n; prenom=p; formation=f; prixFormation=pf; montantPaye=mp; reste=r;
        }
    }

    // ─── Génération de documents ──────────────────────────────────────────

    /** Génère un reçu HTML pour le paiement apprenant sélectionné */
    @FXML public void handleGenererRecuApprenant() {
        if (selectedA == null) {
            setStatusA("⚠️ Sélectionnez d'abord un paiement dans la liste.", true); return;
        }
        try {
            // Récupérer le nom de l'apprenant
            String nomApp = nomApprenant(selectedA.getIdApprenant());

            // Récupérer la formation liée (via inscription)
            String nomFormation = "–";
            double prixFormation = 0;
            if (selectedA.getIdApprenant() != null) {
                List<metier.Inscription> inscs = inscriptionDAO.findByApprenantAvecDetails(selectedA.getIdApprenant());
                if (!inscs.isEmpty()) {
                    metier.Inscription insc = inscs.get(0);
                    if (insc.getSession() != null) {
                        nomFormation = insc.getSession().getLibelle() != null ? insc.getSession().getLibelle() : "–";
                        if (insc.getSession().getIdProgramme() != null) {
                            metier.Programme prog = programmeDAO.findById(insc.getSession().getIdProgramme());
                            if (prog != null) { nomFormation = prog.getIntitule(); prixFormation = prog.getTarif(); }
                        }
                    }
                }
            }

            java.io.File doc = DocumentGenerator.genererRecuPaiement(selectedA, nomApp, nomFormation, prixFormation);
            setStatusA("✅ Reçu généré : " + doc.getName(), false);
            DocumentGenerator.ouvrirDocument(doc);
            javafx.scene.control.Alert info = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION,
                "✅ Reçu généré avec succès !\n\nFichier : " + doc.getAbsolutePath() +
                "\n\nLe document s'ouvre dans votre navigateur.\nVous pouvez l'imprimer (Ctrl+P).",
                javafx.scene.control.ButtonType.OK);
            info.setHeaderText("Document généré");
            info.showAndWait();
        } catch (Exception e) {
            setStatusA("❌ Erreur lors de la génération : " + e.getMessage(), true);
        }
    }

    /** Génère une facture HTML pour l'entreprise filtrée dans l'onglet Finance */
    @FXML public void handleGenererFactureEntreprise() {
        if (tableEntrepriseFinance == null || tableEntrepriseFinance.getItems().isEmpty()) {
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING,
                "⚠️ Aucune donnée à facturer. Sélectionnez d'abord une entreprise.", javafx.scene.control.ButtonType.OK)
                .showAndWait(); return;
        }
        try {
            // Déterminer le nom de l'entreprise depuis le filtre
            Entreprise entFiltre = cbFiltreEntrepriseF != null ? cbFiltreEntrepriseF.getValue() : null;
            String nomEnt   = (entFiltre != null && entFiltre.getIdEntreprise() != 0) ? entFiltre.getNom() : "Toutes les entreprises";
            String emailEnt = (entFiltre != null && entFiltre.getEmail() != null) ? entFiltre.getEmail() : "—";

            java.util.List<double[]> vals = new java.util.ArrayList<>();
            java.util.List<String[]> dets = new java.util.ArrayList<>();
            double totalFacture = 0, totalPaye = 0, totalReste = 0;

            for (LigneEntrepriseFinance l : tableEntrepriseFinance.getItems()) {
                dets.add(new String[]{l.nom + " " + l.prenom, l.formation});
                vals.add(new double[]{l.prixFormation, l.montantPaye, l.reste});
                totalFacture += l.prixFormation;
                totalPaye    += l.montantPaye;
                totalReste   += l.reste;
            }

            java.io.File doc = DocumentGenerator.genererFactureEntreprise(
                nomEnt, emailEnt, vals, dets, totalFacture, totalPaye, totalReste);
            DocumentGenerator.ouvrirDocument(doc);
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION,
                "✅ Facture générée !\n\nFichier : " + doc.getAbsolutePath() +
                "\n\nLe document s'ouvre dans votre navigateur.\nVous pouvez l'imprimer (Ctrl+P).",
                javafx.scene.control.ButtonType.OK).showAndWait();
        } catch (Exception e) {
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR,
                "❌ Erreur : " + e.getMessage(), javafx.scene.control.ButtonType.OK).showAndWait();
        }
    }
}