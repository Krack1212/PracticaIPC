/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
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
import upv.ipc.sportlib.User;

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
    private DatePicker CampoFechaNacimiento;
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
    private static final Set<String> nicknames = new HashSet<>();
    @FXML
    private Button BotonSeleccionAvatar;
    @FXML
    private Button BotonRegistrarse;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        limpiarErrores();
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

        File archivo = chooser.showOpenDialog(ImagenAvatar.getScene().getWindow());
        
        if (archivo != null) {
            Image nuevaImagen = new Image(archivo.toURI().toString());

            this.avatarImage = nuevaImagen;

            ImagenAvatar.setImage(nuevaImagen);
            
        }
    
    }

    @FXML
    private void Registrarse(ActionEvent event) {
        limpiarErrores();
        
        String nickname = CampoNickName.getText().trim();
        String correo = CampoCorreo.getText().trim();
        String password = CampoContraseña.getText();
        LocalDate fechaNacimiento = CampoFechaNacimiento.getValue();
        
        boolean datosValidos = true;

        if (!User.checkNickName(nickname)) {
            ErrorNickname.setText("Solo letras, dígitos, guion o subguion.");
            datosValidos = false; // Cambia a false, ya no se guardará nada
        } else {
            ErrorNickname.setText("");
        }

        if (!User.checkEmail(correo)) {
            ErrorCorreo.setText("Formato válido: usuario@dominio.");
            datosValidos = false;
        } else {
            ErrorCorreo.setText("");
        }

        if (!User.checkPassword(password)) {
            ErrorContraseña.setText("Al menos una mayúscula, minúscula, dígito y símbolo.");
            datosValidos = false;
        } else {
            ErrorContraseña.setText("");
        }

        if (fechaNacimiento == null || !User.isOlderThan(fechaNacimiento, 12)) {
            ErrorFechaNacimiento.setText("Debes ser mayor de 12 años");
            datosValidos = false;
        } else {
            ErrorFechaNacimiento.setText("");
        }
        
        if(datosValidos==false) {
            nicknames.add(nickname);
        }
    }
    
    private void limpiarErrores() {
        ErrorNickname.setText("");
        ErrorCorreo.setText("");
        ErrorContraseña.setText("");
        ErrorFechaNacimiento.setText("");
    }
}
    

