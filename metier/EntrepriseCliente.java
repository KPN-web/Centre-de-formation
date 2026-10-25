package metier;

import java.time.LocalDate;

public class EntrepriseCliente extends Utilisateur {
    private String nomEntreprise;
    private String adresseEntreprise;
    private String telephoneEntreprise;

    public EntrepriseCliente() {
        super();
        setRole("ENTREPRISE");
    }

    public EntrepriseCliente(int idUtilisateur, String nom, String prenom, String email,
                              String motDePasse, LocalDate dateCreation, boolean actif,
                              String nomEntreprise, String adresseEntreprise, String telephoneEntreprise) {
        super(idUtilisateur, nom, prenom, email, motDePasse, "ENTREPRISE", dateCreation, actif);
        this.nomEntreprise = nomEntreprise;
        this.adresseEntreprise = adresseEntreprise;
        this.telephoneEntreprise = telephoneEntreprise;
    }

    public String getNomEntreprise() { return nomEntreprise; }
    public void setNomEntreprise(String nomEntreprise) { this.nomEntreprise = nomEntreprise; }

    public String getAdresseEntreprise() { return adresseEntreprise; }
    public void setAdresseEntreprise(String adresseEntreprise) { this.adresseEntreprise = adresseEntreprise; }

    public String getTelephoneEntreprise() { return telephoneEntreprise; }
    public void setTelephoneEntreprise(String telephoneEntreprise) { this.telephoneEntreprise = telephoneEntreprise; }
}
