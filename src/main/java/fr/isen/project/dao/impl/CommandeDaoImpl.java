package fr.isen.project.dao.impl;

import fr.isen.project.dao.CommandeDao;
import fr.isen.project.model.Commande;
import fr.isen.project.model.LigneCommande;
import java.sql.*;

public class CommandeDaoImpl implements CommandeDao {
    private final String URL = "jdbc:mysql://localhost:3306/isen";
    private final String USER = "root";
    private final String PWD = ""; // Ton mot de passe MySQL

    @Override
    public int ajouterCommande(Commande commande) throws SQLException {
        String sql = "INSERT INTO commande (client_nom, details, total, date_creation) VALUES (?, ?, ?, NOW())";

        StringBuilder details = new StringBuilder();
        double total = 0;

        // Extraction depuis les objets Modelio
        if (commande.ligneCommande != null) {
            for (LigneCommande l : commande.ligneCommande) {
                details.append(l.quantite).append("x ").append(l.plat.nom).append(", ");
                total += (l.plat.prix * l.quantite); //
            }
        }

        try (Connection c = DriverManager.getConnection(URL, USER, PWD);
             PreparedStatement st = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            st.setString(1, commande.nomClient != null ? commande.nomClient : "Borne Asie");
            st.setString(2, details.toString());
            st.setDouble(3, total);
            st.executeUpdate();

            ResultSet rs = st.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    // --- CETTE MÉTHODE RÉSOUT TON ERREUR ---
    @Override
    public Commande getCommande(int id) {
        String sql = "SELECT * FROM commande WHERE id = ?";
        try (Connection c = DriverManager.getConnection(URL, USER, PWD);
             PreparedStatement st = c.prepareStatement(sql)) {

            st.setInt(1, id);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                Commande cmd = new Commande(); //
                cmd.id = rs.getInt("id");
                cmd.nomClient = rs.getString("client_nom");
                cmd.statut = "EN_COURS"; // Valeur par défaut
                return cmd;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Retourne null si la commande n'existe pas
    }
}