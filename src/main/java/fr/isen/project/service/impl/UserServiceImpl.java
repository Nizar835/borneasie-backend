package fr.isen.project.service.impl; // <--- Indique le dossier

// Imports nécessaires pour que Java comprenne les autres fichiers
import fr.isen.project.model.User;
import fr.isen.projet.service.UserService;

// Imports SQL
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserServiceImpl implements UserService {

    // VERIFIE BIEN CES 3 LIGNES AVEC TA PROPRE CONFIGURATION MYSQL
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/isen";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = null; // Mets ton mot de passe ici si tu en as un

    @Override
    public List<User> GetUsers() {

        List<User> users = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM user")) {

            while (rs.next()) {
                User user = new User();
                // Attention: "id", "email", "name" doivent être les noms exacts dans ta table SQL
                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                user.setName(rs.getString("name"));

                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }
}