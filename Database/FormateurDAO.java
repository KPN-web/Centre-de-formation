package Database;

import metier.Formateur;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FormateurDAO {

    private Connection connection;

    public FormateurDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public List<Formateur> findAll() {
        List<Formateur> list = new ArrayList<>();
        String sql = "SELECT u.*, f.specialite, f.cv, f.tauxHoraire, f.disponibilite " +
                     "FROM utilisateurs u JOIN formateur f ON u.idUtilisateur = f.idUtilisateur " +
                     "ORDER BY u.nom, u.prenom";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findAll formateurs : " + e.getMessage());
        }
        return list;
    }

    public Formateur findById(int id) {
        String sql = "SELECT u.*, f.specialite, f.cv, f.tauxHoraire, f.disponibilite " +
                     "FROM utilisateurs u JOIN formateur f ON u.idUtilisateur = f.idUtilisateur " +
                     "WHERE u.idUtilisateur = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) {
            System.err.println("Erreur findById formateur : " + e.getMessage());
        }
        return null;
    }

    public boolean insert(Formateur f) {
        try {
            connection.setAutoCommit(false);
            String sqlU = "INSERT INTO utilisateurs (nom, prenom, email, motDePasse, role, dateCreation, actif) VALUES (?,?,?,?,?,?,?)";
            try (PreparedStatement ps = connection.prepareStatement(sqlU, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, f.getNom());
                ps.setString(2, f.getPrenom());
                ps.setString(3, f.getEmail());
                ps.setString(4, f.getMotDePasse());
                ps.setString(5, "FORMATEUR");
                ps.setDate(6, Date.valueOf(LocalDate.now()));
                ps.setBoolean(7, true);
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) f.setIdUtilisateur(keys.getInt(1));
            }
            String sqlF = "INSERT INTO formateur (idUtilisateur, specialite, cv, tauxHoraire, disponibilite) VALUES (?,?,?,?,?)";
            try (PreparedStatement ps = connection.prepareStatement(sqlF)) {
                ps.setInt(1, f.getIdUtilisateur());
                ps.setString(2, f.getSpecialite());
                ps.setString(3, f.getCv() != null ? f.getCv() : "");
                ps.setDouble(4, f.getTauxHoraire());
                ps.setString(5, f.getDisponibilite() != null ? f.getDisponibilite() : "");
                ps.executeUpdate();
            }
            connection.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur insert formateur : " + e.getMessage());
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
        return false;
    }

    public boolean update(Formateur f) {
        try {
            connection.setAutoCommit(false);
            String sqlU = "UPDATE utilisateurs SET nom=?, prenom=?, email=?, motDePasse=?, actif=? WHERE idUtilisateur=?";
            try (PreparedStatement ps = connection.prepareStatement(sqlU)) {
                ps.setString(1, f.getNom());
                ps.setString(2, f.getPrenom());
                ps.setString(3, f.getEmail());
                ps.setString(4, f.getMotDePasse());
                ps.setBoolean(5, f.isActif());
                ps.setInt(6, f.getIdUtilisateur());
                ps.executeUpdate();
            }
            String sqlF = "UPDATE formateur SET specialite=?, cv=?, tauxHoraire=?, disponibilite=? WHERE idUtilisateur=?";
            try (PreparedStatement ps = connection.prepareStatement(sqlF)) {
                ps.setString(1, f.getSpecialite());
                ps.setString(2, f.getCv());
                ps.setDouble(3, f.getTauxHoraire());
                ps.setString(4, f.getDisponibilite());
                ps.setInt(5, f.getIdUtilisateur());
                ps.executeUpdate();
            }
            connection.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur update formateur : " + e.getMessage());
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM utilisateurs WHERE idUtilisateur = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur delete formateur : " + e.getMessage());
        }
        return false;
    }

    private Formateur mapResultSet(ResultSet rs) throws SQLException {
        Formateur f = new Formateur();
        f.setIdUtilisateur(rs.getInt("idUtilisateur"));
        f.setNom(rs.getString("nom"));
        f.setPrenom(rs.getString("prenom"));
        f.setEmail(rs.getString("email"));
        f.setMotDePasse(rs.getString("motDePasse"));
        f.setRole("FORMATEUR");
        Date dc = rs.getDate("dateCreation");
        f.setDateCreation(dc != null ? dc.toLocalDate() : null);
        f.setActif(rs.getBoolean("actif"));
        f.setSpecialite(rs.getString("specialite"));
        f.setCv(rs.getString("cv"));
        f.setTauxHoraire(rs.getDouble("tauxHoraire"));
        f.setDisponibilite(rs.getString("disponibilite"));
        return f;
    }
}
