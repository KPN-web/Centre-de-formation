package metier;

import java.time.LocalDate;

public class Certification {
    private int idCertification;
    private String typeCertificat;
    private LocalDate dateEmission;
    private String fichierPDF;
    private Integer idApprenant;
    private Integer idSession;

    public Certification() {}

    public Certification(int idCertification, String typeCertificat, LocalDate dateEmission,
                         String fichierPDF, Integer idApprenant, Integer idSession) {
        this.idCertification = idCertification;
        this.typeCertificat = typeCertificat;
        this.dateEmission = dateEmission;
        this.fichierPDF = fichierPDF;
        this.idApprenant = idApprenant;
        this.idSession = idSession;
    }

    public int getIdCertification() { return idCertification; }
    public void setIdCertification(int idCertification) { this.idCertification = idCertification; }

    public String getTypeCertificat() { return typeCertificat; }
    public void setTypeCertificat(String typeCertificat) { this.typeCertificat = typeCertificat; }

    public LocalDate getDateEmission() { return dateEmission; }
    public void setDateEmission(LocalDate dateEmission) { this.dateEmission = dateEmission; }

    public String getFichierPDF() { return fichierPDF; }
    public void setFichierPDF(String fichierPDF) { this.fichierPDF = fichierPDF; }

    public Integer getIdApprenant() { return idApprenant; }
    public void setIdApprenant(Integer idApprenant) { this.idApprenant = idApprenant; }

    public Integer getIdSession() { return idSession; }
    public void setIdSession(Integer idSession) { this.idSession = idSession; }
}
