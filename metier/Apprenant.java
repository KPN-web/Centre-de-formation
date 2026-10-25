package metier;

import java.time.LocalDate;

public class Apprenant extends Utilisateur {
    private String matricule;
    private String telephone;
    private String adresse;
    private LocalDate dateInscription;
    private Integer idEntreprise;  // null = apprenant libre

    public Apprenant() {
        super();
        setRole("APPRENANT");
    }

    public Apprenant(int idUtilisateur, String nom, String prenom, String email,
                     String motDePasse, LocalDate dateCreation, boolean actif,
                     String matricule, String telephone, String adresse, LocalDate dateInscription) {
        super(idUtilisateur, nom, prenom, email, motDePasse, "APPRENANT", dateCreation, actif);
        this.matricule = matricule;
        this.telephone = telephone;
        this.adresse = adresse;
        this.dateInscription = dateInscription;
    }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public LocalDate getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDate dateInscription) { this.dateInscription = dateInscription; }

    public Integer getIdEntreprise() { return idEntreprise; }
    public void setIdEntreprise(Integer idEntreprise) { this.idEntreprise = idEntreprise; }
}
