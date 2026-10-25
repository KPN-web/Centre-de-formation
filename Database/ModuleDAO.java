package Database;

import metier.Module;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleDAO {

    private Connection connection;

    public ModuleDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public List<Module> findAll() {
        List<Module> list = new ArrayList<>();
        String sql = "SELECT * FROM module ORDER BY titre";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findAll modules : " + e.getMessage());
        }
        return list;
    }

    public List<Module> findByProgramme(int idProgramme) {
        List<Module> list = new ArrayList<>();
        String sql = "SELECT * FROM module WHERE idProgramme = ? ORDER BY titre";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idProgramme);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findByProgramme modules : " + e.getMessage());
        }
        return list;
    }

    public boolean insert(Module m) {
        String sql = "INSERT INTO module (titre, description, typeContenu, support, idProgramme) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getTitre());
            ps.setString(2, m.getDescription());
            ps.setString(3, m.getTypeContenu());
            ps.setString(4, m.getSupport());
            if (m.getIdProgramme() != null) ps.setInt(5, m.getIdProgramme()); else ps.setNull(5, Types.INTEGER);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) m.setIdModule(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur insert module : " + e.getMessage());
        }
        return false;
    }

    public boolean update(Module m) {
        String sql = "UPDATE module SET titre=?, description=?, typeContenu=?, support=?, idProgramme=? WHERE idModule=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, m.getTitre());
            ps.setString(2, m.getDescription());
            ps.setString(3, m.getTypeContenu());
            ps.setString(4, m.getSupport());
            if (m.getIdProgramme() != null) ps.setInt(5, m.getIdProgramme()); else ps.setNull(5, Types.INTEGER);
            ps.setInt(6, m.getIdModule());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur update module : " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM module WHERE idModule = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur delete module : " + e.getMessage());
        }
        return false;
    }

    private Module mapResultSet(ResultSet rs) throws SQLException {
        Module m = new Module();
        m.setIdModule(rs.getInt("idModule"));
        m.setTitre(rs.getString("titre"));
        m.setDescription(rs.getString("description"));
        m.setTypeContenu(rs.getString("typeContenu"));
        m.setSupport(rs.getString("support"));
        int idP = rs.getInt("idProgramme"); m.setIdProgramme(rs.wasNull() ? null : idP);
        return m;
    }
}
