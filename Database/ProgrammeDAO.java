package Database;

import metier.Programme;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProgrammeDAO {

    private Connection connection;

    public ProgrammeDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public List<Programme> findAll() {
        List<Programme> list = new ArrayList<>();
        String sql = "SELECT * FROM programme ORDER BY intitule";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findAll programmes : " + e.getMessage());
        }
        return list;
    }

    public Programme findById(int id) {
        String sql = "SELECT * FROM programme WHERE idProgramme = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) {
            System.err.println("Erreur findById programme : " + e.getMessage());
        }
        return null;
    }

    public boolean insert(Programme p) {
        String sql = "INSERT INTO programme (intitule, objectifs, prerequis, duree, modalite, tarif, totalAPayer, publie, archive) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getIntitule());
            ps.setString(2, p.getObjectifs());
            ps.setString(3, p.getPrerequis());
            ps.setInt(4, p.getDuree());
            ps.setString(5, p.getModalite());
            ps.setDouble(6, p.getTarif());
            ps.setDouble(7, p.getTotalAPayer());
            ps.setBoolean(8, p.isPublie());
            ps.setBoolean(9, p.isArchive());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) p.setIdProgramme(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur insert programme : " + e.getMessage());
        }
        return false;
    }

    public boolean update(Programme p) {
        String sql = "UPDATE programme SET intitule=?, objectifs=?, prerequis=?, duree=?, modalite=?, tarif=?, totalAPayer=?, publie=?, archive=? WHERE idProgramme=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, p.getIntitule());
            ps.setString(2, p.getObjectifs());
            ps.setString(3, p.getPrerequis());
            ps.setInt(4, p.getDuree());
            ps.setString(5, p.getModalite());
            ps.setDouble(6, p.getTarif());
            ps.setDouble(7, p.getTotalAPayer());
            ps.setBoolean(8, p.isPublie());
            ps.setBoolean(9, p.isArchive());
            ps.setInt(10, p.getIdProgramme());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur update programme : " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM programme WHERE idProgramme = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur delete programme : " + e.getMessage());
        }
        return false;
    }

    private Programme mapResultSet(ResultSet rs) throws SQLException {
        Programme p = new Programme();
        p.setIdProgramme(rs.getInt("idProgramme"));
        p.setIntitule(rs.getString("intitule"));
        p.setObjectifs(rs.getString("objectifs"));
        p.setPrerequis(rs.getString("prerequis"));
        p.setDuree(rs.getInt("duree"));
        p.setModalite(rs.getString("modalite"));
        p.setTarif(rs.getDouble("tarif"));
        p.setTotalAPayer(rs.getDouble("totalAPayer"));
        p.setPublie(rs.getBoolean("publie"));
        p.setArchive(rs.getBoolean("archive"));
        return p;
    }
}
