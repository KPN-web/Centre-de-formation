package FX;

import metier.Utilisateur;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Stage primaryStage;
    private static Utilisateur currentUser;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setTitle("Agence de Formation - ESTA SIR");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(700);

        // Chargement du logo (optionnel)
        try {
            java.io.InputStream logoStream = MainApp.class.getResourceAsStream("logo.png");
            if (logoStream != null) {
                Image icon = new Image(logoStream);
                if (!icon.isError()) {
                    primaryStage.getIcons().add(icon);
                }
            }
            // Aucun message si logo absent — comportement silencieux
        } catch (Exception e) {
            // Logo absent ou invalide, on continue normalement
        }

        loadLogin();
        primaryStage.show();
    }

    public static void loadLogin() {
        try {
            // Chemin RELATIF à la classe FX/MainApp → cherche FX/login.fxml dans bin/
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 500, 400);
            scene.getStylesheets().add(MainApp.class.getResource("style.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setWidth(500);
            primaryStage.setHeight(400);
            primaryStage.centerOnScreen();
        } catch (Exception e) {
            System.err.println("Erreur chargement login : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void loadDashboard(String role) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("dashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1280, 760);
            scene.getStylesheets().add(MainApp.class.getResource("style.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setWidth(1280);
            primaryStage.setHeight(760);
            primaryStage.centerOnScreen();
        } catch (Exception e) {
            System.err.println("Erreur chargement dashboard : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode générique pour charger une vue FXML depuis le package FX
    public static void loadView(String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlName));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1280, 760);
            scene.getStylesheets().add(MainApp.class.getResource("style.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (Exception e) {
            System.err.println("Erreur chargement vue " + fxmlName + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Utilisateur getCurrentUser() { return currentUser; }
    public static void setCurrentUser(Utilisateur user) { currentUser = user; }
    public static Stage getPrimaryStage() { return primaryStage; }

    public static void main(String[] args) {
        launch(args);
    }
}