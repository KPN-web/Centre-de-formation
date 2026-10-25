package metier;

public class Module {
    private int idModule;
    private String titre;
    private String description;
    private String typeContenu;
    private String support;
    private Integer idProgramme;

    public Module() {}

    public Module(int idModule, String titre, String description,
                  String typeContenu, String support, Integer idProgramme) {
        this.idModule = idModule;
        this.titre = titre;
        this.description = description;
        this.typeContenu = typeContenu;
        this.support = support;
        this.idProgramme = idProgramme;
    }

    public int getIdModule() { return idModule; }
    public void setIdModule(int idModule) { this.idModule = idModule; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTypeContenu() { return typeContenu; }
    public void setTypeContenu(String typeContenu) { this.typeContenu = typeContenu; }

    public String getSupport() { return support; }
    public void setSupport(String support) { this.support = support; }

    public Integer getIdProgramme() { return idProgramme; }
    public void setIdProgramme(Integer idProgramme) { this.idProgramme = idProgramme; }

    @Override
    public String toString() { return titre; }
}
