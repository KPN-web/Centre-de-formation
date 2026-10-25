package metier;

/**
 * Représente une entreprise partenaire qui envoie des employés en formation.
 * Distincte du compte utilisateur EntrepriseCliente (espace web).
 */
public class Entreprise {
    private int idEntreprise;
    private String nom;
    private String adresse;
    private String telephone;
    private String email;
    private String contactReferent;

    public Entreprise() {}

    public Entreprise(int idEntreprise, String nom, String adresse,
                      String telephone, String email, String contactReferent) {
        this.idEntreprise   = idEntreprise;
        this.nom            = nom;
        this.adresse        = adresse;
        this.telephone      = telephone;
        this.email          = email;
        this.contactReferent = contactReferent;
    }

    public int    getIdEntreprise()   { return idEntreprise; }
    public void   setIdEntreprise(int v) { idEntreprise = v; }

    public String getNom()            { return nom; }
    public void   setNom(String v)    { nom = v; }

    public String getAdresse()        { return adresse; }
    public void   setAdresse(String v){ adresse = v; }

    public String getTelephone()      { return telephone; }
    public void   setTelephone(String v){ telephone = v; }

    public String getEmail()          { return email; }
    public void   setEmail(String v)  { email = v; }

    public String getContactReferent(){ return contactReferent; }
    public void   setContactReferent(String v){ contactReferent = v; }

    @Override
    public String toString() { return nom != null ? nom : "(sans nom)"; }
}
