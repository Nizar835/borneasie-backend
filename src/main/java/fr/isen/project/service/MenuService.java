package fr.isen.project.service;

import fr.isen.project.model.Categorie;
import fr.isen.project.model.Plat;
import java.util.List;

public interface MenuService {
    /** Récupère toutes les catégories et leurs plats associés */
    List<Categorie> getAllCategories();
}