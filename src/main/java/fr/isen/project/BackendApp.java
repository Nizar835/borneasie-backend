package fr.isen.project;

import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import fr.isen.project.model.Commande;
import fr.isen.project.service.MenuService;
import fr.isen.project.service.impl.MenuServiceImpl;
import fr.isen.project.dao.impl.CommandeDaoImpl;
import fr.isen.project.service.CommandeService;
import fr.isen.project.service.impl.CommandeServiceImpl;

import java.util.Map;

/**
 * Application Backend ISEN - Port 8080
 * Gère la communication entre la Borne JavaFX et la base MySQL.
 */
public class BackendApp {
    public static void main(String[] args) {

        // 1. CONFIGURATION JACKSON : Indispensable pour lire tes fichiers Modelio
        // Permet d'accéder aux champs publics sans avoir de Getters/Setters.
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

        // 2. INITIALISATION DES COUCHES (Architecture ISEN)
        MenuService menuService = new MenuServiceImpl();
        CommandeDaoImpl dao = new CommandeDaoImpl();
        // Le service utilise le DAO pour parler à MySQL
        CommandeService cmdService = new CommandeServiceImpl();

        // 3. DÉMARRAGE DU SERVEUR SUR LE PORT 8080
        Javalin app = Javalin.create(config -> {
            // Utilise le mapper configuré pour les champs publics
            config.jsonMapper(new JavalinJackson(mapper, true));

            // Configuration du CORS pour autoriser la borne JavaFX
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> it.anyHost());
            });
        }).start(8080);

        System.out.println("--- Backend ISEN opérationnel sur http://localhost:8080 ---");

        // --- ROUTES API ---

        // GET /categories : Renvoie le menu complet chargé depuis MenuServiceImpl
        app.get("/categories", ctx -> {
            ctx.json(menuService.getAllCategories());
        });

        // POST /commande : Reçoit la commande de la borne et l'enregistre dans MySQL
        app.post("/commande", ctx -> {
            try {
                // Désérialisation du JSON vers l'objet Commande Modelio
                Commande cmd = ctx.bodyAsClass(Commande.class);

                // Appel au service pour l'enregistrement (gère les erreurs SQL)
                int idGenere = cmdService.enregistrerCommande(cmd);

                // Réponse de succès avec l'ID généré par la base de données
                ctx.status(201).json(Map.of(
                        "id", idGenere,
                        "message", "Commande enregistrée avec succès"
                ));
            } catch (java.sql.SQLException e) {
                // Gestion spécifique des erreurs de base de données
                e.printStackTrace();
                ctx.status(500).result("Erreur Base de données : " + e.getMessage());
            } catch (Exception e) {
                // Gestion des autres erreurs (ex: commande vide)
                e.printStackTrace();
                ctx.status(400).result("Erreur : " + e.getMessage());
            }
        });
    }
}