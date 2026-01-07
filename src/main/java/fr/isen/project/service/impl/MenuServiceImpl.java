package fr.isen.project.service.impl;

import fr.isen.project.model.Categorie;
import fr.isen.project.model.Plat;
import fr.isen.project.service.MenuService;
import java.util.ArrayList;
import java.util.List;

public class MenuServiceImpl implements MenuService {
    private List<Categorie> categories = new ArrayList<>();

    public MenuServiceImpl() {
        // --- 1. ENTRÉES ---
        Categorie entrees = new Categorie();
        entrees.nom = "Entrées";
        entrees.visuel = "entrees.png"; // Champ public Modelio
        entrees.plats.add(creerPlat(1, "Nems au Poulet", "4 pièces croustillantes", 5.50f));
        entrees.plats.add(creerPlat(2, "Rouleaux de Printemps", "2 pièces fraîches", 4.50f));

        // --- 2. PLATS ---
        Categorie plats = new Categorie();
        plats.nom = "Plats";
        plats.visuel = "plats.png";
        plats.plats.add(creerPlat(3, "Bo Bun", "Vermicelles, bœuf, nems", 10.50f));
        plats.plats.add(creerPlat(4, "Riz Cantonais", "Riz sauté complet", 8.00f));

        // --- 3. DESSERTS ---
        Categorie desserts = new Categorie();
        desserts.nom = "Desserts";
        desserts.visuel = "desserts.png";
        desserts.plats.add(creerPlat(5, "Perles de Coco", "2 boules chaudes", 4.00f));

        categories.add(entrees);
        categories.add(plats);
        categories.add(desserts);
    }

    private Plat creerPlat(int id, String nom, String desc, float prix) {
        Plat p = new Plat();
        p.id = id;
        p.nom = nom;
        p.description = desc;
        p.prix = prix;
        p.estDisponible = true; //
        return p;
    }

    @Override public List<Categorie> getAllCategories() { return categories; }
}