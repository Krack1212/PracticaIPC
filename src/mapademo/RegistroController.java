/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

/**
 * FXML Controller class
 *
 * @author Rafols
 */
public class RegistroController implements Initializable {

    @FXML
    private TextField CampoNickName;
    @FXML
    private TextField CampoCorreo;
    @FXML
    private PasswordField CampoContraseña;
    @FXML
    private Button BotonSeleccionAvatar;
    @FXML
    private DatePicker CampoFechaNacimiento;
    @FXML
    private Button BotonRegistrarse;
    @FXML
    private ImageView ImagenAvatar;
    @FXML
    private Label ErrorCorreo;
    @FXML
    private Label ErrorContraseña;
    @FXML
    private Label ErrorFechaNacimiento;
    @FXML
    private Label ErrorNickname;
    private Image avatarImage = null;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void SeleccionarAvatar(ActionEvent event) {
        FileChooser chooser = new FileChooser();

        chooser.setTitle("Seleccionar avatar");

        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(
                "Imágenes",
                "*.png", "*.jpg", "*.jpeg", "*.gif"
            )
        );

        File archivo = chooser.showOpenDialog(
        ImagenAvatar.getScene().getWindow());
        
    
    }

    @FXML
    private void Registrarse(ActionEvent event) {
        
    }
    
}
