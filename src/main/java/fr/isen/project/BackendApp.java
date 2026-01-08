package fr.isen.project;

import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import io.javalin.http.staticfiles.Location;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import fr.isen.project.model.Commande;
import fr.isen.project.model.LigneCommande;
import fr.isen.project.model.Plat;
import fr.isen.project.model.Categorie;
import fr.isen.project.service.MenuService;
import fr.isen.project.service.impl.MenuServiceImpl;
import fr.isen.project.service.CommandeService;
import fr.isen.project.service.impl.CommandeServiceImpl;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class BackendApp {

    // STOCK GLOBAL : ID Plat -> Quantité
    private static final Map<Integer, Integer> stocksReels = new HashMap<>();
    private static final Map<Integer, String> statusDB = new HashMap<>();

    public static void main(String[] args) {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

        MenuService menuService = new MenuServiceImpl();
        CommandeService cmdService = new CommandeServiceImpl();

        // --- 1. INITIALISATION DES STOCKS ---
        List<Categorie> menuInitial = menuService.getAllCategories();
        for (Categorie c : menuInitial) {
            for (Plat p : c.plats) {
                // Par défaut, tout le monde a 50 en stock
                stocksReels.put(p.id, 50);

                // --- ZONE DE TEST : SIMULATION RUPTURE ---
                // Change le nom ici pour tester sur un autre plat (ex: "Nems", "Coca")
                if(p.nom.contains("Cantonais")) {
                    stocksReels.put(p.id, 0); // 0 = RUPTURE IMMÉDIATE
                    System.out.println("⚠️ RUPTURE SIMULÉE SUR : " + p.nom);
                }
            }
        }

        Map<String, Double> promos = new HashMap<>();
        promos.put("VIP2025", 0.20);
        promos.put("QR_FLASH", 0.05);

        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson(mapper, true));
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
            config.staticFiles.add("public", Location.CLASSPATH);
        }).start(8080);

        System.out.println("--- Backend ISEN DÉMARRÉ ---");

        // Routes Menu (Avec calcul de disponibilité)
        app.get("/menu", ctx -> envoyerMenuAvecStock(ctx, menuService));
        app.get("/categories", ctx -> envoyerMenuAvecStock(ctx, menuService));

        // API Promo
        app.get("/api/promo/{code}", ctx -> {
            if (promos.containsKey(ctx.pathParam("code"))) {
                ctx.json(Map.of("valide", true, "reduction", promos.get(ctx.pathParam("code"))));
            } else {
                ctx.status(404).json(Map.of("valide", false));
            }
        });

        // API Suivi
        app.get("/api/commande/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            if (statusDB.containsKey(id)) {
                ctx.json(Map.of("id", id, "status", statusDB.get(id)));
            } else {
                ctx.status(404).result("Commande introuvable");
            }
        });

        // Images
        app.get("/images/{filename}", ctx -> {
            String filename = ctx.pathParam("filename");
            File file = new File("borneasie-backend/src/main/resources/public/images/" + filename);
            if (file.exists()) {
                if (filename.toLowerCase().endsWith(".png")) ctx.contentType("image/png");
                else ctx.contentType("image/jpeg");
                ctx.result(Files.newInputStream(file.toPath()));
            } else {
                ctx.status(404).result("Image non trouvée");
            }
        });

        // Commande (Avec vérification de stock stricte)
        app.post("/commande", ctx -> {
            try {
                Commande cmd = ctx.bodyAsClass(Commande.class);

                // 1. VÉRIFICATION
                if (cmd.ligneCommande != null) {
                    for (LigneCommande l : cmd.ligneCommande) {
                        int dispo = stocksReels.getOrDefault(l.plat.id, 0);
                        if (l.quantite > dispo) {
                            System.err.println("❌ Refus de commande : Pas assez de stock pour " + l.plat.nom);
                            ctx.status(400).result("Stock insuffisant pour : " + l.plat.nom);
                            return;
                        }
                    }
                }

                // 2. DÉCRÉMENTATION
                if (cmd.ligneCommande != null) {
                    for (LigneCommande l : cmd.ligneCommande) {
                        int current = stocksReels.getOrDefault(l.plat.id, 0);
                        stocksReels.put(l.plat.id, current - l.quantite);
                    }
                }

                int id = cmdService.enregistrerCommande(cmd);
                statusDB.put(id, "EN PRÉPARATION 👨‍🍳");
                System.out.println("✅ Commande #" + id + " validée !");
                ctx.status(201).json(Map.of("id", id, "message", "Succès"));

            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("Erreur: " + e.getMessage());
            }
        });
    }

    // Méthode générique qui met à jour le booléen estDisponible pour TOUS les plats
    private static void envoyerMenuAvecStock(io.javalin.http.Context ctx, MenuService service) {
        List<Categorie> menuAJour = service.getAllCategories();
        for (Categorie c : menuAJour) {
            for (Plat p : c.plats) {
                int qte = stocksReels.getOrDefault(p.id, 0);
                // C'est ICI que ça se décide pour tout le monde :
                p.estDisponible = (qte > 0);
            }
        }
        ctx.json(menuAJour);
    }
}