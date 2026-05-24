/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo;

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
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

/**
 * Controlador de la pantalla de registro de nuevos usuarios.
 *
 * Valida los campos usando los métodos estáticos de User antes de
 * llamar a app.registerUser().
 */
public class RegistroController implements Initializable {

    @FXML private TextField   txtNick;
    @FXML private TextField   txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private DatePicker  dpBirthDate;
    @FXML private ImageView   ivAvatar;

    @FXML private Label lblNickError;
    @FXML private Label lblEmailError;
    @FXML private Label lblPassError;
    @FXML private Label lblFechaError;
    @FXML private Label lblMensaje;

    private final SportActivityApp app = SportActivityApp.getInstance();
    private String avatarPath = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dpBirthDate.setConverter(new javafx.util.StringConverter<LocalDate>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override public String toString(LocalDate d)    { return d != null ? fmt.format(d) : ""; }
            @Override public LocalDate fromString(String s)  { return (s != null && !s.isEmpty()) ? LocalDate.parse(s, fmt) : null; }
        });
    }

    /** Abre FileChooser para seleccionar imagen de avatar. */
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
            ivAvatar.setImage(new Image(file.toURI().toString(), 52, 52, true, true));
        }
    }

    /** Valida todos los campos y registra el usuario si todo es correcto. */
    @FXML
    private void registrar() {
        limpiarErrores();
        boolean valido = true;

        String nick  = txtNick.getText().trim();
        String email = txtEmail.getText().trim();
        String pass  = txtPassword.getText();
        LocalDate birth = dpBirthDate.getValue();

        // Validar nickname
        if (!User.checkNickName(nick)) {
            mostrarCampoError(lblNickError, "Nickname inválido: 6-15 caracteres, solo letras, dígitos, - o _");
            valido = false;
        }

        // Validar email
        if (!User.checkEmail(email)) {
            mostrarCampoError(lblEmailError, "Email inválido: formato usuario@dominio.extensión");
            valido = false;
        }

        // Validar contraseña
        if (!User.checkPassword(pass)) {
            mostrarCampoError(lblPassError,
                "Contraseña inválida: 8-20 chars, al menos una mayúscula, minúscula, dígito y símbolo (!@#$%&*()-+=)");
            valido = false;
        }

        // Validar fecha y edad mínima (> 12 años)
        if (birth == null) {
            mostrarCampoError(lblFechaError, "Debes indicar la fecha de nacimiento.");
            valido = false;
        } else if (!User.isOlderThan(birth, 12)) {
            mostrarCampoError(lblFechaError, "Debes tener más de 12 años para registrarte.");
            valido = false;
        }

        if (!valido) return;

        boolean ok = app.registerUser(nick, email, pass, birth, avatarPath);
        if (ok) {
            mostrarMensaje("¡Registro completado! Ya puedes iniciar sesión.", true);
        } else {
            mostrarMensaje("El nickname o el email ya están en uso. Prueba con otros.", false);
        }
    }

    /** Vuelve a la pantalla de login. */
    @FXML
    private void volverLogin() {
        MapaDemo.cargarVista("/autenticarse/Autenticarse.fxml", 480, 560, false);
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
        for (Label lbl : new Label[]{lblNickError, lblEmailError, lblPassError, lblFechaError, lblMensaje}) {
            lbl.setVisible(false);
            lbl.setManaged(false);
        }
    }
}
