package Controller;

import Database.*;
import FX.MainApp;
import metier.*;
import javafx.beans.property.*;
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

public class EntrepriseController {

    // ── KPI ─────────────────────────────────────────────────────────────
    @FXML private Label lblNbApprenants;
    @FXML private Label lblNbSessions;
    @FXML private Label lblTotalFacture;
    @FXML private Label lblTotalPaye;
    @FXML private Label lblResteAPayer;
    @FXML private Label lblSatisfaction;
    @FXML private VBox  cardTotalFacture;
    @FXML private VBox  cardTotalPaye;
    @FXML private VBox  cardResteAPayer;

    // ── Titre page ───────────────────────────────────────────────────────
    @FXML private Label lblTitrePage;

    // ── Onglet Mes Apprenants ────────────────────────────────────────────
    @FXML private TableView<LigneApprenant>           tableApprenants;
    @FXML private TableColumn<LigneApprenant, String> colNom;
    @FXML private TableColumn<LigneApprenant, String> colPrenom;
    @FXML private TableColumn<LigneApprenant, String> colSession;
    @FXML private TableColumn<LigneApprenant, String> colStatut;
    @FXML private TableColumn<LigneApprenant, String> colResultat;
    @FXML private TableColumn<LigneApprenant, String> colMontantPaye;
    @FXML private TableColumn<LigneApprenant, String> colResteAPayer;

    // ── Onglet Formations disponibles ────────────────────────────────────
    @FXML private TableView<Programme>               tableProgrammes;
    @FXML private TableColumn<Programme, String>     colPIntitule;
    @FXML private TableColumn<Programme, Integer>    colPDuree;
    @FXML private TableColumn<Programme, String>     colPTarif;
    @FXML private TableColumn<Programme, String>     colPNiveau;

    // ── Onglet Paiements ─────────────────────────────────────────────────
    @FXML private TableView<Paiement>                tablePaiements;
    @FXML private TableColumn<Paiement, String>      colPApprenant;
    @FXML private TableColumn<Paiement, String>      colPMontant;
    @FXML private TableColumn<Paiement, LocalDate>   colPDate;
    @FXML private TableColumn<Paiement, String>      colPMode;
    @FXML private TableColumn<Paiement, String>      colPStatut;

    // ── Onglet Finance ───────────────────────────────────────────────────
    @FXML private TableView<LigneFinance>            tableFinance;
    @FXML private TableColumn<LigneFinance, String>  colFNom;
    @FXML private TableColumn<LigneFinance, String>  colFPrenom;
    @FXML private TableColumn<LigneFinance, String>  colFFormation;
    @FXML private TableColumn<LigneFinance, String>  colFPrixFormation;
    @FXML private TableColumn<LigneFinance, String>  colFPaye;
    @FXML private TableColumn<LigneFinance, String>  colFReste;
    @FXML private Label lblTotalFinance;
    @FXML private Label lblFinTotalFacture;
    @FXML private Label lblFinTotalPaye;
    @FXML private Label lblFinResteAPayer;

    // ── Onglet Gestion Entreprises ───────────────────────────────────────
    @FXML private Tab                                tabGestionEntreprises;
    @FXML private TableView<Entreprise>              tableEntreprises;
    @FXML private TableColumn<Entreprise, String>    colENom;
    @FXML private TableColumn<Entreprise, String>    colEAdresse;
    @FXML private TableColumn<Entreprise, String>    colETelephone;
    @FXML private TableColumn<Entreprise, String>    colEEmail;
    @FXML private TableColumn<Entreprise, String>    colEContact;
    @FXML private VBox  vboxFormulaireEntreprise;
    @FXML private HBox  hboxBoutonsEntreprise;
    @FXML private TextField txtENom;
    @FXML private TextField txtEAdresse;
    @FXML private TextField txtETelephone;
    @FXML private TextField txtEEmail;
    @FXML private TextField txtEContact;
    @FXML private Label lblEStatus;

    // ── Onglet Accueil ───────────────────────────────────────────────────
    @FXML private TabPane tabPane;
    @FXML private Tab     tabAccueil;
    @FXML private Tab     tabMesMembres;
    @FXML private Label   lblAccueilTitre;
    @FXML private Label   lblAccueilSousTitre;
    @FXML private Label   lblKpiApprenants;
    @FXML private Label   lblKpiPaye;
    @FXML private Label   lblKpiReste;
    @FXML private Label   lblKpiSessions;

    // ── Onglet Mes Membres (ENTREPRISE) ─────────────────────────────────
    @FXML private TextField txtMembreNomE;
    @FXML private TextField txtMembrePrenomE;
    @FXML private TextField txtMembreEmailE;
    @FXML private TextField txtMembreTelephoneE;
    @FXML private Label     lblMembreStatusE;
    @FXML private TableView<Apprenant>              tableMembresEntreprise;
    @FXML private TableColumn<Apprenant, String>    colMENom;
    @FXML private TableColumn<Apprenant, String>    colMEPrenom;
    @FXML private TableColumn<Apprenant, String>    colMEEmail;
    @FXML private TableColumn<Apprenant, String>    colMETelephone;
    @FXML private TableColumn<Apprenant, String>    colMEMatricule;
    @FXML private TableColumn<Apprenant, String>    colMEPrixFormation;

    private Apprenant selectedMembreE = null;

    // ── Filtre entreprise ────────────────────────────────────────────────
    @FXML private ComboBox<Entreprise> cbFiltreEntreprise;
    @FXML private Label lblFiltreLabel;

    // ── DAOs ─────────────────────────────────────────────────────────────
    private final ApprenantDAO   apprenantDAO   = new ApprenantDAO();
    private final InscriptionDAO inscriptionDAO = new InscriptionDAO();
    private final SessionDAO     sessionDAO     = new SessionDAO();
    private final ProgrammeDAO   programmeDAO   = new ProgrammeDAO();
    private final PaiementDAO    paiementDAO    = new PaiementDAO();
    private final EntrepriseDAO  entrepriseDAO  = new EntrepriseDAO();
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    private Entreprise selectedEntreprise = null;

    @FXML
    public void initialize() {
        setupTableApprenants();
        setupTableProgrammes();
        setupTablePaiements();
        setupTableFinance();
        setupTableEntreprises();
        setupTableMembresEntreprise();

        Utilisateur user = MainApp.getCurrentUser();
        String role = (user != null) ? user.getRole() : "";

        appliquerPermissions(user, role);
        charger();
        initAccueil(user, role);

        if (tableEntreprises != null) {
            tableEntreprises.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> { if (sel != null) { selectedEntreprise = sel; fillFormulaireEntreprise(sel); } });
        }
        if (tableMembresEntreprise != null) {
            tableMembresEntreprise.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> { if (sel != null) selectedMembreE = sel; });
        }
    }

    // ─── Onglet Accueil ──────────────────────────────────────────────
    private void initAccueil(Utilisateur user, String role) {
        boolean isEntreprise = role.equals("ENTREPRISE");

        // Titre de bienvenue
        String nomAffiche = isEntreprise ? user.getNom() : "Espace Entreprises";
        if (lblAccueilTitre    != null) lblAccueilTitre.setText("Bienvenue, " + nomAffiche + " 👋");
        if (lblAccueilSousTitre!= null) lblAccueilSousTitre.setText(isEntreprise ? "Tableau de bord de votre entreprise" : "Vue globale toutes entreprises");

        // KPI
        Integer idEnt = getIdEntrepriseFiltre(user, role);
        List<Apprenant> apprenants = (idEnt != null) ? apprenantDAO.findByEntrepriseId(idEnt) : apprenantDAO.findAll();
        long nbSessionsActives = sessionDAO.findAll().stream()
            .filter(s -> "EN_COURS".equalsIgnoreCase(s.getStatut()) || "EN COURS".equalsIgnoreCase(s.getStatut()))
            .count();

        double totalPaye = 0, totalReste = 0;
        for (Apprenant a : apprenants) {
            totalPaye  += paiementDAO.getMontantPayeParApprenant(a.getIdUtilisateur());
            totalReste += paiementDAO.getMontantRestantParApprenant(a.getIdUtilisateur());
        }

        if (lblKpiApprenants != null) lblKpiApprenants.setText(String.valueOf(apprenants.size()));
        if (lblKpiPaye       != null) lblKpiPaye.setText(String.format("%,.0f FCFA", totalPaye));
        if (lblKpiReste      != null) lblKpiReste.setText(String.format("%,.0f FCFA", totalReste));
        if (lblKpiSessions   != null) lblKpiSessions.setText(String.valueOf(nbSessionsActives));

        // Sélectionner l'onglet Accueil en premier
        if (tabPane != null && tabAccueil != null)
            tabPane.getSelectionModel().select(tabAccueil);

        // Charger la table membres si rôle ENTREPRISE
        if (isEntreprise) {
            Entreprise monEnt = trouverEntrepriseDuUser(user);
            if (monEnt != null) chargerTableMembresEntreprise(monEnt.getIdEntreprise());
        }
    }

    @FXML public void handleGoApprenants() { selectTab(1); }
    @FXML public void handleGoFormations() { selectTab(2); }
    @FXML public void handleGoPaiements()  { selectTab(3); }
    @FXML public void handleGoFinance()    { selectTab(4); }

    private void selectTab(int index) {
        if (tabPane != null && tabPane.getTabs().size() > index)
            tabPane.getSelectionModel().select(index);
    }

    /**
     * Applique les restrictions d'accès selon le rôle :
     *
     * ADMIN / RESPONSABLE  → accès complet + formulaire CRUD entreprises
     * SERVICE_FINANCIER    → toutes les entreprises en lecture seule,
     *                        onglet Finance visible (toutes entreprises),
     *                        onglet Entreprises visible (lecture seule, sans formulaire CRUD)
     * ENTREPRISE           → voit uniquement SES données, onglet Finance = sa propre situation,
     *                        onglet "Entreprises Partenaires" masqué
     */
    private void appliquerPermissions(Utilisateur user, String role) {
        boolean isGestionnaire  = role.equals("ADMIN") || role.equals("RESPONSABLE");
        boolean isFinancier     = role.equals("SERVICE_FINANCIER") || role.equals("FINANCIER");
        boolean isEntreprise    = role.equals("ENTREPRISE");

        // ── Titre ──────────────────────────────────────────────────────
        if (lblTitrePage != null) {
            if (isEntreprise) {
                String nom = (user instanceof EntrepriseCliente) ? ((EntrepriseCliente) user).getNomEntreprise() : "Entreprise";
                lblTitrePage.setText("🏢 Espace " + nom);
            } else if (isFinancier) {
                lblTitrePage.setText("💳 Suivi Financier – Toutes les Entreprises");
            } else {
                lblTitrePage.setText("🏭 Gestion des Entreprises Partenaires");
            }
        }

        // ── Filtre entreprise ──────────────────────────────────────────
        // Visible pour ADMIN/RESPONSABLE/SERVICE_FINANCIER, masqué pour ENTREPRISE
        if (cbFiltreEntreprise != null) {
            boolean showFiltre = isGestionnaire || isFinancier;
            cbFiltreEntreprise.setVisible(showFiltre);
            cbFiltreEntreprise.setManaged(showFiltre);
            if (lblFiltreLabel != null) { lblFiltreLabel.setVisible(showFiltre); lblFiltreLabel.setManaged(showFiltre); }
            if (showFiltre) {
                List<Entreprise> toutes = entrepriseDAO.findAll();
                Entreprise tout = new Entreprise(); tout.setIdEntreprise(0); tout.setNom("— Toutes —");
                toutes.add(0, tout);
                cbFiltreEntreprise.setItems(FXCollections.observableArrayList(toutes));
                cbFiltreEntreprise.setValue(tout);
                cbFiltreEntreprise.setOnAction(e -> charger());
            }
        }

        // ── Formulaire CRUD entreprises ────────────────────────────────
        // Visible uniquement pour ADMIN/RESPONSABLE
        boolean showFormulaire = isGestionnaire;
        if (vboxFormulaireEntreprise != null) {
            vboxFormulaireEntreprise.setVisible(showFormulaire);
            vboxFormulaireEntreprise.setManaged(showFormulaire);
        }
        if (hboxBoutonsEntreprise != null) {
            hboxBoutonsEntreprise.setVisible(showFormulaire);
            hboxBoutonsEntreprise.setManaged(showFormulaire);
        }

        // ── Onglet "Entreprises Partenaires" ───────────────────────────
        // Masqué pour le rôle ENTREPRISE (chaque entreprise ne voit que ses données)
        if (tabGestionEntreprises != null) {
            boolean showOngletEntreprises = isGestionnaire || isFinancier;
            tabGestionEntreprises.setDisable(!showOngletEntreprises);
            // On peut aussi le masquer complètement pour ENTREPRISE
            // Note : TabPane ne supporte pas la visibilité native, on désactive
        }

        // ── Onglet "Mes Membres" : visible UNIQUEMENT pour ENTREPRISE ─────
        if (tabMesMembres != null) {
            tabMesMembres.setDisable(!isEntreprise);
        }

        // ── KPI financiers (masqués pour ENTREPRISE dans le bandeau) ──
        // L'entreprise accède aux chiffres via l'onglet Finance uniquement
        boolean showKPIFinancier = isGestionnaire || isFinancier;
        setCardVisible(cardTotalFacture,  showKPIFinancier);
        setCardVisible(cardTotalPaye,     showKPIFinancier);
        setCardVisible(cardResteAPayer,   showKPIFinancier);
    }

    // ─── Chargement ──────────────────────────────────────────────────────
    private void charger() {
        Utilisateur user = MainApp.getCurrentUser();
        String role = (user != null) ? user.getRole() : "";

        Integer idEntrepriseFiltre = getIdEntrepriseFiltre(user, role);

        // ENTREPRISE : filtre strict — ne voir QUE ses apprenants (jamais les libres ni ceux d'autres entreprises)
        List<Apprenant> apprenants;
        if (role.equals("ENTREPRISE")) {
            apprenants = (idEntrepriseFiltre != null)
                ? apprenantDAO.findByEntrepriseId(idEntrepriseFiltre)
                : java.util.Collections.emptyList();
        } else {
            apprenants = (idEntrepriseFiltre != null)
                ? apprenantDAO.findByEntrepriseId(idEntrepriseFiltre)
                : apprenantDAO.findAll();
        }

        long nbSessionsActives = sessionDAO.findAll().stream()
            .filter(s -> "EN_COURS".equalsIgnoreCase(s.getStatut()) || "EN COURS".equalsIgnoreCase(s.getStatut()))
            .count();

        double totalFacture = 0, totalPaye = 0;
        ObservableList<LigneApprenant> lignesApp = FXCollections.observableArrayList();
        ObservableList<LigneFinance>   lignesFin = FXCollections.observableArrayList();

        for (Apprenant a : apprenants) {
            List<Inscription> inscA = inscriptionDAO.findByApprenantAvecDetails(a.getIdUtilisateur());
            double payeA  = paiementDAO.getMontantPayeParApprenant(a.getIdUtilisateur());
            double resteA = paiementDAO.getMontantRestantParApprenant(a.getIdUtilisateur());

            for (Inscription insc : inscA) {
                String sessionLib = insc.getSession() != null ? insc.getSession().getLibelle() : "–";
                String result = insc.getResultSession() != null ? insc.getResultSession() : "";
                lignesApp.add(new LigneApprenant(a.getNom(),
                    a.getPrenom() != null ? a.getPrenom() : "",
                    sessionLib, insc.getStatut() != null ? insc.getStatut() : "", result, payeA, resteA));

                double prixFormation = 0;
                if (insc.getSession() != null && insc.getSession().getIdProgramme() != null) {
                    Programme prog = programmeDAO.findById(insc.getSession().getIdProgramme());
                    if (prog != null) prixFormation = prog.getTarif();
                }
                lignesFin.add(new LigneFinance(a.getNom(),
                    a.getPrenom() != null ? a.getPrenom() : "",
                    sessionLib, prixFormation, payeA, resteA));
            }
            totalPaye    += payeA;
            totalFacture += payeA + resteA;
        }

        if (tableApprenants != null) tableApprenants.setItems(lignesApp);
        if (tableFinance    != null) tableFinance.setItems(lignesFin);

        // ── Résumé financier dans l'onglet Finance ────────────────────
        double totalARegler = lignesFin.stream().mapToDouble(l -> l.prixFormation).sum();
        double totPayeF     = lignesFin.stream().mapToDouble(l -> l.montantPaye).sum();
        double totResteF    = lignesFin.stream().mapToDouble(l -> l.reste).sum();

        safe(lblFinTotalFacture, String.format("%,.0f FCFA", totalARegler));
        safe(lblFinTotalPaye,    String.format("%,.0f FCFA", totPayeF));
        safe(lblFinResteAPayer,  String.format("%,.0f FCFA", totResteF));

        if (lblTotalFinance != null)
            lblTotalFinance.setText("Détail par apprenant (" + lignesFin.size() + " entrée(s)) :");

        if (tableProgrammes != null)
            tableProgrammes.setItems(FXCollections.observableArrayList(programmeDAO.findAll()));

        // Paiements : ENTREPRISE voit uniquement les paiements de SES apprenants
        if (tablePaiements != null) {
            List<Paiement> tousP = paiementDAO.findPaiementsApprenants();
            if (role.equals("ENTREPRISE")) {
                // Filtre strict : uniquement les apprenants chargés (déjà filtrés par entreprise)
                final List<Integer> ids = apprenants.stream()
                    .map(Apprenant::getIdUtilisateur).collect(Collectors.toList());
                tousP = tousP.stream()
                    .filter(p -> p.getIdApprenant() != null && ids.contains(p.getIdApprenant()))
                    .collect(Collectors.toList());
            } else if (idEntrepriseFiltre != null) {
                final List<Integer> ids = apprenants.stream()
                    .map(Apprenant::getIdUtilisateur).collect(Collectors.toList());
                tousP = tousP.stream()
                    .filter(p -> p.getIdApprenant() != null && ids.contains(p.getIdApprenant()))
                    .collect(Collectors.toList());
            }
            tablePaiements.setItems(FXCollections.observableArrayList(tousP));
        }

        if (tableEntreprises != null)
            tableEntreprises.setItems(FXCollections.observableArrayList(entrepriseDAO.findAll()));

        safe(lblNbApprenants, String.valueOf(lignesApp.size()));
        safe(lblNbSessions,   String.valueOf(nbSessionsActives));
        safe(lblTotalFacture, String.format("%,.0f FCFA", totalFacture));
        safe(lblTotalPaye,    String.format("%,.0f FCFA", totalPaye));
        safe(lblResteAPayer,  String.format("%,.0f FCFA", Math.max(0, totalFacture - totalPaye)));
        safe(lblSatisfaction, String.format("%.1f%%", paiementDAO.getTauxSatisfactionGlobal()));
    }

    /**
     * Détermine l'idEntreprise à filtrer selon le rôle :
     * - ENTREPRISE connectée → forcément sa propre entreprise
     * - ADMIN/RESPONSABLE/FINANCIER avec filtre → l'entreprise choisie dans le ComboBox
     * - Sinon → null (toutes les entreprises)
     */
    private Integer getIdEntrepriseFiltre(Utilisateur user, String role) {
        if (role.equals("ENTREPRISE") && user != null) {
            // Le compte ENTREPRISE est créé avec u.setNom(e.getNom()) → on cherche par nom
            String nomE = user.getNom();
            Entreprise found = entrepriseDAO.findAll().stream()
                .filter(e -> e.getNom() != null && e.getNom().equalsIgnoreCase(nomE))
                .findFirst().orElse(null);
            // Fallback : chercher aussi par email si le nom ne correspond pas
            if (found == null) {
                found = entrepriseDAO.findAll().stream()
                    .filter(e -> e.getEmail() != null && e.getEmail().equalsIgnoreCase(user.getEmail()))
                    .findFirst().orElse(null);
            }
            return found != null ? found.getIdEntreprise() : null;
        }
        // Filtre combo (ADMIN/RESPONSABLE/FINANCIER)
        if (cbFiltreEntreprise != null && cbFiltreEntreprise.getValue() != null
                && cbFiltreEntreprise.getValue().getIdEntreprise() != 0)
            return cbFiltreEntreprise.getValue().getIdEntreprise();
        return null;
    }

    // ─── CRUD Entreprises (ADMIN / RESPONSABLE uniquement) ───────────────
    @FXML public void handleAjouterEntreprise() {
        if (txtENom == null || txtENom.getText().isEmpty()) { safeE("⚠️ Nom obligatoire."); return; }
        if (txtEEmail == null || txtEEmail.getText().isEmpty()) { safeE("⚠️ Email obligatoire pour créer le compte utilisateur."); return; }
        Entreprise e = buildEntrepriseFromForm();
        if (entrepriseDAO.insert(e)) {
            // Créer automatiquement un compte utilisateur avec le rôle ENTREPRISE
            Utilisateur u = new Utilisateur();
            u.setNom(e.getNom());
            u.setPrenom(e.getContactReferent() != null ? e.getContactReferent() : "");
            u.setEmail(e.getEmail());
            u.setMotDePasse("entreprise123"); // mot de passe par défaut à changer
            u.setRole("ENTREPRISE");
            u.setDateCreation(LocalDate.now());
            u.setActif(true);
            utilisateurDAO.insert(u);
            safeE("✅ Entreprise ajoutée + compte utilisateur ENTREPRISE créé (mdp: entreprise123)."); charger(); clearFormulaireEntreprise();
        } else safeE("❌ Erreur lors de l'ajout (email déjà utilisé?).");
    }

    @FXML public void handleModifierEntreprise() {
        if (selectedEntreprise == null) { safeE("⚠️ Sélectionnez une entreprise."); return; }
        Entreprise e = buildEntrepriseFromForm();
        e.setIdEntreprise(selectedEntreprise.getIdEntreprise());
        if (entrepriseDAO.update(e)) {
            safeE("✅ Entreprise modifiée."); charger(); clearFormulaireEntreprise(); selectedEntreprise = null;
        } else safeE("❌ Erreur.");
    }

    @FXML public void handleSupprimerEntreprise() {
        if (selectedEntreprise == null) { safeE("⚠️ Sélectionnez une entreprise."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer " + selectedEntreprise.getNom() + " ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> r = c.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.YES) {
            if (entrepriseDAO.delete(selectedEntreprise.getIdEntreprise())) {
                safeE("✅ Supprimée."); charger(); clearFormulaireEntreprise(); selectedEntreprise = null;
            } else safeE("❌ Erreur.");
        }
    }

    @FXML public void handleNouvelleEntreprise() { clearFormulaireEntreprise(); selectedEntreprise = null; }
    @FXML public void handleActualiser()         { charger(); }

    private Entreprise buildEntrepriseFromForm() {
        Entreprise e = new Entreprise();
        e.setNom(txtENom != null ? txtENom.getText().trim() : "");
        e.setAdresse(txtEAdresse != null ? txtEAdresse.getText().trim() : "");
        e.setTelephone(txtETelephone != null ? txtETelephone.getText().trim() : "");
        e.setEmail(txtEEmail != null ? txtEEmail.getText().trim() : "");
        e.setContactReferent(txtEContact != null ? txtEContact.getText().trim() : "");
        return e;
    }

    private void fillFormulaireEntreprise(Entreprise e) {
        if (txtENom      != null) txtENom.setText(e.getNom());
        if (txtEAdresse  != null) txtEAdresse.setText(e.getAdresse() != null ? e.getAdresse() : "");
        if (txtETelephone!= null) txtETelephone.setText(e.getTelephone() != null ? e.getTelephone() : "");
        if (txtEEmail    != null) txtEEmail.setText(e.getEmail() != null ? e.getEmail() : "");
        if (txtEContact  != null) txtEContact.setText(e.getContactReferent() != null ? e.getContactReferent() : "");
    }

    private void clearFormulaireEntreprise() {
        if (txtENom      != null) txtENom.clear();
        if (txtEAdresse  != null) txtEAdresse.clear();
        if (txtETelephone!= null) txtETelephone.clear();
        if (txtEEmail    != null) txtEEmail.clear();
        if (txtEContact  != null) txtEContact.clear();
    }

    // ─── Setup tables ────────────────────────────────────────────────────
    private void setupTableApprenants() {
        if (colNom        != null) colNom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().nom));
        if (colPrenom     != null) colPrenom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().prenom));
        if (colSession    != null) colSession.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().session));
        if (colStatut     != null) colStatut.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().statut));
        if (colResultat   != null) colResultat.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().resultat));
        if (colMontantPaye!= null) colMontantPaye.setCellValueFactory(d -> new SimpleStringProperty(
            String.format("%,.0f", d.getValue().montantPaye)));
        if (colResteAPayer!= null) colResteAPayer.setCellValueFactory(d -> new SimpleStringProperty(
            String.format("%,.0f", d.getValue().resteAPayer)));
    }

    private void setupTableProgrammes() {
        if (colPIntitule != null) colPIntitule.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIntitule()));
        if (colPDuree    != null) colPDuree.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getDuree()).asObject());
        if (colPTarif    != null) colPTarif.setCellValueFactory(d -> new SimpleStringProperty(
            String.format("%,.0f FCFA", d.getValue().getTarif())));
        if (colPNiveau   != null) colPNiveau.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getModalite() != null ? d.getValue().getModalite() : "–"));
    }

    private void setupTablePaiements() {
        List<Apprenant> apprenants = apprenantDAO.findAll();
        if (colPApprenant!= null) colPApprenant.setCellValueFactory(d -> {
            Integer idA = d.getValue().getIdApprenant();
            String nom = apprenants.stream()
                .filter(a -> a.getIdUtilisateur() == (idA != null ? idA : -1))
                .map(Apprenant::getNomComplet).findFirst().orElse("–");
            return new SimpleStringProperty(nom);
        });
        if (colPMontant  != null) colPMontant.setCellValueFactory(d -> new SimpleStringProperty(
            String.format("%,.0f FCFA", d.getValue().getMontant())));
        if (colPDate     != null) colPDate.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDatePaiement()));
        if (colPMode     != null) colPMode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getModePaiement()));
        if (colPStatut   != null) colPStatut.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatutPaiement()));
    }

    private void setupTableFinance() {
        if (colFNom          != null) colFNom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().nom));
        if (colFPrenom       != null) colFPrenom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().prenom));
        if (colFFormation    != null) colFFormation.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().formation));
        if (colFPrixFormation!= null) colFPrixFormation.setCellValueFactory(d -> new SimpleStringProperty(
            String.format("%,.0f FCFA", d.getValue().prixFormation)));
        if (colFPaye         != null) colFPaye.setCellValueFactory(d -> new SimpleStringProperty(
            String.format("%,.0f FCFA", d.getValue().montantPaye)));
        if (colFReste        != null) colFReste.setCellValueFactory(d -> new SimpleStringProperty(
            String.format("%,.0f FCFA", d.getValue().reste)));
    }

    private void setupTableEntreprises() {
        if (colENom      != null) colENom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNom()));
        if (colEAdresse  != null) colEAdresse.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getAdresse() != null ? d.getValue().getAdresse() : ""));
        if (colETelephone!= null) colETelephone.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getTelephone() != null ? d.getValue().getTelephone() : ""));
        if (colEEmail    != null) colEEmail.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getEmail() != null ? d.getValue().getEmail() : ""));
        if (colEContact  != null) colEContact.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getContactReferent() != null ? d.getValue().getContactReferent() : ""));
    }

    private void setupTableMembresEntreprise() {
        if (colMENom       != null) colMENom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNom()));
        if (colMEPrenom    != null) colMEPrenom.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getPrenom() != null ? d.getValue().getPrenom() : ""));
        if (colMEEmail     != null) colMEEmail.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getEmail() != null ? d.getValue().getEmail() : ""));
        if (colMETelephone != null) colMETelephone.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getTelephone() != null ? d.getValue().getTelephone() : ""));
        if (colMEMatricule != null) colMEMatricule.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getMatricule() != null ? d.getValue().getMatricule() : ""));
        if (colMEPrixFormation != null) colMEPrixFormation.setCellValueFactory(d -> {
            // Chercher la dernière inscription active et le tarif de la formation
            List<Inscription> inscs = inscriptionDAO.findByApprenantAvecDetails(d.getValue().getIdUtilisateur());
            double tarif = inscs.stream().mapToDouble(ins -> {
                if (ins.getSession() != null && ins.getSession().getIdProgramme() != null) {
                    Programme p = programmeDAO.findById(ins.getSession().getIdProgramme());
                    return p != null ? p.getTarif() : 0;
                }
                return 0;
            }).sum();
            return new SimpleStringProperty(tarif > 0 ? String.format("%,.0f FCFA", tarif) : "–");
        });
    }

    // ─── ENTREPRISE : handlers onglet Mes Membres ────────────────────────
    @FXML public void handleAjouterMembreEntreprise() {
        Utilisateur user = MainApp.getCurrentUser();
        if (user == null) return;
        String nom    = txtMembreNomE    != null ? txtMembreNomE.getText().trim() : "";
        String prenom = txtMembrePrenomE != null ? txtMembrePrenomE.getText().trim() : "";
        String email  = txtMembreEmailE  != null ? txtMembreEmailE.getText().trim() : "";
        String tel    = txtMembreTelephoneE != null ? txtMembreTelephoneE.getText().trim() : "";

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
            safeMembreE("⚠️ Nom, Prénom et Email sont obligatoires."); return;
        }

        // Retrouver l'entreprise du user connecté
        Entreprise monEnt = entrepriseDAO.findAll().stream()
            .filter(e -> e.getNom() != null && e.getNom().equalsIgnoreCase(user.getNom()))
            .findFirst().orElse(null);
        if (monEnt == null) {
            monEnt = entrepriseDAO.findAll().stream()
                .filter(e -> e.getEmail() != null && e.getEmail().equalsIgnoreCase(user.getEmail()))
                .findFirst().orElse(null);
        }
        if (monEnt == null) { safeMembreE("⚠️ Votre entreprise n'est pas trouvée en base."); return; }

        Apprenant membre = new Apprenant();
        membre.setNom(nom);
        membre.setPrenom(prenom);
        membre.setEmail(email);
        membre.setTelephone(tel);
        membre.setMotDePasse(prenom.toLowerCase() + "123");
        membre.setMatricule("MAT" + monEnt.getIdEntreprise() + "_" + (System.currentTimeMillis() % 10000));
        membre.setAdresse("");
        membre.setDateInscription(LocalDate.now());
        membre.setDateCreation(LocalDate.now());
        membre.setActif(true);
        membre.setIdEntreprise(monEnt.getIdEntreprise());

        ApprenantDAO aDAO = new ApprenantDAO();
        if (aDAO.insert(membre)) {
            safeMembreE("✅ Membre ajouté : " + prenom + " " + nom + " (mdp provisoire : " + prenom.toLowerCase() + "123)");
            clearFormMembreE();
            chargerTableMembresEntreprise(monEnt.getIdEntreprise());
        } else {
            safeMembreE("❌ Erreur lors de l'ajout. Email déjà utilisé ?");
        }
    }

    @FXML public void handleRetirerMembreEntreprise() {
        if (selectedMembreE == null) { safeMembreE("⚠️ Sélectionnez un membre dans la liste."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
            "Retirer " + selectedMembreE.getNom() + " " + selectedMembreE.getPrenom() +
            " de votre entreprise ?\nSon compte sera conservé mais dissocié.",
            ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> r = c.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.YES) {
            selectedMembreE.setIdEntreprise(null);
            ApprenantDAO aDAO = new ApprenantDAO();
            if (aDAO.update(selectedMembreE)) {
                safeMembreE("✅ Membre retiré.");
                selectedMembreE = null;
                Utilisateur user = MainApp.getCurrentUser();
                Entreprise monEnt = trouverEntrepriseDuUser(user);
                if (monEnt != null) chargerTableMembresEntreprise(monEnt.getIdEntreprise());
            } else safeMembreE("❌ Erreur lors du retrait.");
        }
    }

    @FXML public void handleActualiserMembresEntreprise() {
        Utilisateur user = MainApp.getCurrentUser();
        Entreprise monEnt = trouverEntrepriseDuUser(user);
        if (monEnt != null) chargerTableMembresEntreprise(monEnt.getIdEntreprise());
        safeMembreE("✅ Liste actualisée.");
    }

    private void chargerTableMembresEntreprise(int idEntreprise) {
        if (tableMembresEntreprise == null) return;
        ApprenantDAO aDAO = new ApprenantDAO();
        tableMembresEntreprise.setItems(FXCollections.observableArrayList(aDAO.findByEntrepriseId(idEntreprise)));
    }

    private Entreprise trouverEntrepriseDuUser(Utilisateur user) {
        if (user == null) return null;
        Entreprise found = entrepriseDAO.findAll().stream()
            .filter(e -> e.getNom() != null && e.getNom().equalsIgnoreCase(user.getNom()))
            .findFirst().orElse(null);
        if (found == null)
            found = entrepriseDAO.findAll().stream()
                .filter(e -> e.getEmail() != null && e.getEmail().equalsIgnoreCase(user.getEmail()))
                .findFirst().orElse(null);
        return found;
    }

    private void clearFormMembreE() {
        if (txtMembreNomE       != null) txtMembreNomE.clear();
        if (txtMembrePrenomE    != null) txtMembrePrenomE.clear();
        if (txtMembreEmailE     != null) txtMembreEmailE.clear();
        if (txtMembreTelephoneE != null) txtMembreTelephoneE.clear();
    }

    private void safeMembreE(String t) { if (lblMembreStatusE != null) lblMembreStatusE.setText(t); }

    // ─── Utilitaires ─────────────────────────────────────────────────────
    private void safe(Label l, String t)       { if (l != null) l.setText(t); }
    private void safeE(String t)               { if (lblEStatus != null) lblEStatus.setText(t); }
    private void setCardVisible(VBox card, boolean v) {
        if (card != null) { card.setVisible(v); card.setManaged(v); }
    }

    // ─── Classes internes ────────────────────────────────────────────────
    public static class LigneApprenant {
        String nom, prenom, session, statut, resultat;
        double montantPaye, resteAPayer;
        LigneApprenant(String n, String p, String s, String st, String r, double mp, double ra) {
            nom=n; prenom=p; session=s; statut=st; resultat=r; montantPaye=mp; resteAPayer=ra;
        }
    }

    public static class LigneFinance {
        String nom, prenom, formation;
        double prixFormation, montantPaye, reste;
        LigneFinance(String n, String p, String f, double pf, double mp, double r) {
            nom=n; prenom=p; formation=f; prixFormation=pf; montantPaye=mp; reste=r;
        }
    }
}