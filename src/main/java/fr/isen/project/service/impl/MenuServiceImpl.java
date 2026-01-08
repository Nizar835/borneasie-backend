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
        entrees.plats.add(creerPlat(1, "Nems au Poulet", "4 pièces croustillantes", 5.50f, "nems.jpg"));
        entrees.plats.add(creerPlat(2, "Rouleaux de Printemps", "Crevettes et menthe", 4.90f, "rouleaux.jpg"));

        // --- 2. PLATS ---
        Categorie plats = new Categorie();
        plats.nom = "Plats";
        plats.plats.add(creerPlat(3, "Bo Bun", "Bœuf sauté et nems", 13.50f, "bobun.jpg"));
        plats.plats.add(creerPlat(4, "Riz Cantonais", "Riz sauté complet", 9.50f, "riz.jpg"));

        // --- 3. DESSERTS ---
        Categorie desserts = new Categorie();
        desserts.nom = "Desserts";
        desserts.plats.add(creerPlat(5, "Nougat Chinois", "Miel et sésame", 4.50f, "nougat.jpg"));
        desserts.plats.add(creerPlat(6, "Perles de Coco", "Cœur coco fondant", 3.90f, "coco.jpg"));

        categories.add(entrees);
        categories.add(plats);
        categories.add(desserts);
    }

    private Plat creerPlat(int id, String nom, String desc, float prix, String img) {
        Plat p = new Plat();
        p.id = id;
        p.nom = nom; // Champ public Modelio
        p.description = desc;
        p.prix = prix;
        p.image = img; // Utilisation de tes JPG
        p.estDisponible = true;
        return p;
    }

    @Override public List<Categorie> getAllCategories() { return categories; }
}