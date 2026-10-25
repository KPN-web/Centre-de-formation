package metier;

import java.time.LocalDate;

public class Inscription {
    private int idInscription;
    private LocalDate dateInscription;
    private String statut;
    private boolean validationAdministrative;
    private String resultSession; // ADMIS, AJOURNE, ou null si session pas terminée
    private Integer idApprenant;
    private Integer idSession;
    // Objets liés
    private Apprenant apprenant;
    private Session session;

    public Inscription() {}

    public Inscription(int idInscription, LocalDate dateInscription, String statut,
                       boolean validationAdministrative, Integer idApprenant, Integer idSession) {
        this.idInscription = idInscription;
        this.dateInscription = dateInscription;
        this.statut = statut;
        this.validationAdministrative = validationAdministrative;
        this.idApprenant = idApprenant;
        this.idSession = idSession;
    }

    public int getIdInscription() { return idInscription; }
    public void setIdInscription(int idInscription) { this.idInscription = idInscription; }

    public LocalDate getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDate dateInscription) { this.dateInscription = dateInscription; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public boolean isValidationAdministrative() { return validationAdministrative; }
    public void setValidationAdministrative(boolean validationAdministrative) { this.validationAdministrative = validationAdministrative; }

    public Integer getIdApprenant() { return idApprenant; }
    public void setIdApprenant(Integer idApprenant) { this.idApprenant = idApprenant; }

    public Integer getIdSession() { return idSession; }
    public void setIdSession(Integer idSession) { this.idSession = idSession; }

    public Apprenant getApprenant() { return apprenant; }
    public void setApprenant(Apprenant apprenant) { this.apprenant = apprenant; }

    public Session getSession() { return session; }
    public void setSession(Session session) { this.session = session; }

    public String getResultSession() { return resultSession; }
    public void setResultSession(String resultSession) { this.resultSession = resultSession; }
    /**
     * Libellé lisible pour les ComboBox.
     * Format : "Programme – Lieu (DateDebut) — Apprenant"
     */
    public String toString() {
        String sessionLib = (session != null) ? session.getLibelle()
                          : (idSession != null ? "Session " + idSession : "?");
        String appLib = (apprenant != null) ? apprenant.getNomComplet()
                       : (idApprenant != null ? "Apprenant " + idApprenant : "?");
        return sessionLib + " — " + appLib;
    }
}
