package Database;

import metier.Certification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CertificationDAO {

    private Connection connection;

    public CertificationDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public List<Certification> findAll() {
        List<Certification> list = new ArrayList<>();
        String sql = "SELECT * FROM certification ORDER BY dateEmission DESC";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findAll certifications : " + e.getMessage());
        }
        return list;
    }

    public List<Certification> findByApprenant(int idApprenant) {
        List<Certification> list = new ArrayList<>();
        String sql = "SELECT * FROM certification WHERE idApprenant = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idApprenant);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findByApprenant certifications : " + e.getMessage());
        }
        return list;
    }

    public boolean insert(Certification c) {
        String sql = "INSERT INTO certification (typeCertificat, dateEmission, fichierPDF, idApprenant, idSession) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getTypeCertificat());
            ps.setDate(2, Date.valueOf(c.getDateEmission()));
            ps.setString(3, c.getFichierPDF());
            if (c.getIdApprenant() != null) ps.setInt(4, c.getIdApprenant()); else ps.setNull(4, Types.INTEGER);
            if (c.getIdSession() != null) ps.setInt(5, c.getIdSession()); else ps.setNull(5, Types.INTEGER);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) c.setIdCertification(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur insert certification : " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM certification WHERE idCertification = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur delete certification : " + e.getMessage());
        }
        return false;
    }

    private Certification mapResultSet(ResultSet rs) throws SQLException {
        Certification c = new Certification();
        c.setIdCertification(rs.getInt("idCertification"));
        c.setTypeCertificat(rs.getString("typeCertificat"));
        c.setDateEmission(rs.getDate("dateEmission").toLocalDate());
        c.setFichierPDF(rs.getString("fichierPDF"));
        int idA = rs.getInt("idApprenant"); c.setIdApprenant(rs.wasNull() ? null : idA);
        int idS = rs.getInt("idSession"); c.setIdSession(rs.wasNull() ? null : idS);
        return c;
    }

    public boolean update(metier.Certification c) {
        String sql = "UPDATE certification SET typeCertificat=?, fichierPDF=?, idApprenant=?, idSession=? WHERE idCertification=?";
        try (java.sql.PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, c.getTypeCertificat());
            ps.setString(2, c.getFichierPDF());
            if (c.getIdApprenant() != null) ps.setInt(3, c.getIdApprenant()); else ps.setNull(3, java.sql.Types.INTEGER);
            if (c.getIdSession()   != null) ps.setInt(4, c.getIdSession());   else ps.setNull(4, java.sql.Types.INTEGER);
            ps.setInt(5, c.getIdCertification());
            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLException e) {
            System.err.println("Erreur update certification : " + e.getMessage());
        }
        return false;
    }

}