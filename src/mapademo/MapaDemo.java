/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mapademo;

import java.io.InputStream;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Clase principal — Running la Safor (IPC 2026).
 *
 * Punto de entrada de la aplicación. Carga la vista de autenticación
 * y expone métodos estáticos para cambiar de vista sin reabrir la ventana.
 */
public class MapaDemo extends Application {

    /** Referencia estática al Stage principal para cambiar de escena. */
    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.setTitle("Running la Safor — IPC 2026");

        InputStream logo = getClass().getResourceAsStream("/resources/logo.png");
        if (logo != null) {
            stage.getIcons().add(new Image(logo));
        }

        // Arrancar en la pantalla de login
        cargarVista("/autenticarse/Autenticarse.fxml", 480, 560, false);
        stage.show();
    }

    
    public static void cargarVista(String fxmlPath, int w, int h, boolean maximized) {
        try {
            FXMLLoader loader = new FXMLLoader(MapaDemo.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, w, h);

            String css = MapaDemo.class.getResource("/Style/Registro.css").toExternalForm();
            scene.getStylesheets().add(css);

            primaryStage.setScene(scene);
            primaryStage.setResizable(maximized);
            if (maximized) {
                primaryStage.setMaximized(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
