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
      
        User usuarioEncontrado = null;
        
        for (User u : users) {
        if (u.getEmail().equals(nickname) && u.getPassword().equals(password)) {
            usuarioEncontrado = u;
            break;
        }
        
        if(usuarioEncontrado != null){
            //siguiente pestaña
            FXMLLoader miCargador = new
            FXMLLoader(getClass().getResource("/ventanaPPal/VentanaPPal.fxml"));
            Parent root = miCargador.load();
            Scene scene = new Scene(root,500,300);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            //la ventana se muestra modal
            stage.show();
        }
        else{
            credencialesIncorrectas.setText("Credenciales Incorrectas!");
        }
    }
    }

    
}
