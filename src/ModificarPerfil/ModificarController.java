package ModificarPerfil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

/**
 * FXML Controller class
 *
 * @author Rafols
 */
public class ModificarController implements Initializable {

    // --- Componentes FXML de los Campos ---
    @FXML private ImageView imagenAvatar;
    @FXML private TextField campoNickname;
    @FXML private TextField campoCorreo;
    @FXML private PasswordField campoContraseña;
    @FXML private DatePicker campoFechaNacimiento;
    @FXML private Button botonCambiarFoto;

    // --- Componentes FXML de los Mensajes de Error (Rojo) ---
    @FXML private Label errorCorreo;
    @FXML private Label errorContraseña;
    @FXML private Label errorFechaNacimiento;

    /**
     * Se ejecuta automáticamente al cargar la pantalla.
     * Vuelca los datos del usuario autenticado en los campos de la interfaz.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Configurar aspecto circular estricto para el avatar
        imagenAvatar.setFitWidth(150);
        imagenAvatar.setFitHeight(150);
        Circle clip = new Circle(75, 75, 75);
        imagenAvatar.setClip(clip);

        // 2. Limpiar textos de error residuales
        limpiarErrores();

        try {
            // 3. Recuperar usuario actual desde la librería persistente
            User usuarioActual = SportActivityApp.getInstance().getCurrentUser();

            if (usuarioActual != null) {
                campoNickname.setText(usuarioActual.getNickName());
                campoCorreo.setText(usuarioActual.getEmail());
                campoContraseña.setText(usuarioActual.getPassword());
                campoFechaNacimiento.setValue(usuarioActual.getBirthDate());

                if (usuarioActual.getAvatar() != null) {
                    imagenAvatar.setImage(usuarioActual.getAvatar());
                }
            } else {
                System.out.println("Error: No hay ningún usuario autenticado en el sistema.");
            }
        } catch (Exception e) {
            System.out.println("Error al cargar los datos del usuario: " + e.getMessage());
        }
    }

    /**
     * Gestión del explorador de archivos para actualizar la imagen de perfil.
     */
    @FXML
    private void cambiarFotoPressed(ActionEvent event) {
        FileChooser selectorFicheros = new FileChooser();
        selectorFicheros.setTitle("Selecciona tu nuevo avatar");
        selectorFicheros.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );

        Stage ventanaActual = (Stage) imagenAvatar.getScene().getWindow();
        File archivoSeleccionado = selectorFicheros.showOpenDialog(ventanaActual);

        if (archivoSeleccionado != null) {
            Image nuevoAvatar = new Image(archivoSeleccionado.toURI().toString());
            imagenAvatar.setImage(nuevoAvatar);
        }
    }

    /**
     * Recoge los valores modificados, los valida dinámicamente y los persiste en la BD.
     */
    @FXML
    private void guardarPressed(ActionEvent event) throws IOException {
        // 1. Limpiar la interfaz de errores antiguos
        limpiarErrores();

        // 2. Extraer cadenas de texto nativas
        String correo = campoCorreo.getText().trim();
        String password = campoContraseña.getText();
        LocalDate fechaNacimiento = campoFechaNacimiento.getValue();
        Image avatar = imagenAvatar.getImage();

        boolean datosValidos = true;

        // --- VALIDACIONES CON LA LIBRERÍA DE LA UPV ---
        if (!User.checkEmail(correo)) {
            errorCorreo.setText("Formato válido obligatorio: usuario@dominio.");
            datosValidos = false;
        }

        if (!User.checkPassword(password)) {
            errorContraseña.setText("Al menos una mayúscula, minúscula, dígito y símbolo.");
            datosValidos = false;
        }

        if (fechaNacimiento == null || !User.isOlderThan(fechaNacimiento, 12)) {
            errorFechaNacimiento.setText("Debes ser mayor de 12 años para usar la aplicación.");
            datosValidos = false;
        }

        // --- PERSISTENCIA Y REDIRECCIÓN ---
        if (datosValidos) {
            try {
                // Guardar los datos en el sistema
                SportActivityApp.getInstance().updateCurrentUser(correo, password, fechaNacimiento, avatar);

                // Notificar al usuario con un diálogo modal
                Alert exito = new Alert(AlertType.INFORMATION);
                exito.setTitle("Perfil Actualizado");
                exito.setHeaderText(null);
                exito.setContentText("¡Los cambios se han guardado con éxito en la base de datos!");
                exito.showAndWait();

                // Redirección segura de vuelta al Panel Principal solo si todo salió bien
                FXMLLoader miCargador = new FXMLLoader(getClass().getResource("/PantallaPrincipal/PantallaPrincipal.fxml"));
                Parent root = miCargador.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                
                stage.setScene(scene);
                stage.setTitle("Panel Principal");
                stage.show();

            } catch (Exception e) {
                System.out.println("Error al escribir en la base de datos: " + e.getMessage());
            }
        }
    }

    /**
     * Cancela la edición actual y cierra la ventana volviendo al Panel Principal.
     */
    @FXML
    private void cancelarPressed(ActionEvent event) throws IOException {
        FXMLLoader miCargador = new FXMLLoader(getClass().getResource("/PantallaPrincipal/PantallaPrincipal.fxml"));
        Parent root = miCargador.load();
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Panel Principal");
        
        Stage ventanaLogin = (Stage) ((Node) event.getSource()).getScene().getWindow();
        ventanaLogin.close();
        
        stage.show();
    }

    /**
     * Resetea el contenido de los textos de error.
     */
    private void limpiarErrores() {
        if (errorCorreo != null) errorCorreo.setText("");
        if (errorContraseña != null) errorContraseña.setText("");
        if (errorFechaNacimiento != null) errorFechaNacimiento.setText("");
    }
}
