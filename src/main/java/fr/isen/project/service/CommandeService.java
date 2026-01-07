package fr.isen.project.service;

import fr.isen.project.model.Commande;

public interface CommandeService {
    // On ajoute 'throws Exception' pour couvrir les erreurs SQL
    int enregistrerCommande(Commande c) throws Exception;
}