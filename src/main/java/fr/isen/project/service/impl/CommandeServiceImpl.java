package fr.isen.project.service.impl;

import fr.isen.project.dao.CommandeDao;
import fr.isen.project.dao.impl.CommandeDaoImpl;
import fr.isen.project.model.Commande;
import fr.isen.project.service.CommandeService;

public class CommandeServiceImpl implements CommandeService {
    private final CommandeDao dao = new CommandeDaoImpl();

    @Override
    public int enregistrerCommande(Commande c) throws Exception {
        // Validation métier simple
        if (c.ligneCommande == null || c.ligneCommande.isEmpty()) {
            throw new Exception("La commande est vide !");
        }

        // On appelle le DAO. L'exception SQL est propagée grâce au 'throws Exception'
        return dao.ajouterCommande(c);
    }
}