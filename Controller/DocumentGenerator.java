package Controller;

import metier.*;

// java.awt.Desktop retiré : incompatible avec le module javafx sur certaines JVM
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Génère de vrais documents (reçu de paiement, facture entreprise, certificat)
 * au format texte imprimable (compatible impression système Java).
 *
 * Stratégie : génération d'un fichier HTML riche (imprimable via Desktop.open)
 * sans dépendance externe. Les fichiers sont sauvegardés dans le dossier
 * "documents" à la racine du projet.
 */
public class DocumentGenerator {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String DOSSIER = System.getProperty("user.home") + File.separator + "Documents_CF";

    static {
        // Créer le dossier de sortie au démarrage
        new File(DOSSIER).mkdirs();
    }

    // ─────────────────────────────────────────────────────────────────────
    //  REÇU DE PAIEMENT APPRENANT
    // ─────────────────────────────────────────────────────────────────────
    public static File genererRecuPaiement(metier.Paiement paiement,
                                            String nomApprenant,
                                            String nomFormation,
                                            double prixFormation) throws IOException {
        String numero = "REF-" + paiement.getIdPaiement() + "-" + System.currentTimeMillis() % 10000;
        String dateDoc = LocalDate.now().format(FMT);
        String datePaiement = paiement.getDatePaiement() != null
            ? paiement.getDatePaiement().format(FMT) : dateDoc;

        String html = "<!DOCTYPE html><html lang='fr'><head><meta charset='UTF-8'>" +
            "<title>Reçu de Paiement</title>" +
            "<style>" +
            "  body{font-family:'Segoe UI',Arial,sans-serif;margin:0;padding:30px;color:#222;background:#fff;}" +
            "  .header{display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:30px;" +
            "          border-bottom:3px solid #1a3c5e;padding-bottom:16px;}" +
            "  .logo-bloc h1{margin:0;font-size:26px;color:#1a3c5e;}" +
            "  .logo-bloc p{margin:2px 0;font-size:12px;color:#555;}" +
            "  .badge{background:#1a3c5e;color:white;padding:10px 20px;border-radius:8px;text-align:center;}" +
            "  .badge .type{font-size:18px;font-weight:bold;letter-spacing:1px;}" +
            "  .badge .ref{font-size:11px;margin-top:4px;color:#aecce4;}" +
            "  .info-grid{display:grid;grid-template-columns:1fr 1fr;gap:12px;margin:20px 0;}" +
            "  .info-box{background:#f5f8fc;border:1px solid #d0dce9;border-radius:6px;padding:12px;}" +
            "  .info-box .label{font-size:11px;color:#666;text-transform:uppercase;letter-spacing:.5px;}" +
            "  .info-box .value{font-size:15px;font-weight:bold;color:#1a3c5e;margin-top:3px;}" +
            "  .montant-bloc{background:#1a3c5e;color:white;border-radius:8px;padding:20px;text-align:center;margin:24px 0;}" +
            "  .montant-bloc .label{font-size:13px;color:#aecce4;}" +
            "  .montant-bloc .amount{font-size:32px;font-weight:bold;margin:6px 0;}" +
            "  .table-detail{width:100%;border-collapse:collapse;margin:16px 0;font-size:13px;}" +
            "  .table-detail th{background:#1a3c5e;color:white;padding:8px 12px;text-align:left;}" +
            "  .table-detail td{padding:8px 12px;border-bottom:1px solid #eee;}" +
            "  .table-detail tr:nth-child(even) td{background:#f5f8fc;}" +
            "  .footer{margin-top:40px;padding-top:14px;border-top:1px solid #ddd;font-size:11px;color:#888;text-align:center;}" +
            "  .statut-ok{background:#27ae60;color:white;padding:3px 10px;border-radius:12px;font-size:12px;}" +
            "  .statut-att{background:#f39c12;color:white;padding:3px 10px;border-radius:12px;font-size:12px;}" +
            "  @media print{body{padding:15px;}}" +
            "</style></head><body>" +

            "<div class='header'>" +
            "  <div class='logo-bloc'>" +
            "    <h1>🎓 Centre de Formation</h1>" +
            "    <p>Formation Professionnelle & Continue</p>" +
            "    <p>Ouagadougou, Burkina Faso | contact@centreformation.bf</p>" +
            "  </div>" +
            "  <div class='badge'>" +
            "    <div class='type'>REÇU DE PAIEMENT</div>" +
            "    <div class='ref'>" + numero + "</div>" +
            "    <div style='font-size:12px;margin-top:4px;'>Le " + dateDoc + "</div>" +
            "  </div>" +
            "</div>" +

            "<div class='info-grid'>" +
            "  <div class='info-box'><div class='label'>Apprenant</div><div class='value'>" + esc(nomApprenant) + "</div></div>" +
            "  <div class='info-box'><div class='label'>Formation</div><div class='value'>" + esc(nomFormation) + "</div></div>" +
            "  <div class='info-box'><div class='label'>Date du paiement</div><div class='value'>" + datePaiement + "</div></div>" +
            "  <div class='info-box'><div class='label'>Mode de paiement</div><div class='value'>" + esc(paiement.getModePaiement()) + "</div></div>" +
            "</div>" +

            "<div class='montant-bloc'>" +
            "  <div class='label'>Montant réglé</div>" +
            "  <div class='amount'>" + String.format("%,.0f FCFA", paiement.getMontant()) + "</div>" +
            "  <div style='font-size:13px;margin-top:4px;'>Statut : " +
            "    <span class='" + ("REGLE".equalsIgnoreCase(paiement.getStatutPaiement()) ? "statut-ok" : "statut-att") + "'>" +
            esc(paiement.getStatutPaiement()) + "</span></div>" +
            "</div>" +

            "<table class='table-detail'>" +
            "  <tr><th>Désignation</th><th>Détail</th></tr>" +
            "  <tr><td>Prix total de la formation</td><td><strong>" + String.format("%,.0f FCFA", prixFormation) + "</strong></td></tr>" +
            "  <tr><td>Montant payé lors de cette transaction</td><td><strong>" + String.format("%,.0f FCFA", paiement.getMontant()) + "</strong></td></tr>" +
            "  <tr><td>Identifiant interne</td><td>ID-" + paiement.getIdPaiement() + "</td></tr>" +
            "</table>" +

            "<p style='font-size:12px;color:#555;font-style:italic;'>" +
            "Ce document atteste que le paiement ci-dessus a bien été enregistré dans notre système. " +
            "Conservez ce reçu comme justificatif.</p>" +

            "<div class='footer'>" +
            "  Document généré automatiquement le " + dateDoc +
            " | Centre de Formation Professionnelle – Ouagadougou, Burkina Faso" +
            "</div>" +
            "</body></html>";

        String nom = "recu_paiement_" + nomApprenant.replace(" ", "_") + "_" + System.currentTimeMillis() + ".html";
        File fichier = new File(DOSSIER, nom);
        Files.writeString(fichier.toPath(), html);
        return fichier;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  FACTURE ENTREPRISE
    // ─────────────────────────────────────────────────────────────────────
    public static File genererFactureEntreprise(String nomEntreprise,
                                                 String emailEntreprise,
                                                 List<double[]> lignes,   // [prixFormation, montantPaye, reste]
                                                 List<String[]> details,   // [nomApprenant, formation]
                                                 double totalFacture,
                                                 double totalPaye,
                                                 double totalReste) throws IOException {
        String numero = "FACT-ENT-" + System.currentTimeMillis() % 100000;
        String dateDoc = LocalDate.now().format(FMT);

        StringBuilder rows = new StringBuilder();
        for (int i = 0; i < details.size(); i++) {
            String[] det = details.get(i);
            double[] vals = lignes.size() > i ? lignes.get(i) : new double[]{0, 0, 0};
            String statusClass = vals[2] <= 0 ? "statut-ok" : "statut-att";
            String statusLabel = vals[2] <= 0 ? "Soldé" : "Reste " + String.format("%,.0f", vals[2]);
            rows.append("<tr>")
                .append("<td>").append(esc(det[0])).append("</td>")
                .append("<td>").append(esc(det[1])).append("</td>")
                .append("<td style='text-align:right'>").append(String.format("%,.0f", vals[0])).append("</td>")
                .append("<td style='text-align:right;color:#27ae60;'>").append(String.format("%,.0f", vals[1])).append("</td>")
                .append("<td style='text-align:center'><span class='").append(statusClass).append("'>").append(statusLabel).append("</span></td>")
                .append("</tr>");
        }

        String html = "<!DOCTYPE html><html lang='fr'><head><meta charset='UTF-8'>" +
            "<title>Facture Entreprise</title>" +
            "<style>" +
            "  body{font-family:'Segoe UI',Arial,sans-serif;margin:0;padding:30px;color:#222;background:#fff;}" +
            "  .header{display:flex;justify-content:space-between;align-items:flex-start;" +
            "          border-bottom:3px solid #1a3c5e;padding-bottom:16px;margin-bottom:24px;}" +
            "  .logo h1{margin:0;font-size:24px;color:#1a3c5e;}" +
            "  .logo p{margin:2px 0;font-size:12px;color:#666;}" +
            "  .fact-info{background:#1a3c5e;color:white;padding:12px 20px;border-radius:8px;text-align:right;}" +
            "  .fact-info .type{font-size:20px;font-weight:bold;}" +
            "  .fact-info .ref{font-size:11px;color:#aecce4;}" +
            "  .client-bloc{background:#f5f8fc;border-left:4px solid #1a3c5e;padding:14px;margin:16px 0;border-radius:0 6px 6px 0;}" +
            "  .client-bloc h3{margin:0 0 4px;color:#1a3c5e;}" +
            "  table.det{width:100%;border-collapse:collapse;margin:16px 0;font-size:13px;}" +
            "  table.det th{background:#1a3c5e;color:white;padding:9px 12px;text-align:left;}" +
            "  table.det td{padding:8px 12px;border-bottom:1px solid #eee;}" +
            "  table.det tr:nth-child(even) td{background:#f5f8fc;}" +
            "  .totaux{display:flex;gap:16px;margin:20px 0;}" +
            "  .tot-card{flex:1;border-radius:8px;padding:14px;text-align:center;}" +
            "  .tot-card .lbl{font-size:11px;text-transform:uppercase;letter-spacing:.5px;}" +
            "  .tot-card .val{font-size:20px;font-weight:bold;margin-top:4px;}" +
            "  .tc-blue{background:#1a3c5e;color:white;}" +
            "  .tc-green{background:#e8f5e9;color:#2e7d32;}" +
            "  .tc-red{background:#fce4ec;color:#c62828;}" +
            "  .statut-ok{background:#27ae60;color:white;padding:2px 9px;border-radius:10px;font-size:11px;}" +
            "  .statut-att{background:#e74c3c;color:white;padding:2px 9px;border-radius:10px;font-size:11px;}" +
            "  .footer{margin-top:40px;padding-top:12px;border-top:1px solid #ddd;font-size:11px;color:#888;text-align:center;}" +
            "  @media print{body{padding:15px;}}" +
            "</style></head><body>" +

            "<div class='header'>" +
            "  <div class='logo'><h1>🎓 Centre de Formation</h1>" +
            "    <p>Formation Professionnelle & Continue</p>" +
            "    <p>Ouagadougou, Burkina Faso | contact@centreformation.bf</p>" +
            "  </div>" +
            "  <div class='fact-info'>" +
            "    <div class='type'>FACTURE</div>" +
            "    <div class='ref'>" + numero + "</div>" +
            "    <div style='font-size:12px;margin-top:4px;'>Date : " + dateDoc + "</div>" +
            "  </div>" +
            "</div>" +

            "<div class='client-bloc'>" +
            "  <h3>🏢 Facturé à : " + esc(nomEntreprise) + "</h3>" +
            "  <p style='margin:2px 0;font-size:13px;color:#555;'>Email : " + esc(emailEntreprise) + "</p>" +
            "  <p style='margin:2px 0;font-size:12px;color:#777;'>Date de facturation : " + dateDoc + "</p>" +
            "</div>" +

            "<table class='det'>" +
            "  <tr><th>Apprenant</th><th>Formation</th><th style='text-align:right'>Prix (FCFA)</th>" +
            "      <th style='text-align:right'>Payé (FCFA)</th><th style='text-align:center'>Statut</th></tr>" +
            rows.toString() +
            "</table>" +

            "<div class='totaux'>" +
            "  <div class='tot-card tc-blue'><div class='lbl'>Total Facturé</div><div class='val'>" + String.format("%,.0f FCFA", totalFacture) + "</div></div>" +
            "  <div class='tot-card tc-green'><div class='lbl'>Total Payé</div><div class='val'>" + String.format("%,.0f FCFA", totalPaye) + "</div></div>" +
            "  <div class='tot-card tc-red'><div class='lbl'>Reste à Régler</div><div class='val'>" + String.format("%,.0f FCFA", totalReste) + "</div></div>" +
            "</div>" +

            "<p style='font-size:12px;color:#555;font-style:italic;'>Merci de régler le solde restant dans les délais convenus. " +
            "Pour toute question, contactez notre service financier.</p>" +

            "<div class='footer'>" +
            "  Facture générée le " + dateDoc +
            " | Centre de Formation Professionnelle – Ouagadougou, Burkina Faso" +
            "</div>" +
            "</body></html>";

        String nom = "facture_" + nomEntreprise.replace(" ", "_") + "_" + System.currentTimeMillis() + ".html";
        File fichier = new File(DOSSIER, nom);
        Files.writeString(fichier.toPath(), html);
        return fichier;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  CERTIFICAT / ATTESTATION DE FORMATION
    // ─────────────────────────────────────────────────────────────────────
    public static File genererAttestationFormation(String nomApprenant,
                                                    String formation,
                                                    String resultat,
                                                    String session,
                                                    LocalDate dateFin) throws IOException {
        String dateDoc = LocalDate.now().format(FMT);
        String dateFinStr = dateFin != null ? dateFin.format(FMT) : dateDoc;
        boolean reussite = "ADMIS".equalsIgnoreCase(resultat) || "RÉUSSI".equalsIgnoreCase(resultat);

        String html = "<!DOCTYPE html><html lang='fr'><head><meta charset='UTF-8'>" +
            "<title>Attestation de Formation</title>" +
            "<style>" +
            "  body{font-family:'Georgia',serif;margin:0;padding:40px;color:#1a1a1a;background:#fff;}" +
            "  .bordure{border:6px double #1a3c5e;padding:40px;min-height:600px;position:relative;}" +
            "  .coin{position:absolute;width:50px;height:50px;border-color:#c8a96e;border-style:solid;}" +
            "  .tl{top:8px;left:8px;border-width:3px 0 0 3px;}" +
            "  .tr{top:8px;right:8px;border-width:3px 3px 0 0;}" +
            "  .bl{bottom:8px;left:8px;border-width:0 0 3px 3px;}" +
            "  .br{bottom:8px;right:8px;border-width:0 3px 3px 0;}" +
            "  .titre-centre{text-align:center;margin-bottom:30px;}" +
            "  .titre-centre h1{font-size:14px;letter-spacing:4px;text-transform:uppercase;color:#666;margin:0;}" +
            "  .titre-centre h2{font-size:32px;color:#1a3c5e;margin:8px 0;}" +
            "  .titre-centre p{font-size:13px;color:#888;font-style:italic;}" +
            "  .separateur{text-align:center;color:#c8a96e;font-size:22px;margin:16px 0;}" +
            "  .corps{text-align:center;line-height:1.9;font-size:16px;margin:24px 0;}" +
            "  .nom{font-size:28px;font-weight:bold;color:#1a3c5e;display:block;margin:12px 0;border-bottom:2px solid #c8a96e;padding-bottom:6px;}" +
            "  .formation{font-size:18px;font-style:italic;color:#333;display:block;margin:8px 0;}" +
            "  .resultat{display:inline-block;padding:6px 20px;border-radius:20px;font-size:14px;font-weight:bold;margin:16px 0;}" +
            "  .res-ok{background:#27ae60;color:white;}" +
            "  .res-att{background:#e67e22;color:white;}" +
            "  .info-ligne{font-size:13px;color:#555;margin:4px 0;}" +
            "  .signatures{display:flex;justify-content:space-between;margin-top:50px;}" +
            "  .sig-bloc{text-align:center;width:200px;}" +
            "  .sig-ligne{border-top:1px solid #333;padding-top:6px;font-size:12px;color:#555;margin-top:40px;}" +
            "  .watermark{position:absolute;top:50%;left:50%;transform:translate(-50%,-50%) rotate(-30deg);" +
            "             font-size:80px;color:rgba(26,60,94,0.05);font-weight:bold;white-space:nowrap;z-index:0;}" +
            "  @media print{body{padding:20px;}.bordure{min-height:auto;}}" +
            "</style></head><body>" +

            "<div class='bordure'>" +
            "  <div class='coin tl'></div><div class='coin tr'></div>" +
            "  <div class='coin bl'></div><div class='coin br'></div>" +
            "  <div class='watermark'>CENTRE DE FORMATION</div>" +

            "  <div class='titre-centre'>" +
            "    <h1>Centre de Formation Professionnelle & Continue</h1>" +
            "    <h2>" + (reussite ? "🎓 CERTIFICAT DE RÉUSSITE" : "📋 ATTESTATION DE PARTICIPATION") + "</h2>" +
            "    <p>Ouagadougou, Burkina Faso</p>" +
            "  </div>" +

            "  <div class='separateur'>✦ ✦ ✦</div>" +

            "  <div class='corps'>" +
            "    <p>Nous certifions que</p>" +
            "    <span class='nom'>" + esc(nomApprenant) + "</span>" +
            "    <p>a " + (reussite ? "suivi et <strong>réussi</strong>" : "participé à") + " la formation</p>" +
            "    <span class='formation'>« " + esc(formation) + " »</span>" +
            "    <br>" +
            "    <span class='resultat " + (reussite ? "res-ok" : "res-att") + "'>" +
            (reussite ? "✅ ADMIS(E)" : "📋 " + esc(resultat)) +
            "    </span>" +
            "    <br>" +
            "    <p class='info-ligne'>Session : <strong>" + esc(session) + "</strong></p>" +
            "    <p class='info-ligne'>Date de fin : <strong>" + dateFinStr + "</strong></p>" +
            "  </div>" +

            "  <div class='signatures'>" +
            "    <div class='sig-bloc'><div class='sig-ligne'>Le / La Responsable Pédagogique</div></div>" +
            "    <div class='sig-bloc' style='text-align:center'>" +
            "      <p style='font-size:11px;color:#999;margin:0;'>Délivré le</p>" +
            "      <p style='font-size:14px;font-weight:bold;color:#1a3c5e;'>" + dateDoc + "</p>" +
            "      <p style='font-size:10px;color:#bbb;font-family:monospace;'>REF: ATT-" + System.currentTimeMillis() % 100000 + "</p>" +
            "    </div>" +
            "    <div class='sig-bloc'><div class='sig-ligne'>Le / La Directeur(trice)</div></div>" +
            "  </div>" +
            "</div>" +
            "</body></html>";

        String nom = "attestation_" + nomApprenant.replace(" ", "_") + "_" + System.currentTimeMillis() + ".html";
        File fichier = new File(DOSSIER, nom);
        Files.writeString(fichier.toPath(), html);
        return fichier;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  UTILITAIRE : ouvrir le document dans le navigateur
    // ─────────────────────────────────────────────────────────────────────
    public static void ouvrirDocument(File fichier) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            if (os.contains("win")) {
                pb = new ProcessBuilder("cmd", "/c", "start", "", fichier.getAbsolutePath());
            } else if (os.contains("mac")) {
                pb = new ProcessBuilder("open", fichier.getAbsolutePath());
            } else {
                // Linux / CI
                pb = new ProcessBuilder("xdg-open", fichier.getAbsolutePath());
            }
            pb.start();
        } catch (IOException e) {
            System.err.println("Impossible d'ouvrir le document : " + e.getMessage());
        }
    }

    public static String getDossier() { return DOSSIER; }

    private static String esc(String s) {
        if (s == null) return "–";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}
