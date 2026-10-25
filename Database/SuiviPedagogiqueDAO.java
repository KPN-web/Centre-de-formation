package Database;

import metier.SuiviPedagogique;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SuiviPedagogiqueDAO {

    private Connection connection;

    public SuiviPedagogiqueDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public List<SuiviPedagogique> findAll() {
        List<SuiviPedagogique> list = new ArrayList<>();
        String sql = "SELECT * FROM suivipedagogique";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findAll suivis : " + e.getMessage());
        }
        return list;
    }

    /** Tous les suivis avec Apprenant et Session (libellé) chargés */
    public List<SuiviPedagogique> findAllAvecDetails() {
        return findAvecDetails("", -1);
    }

    /** Suivis d'une session avec détails */
    public List<SuiviPedagogique> findBySessionAvecDetails(int idSession) {
        return findAvecDetails("WHERE sp.idSession = ?", idSession);
    }

    /** Suivis des sessions d'un formateur avec détails */
    public List<SuiviPedagogique> findByFormateurAvecDetails(int idFormateur) {
        List<SuiviPedagogique> list = new ArrayList<>();
        String sql = "SELECT sp.*, " +
                     "u.nom AS nomApprenant, u.prenom AS prenomApprenant, " +
                     "s.dateDebut, s.lieu, p.intitule AS intituleProgramme " +
                     "FROM suivipedagogique sp " +
                     "LEFT JOIN utilisateurs u ON sp.idApprenant = u.idUtilisateur " +
                     "LEFT JOIN session s ON sp.idSession = s.idSession " +
                     "LEFT JOIN programme p ON s.idProgramme = p.idProgramme " +
                     "WHERE s.idFormateur = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idFormateur);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSetAvecDetails(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findByFormateurAvecDetails : " + e.getMessage());
        }
        return list;
    }

    private List<SuiviPedagogique> findAvecDetails(String whereClause, int param) {
        List<SuiviPedagogique> list = new ArrayList<>();
        String sql = "SELECT sp.*, " +
                     "u.nom AS nomApprenant, u.prenom AS prenomApprenant, " +
                     "s.dateDebut, s.lieu, p.intitule AS intituleProgramme " +
                     "FROM suivipedagogique sp " +
                     "LEFT JOIN utilisateurs u ON sp.idApprenant = u.idUtilisateur " +
                     "LEFT JOIN session s ON sp.idSession = s.idSession " +
                     "LEFT JOIN programme p ON s.idProgramme = p.idProgramme " +
                     (whereClause.isEmpty() ? "" : whereClause);
        try {
            if (param >= 0) {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setInt(1, param);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) list.add(mapResultSetAvecDetails(rs));
            } else {
                Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql);
                while (rs.next()) list.add(mapResultSetAvecDetails(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur findAvecDetails suivis : " + e.getMessage());
        }
        return list;
    }

    public List<SuiviPedagogique> findBySession(int idSession) {
        List<SuiviPedagogique> list = new ArrayList<>();
        String sql = "SELECT * FROM suivipedagogique WHERE idSession = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idSession);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findBySession suivis : " + e.getMessage());
        }
        return list;
    }

    public List<SuiviPedagogique> findByApprenant(int idApprenant) {
        List<SuiviPedagogique> list = new ArrayList<>();
        String sql = "SELECT * FROM suivipedagogique WHERE idApprenant = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idApprenant);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findByApprenant suivis : " + e.getMessage());
        }
        return list;
    }

    public double getTauxPresence(int idSession) {
        String sql = "SELECT AVG(presence) * 100 FROM suivipedagogique WHERE idSession = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idSession);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Erreur getTauxPresence : " + e.getMessage());
        }
        return 0;
    }

    public boolean insert(SuiviPedagogique s) {
        String sql = "INSERT INTO suivipedagogique (presence, note, feedback, idApprenant, idSession) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBoolean(1, s.isPresence());
            ps.setDouble(2, s.getNote());
            ps.setString(3, s.getFeedback() != null ? s.getFeedback() : "");
            if (s.getIdApprenant() != null) ps.setInt(4, s.getIdApprenant()); else ps.setNull(4, Types.INTEGER);
            if (s.getIdSession() != null) ps.setInt(5, s.getIdSession()); else ps.setNull(5, Types.INTEGER);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) s.setIdSuivi(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur insert suivi : " + e.getMessage());
        }
        return false;
    }

    public boolean updateStatut(int idSuivi, String statut) {
        // Tente d'abord avec la colonne statutSuivi ; si elle n'existe pas en base, on ignore silencieusement
        String sql = "UPDATE suivipedagogique SET statutSuivi=? WHERE idSuivi=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, idSuivi);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("updateStatut : " + e.getMessage());
        }
        return false;
    }

    public boolean update(SuiviPedagogique s) {
        String sql = "UPDATE suivipedagogique SET presence=?, note=?, feedback=? WHERE idSuivi=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, s.isPresence());
            ps.setDouble(2, s.getNote());
            ps.setString(3, s.getFeedback());
            ps.setInt(4, s.getIdSuivi());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur update suivi : " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM suivipedagogique WHERE idSuivi = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur delete suivi : " + e.getMessage());
        }
        return false;
    }

    private SuiviPedagogique mapResultSet(ResultSet rs) throws SQLException {
        SuiviPedagogique s = new SuiviPedagogique();
        s.setIdSuivi(rs.getInt("idSuivi"));
        s.setPresence(rs.getBoolean("presence"));
        s.setNote(rs.getDouble("note"));
        s.setFeedback(rs.getString("feedback"));
        int idA = rs.getInt("idApprenant"); s.setIdApprenant(rs.wasNull() ? null : idA);
        int idSes = rs.getInt("idSession"); s.setIdSession(rs.wasNull() ? null : idSes);
        try { s.setStatutSuivi(rs.getString("statutSuivi")); } catch (SQLException ignored) {}
        return s;
    }

    private SuiviPedagogique mapResultSetAvecDetails(ResultSet rs) throws SQLException {
        SuiviPedagogique s = mapResultSet(rs);
        try {
            String nom    = rs.getString("nomApprenant");
            String prenom = rs.getString("prenomApprenant");
            if (nom != null) {
                metier.Apprenant a = new metier.Apprenant();
                if (s.getIdApprenant() != null) a.setIdUtilisateur(s.getIdApprenant());
                a.setNom(nom);
                a.setPrenom(prenom != null ? prenom : "");
                s.setApprenant(a);
            }
        } catch (SQLException ignored) {}
        try {
            java.sql.Date dateDebut = rs.getDate("dateDebut");
            String lieu  = rs.getString("lieu");
            String prog  = rs.getString("intituleProgramme");
            if (lieu != null) {
                metier.Session ses = new metier.Session();
                if (s.getIdSession() != null) ses.setIdSession(s.getIdSession());
                ses.setDateDebut(dateDebut != null ? dateDebut.toLocalDate() : java.time.LocalDate.now());
                ses.setLieu(lieu);
                if (prog != null) {
                    metier.Programme p = new metier.Programme();
                    p.setIntitule(prog);
                    ses.setProgramme(p);
                }
                s.setSession(ses);
            }
        } catch (SQLException ignored) {}
        return s;
    }
}
