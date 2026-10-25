package metier;

import java.util.ArrayList;
import java.util.List;

public class Programme {
    private int idProgramme;
    private String intitule;
    private String objectifs;
    private String prerequis;
    private int duree;
    private String modalite;
    private double tarif;
    private double totalAPayer;
    private boolean publie;
    private boolean archive;
    private List<Module> modules;

    public Programme() {
        this.modules = new ArrayList<>();
    }

    public Programme(int idProgramme, String intitule, String objectifs, String prerequis,
                     int duree, String modalite, double tarif, double totalAPayer,
                     boolean publie, boolean archive) {
        this.idProgramme = idProgramme;
        this.intitule = intitule;
        this.objectifs = objectifs;
        this.prerequis = prerequis;
        this.duree = duree;
        this.modalite = modalite;
        this.tarif = tarif;
        this.totalAPayer = totalAPayer;
        this.publie = publie;
        this.archive = archive;
        this.modules = new ArrayList<>();
    }

    public int getIdProgramme() { return idProgramme; }
    public void setIdProgramme(int idProgramme) { this.idProgramme = idProgramme; }

    public String getIntitule() { return intitule; }
    public void setIntitule(String intitule) { this.intitule = intitule; }

    public String getObjectifs() { return objectifs; }
    public void setObjectifs(String objectifs) { this.objectifs = objectifs; }

    public String getPrerequis() { return prerequis; }
    public void setPrerequis(String prerequis) { this.prerequis = prerequis; }

    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }

    public String getModalite() { return modalite; }
    public void setModalite(String modalite) { this.modalite = modalite; }

    public double getTarif() { return tarif; }
    public void setTarif(double tarif) { this.tarif = tarif; }

    public double getTotalAPayer() { return totalAPayer; }
    public void setTotalAPayer(double totalAPayer) { this.totalAPayer = totalAPayer; }

    public boolean isPublie() { return publie; }
    public void setPublie(boolean publie) { this.publie = publie; }

    public boolean isArchive() { return archive; }
    public void setArchive(boolean archive) { this.archive = archive; }

    public List<Module> getModules() { return modules; }
    public void setModules(List<Module> modules) { this.modules = modules; }

    @Override
    public String toString() { return intitule; }
}
