package metier;

import java.time.LocalDate;

public class Formateur extends Utilisateur {
    private String specialite;
    private String cv;
    private double tauxHoraire;
    private String disponibilite;

    public Formateur() {
        super();
        setRole("FORMATEUR");
    }

    public Formateur(int idUtilisateur, String nom, String prenom, String email,
                     String motDePasse, LocalDate dateCreation, boolean actif,
                     String specialite, String cv, double tauxHoraire, String disponibilite) {
        super(idUtilisateur, nom, prenom, email, motDePasse, "FORMATEUR", dateCreation, actif);
        this.specialite = specialite;
        this.cv = cv;
        this.tauxHoraire = tauxHoraire;
        this.disponibilite = disponibilite;
    }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public String getCv() { return cv; }
    public void setCv(String cv) { this.cv = cv; }

    public double getTauxHoraire() { return tauxHoraire; }
    public void setTauxHoraire(double tauxHoraire) { this.tauxHoraire = tauxHoraire; }

    public String getDisponibilite() { return disponibilite; }
    public void setDisponibilite(String disponibilite) { this.disponibilite = disponibilite; }
}
