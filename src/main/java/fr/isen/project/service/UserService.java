package fr.isen.projet.service; // <--- Indique le dossier

import fr.isen.project.model.User; // Importe le User qu'on a créé au-dessus
import java.util.List;

public interface UserService {
    public List<User> GetUsers();
}