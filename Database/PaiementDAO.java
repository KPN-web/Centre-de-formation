package Database;

import metier.Paiement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaiementDAO {

    private Connection connection;

    public PaiementDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    // ── Tous les paiements ──────────────────────────────────────────────
    public List<Paiement> findAll() {
        List<Paiement> list = new ArrayList<>();
        String sql = "SELECT * FROM paiement ORDER BY datePaiement DESC";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findAll paiements : " + e.getMessage());
        }
        return list;
    }

    // ── Paiements des apprenants uniquement ─────────────────────────────
    public List<Paiement> findPaiementsApprenants() {
        List<Paiement> list = new ArrayList<>();
        String sql = "SELECT * FROM paiement WHERE typePaiement = 'APPRENANT' OR typePaiement IS NULL ORDER BY datePaiement DESC";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findPaiementsApprenants : " + e.getMessage());
        }
        return list;
    }

    // ── Paiements des formateurs uniquement ─────────────────────────────
    public List<Paiement> findPaiementsFormateurs() {
        List<Paiement> list = new ArrayList<>();
        String sql = "SELECT * FROM paiement WHERE typePaiement = 'FORMATEUR' ORDER BY datePaiement DESC";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findPaiementsFormateurs : " + e.getMessage());
        }
        return list;
    }

    // ── Paiements d'un apprenant donné ──────────────────────────────────
    public List<Paiement> findByApprenant(int idApprenant) {
        List<Paiement> list = new ArrayList<>();
        String sql = "SELECT * FROM paiement WHERE idApprenant = ? ORDER BY datePaiement DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idApprenant);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findByApprenant paiements : " + e.getMessage());
        }
        return list;
    }

    // ── Paiements d'un formateur donné ──────────────────────────────────
    public List<Paiement> findByFormateur(int idFormateur) {
        List<Paiement> list = new ArrayList<>();
        String sql = "SELECT * FROM paiement WHERE idFormateur = ? ORDER BY datePaiement DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idFormateur);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Erreur findByFormateur paiements : " + e.getMessage());
        }
        return list;
    }

    public Paiement findById(int id) {
        String sql = "SELECT * FROM paiement WHERE idPaiement = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) {
            System.err.println("Erreur findById paiement : " + e.getMessage());
        }
        return null;
    }

    // ── Montant total payé par un apprenant (paiements validés) ─────────
    public double getMontantPayeParApprenant(int idApprenant) {
        String sql = "SELECT COALESCE(SUM(montant), 0) FROM paiement WHERE idApprenant = ? AND statutPaiement = 'VALIDE'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idApprenant);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Erreur getMontantPayeParApprenant : " + e.getMessage());
        }
        return 0;
    }

    /**
     * Calcule le reste à payer pour un apprenant :
     * Somme des tarifs des programmes auxquels il est inscrit (VALIDEE) - montant déjà payé.
     */
    public double getMontantRestantParApprenant(int idApprenant) {
        String sqlTotal = "SELECT COALESCE(SUM(p.tarif), 0) " +
                          "FROM inscription i " +
                          "JOIN session s ON i.idSession = s.idSession " +
                          "JOIN programme p ON s.idProgramme = p.idProgramme " +
                          "WHERE i.idApprenant = ? AND i.statut = 'VALIDEE'";
        double total = 0;
        try (PreparedStatement ps = connection.prepareStatement(sqlTotal)) {
            ps.setInt(1, idApprenant);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) total = rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Erreur getMontantRestant (total) : " + e.getMessage());
        }
        double paye = getMontantPayeParApprenant(idApprenant);
        return Math.max(0, total - paye);
    }

    public double getTotalChiffreAffaires() {
        String sql = "SELECT SUM(montant) FROM paiement WHERE statutPaiement = 'VALIDE' AND (typePaiement = 'APPRENANT' OR typePaiement IS NULL)";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Erreur getTotalChiffreAffaires : " + e.getMessage());
        }
        return 0;
    }

    public double getTotalSalairesFormateurs() {
        String sql = "SELECT SUM(montant) FROM paiement WHERE statutPaiement = 'VALIDE' AND typePaiement = 'FORMATEUR'";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Erreur getTotalSalaires : " + e.getMessage());
        }
        return 0;
    }

    public boolean insert(Paiement p) {
        String sql = "INSERT INTO paiement (montant, datePaiement, modePaiement, statutPaiement, emailPayeur, idInscription, idApprenant, idFormateur, typePaiement, description) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, p.getMontant());
            ps.setDate(2, Date.valueOf(p.getDatePaiement()));
            ps.setString(3, p.getModePaiement());
            ps.setString(4, p.getStatutPaiement() != null ? p.getStatutPaiement() : "EN_ATTENTE");
            ps.setString(5, p.getEmailPayeur() != null ? p.getEmailPayeur() : "");
            if (p.getIdInscription() != null) ps.setInt(6, p.getIdInscription()); else ps.setNull(6, Types.INTEGER);
            if (p.getIdApprenant()   != null) ps.setInt(7, p.getIdApprenant());   else ps.setNull(7, Types.INTEGER);
            if (p.getIdFormateur()   != null) ps.setInt(8, p.getIdFormateur());   else ps.setNull(8, Types.INTEGER);
            ps.setString(9, p.getTypePaiement() != null ? p.getTypePaiement() : "APPRENANT");
            ps.setString(10, p.getDescription() != null ? p.getDescription() : "");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) p.setIdPaiement(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur insert paiement : " + e.getMessage());
        }
        return false;
    }

    public boolean update(Paiement p) {
        String sql = "UPDATE paiement SET montant=?, modePaiement=?, statutPaiement=?, emailPayeur=?, idApprenant=?, idFormateur=?, typePaiement=?, description=? WHERE idPaiement=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, p.getMontant());
            ps.setString(2, p.getModePaiement());
            ps.setString(3, p.getStatutPaiement());
            ps.setString(4, p.getEmailPayeur());
            if (p.getIdApprenant() != null) ps.setInt(5, p.getIdApprenant()); else ps.setNull(5, Types.INTEGER);
            if (p.getIdFormateur() != null) ps.setInt(6, p.getIdFormateur()); else ps.setNull(6, Types.INTEGER);
            ps.setString(7, p.getTypePaiement() != null ? p.getTypePaiement() : "APPRENANT");
            ps.setString(8, p.getDescription() != null ? p.getDescription() : "");
            ps.setInt(9, p.getIdPaiement());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur update paiement : " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM paiement WHERE idPaiement = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur delete paiement : " + e.getMessage());
        }
        return false;
    }


    // ── Montant total déjà versé à un formateur ─────────────────────────
    public double getTotalMontantFormateur(int idFormateur) {
        String sql = "SELECT COALESCE(SUM(montant), 0) FROM paiement WHERE idFormateur = ? AND statutPaiement = 'VALIDE' AND typePaiement = 'FORMATEUR'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idFormateur);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Erreur getTotalMontantFormateur : " + e.getMessage());
        }
        return 0;
    }

    // ── Vote satisfaction apprenant (stocké dans paiement avec typePaiement=SATISFACTION) ──
    public boolean hasVoteSatisfaction(int idInscription) {
        String sql = "SELECT COUNT(*) FROM paiement WHERE idInscription = ? AND typePaiement = 'SATISFACTION'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idInscription);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { System.err.println("Erreur hasVoteSatisfaction : " + e.getMessage()); }
        return false;
    }

    public boolean enregistrerVoteSatisfaction(int idInscription, int idApprenant, boolean satisfait) {
        // supprimer éventuel vote précédent
        String del = "DELETE FROM paiement WHERE idInscription = ? AND typePaiement = 'SATISFACTION'";
        try (PreparedStatement ps = connection.prepareStatement(del)) {
            ps.setInt(1, idInscription);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("Erreur delete vote : " + e.getMessage()); }
        String ins = "INSERT INTO paiement (montant, datePaiement, modePaiement, statutPaiement, emailPayeur, idInscription, idApprenant, typePaiement, description) VALUES (0, CURRENT_DATE, 'VOTE', 'VALIDE', '', ?, ?, 'SATISFACTION', ?)";
        try (PreparedStatement ps = connection.prepareStatement(ins)) {
            ps.setInt(1, idInscription);
            ps.setInt(2, idApprenant);
            ps.setString(3, satisfait ? "SATISFAIT" : "NON_SATISFAIT");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Erreur enregistrerVoteSatisfaction : " + e.getMessage()); }
        return false;
    }

    // ── Taux de satisfaction global (tous apprenants) ────────────────────
    public double getTauxSatisfactionGlobal() {
        String sql = "SELECT COUNT(*) AS total, SUM(CASE WHEN description = 'SATISFAIT' THEN 1 ELSE 0 END) AS satisfaits FROM paiement WHERE typePaiement = 'SATISFACTION'";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                int total = rs.getInt("total");
                if (total == 0) return 0;
                return 100.0 * rs.getInt("satisfaits") / total;
            }
        } catch (SQLException e) { System.err.println("Erreur getTauxSatisfactionGlobal : " + e.getMessage()); }
        return 0;
    }

    public String getVoteApprenant(int idInscription) {
        String sql = "SELECT description FROM paiement WHERE idInscription = ? AND typePaiement = 'SATISFACTION' LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idInscription);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);
        } catch (SQLException e) { System.err.println("Erreur getVoteApprenant : " + e.getMessage()); }
        return null;
    }

    private Paiement mapResultSet(ResultSet rs) throws SQLException {
        Paiement p = new Paiement();
        p.setIdPaiement(rs.getInt("idPaiement"));
        p.setMontant(rs.getDouble("montant"));
        p.setDatePaiement(rs.getDate("datePaiement").toLocalDate());
        p.setModePaiement(rs.getString("modePaiement"));
        p.setStatutPaiement(rs.getString("statutPaiement"));
        p.setEmailPayeur(rs.getString("emailPayeur"));
        int idI = rs.getInt("idInscription"); p.setIdInscription(rs.wasNull() ? null : idI);
        try {
            int idA = rs.getInt("idApprenant"); p.setIdApprenant(rs.wasNull() ? null : idA);
            int idF = rs.getInt("idFormateur"); p.setIdFormateur(rs.wasNull() ? null : idF);
            p.setTypePaiement(rs.getString("typePaiement"));
            p.setDescription(rs.getString("description"));
        } catch (SQLException e) {
            // Colonnes pas encore migrées — compatibilité ascendante
        }
        return p;
    }
}
