package metier;

import java.time.LocalDate;

public class Session {
    private int idSession;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String lieu;
    private int capaciteMax;
    private String statut;
    private Integer idProgramme;
    private Integer idFormateur;
    // Objets liés pour affichage
    private Programme programme;
    private Formateur formateur;

    public Session() {}

    public Session(int idSession, LocalDate dateDebut, LocalDate dateFin, String lieu,
                   int capaciteMax, String statut, Integer idProgramme, Integer idFormateur) {
        this.idSession = idSession;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.lieu = lieu;
        this.capaciteMax = capaciteMax;
        this.statut = statut;
        this.idProgramme = idProgramme;
        this.idFormateur = idFormateur;
    }

    public int getIdSession() { return idSession; }
    public void setIdSession(int idSession) { this.idSession = idSession; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public int getCapaciteMax() { return capaciteMax; }
    public void setCapaciteMax(int capaciteMax) { this.capaciteMax = capaciteMax; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Integer getIdProgramme() { return idProgramme; }
    public void setIdProgramme(Integer idProgramme) { this.idProgramme = idProgramme; }

    public Integer getIdFormateur() { return idFormateur; }
    public void setIdFormateur(Integer idFormateur) { this.idFormateur = idFormateur; }

    public Programme getProgramme() { return programme; }
    public void setProgramme(Programme programme) { this.programme = programme; }

    public Formateur getFormateur() { return formateur; }
    public void setFormateur(Formateur formateur) { this.formateur = formateur; }

    /**
     * Libellé lisible pour identifier une session sans utiliser l'ID technique.
     * Format : "Programme – Lieu (DateDebut)"
     * Utilisé dans les ComboBox et colonnes des TableView.
     */
    public String getLibelle() {
        String prog = (programme != null) ? programme.getIntitule()
                    : (idProgramme != null ? "Prog." + idProgramme : "Sans programme");
        String debut = (dateDebut != null) ? dateDebut.toString() : "?";
        return prog + " – " + lieu + " (" + debut + ")";
    }

    @Override
    public String toString() {
        return getLibelle();
    }
}
