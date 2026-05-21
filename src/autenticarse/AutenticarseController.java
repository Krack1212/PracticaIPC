/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package autenticarse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import upv.ipc.sportlib.Session;
import upv.ipc.sportlib.User;

/**
 *
 * @author Usuario
 */
public class AutenticarseController {

    @FXML
    private PasswordField CampoContraseña;
    @FXML
    private Label errorContraseña;
    @FXML
    private Button autenticarseButton;
    @FXML
    private TextField CampoNickName;
    @FXML
    private Label errorNickname;
    
    private List<User> users = new ArrayList<>();
    @FXML
    private Label credencialesIncorrectas;


    @FXML
    private void autenticarseButtonPressed(ActionEvent event) throws IOException {
        String nickname = CampoNickName.getText();
        String password = CampoContraseña.getText();
      
        if (nickname.isEmpty() || password.isEmpty()) {
            credencialesIncorrectas.setText("Por favor, rellena todos los campos.");
            return;
        }
        
        // 1. AUTENTICACIÓN OFICIAL: Le pedimos a la librería de la UPV que verifique las credenciales
        // El método .login() busca en la base de datos SQLite y devuelve true si coinciden [cite: 20, 249]
        boolean loginCorrecto = false;
        try {
            loginCorrecto = upv.ipc.sportlib.SportActivityApp.getInstance().login(nickname, password);
        } catch (Exception e) {
            System.out.println("Error al conectar con la base de datos: " + e.getMessage());
        }
        
        // 2. COMPROBACIÓN (Fuera de cualquier bucle)
        if (loginCorrecto){
            //siguiente pestaña
            FXMLLoader miCargador = new
            FXMLLoader(getClass().getResource("/PantallaPrincipal/PantallaPrincipal.fxml"));
            Parent root = miCargador.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Panel Principal");
            Stage ventanaLogin = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            ventanaLogin.close();
            //la ventana se muestra modal
            stage.show();
        }
        else{
            credencialesIncorrectas.setText("Credenciales Incorrectas!");
        }
    }
    
    @FXML
    private void irAlRegistro(ActionEvent event) {
        try {
            FXMLLoader miCargador = new FXMLLoader(getClass().getResource("/mapademo/Registro.fxml"));
            Parent root = miCargador.load();
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Usamos la misma ventana
            
            stage.setScene(scene);
            stage.setTitle("Formulario de Registro");
            stage.show();
        } catch (IOException e) {
            System.out.println("Error al abrir la pantalla de registro: " + e.getMessage());
        }
    }

    
}
