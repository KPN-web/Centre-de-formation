package Database;

import metier.Session;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessionDAO {

    private Connection connection;

    public SessionDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public List<Session> findAll() {
        List<Session> list = new ArrayList<>();
        String sql = "SELECT s.*, p.intitule AS intituleProgramme " +
                     "FROM session s LEFT JOIN programme p ON s.idProgramme = p.idProgramme " +
                     "ORDER BY s.dateDebut DESC";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSetWithProgramme(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findAll sessions : " + e.getMessage());
        }
        return list;
    }

    public Session findById(int id) {
        String sql = "SELECT * FROM session WHERE idSession = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) {
            System.err.println("Erreur findById session : " + e.getMessage());
        }
        return null;
    }

    public List<Session> findByFormateur(int idFormateur) {
        List<Session> list = new ArrayList<>();
        String sql = "SELECT s.*, p.intitule AS intituleProgramme " +
                     "FROM session s LEFT JOIN programme p ON s.idProgramme = p.idProgramme " +
                     "WHERE s.idFormateur = ? ORDER BY s.dateDebut DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idFormateur);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSetWithProgramme(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findByFormateur session : " + e.getMessage());
        }
        return list;
    }

    public boolean insert(Session s) {
        String sql = "INSERT INTO session (dateDebut, dateFin, lieu, capaciteMax, statut, idProgramme, idFormateur) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(s.getDateDebut()));
            ps.setDate(2, Date.valueOf(s.getDateFin()));
            ps.setString(3, s.getLieu());
            ps.setInt(4, s.getCapaciteMax());
            ps.setString(5, s.getStatut() != null ? s.getStatut() : "PLANIFIEE");
            if (s.getIdProgramme() != null) ps.setInt(6, s.getIdProgramme()); else ps.setNull(6, Types.INTEGER);
            if (s.getIdFormateur() != null) ps.setInt(7, s.getIdFormateur()); else ps.setNull(7, Types.INTEGER);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) s.setIdSession(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur insert session : " + e.getMessage());
        }
        return false;
    }

    public boolean update(Session s) {
        String sql = "UPDATE session SET dateDebut=?, dateFin=?, lieu=?, capaciteMax=?, statut=?, idProgramme=?, idFormateur=? WHERE idSession=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(s.getDateDebut()));
            ps.setDate(2, Date.valueOf(s.getDateFin()));
            ps.setString(3, s.getLieu());
            ps.setInt(4, s.getCapaciteMax());
            ps.setString(5, s.getStatut());
            if (s.getIdProgramme() != null) ps.setInt(6, s.getIdProgramme()); else ps.setNull(6, Types.INTEGER);
            if (s.getIdFormateur() != null) ps.setInt(7, s.getIdFormateur()); else ps.setNull(7, Types.INTEGER);
            ps.setInt(8, s.getIdSession());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur update session : " + e.getMessage());
        }
        return false;
    }

    /** Termine manuellement une session (statut → TERMINEE), quelle que soit la date. */
    public boolean terminerSession(int idSession) {
        String sql = "UPDATE session SET statut = 'TERMINEE' WHERE idSession = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idSession);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur terminerSession : " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM session WHERE idSession = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur delete session : " + e.getMessage());
        }
        return false;
    }

    private Session mapResultSet(ResultSet rs) throws SQLException {
        Session s = new Session();
        s.setIdSession(rs.getInt("idSession"));
        s.setDateDebut(rs.getDate("dateDebut").toLocalDate());
        s.setDateFin(rs.getDate("dateFin").toLocalDate());
        s.setLieu(rs.getString("lieu"));
        s.setCapaciteMax(rs.getInt("capaciteMax"));
        s.setStatut(rs.getString("statut"));
        int idP = rs.getInt("idProgramme"); s.setIdProgramme(rs.wasNull() ? null : idP);
        int idF = rs.getInt("idFormateur"); s.setIdFormateur(rs.wasNull() ? null : idF);
        return s;
    }

    private Session mapResultSetWithProgramme(ResultSet rs) throws SQLException {
        Session s = mapResultSet(rs);
        try {
            String intitule = rs.getString("intituleProgramme");
            if (intitule != null) {
                metier.Programme p = new metier.Programme();
                p.setIntitule(intitule);
                if (s.getIdProgramme() != null) p.setIdProgramme(s.getIdProgramme());
                s.setProgramme(p);
            }
        } catch (SQLException ignored) {}
        return s;
    }
}
