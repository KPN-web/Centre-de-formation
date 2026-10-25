package metier;

import java.time.LocalDate;

public class Paiement {
    private int idPaiement;
    private double montant;
    private LocalDate datePaiement;
    private String modePaiement;
    private String statutPaiement;
    private String emailPayeur;
    private Integer idInscription;  // lié à une inscription (apprenant)
    private Integer idApprenant;    // lien direct vers l'apprenant
    private Integer idFormateur;    // pour paiement formateur (salaire)
    private String typePaiement;    // "APPRENANT" ou "FORMATEUR"
    private String description;     // motif du paiement

    public Paiement() {}

    public int getIdPaiement() { return idPaiement; }
    public void setIdPaiement(int idPaiement) { this.idPaiement = idPaiement; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public LocalDate getDatePaiement() { return datePaiement; }
    public void setDatePaiement(LocalDate datePaiement) { this.datePaiement = datePaiement; }

    public String getModePaiement() { return modePaiement; }
    public void setModePaiement(String modePaiement) { this.modePaiement = modePaiement; }

    public String getStatutPaiement() { return statutPaiement; }
    public void setStatutPaiement(String statutPaiement) { this.statutPaiement = statutPaiement; }

    public String getEmailPayeur() { return emailPayeur; }
    public void setEmailPayeur(String emailPayeur) { this.emailPayeur = emailPayeur; }

    public Integer getIdInscription() { return idInscription; }
    public void setIdInscription(Integer idInscription) { this.idInscription = idInscription; }

    public Integer getIdApprenant() { return idApprenant; }
    public void setIdApprenant(Integer idApprenant) { this.idApprenant = idApprenant; }

    public Integer getIdFormateur() { return idFormateur; }
    public void setIdFormateur(Integer idFormateur) { this.idFormateur = idFormateur; }

    public String getTypePaiement() { return typePaiement; }
    public void setTypePaiement(String typePaiement) { this.typePaiement = typePaiement; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
