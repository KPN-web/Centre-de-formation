package Controller;

import Database.*;
import FX.MainApp;
import metier.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

public class StatistiqueController {

    // Cartes KPI
    @FXML private Label lblApprenants;
    @FXML private Label lblFormateurs;
    @FXML private Label lblProgrammes;
    @FXML private Label lblSessions;
    @FXML private Label lblInscriptions;
    @FXML private Label lblCertifications;
    @FXML private Label lblTauxSatisfaction;

    // Carte CA - visible ADMIN/FINANCIER uniquement
    @FXML private VBox  cardCA;
    @FXML private Label lblChiffreAffaires;

    @FXML private ComboBox<String> cbAnnee;

    // Onglet rentabilité par session
    @FXML private TableView<StatSession>             tableRentabilite;
    @FXML private TableColumn<StatSession, String>    colRSession;
    @FXML private TableColumn<StatSession, String>    colRProgramme;
    @FXML private TableColumn<StatSession, Integer>   colRInscrits;
    @FXML private TableColumn<StatSession, Double>    colRRevenu;
    @FXML private TableColumn<StatSession, Double>    colRCout;
    @FXML private TableColumn<StatSession, Double>    colRMarge;

    // Onglet inscriptions par programme (accès ADMIN)
    @FXML private TableView<StatLigne>             tableTop;
    @FXML private TableColumn<StatLigne, String>   colProgramme;
    @FXML private TableColumn<StatLigne, Integer>  colNbInscrits;
    @FXML private TableColumn<StatLigne, Double>   colRevenu;

    // Onglet performance formateurs (RESPONSABLE uniquement)
    @FXML private TableView<StatFormateur>           tableFormateurs;
    @FXML private TableColumn<StatFormateur, String>  colFNom;
    @FXML private TableColumn<StatFormateur, Integer> colFSessions;
    @FXML private TableColumn<StatFormateur, Double>  colFNotemoyenne;
    @FXML private TableColumn<StatFormateur, Double>  colFTauxPresence;
    @FXML private TableColumn<StatFormateur, Double>  colFTauxReussite;

    // Onglet satisfaction (taux présence/réussite par session)
    @FXML private TableView<StatSatisfaction>              tableSatisfaction;
    @FXML private TableColumn<StatSatisfaction, String>    colSSession;
    @FXML private TableColumn<StatSatisfaction, Double>    colSTauxPresence;
    @FXML private TableColumn<StatSatisfaction, Double>    colSTauxReussite;
    @FXML private TableColumn<StatSatisfaction, Double>    colSTauxAbandon;
    @FXML private TableColumn<StatSatisfaction, Double>    colSNoteMoyenne;
    @FXML private TableColumn<StatSatisfaction, Double>    colSTauxVote;

    private ApprenantDAO     apprenantDAO     = new ApprenantDAO();
    private FormateurDAO     formateurDAO     = new FormateurDAO();
    private ProgrammeDAO     programmeDAO     = new ProgrammeDAO();
    private SessionDAO       sessionDAO       = new SessionDAO();
    private InscriptionDAO   inscriptionDAO   = new InscriptionDAO();
    private CertificationDAO certificationDAO = new CertificationDAO();
    private PaiementDAO      paiementDAO      = new PaiementDAO();
    private SuiviPedagogiqueDAO suiviDAO      = new SuiviPedagogiqueDAO();

    @FXML
    public void initialize() {
        // Permissions financières
        Utilisateur user = MainApp.getCurrentUser();
        String role = (user != null) ? user.getRole() : "";
        boolean voitArgent = role.equals("ADMIN") || role.equals("FINANCIER") || role.equals("SERVICE_FINANCIER");
        if (cardCA != null) { cardCA.setVisible(voitArgent); cardCA.setManaged(voitArgent); }

        setupTableRentabilite();
        setupTableTop();
        setupTableFormateurs();
        setupTableSatisfaction();
        chargerStats();
        if (cbAnnee != null) cbAnnee.setItems(FXCollections.observableArrayList("Toutes", "2024", "2025", "2026"));
    }

    private void setupTableRentabilite() {
        if (colRSession   != null) colRSession.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().session));
        if (colRProgramme != null) colRProgramme.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().programme));
        if (colRInscrits  != null) colRInscrits.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().nbInscrits).asObject());
        if (colRRevenu    != null) colRRevenu.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().revenu).asObject());
        if (colRCout      != null) colRCout.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().coutFormateur).asObject());
        if (colRMarge     != null) colRMarge.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().marge).asObject());
    }

    private void setupTableTop() {
        if (colProgramme  != null) colProgramme.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProgramme()));
        if (colNbInscrits != null) colNbInscrits.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getNbInscrits()).asObject());
        if (colRevenu     != null) colRevenu.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getRevenu()).asObject());
    }

    private void setupTableFormateurs() {
        if (colFNom          != null) colFNom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().nom));
        if (colFSessions     != null) colFSessions.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().nbSessions).asObject());
        if (colFNotemoyenne   != null) colFNotemoyenne.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().noteMoyenne).asObject());
        if (colFTauxPresence  != null) colFTauxPresence.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().tauxPresence).asObject());
        if (colFTauxReussite  != null) colFTauxReussite.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().tauxReussite).asObject());
    }

    private void setupTableSatisfaction() {
        if (colSSession      != null) colSSession.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().session));
        if (colSTauxPresence  != null) colSTauxPresence.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().tauxPresence).asObject());
        if (colSTauxReussite  != null) colSTauxReussite.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().tauxReussite).asObject());
        if (colSTauxAbandon   != null) colSTauxAbandon.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().tauxAbandon).asObject());
        if (colSNoteMoyenne   != null) colSNoteMoyenne.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().noteMoyenne).asObject());
        if (colSTauxVote != null) colSTauxVote.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().tauxVote).asObject());
    }

    private void chargerStats() {
        safe(lblApprenants,     String.valueOf(apprenantDAO.findAll().size()));
        safe(lblFormateurs,     String.valueOf(formateurDAO.findAll().size()));
        safe(lblProgrammes,     String.valueOf(programmeDAO.findAll().size()));
        safe(lblSessions,       String.valueOf(sessionDAO.findAll().size()));
        safe(lblInscriptions,   String.valueOf(inscriptionDAO.findAll().size()));
        safe(lblCertifications, String.valueOf(certificationDAO.findAll().size()));
        safe(lblChiffreAffaires, String.format("%,.0f FCFA", paiementDAO.getTotalChiffreAffaires()));
        double tauxSat = paiementDAO.getTauxSatisfactionGlobal();
        safe(lblTauxSatisfaction, String.format("%.1f%%", tauxSat));

        chargerRentabilite();
        chargerTopProgrammes();
        chargerPerformanceFormateurs();
        chargerSatisfaction();
    }

    private void chargerRentabilite() {
        if (tableRentabilite == null) return;
        ObservableList<StatSession> lignes = FXCollections.observableArrayList();
        List<Session> sessions = sessionDAO.findAll();
        List<Inscription> inscriptions = inscriptionDAO.findAll();
        List<Formateur> formateurs = formateurDAO.findAll();

        for (Session s : sessions) {
            String nomSession = s.getLibelle();
            String nomProg = "";
            int duree = 0;
            double tarif = 0;

            // CORRECTION 1 & 2 : un seul appel DAO pour récupérer nom, durée ET tarif
            if (s.getIdProgramme() != null) {
                Programme p = programmeDAO.findById(s.getIdProgramme());
                if (p != null) {
                    nomProg = p.getIntitule();
                    duree   = p.getDuree();
                    tarif   = p.getTarif();
                }
            }

            // CORRECTION 3 : variable finale pour utilisation dans le lambda
            final int idSession = s.getIdSession();
            long nb = inscriptions.stream()
                .filter(i -> i.getIdSession() != null && i.getIdSession() == idSession)
                .count();

            double revenu = nb * tarif;

            // CORRECTION 3 (suite) : duree doit être effectively final pour le lambda
            final int dureeF = duree;
            double coutFormateur = 0;
            if (s.getIdFormateur() != null) {
                int idF = s.getIdFormateur();
                coutFormateur = formateurs.stream()
                    .filter(f -> f.getIdUtilisateur() == idF)
                    .mapToDouble(f -> f.getTauxHoraire() * dureeF)
                    .findFirst().orElse(0);
            }
            lignes.add(new StatSession(nomSession, nomProg, (int) nb, revenu, coutFormateur));
        }
        tableRentabilite.setItems(lignes);
    }

    private void chargerTopProgrammes() {
        if (tableTop == null) return;
        ObservableList<StatLigne> lignes = FXCollections.observableArrayList();
        List<Inscription> inscriptions = inscriptionDAO.findAll();
        programmeDAO.findAll().forEach(p -> {
            long nb = inscriptions.stream().filter(i -> {
                if (i.getIdSession() == null) return false;
                Session s = sessionDAO.findById(i.getIdSession());
                return s != null && s.getIdProgramme() != null && s.getIdProgramme() == p.getIdProgramme();
            }).count();
            lignes.add(new StatLigne(p.getIntitule(), (int) nb, nb * p.getTarif()));
        });
        lignes.sort((a, b) -> b.getNbInscrits() - a.getNbInscrits());
        tableTop.setItems(lignes);
    }

    private void chargerPerformanceFormateurs() {
        if (tableFormateurs == null) return;
        ObservableList<StatFormateur> lignes = FXCollections.observableArrayList();
        for (Formateur f : formateurDAO.findAll()) {
            List<Session> sessions = sessionDAO.findByFormateur(f.getIdUtilisateur());
            List<SuiviPedagogique> suivis = suiviDAO.findByFormateurAvecDetails(f.getIdUtilisateur());
            int total = suivis.size();
            double noteMoy = total > 0 ? suivis.stream().mapToDouble(SuiviPedagogique::getNote).average().orElse(0) : 0;
            double tauxPres = total > 0 ? 100.0 * suivis.stream().filter(SuiviPedagogique::isPresence).count() / total : 0;
            double tauxReus = total > 0 ? 100.0 * suivis.stream().filter(s -> "Réussi".equals(s.getStatutSuivi())).count() / total : 0;
            lignes.add(new StatFormateur(f.getNomComplet(), sessions.size(),
                Math.round(noteMoy * 10.0) / 10.0,
                Math.round(tauxPres * 10.0) / 10.0,
                Math.round(tauxReus * 10.0) / 10.0));
        }
        tableFormateurs.setItems(lignes);
    }

    private void chargerSatisfaction() {
        if (tableSatisfaction == null) return;
        ObservableList<StatSatisfaction> lignes = FXCollections.observableArrayList();
        for (Session s : sessionDAO.findAll()) {
            List<SuiviPedagogique> suivis = suiviDAO.findBySessionAvecDetails(s.getIdSession());
            int total = suivis.size();
            if (total == 0) continue;
            double pres = 100.0 * suivis.stream().filter(SuiviPedagogique::isPresence).count() / total;
            double reus = 100.0 * suivis.stream().filter(sp -> "Réussi".equals(sp.getStatutSuivi())).count() / total;
            double aban = 100.0 * suivis.stream().filter(sp -> "Abandon".equals(sp.getStatutSuivi())).count() / total;
            double note = suivis.stream().mapToDouble(SuiviPedagogique::getNote).average().orElse(0);
            // Taux satisfaction basé sur votes apprenants
            java.util.List<metier.Inscription> inscSess = inscriptionDAO.findBySession(s.getIdSession());
            long nbVotes = inscSess.stream().filter(i -> paiementDAO.hasVoteSatisfaction(i.getIdInscription())).count();
            long nbSatisfaits = inscSess.stream().filter(i -> "SATISFAIT".equals(paiementDAO.getVoteApprenant(i.getIdInscription()))).count();
            double tauxVote = nbVotes > 0 ? 100.0 * nbSatisfaits / nbVotes : 0;
            lignes.add(new StatSatisfaction(s.getLibelle(),
                round(pres), round(reus), round(aban), round(note), round(tauxVote)));
        }
        tableSatisfaction.setItems(lignes);
    }

    private double round(double v) { return Math.round(v * 10.0) / 10.0; }

    @FXML public void handleActualiser() { chargerStats(); }

    private void safe(Label l, String t) { if (l != null) l.setText(t); }

    // ── Classes internes ─────────────────────────────────────────────────
    public static class StatLigne {
        private String programme; private int nbInscrits; private double revenu;
        public StatLigne(String p, int n, double r) { programme=p; nbInscrits=n; revenu=r; }
        public String getProgramme()  { return programme; }
        public int    getNbInscrits() { return nbInscrits; }
        public double getRevenu()     { return revenu; }
    }
    public static class StatSession {
        String session, programme; int nbInscrits; double revenu, coutFormateur, marge;
        StatSession(String s, String p, int n, double r, double c) {
            session=s; programme=p; nbInscrits=n; revenu=r; coutFormateur=c; marge=r-c; }
    }
    public static class StatFormateur {
        String nom; int nbSessions; double noteMoyenne, tauxPresence, tauxReussite;
        StatFormateur(String n, int s, double nm, double tp, double tr) {
            nom=n; nbSessions=s; noteMoyenne=nm; tauxPresence=tp; tauxReussite=tr; }
    }
    public static class StatSatisfaction {
        String session; double tauxPresence, tauxReussite, tauxAbandon, noteMoyenne, tauxVote;
        StatSatisfaction(String s, double tp, double tr, double ta, double nm, double tv) {
            session=s; tauxPresence=tp; tauxReussite=tr; tauxAbandon=ta; noteMoyenne=nm; tauxVote=tv; }
    }
}
