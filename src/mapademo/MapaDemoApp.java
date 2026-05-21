/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mapademo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author jose
 */
public class MapaDemoApp extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        //Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        //stage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/logo.png")));
        //Scene scene = new Scene(root);
        //stage.setTitle("Demo mapas - IPC");
        //stage.setScene(scene);
        //stage.show();
        try {
            // Cargamos el FXML usando la ruta absoluta del paquete
            Parent root = FXMLLoader.load(getClass().getResource("/autenticarse/Autenticarse.fxml"));
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Autenticación");
            stage.show();
            
        } catch (Exception e) {
            // Si vuelve a fallar, esto imprimirá el motivo real en la consola de NetBeans
            System.out.println("❌ ERROR CRÍTICO AL ARRANCAR LA APP: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
