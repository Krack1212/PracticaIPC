/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
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
    private static final Set<String> nicknames = new HashSet<>();
    /**
     * Initializes the controller class.
     */
    // Guardamos la referencia de la imagen cargada (será null si no elige ninguna)

    
    // Suponiendo que tienes acceso a la instancia de la aplicación o una clase Singleton
    // Si tu objeto app se obtiene de otra forma, ajusta esta línea.
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Limpiamos los textos de error al iniciar la pantalla
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
        
        // El "if" asegura que SOLO cambie la imagen si el usuario de verdad eligió un archivo válido
        if (archivo != null) {
            // 1. Creamos el nuevo objeto Image con el archivo seleccionado
            Image nuevaImagen = new Image(archivo.toURI().toString());
            
            // 2. Guardamos la referencia en tu variable global del controlador
            this.avatarImage = nuevaImagen;
            
            // 3. Cambiamos la imagen del recuadro. 
            // JavaFX quita automáticamente la silueta gris antigua y dibuja esta.
            ImagenAvatar.setImage(nuevaImagen);
            
        }
        // Si archivo es null (le dio a Cancelar), no entra aquí, 
        // manteniendo intacta la imagen que ya estuviera puesta sin romper nada.
    
    }

    @FXML
    private void Registrarse(ActionEvent event) {
        // 1. Limpiar mensajes de error anteriores
        limpiarErrores();
        
        // 2. Capturar los datos de la interfaz de forma correcta
        String nickname = CampoNickName.getText().trim();
        String correo = CampoCorreo.getText().trim();
        String password = CampoContraseña.getText(); // Correcto para PasswordField en JavaFX
        LocalDate fechaNacimiento = CampoFechaNacimiento.getValue(); // Correcto para DatePicker
        
        boolean datosValidos = true; // Empezamos asumiendo que todo está bien

        // Validar Nickname
        if (!User.checkNickName(nickname)) {
            ErrorNickname.setText("Solo letras, dígitos, guion o subguion.");
            datosValidos = false; // Cambia a false, ya no se guardará nada
        } else {
            ErrorNickname.setText("");
        }

        // Validar Correo
        if (!User.checkEmail(correo)) {
            ErrorCorreo.setText("Formato válido: usuario@dominio.");
            datosValidos = false;
        } else {
            ErrorCorreo.setText("");
        }

        // Validar Contraseña
        if (!User.checkPassword(password)) {
            ErrorContraseña.setText("Al menos una mayúscula, minúscula, dígito y símbolo.");
            datosValidos = false;
        } else {
            ErrorContraseña.setText("");
        }

        // Validar Fecha
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

    /**
     * Limpia los textos de todos los labels de error.
     */
    private void limpiarErrores() {
        ErrorNickname.setText("");
        ErrorCorreo.setText("");
        ErrorContraseña.setText("");
        ErrorFechaNacimiento.setText("");
    }

    /**
     * Muestra una ventana emergente (Alert) de JavaFX de forma rápida.
     */

}
    

