package fr.isen.project.dao;

import fr.isen.project.model.Commande;
import java.sql.SQLException;

public interface CommandeDao {
    // Pour enregistrer une commande et récupérer son ID
    int ajouterCommande(Commande commande) throws SQLException;

    // La méthode qui manquait dans ton implémentation
    Commande getCommande(int id);
}