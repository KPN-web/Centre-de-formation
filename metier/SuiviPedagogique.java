package metier;

public class SuiviPedagogique {
    private int idSuivi;
    private boolean presence;
    private double note;
    private String feedback;
    private Integer idApprenant;
    private Integer idSession;

    public SuiviPedagogique() {}

    public SuiviPedagogique(int idSuivi, boolean presence, double note, String feedback,
                             Integer idApprenant, Integer idSession) {
        this.idSuivi = idSuivi;
        this.presence = presence;
        this.note = note;
        this.feedback = feedback;
        this.idApprenant = idApprenant;
        this.idSession = idSession;
    }

    public int getIdSuivi() { return idSuivi; }
    public void setIdSuivi(int idSuivi) { this.idSuivi = idSuivi; }

    public boolean isPresence() { return presence; }
    public void setPresence(boolean presence) { this.presence = presence; }

    public double getNote() { return note; }
    public void setNote(double note) { this.note = note; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public Integer getIdApprenant() { return idApprenant; }
    public void setIdApprenant(Integer idApprenant) { this.idApprenant = idApprenant; }

    public Integer getIdSession() { return idSession; }
    public void setIdSession(Integer idSession) { this.idSession = idSession; }

    // Statut pédagogique : "En cours", "Réussi", "Abandon"
    private String statutSuivi;

    public String getStatutSuivi() { return statutSuivi; }
    public void setStatutSuivi(String statutSuivi) { this.statutSuivi = statutSuivi; }

    // Objets liés pour affichage (non persistés directement)
    private Apprenant apprenant;
    private Session   session;

    public Apprenant getApprenant() { return apprenant; }
    public void setApprenant(Apprenant apprenant) { this.apprenant = apprenant; }

    public Session getSession() { return session; }
    public void setSession(Session session) { this.session = session; }
}
