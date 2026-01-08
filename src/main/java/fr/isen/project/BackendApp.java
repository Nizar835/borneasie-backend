package fr.isen.project;

import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import io.javalin.http.staticfiles.Location;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import fr.isen.project.model.Commande;
import fr.isen.project.model.LigneCommande;
import fr.isen.project.service.MenuService;
import fr.isen.project.service.impl.MenuServiceImpl;
import fr.isen.project.service.CommandeService;
import fr.isen.project.service.impl.CommandeServiceImpl;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.HashMap;

public class BackendApp {
    public static void main(String[] args) {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

        MenuService menuService = new MenuServiceImpl();
        CommandeService cmdService = new CommandeServiceImpl();

        // --- 1. LISTE DES CODES PROMOS REALISTES ---
        Map<String, Double> promos = new HashMap<>();
        promos.put("VIP2025", 0.20);      // 20% (Code VIP)
        promos.put("BIENVENUE", 0.10);    // 10% (Nouveau client)
        promos.put("ISEN_STUDENT", 0.15); // 15% (Etudiants)
        promos.put("QR_FLASH", 0.05);     // 5% (Le code du QR Code)

        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson(mapper, true));
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
            config.staticFiles.add("public", Location.CLASSPATH);
        }).start(8080);

        System.out.println("--- Backend ISEN DÉMARRÉ ---");

        app.get("/categories", ctx -> ctx.json(menuService.getAllCategories()));
        app.get("/menu", ctx -> ctx.json(menuService.getAllCategories()));

        // API Vérification Promo
        app.get("/api/promo/{code}", ctx -> {
            String code = ctx.pathParam("code").toUpperCase(); // On gère les majuscules
            if (promos.containsKey(code)) {
                ctx.json(Map.of("valide", true, "reduction", promos.get(code)));
            } else {
                ctx.status(404).json(Map.of("valide", false));
            }
        });

        // IMAGES (Ton chemin corrigé)
        app.get("/images/{filename}", ctx -> {
            String filename = ctx.pathParam("filename");
            File file = new File("borneasie-backend/src/main/resources/public/images/" + filename);

            if (file.exists()) {
                if (filename.toLowerCase().endsWith(".png")) {
                    ctx.contentType("image/png");
                } else {
                    ctx.contentType("image/jpeg");
                }
                ctx.result(Files.newInputStream(file.toPath()));
            } else {
                System.err.println("❌ Introuvable : " + file.getAbsolutePath());
                ctx.status(404).result("Image non trouvée");
            }
        });

        app.post("/commande", ctx -> {
            try {
                Commande cmd = ctx.bodyAsClass(Commande.class);
                // Log Console
                double totalLog = 0;
                if (cmd.ligneCommande != null) {
                    for (LigneCommande l : cmd.ligneCommande) {
                        double p = l.plat.prix;
                        if (l.options != null && l.options.contains("Cantonais")) p += 2.0;
                        totalLog += p * l.quantite;
                    }
                }
                System.out.println("📥 Commande reçue : " + cmd.nomClient + " | Total env: " + totalLog + "€");

                int id = cmdService.enregistrerCommande(cmd);
                ctx.status(201).json(Map.of("id", id, "message", "Succès"));
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Erreur: " + e.getMessage());
            }
        });
    }
}