package autenticarse;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import mapademo.MapaDemo;
import upv.ipc.sportlib.SportActivityApp;

/**
 * Controlador de la pantalla de autenticación.
 *
 * Valida credenciales mediante SportActivityApp y, si son correctas,
 * carga la pantalla principal de la aplicación.
 */
public class AutenticarseController implements Initializable {

    @FXML private TextField     txtNick;
    @FXML private PasswordField txtPassword;
    @FXML private Label         lblError;

    private final SportActivityApp app = SportActivityApp.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Ocultar error al cambiar el contenido de los campos
        txtNick.textProperty().addListener((o, v, n) -> ocultarError());
        txtPassword.textProperty().addListener((o, v, n) -> ocultarError());
    }

    /** Intenta autenticar con los datos introducidos. */
    @FXML
    private void login() {
        String nick = txtNick.getText().trim();
        String pass = txtPassword.getText();

        if (nick.isEmpty() || pass.isEmpty()) {
            mostrarError("Por favor, introduce nickname y contraseña.");
            return;
        }

        boolean ok = app.login(nick, pass);
        if (ok) {
            MapaDemo.cargarVista("/PantallaPrincipal/PantallaPrincipal.fxml", 1200, 750, true);
        } else {
            mostrarError("Nickname o contraseña incorrectos.");
            txtPassword.clear();
        }
    }

    /** Navega a la pantalla de registro. */
    @FXML
    private void irARegistro() {
        MapaDemo.cargarVista("/mapademo/Registro.fxml", 480, 660, false);
    }

    private void mostrarError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void ocultarError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }
}
