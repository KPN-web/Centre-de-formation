package Database;

import metier.Inscription;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InscriptionDAO {

    private Connection connection;

    public InscriptionDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public List<Inscription> findAll() {
        List<Inscription> list = new ArrayList<>();
        String sql = "SELECT * FROM inscription ORDER BY dateInscription DESC";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findAll inscriptions : " + e.getMessage());
        }
        return list;
    }

    /** Toutes les inscriptions avec Apprenant et Session chargés (libellés) */
    public List<Inscription> findAllAvecDetails() {
        List<Inscription> list = new ArrayList<>();
        String sql = "SELECT i.*, " +
                     "u.nom AS nomApprenant, u.prenom AS prenomApprenant, " +
                     "s.dateDebut, s.lieu, p.intitule AS intituleProgramme " +
                     "FROM inscription i " +
                     "LEFT JOIN utilisateurs u ON i.idApprenant = u.idUtilisateur " +
                     "LEFT JOIN session s ON i.idSession = s.idSession " +
                     "LEFT JOIN programme p ON s.idProgramme = p.idProgramme " +
                     "ORDER BY i.dateInscription DESC";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSetAvecDetails(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findAllAvecDetails inscriptions : " + e.getMessage());
        }
        return list;
    }

    /** Inscriptions d'un apprenant avec Session chargée */
    public List<Inscription> findByApprenantAvecDetails(int idApprenant) {
        List<Inscription> list = new ArrayList<>();
        String sql = "SELECT i.*, " +
                     "u.nom AS nomApprenant, u.prenom AS prenomApprenant, " +
                     "s.dateDebut, s.lieu, p.intitule AS intituleProgramme " +
                     "FROM inscription i " +
                     "LEFT JOIN utilisateurs u ON i.idApprenant = u.idUtilisateur " +
                     "LEFT JOIN session s ON i.idSession = s.idSession " +
                     "LEFT JOIN programme p ON s.idProgramme = p.idProgramme " +
                     "WHERE i.idApprenant = ? ORDER BY i.dateInscription DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idApprenant);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSetAvecDetails(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findByApprenantAvecDetails : " + e.getMessage());
        }
        return list;
    }

    public Inscription findById(int id) {
        String sql = "SELECT * FROM inscription WHERE idInscription = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) {
            System.err.println("Erreur findById inscription : " + e.getMessage());
        }
        return null;
    }

    public List<Inscription> findByApprenant(int idApprenant) {
        List<Inscription> list = new ArrayList<>();
        String sql = "SELECT * FROM inscription WHERE idApprenant = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idApprenant);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findByApprenant inscription : " + e.getMessage());
        }
        return list;
    }



    public java.util.List<metier.Inscription> findBySession(int idSession) {
        java.util.List<metier.Inscription> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM inscription WHERE idSession = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idSession);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findBySession : " + e.getMessage());
        }
        return list;
    }

    public boolean updateResultSession(int idInscription, String resultat) {
        // resultat: ADMIS, AJOURNE, ou null
        String sql = "UPDATE inscription SET resultSession = ? WHERE idInscription = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (resultat != null) ps.setString(1, resultat); else ps.setNull(1, Types.VARCHAR);
            ps.setInt(2, idInscription);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Colonne peut ne pas exister encore (migration)
            System.err.println("Erreur updateResultSession : " + e.getMessage());
        }
        return false;
    }

    public boolean insert(Inscription i) {
        String sql = "INSERT INTO inscription (dateInscription, statut, validationAdministrative, idApprenant, idSession) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(i.getDateInscription()));
            ps.setString(2, i.getStatut() != null ? i.getStatut() : "EN_ATTENTE");
            ps.setBoolean(3, i.isValidationAdministrative());
            if (i.getIdApprenant() != null) ps.setInt(4, i.getIdApprenant()); else ps.setNull(4, Types.INTEGER);
            if (i.getIdSession() != null) ps.setInt(5, i.getIdSession()); else ps.setNull(5, Types.INTEGER);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) i.setIdInscription(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur insert inscription : " + e.getMessage());
        }
        return false;
    }

    public boolean update(Inscription i) {
        String sql = "UPDATE inscription SET statut=?, validationAdministrative=? WHERE idInscription=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, i.getStatut());
            ps.setBoolean(2, i.isValidationAdministrative());
            ps.setInt(3, i.getIdInscription());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur update inscription : " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM inscription WHERE idInscription = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur delete inscription : " + e.getMessage());
        }
        return false;
    }

    private Inscription mapResultSet(ResultSet rs) throws SQLException {
        Inscription i = new Inscription();
        i.setIdInscription(rs.getInt("idInscription"));
        i.setDateInscription(rs.getDate("dateInscription").toLocalDate());
        i.setStatut(rs.getString("statut"));
        i.setValidationAdministrative(rs.getBoolean("validationAdministrative"));
        int idA = rs.getInt("idApprenant"); i.setIdApprenant(rs.wasNull() ? null : idA);
        int idS = rs.getInt("idSession"); i.setIdSession(rs.wasNull() ? null : idS);
        try { i.setResultSession(rs.getString("resultSession")); } catch (SQLException ignored) {}
        return i;
    }

    private Inscription mapResultSetAvecDetails(ResultSet rs) throws SQLException {
        Inscription i = mapResultSet(rs);
        try {
            String nom    = rs.getString("nomApprenant");
            String prenom = rs.getString("prenomApprenant");
            if (nom != null) {
                metier.Apprenant a = new metier.Apprenant();
                if (i.getIdApprenant() != null) a.setIdUtilisateur(i.getIdApprenant());
                a.setNom(nom);
                a.setPrenom(prenom != null ? prenom : "");
                i.setApprenant(a);
            }
        } catch (SQLException ignored) {}
        try {
            java.sql.Date dateDebut = rs.getDate("dateDebut");
            String lieu = rs.getString("lieu");
            String prog = rs.getString("intituleProgramme");
            if (lieu != null) {
                metier.Session s = new metier.Session();
                if (i.getIdSession() != null) s.setIdSession(i.getIdSession());
                s.setDateDebut(dateDebut != null ? dateDebut.toLocalDate() : java.time.LocalDate.now());
                s.setLieu(lieu);
                if (prog != null) {
                    metier.Programme p = new metier.Programme();
                    p.setIntitule(prog);
                    s.setProgramme(p);
                }
                i.setSession(s);
            }
        } catch (SQLException ignored) {}
        return i;
    }
}
