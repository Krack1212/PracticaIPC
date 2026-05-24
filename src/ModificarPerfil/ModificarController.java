package ModificarPerfil;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import mapademo.MapaDemo;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

/**
 * Controlador de la pantalla de modificación de perfil.
 *
 * Carga los datos actuales del usuario y los persiste tras validarlos
 * mediante User.checkEmail(), User.checkPassword() y User.isOlderThan().
 */
public class ModificarController implements Initializable {

    @FXML private TextField     txtNick;
    @FXML private TextField     txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private DatePicker    dpBirthDate;
    @FXML private ImageView     ivAvatar;

    @FXML private Label lblEmailError;
    @FXML private Label lblPassError;
    @FXML private Label lblFechaError;
    @FXML private Label lblMensaje;

    private final SportActivityApp app = SportActivityApp.getInstance();
    private String avatarPath;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dpBirthDate.setConverter(new javafx.util.StringConverter<LocalDate>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override public String toString(LocalDate d)   { return d != null ? fmt.format(d) : ""; }
            @Override public LocalDate fromString(String s) { return (s != null && !s.isEmpty()) ? LocalDate.parse(s, fmt) : null; }
        });

        cargarDatosUsuario();
    }

    /** Rellena el formulario con los datos actuales del usuario. */
    private void cargarDatosUsuario() {
        User u = app.getCurrentUser();
        if (u == null) return;

        txtNick.setText(u.getNickName());
        txtEmail.setText(u.getEmail() != null ? u.getEmail() : "");
        dpBirthDate.setValue(u.getBirthDate());
        avatarPath = u.getAvatarPath();

        if (avatarPath != null) {
            File f = new File(avatarPath);
            if (f.exists()) {
                ivAvatar.setImage(new Image(f.toURI().toString(), 60, 60, true, true));
            }
        }
    }

    /** Abre selector de imagen para el avatar. */
    @FXML
    private void seleccionarAvatar() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar avatar");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = fc.showOpenDialog(txtNick.getScene().getWindow());
        if (file != null) {
            avatarPath = file.getAbsolutePath();
            ivAvatar.setImage(new Image(file.toURI().toString(), 60, 60, true, true));
        }
    }

    /** Elimina el avatar actual. */
    @FXML
    private void quitarAvatar() {
        avatarPath = null;
        ivAvatar.setImage(null);
    }

    /** Valida y guarda los cambios del perfil. */
    @FXML
    private void guardar() {
        limpiarErrores();
        boolean valido = true;

        String email = txtEmail.getText().trim();
        String pass  = txtPassword.getText();
        LocalDate birth = dpBirthDate.getValue();

        if (!User.checkEmail(email)) {
            mostrarCampoError(lblEmailError, "Email inválido: formato usuario@dominio.extensión");
            valido = false;
        }

        // Contraseña vacía → conservar la actual; si hay texto → validar
        if (!pass.isEmpty() && !User.checkPassword(pass)) {
            mostrarCampoError(lblPassError,
                "Contraseña inválida: 8-20 chars, mayúscula, minúscula, dígito y símbolo");
            valido = false;
        }

        if (birth == null) {
            mostrarCampoError(lblFechaError, "Debes indicar la fecha de nacimiento.");
            valido = false;
        } else if (!User.isOlderThan(birth, 12)) {
            mostrarCampoError(lblFechaError, "Debes tener más de 12 años.");
            valido = false;
        }

        if (!valido) return;

        // Si el campo contraseña está vacío, conservar la contraseña actual
        String passToSave = pass.isEmpty() ? app.getCurrentUser().getPassword() : pass;

        boolean ok = app.updateCurrentUser(email, passToSave, birth, avatarPath);
        if (ok) {
            mostrarMensaje("Perfil actualizado correctamente.", true);
        } else {
            mostrarMensaje("Error al guardar los cambios. Inténtalo de nuevo.", false);
        }
    }

    /** Vuelve a la pantalla principal sin guardar cambios. */
    @FXML
    private void cancelar() {
        // Si se abre como Stage separado, cerrar; si está embebida, volver al main
        try {
            Stage stage = (Stage) txtNick.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            MapaDemo.cargarVista("/PantallaPrincipal/PantallaPrincipal.fxml", 1200, 750, true);
        }
    }

    // ---- helpers ----

    private void mostrarCampoError(Label lbl, String msg) {
        lbl.setText(msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    private void mostrarMensaje(String msg, boolean exito) {
        lblMensaje.setText(msg);
        lblMensaje.setStyle(exito ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }

    private void limpiarErrores() {
        for (Label lbl : new Label[]{lblEmailError, lblPassError, lblFechaError, lblMensaje}) {
            lbl.setVisible(false);
            lbl.setManaged(false);
        }
    }
}
