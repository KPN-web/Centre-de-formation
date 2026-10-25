package Database;

import metier.Apprenant;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ApprenantDAO {

    private Connection connection;

    public ApprenantDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public List<Apprenant> findAll() {
        List<Apprenant> list = new ArrayList<>();
        String sql = "SELECT u.*, a.matricule, a.telephone, a.adresse, a.dateInscription, a.idEntreprise " +
                     "FROM utilisateurs u JOIN apprenant a ON u.idUtilisateur = a.idUtilisateur " +
                     "ORDER BY u.nom, u.prenom";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findAll apprenants : " + e.getMessage());
        }
        return list;
    }

    public Apprenant findById(int id) {
        String sql = "SELECT u.*, a.matricule, a.telephone, a.adresse, a.dateInscription, a.idEntreprise " +
                     "FROM utilisateurs u JOIN apprenant a ON u.idUtilisateur = a.idUtilisateur " +
                     "WHERE u.idUtilisateur = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) {
            System.err.println("Erreur findById apprenant : " + e.getMessage());
        }
        return null;
    }

    /** Retourne uniquement les apprenants appartenant à une entreprise donnée. */
    public List<Apprenant> findByEntrepriseId(int idEntreprise) {
        List<Apprenant> list = new ArrayList<>();
        String sql = "SELECT u.*, a.matricule, a.telephone, a.adresse, a.dateInscription, a.idEntreprise " +
                     "FROM utilisateurs u JOIN apprenant a ON u.idUtilisateur = a.idUtilisateur " +
                     "WHERE a.idEntreprise = ? ORDER BY u.nom, u.prenom";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idEntreprise);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findByEntrepriseId apprenants : " + e.getMessage());
        }
        return list;
    }

    public boolean insert(Apprenant a) {
        try {
            connection.setAutoCommit(false);
            // Insert utilisateurs
            String sqlU = "INSERT INTO utilisateurs (nom, prenom, email, motDePasse, role, dateCreation, actif) VALUES (?,?,?,?,?,?,?)";
            try (PreparedStatement ps = connection.prepareStatement(sqlU, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, a.getNom());
                ps.setString(2, a.getPrenom());
                ps.setString(3, a.getEmail());
                ps.setString(4, a.getMotDePasse());
                ps.setString(5, "APPRENANT");
                ps.setDate(6, Date.valueOf(a.getDateCreation() != null ? a.getDateCreation() : LocalDate.now()));
                ps.setBoolean(7, a.isActif());
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) a.setIdUtilisateur(keys.getInt(1));
            }
            // Insert apprenant
            String sqlA = "INSERT INTO apprenant (idUtilisateur, matricule, telephone, adresse, dateInscription, idEntreprise) VALUES (?,?,?,?,?,?)";
            try (PreparedStatement ps = connection.prepareStatement(sqlA)) {
                ps.setInt(1, a.getIdUtilisateur());
                ps.setString(2, a.getMatricule());
                ps.setString(3, a.getTelephone() != null ? a.getTelephone() : "");
                ps.setString(4, a.getAdresse() != null ? a.getAdresse() : "");
                ps.setDate(5, Date.valueOf(a.getDateInscription() != null ? a.getDateInscription() : LocalDate.now()));
                if (a.getIdEntreprise() != null) ps.setInt(6, a.getIdEntreprise());
                else ps.setNull(6, java.sql.Types.INTEGER);
                ps.executeUpdate();
            }
            connection.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur insert apprenant : " + e.getMessage());
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
        return false;
    }

    public boolean update(Apprenant a) {
        try {
            connection.setAutoCommit(false);
            String sqlU = "UPDATE utilisateurs SET nom=?, prenom=?, email=?, motDePasse=?, actif=? WHERE idUtilisateur=?";
            try (PreparedStatement ps = connection.prepareStatement(sqlU)) {
                ps.setString(1, a.getNom());
                ps.setString(2, a.getPrenom());
                ps.setString(3, a.getEmail());
                ps.setString(4, a.getMotDePasse());
                ps.setBoolean(5, a.isActif());
                ps.setInt(6, a.getIdUtilisateur());
                ps.executeUpdate();
            }
            String sqlA = "UPDATE apprenant SET matricule=?, telephone=?, adresse=?, idEntreprise=? WHERE idUtilisateur=?";
            try (PreparedStatement ps = connection.prepareStatement(sqlA)) {
                ps.setString(1, a.getMatricule());
                ps.setString(2, a.getTelephone());
                ps.setString(3, a.getAdresse());
                if (a.getIdEntreprise() != null) ps.setInt(4, a.getIdEntreprise());
                else ps.setNull(4, java.sql.Types.INTEGER);
                ps.setInt(5, a.getIdUtilisateur());
                ps.executeUpdate();
            }
            connection.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur update apprenant : " + e.getMessage());
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
            System.err.println("Erreur delete apprenant : " + e.getMessage());
        }
        return false;
    }

    private Apprenant mapResultSet(ResultSet rs) throws SQLException {
        Apprenant a = new Apprenant();
        a.setIdUtilisateur(rs.getInt("idUtilisateur"));
        a.setNom(rs.getString("nom"));
        a.setPrenom(rs.getString("prenom"));
        a.setEmail(rs.getString("email"));
        a.setMotDePasse(rs.getString("motDePasse"));
        a.setRole("APPRENANT");
        Date dc = rs.getDate("dateCreation");
        a.setDateCreation(dc != null ? dc.toLocalDate() : null);
        a.setActif(rs.getBoolean("actif"));
        a.setMatricule(rs.getString("matricule"));
        a.setTelephone(rs.getString("telephone"));
        a.setAdresse(rs.getString("adresse"));
        Date di = rs.getDate("dateInscription");
        a.setDateInscription(di != null ? di.toLocalDate() : null);
        try {
            int idE = rs.getInt("idEntreprise");
            a.setIdEntreprise(rs.wasNull() ? null : idE);
        } catch (SQLException ignored) {}
        return a;
    }
}