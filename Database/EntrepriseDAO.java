package Database;

import metier.Entreprise;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EntrepriseDAO {

    private Connection connection;

    public EntrepriseDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
        createTableIfNotExists();
    }

    /** Crée la table entreprise si elle n'existe pas encore (migration douce). */
    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS entreprise (" +
                     "  idEntreprise   INT AUTO_INCREMENT PRIMARY KEY, " +
                     "  nom            VARCHAR(150) NOT NULL, " +
                     "  adresse        VARCHAR(255), " +
                     "  telephone      VARCHAR(50), " +
                     "  email          VARCHAR(150), " +
                     "  contactReferent VARCHAR(150)" +
                     ")";
        try (Statement st = connection.createStatement()) {
            st.execute(sql);
            // Ajouter colonne idEntreprise dans apprenant si absente
            addColumnIfAbsent("apprenant", "idEntreprise", "INT NULL");
        } catch (SQLException e) {
            System.err.println("EntrepriseDAO init : " + e.getMessage());
        }
    }

    private void addColumnIfAbsent(String table, String column, String type) {
        try (ResultSet rs = connection.getMetaData().getColumns(null, null, table, column)) {
            if (!rs.next()) {
                String sql = "ALTER TABLE " + table + " ADD COLUMN " + column + " " + type;
                try (Statement st = connection.createStatement()) {
                    st.execute(sql);
                }
            }
        } catch (SQLException e) {
            System.err.println("addColumnIfAbsent " + column + " : " + e.getMessage());
        }
    }

    public List<Entreprise> findAll() {
        List<Entreprise> list = new ArrayList<>();
        String sql = "SELECT * FROM entreprise ORDER BY nom";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("EntrepriseDAO findAll : " + e.getMessage());
        }
        return list;
    }

    public Entreprise findById(int id) {
        String sql = "SELECT * FROM entreprise WHERE idEntreprise = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) {
            System.err.println("EntrepriseDAO findById : " + e.getMessage());
        }
        return null;
    }

    public boolean insert(Entreprise e) {
        String sql = "INSERT INTO entreprise (nom, adresse, telephone, email, contactReferent) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getNom());
            ps.setString(2, e.getAdresse() != null ? e.getAdresse() : "");
            ps.setString(3, e.getTelephone() != null ? e.getTelephone() : "");
            ps.setString(4, e.getEmail() != null ? e.getEmail() : "");
            ps.setString(5, e.getContactReferent() != null ? e.getContactReferent() : "");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) e.setIdEntreprise(keys.getInt(1));
                return true;
            }
        } catch (SQLException ex) {
            System.err.println("EntrepriseDAO insert : " + ex.getMessage());
        }
        return false;
    }

    public boolean update(Entreprise e) {
        String sql = "UPDATE entreprise SET nom=?, adresse=?, telephone=?, email=?, contactReferent=? WHERE idEntreprise=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, e.getNom());
            ps.setString(2, e.getAdresse() != null ? e.getAdresse() : "");
            ps.setString(3, e.getTelephone() != null ? e.getTelephone() : "");
            ps.setString(4, e.getEmail() != null ? e.getEmail() : "");
            ps.setString(5, e.getContactReferent() != null ? e.getContactReferent() : "");
            ps.setInt(6, e.getIdEntreprise());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("EntrepriseDAO update : " + ex.getMessage());
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM entreprise WHERE idEntreprise = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("EntrepriseDAO delete : " + e.getMessage());
        }
        return false;
    }

    /** Retourne les apprenants liés à une entreprise (nom seulement pour l'espace entreprise). */
    public List<metier.Apprenant> findApprenantsByEntreprise(int idEntreprise) {
        List<metier.Apprenant> list = new ArrayList<>();
        String sql = "SELECT u.*, a.matricule, a.telephone, a.adresse, a.dateInscription, a.idEntreprise " +
                     "FROM utilisateurs u JOIN apprenant a ON u.idUtilisateur = a.idUtilisateur " +
                     "WHERE a.idEntreprise = ? ORDER BY u.nom, u.prenom";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idEntreprise);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                metier.Apprenant ap = new metier.Apprenant();
                ap.setIdUtilisateur(rs.getInt("idUtilisateur"));
                ap.setNom(rs.getString("nom"));
                ap.setPrenom(rs.getString("prenom") != null ? rs.getString("prenom") : "");
                ap.setEmail(rs.getString("email"));
                ap.setMatricule(rs.getString("matricule"));
                ap.setTelephone(rs.getString("telephone"));
                ap.setAdresse(rs.getString("adresse"));
                java.sql.Date di = rs.getDate("dateInscription");
                ap.setDateInscription(di != null ? di.toLocalDate() : null);
                int idE = rs.getInt("idEntreprise");
                ap.setIdEntreprise(rs.wasNull() ? null : idE);
                list.add(ap);
            }
        } catch (SQLException e) {
            System.err.println("findApprenantsByEntreprise : " + e.getMessage());
        }
        return list;
    }

    private Entreprise map(ResultSet rs) throws SQLException {
        Entreprise e = new Entreprise();
        e.setIdEntreprise(rs.getInt("idEntreprise"));
        e.setNom(rs.getString("nom"));
        e.setAdresse(rs.getString("adresse"));
        e.setTelephone(rs.getString("telephone"));
        e.setEmail(rs.getString("email"));
        e.setContactReferent(rs.getString("contactReferent"));
        return e;
    }
}
