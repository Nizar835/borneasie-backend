package fr.isen.project;

import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import io.javalin.http.staticfiles.Location;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import fr.isen.project.model.Commande;
import fr.isen.project.service.MenuService;
import fr.isen.project.service.impl.MenuServiceImpl;
import fr.isen.project.service.CommandeService;
import fr.isen.project.service.impl.CommandeServiceImpl;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.HashMap;

/**
 * Application Backend ISEN - Port 8080
 */
public class BackendApp {
    public static void main(String[] args) {

        // 1. CONFIGURATION JACKSON
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

        // 2. INITIALISATION DES COUCHES
        MenuService menuService = new MenuServiceImpl();
        CommandeService cmdService = new CommandeServiceImpl();

        // 3. BASE DE DONNÉES DES PROMOS
        Map<String, Double> promos = new HashMap<>();
        promos.put("VIP2025", 0.20);

        // 4. DÉMARRAGE DU SERVEUR
        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson(mapper, true));
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
            config.staticFiles.add("public", Location.CLASSPATH);
        }).start(8080);

        System.out.println("--- Backend ISEN opérationnel sur http://localhost:8080 ---");

        // --- ROUTES API ---

        // MENU (Double route pour sécurité)
        app.get("/categories", ctx -> ctx.json(menuService.getAllCategories()));
        app.get("/menu", ctx -> ctx.json(menuService.getAllCategories()));

        // PROMOS
        app.get("/api/promo/{code}", ctx -> {
            String codeRecu = ctx.pathParam("code");
            if (promos.containsKey(codeRecu)) {
                ctx.json(Map.of("valide", true, "reduction", promos.get(codeRecu)));
            } else {
                ctx.status(404).json(Map.of("valide", false));
            }
        });

        // IMAGES : CHEMIN SIMPLE (C:/images/)
        // Cela contourne le bug de l'accent 'yncréa'
        app.get("/images/{filename}", ctx -> {
            String filename = ctx.pathParam("filename");
            File file = new File("C:/images/" + filename); // <--- MODIFICATION ICI

            if (file.exists()) {
                ctx.contentType("image/jpeg");
                ctx.result(Files.newInputStream(file.toPath()));
            } else {
                System.err.println("❌ Image introuvable dans C:/images/ : " + filename);
                ctx.status(404).result("Image non trouvée");
            }
        });

        // COMMANDE
        app.post("/commande", ctx -> {
            try {
                Commande cmd = ctx.bodyAsClass(Commande.class);
                int idGenere = cmdService.enregistrerCommande(cmd);
                ctx.status(201).json(Map.of("id", idGenere, "message", "Succès"));
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Erreur: " + e.getMessage());
            }
        });
    }
}