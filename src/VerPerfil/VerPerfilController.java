package VerPerfil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;


import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;


public class VerPerfilController implements Initializable {

    @FXML private Label lblDisplayNick;
    @FXML private ImageView ivAvatar;
    @FXML private Label lblNick;
    @FXML private Label lblEmail;
    @FXML private Label lblBirthDate;

    
    private final SportActivityApp app = SportActivityApp.getInstance();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarDatosUsuario();
    }    

    
    private void cargarDatosUsuario() {
        
        User u = app.getCurrentUser();
        if (u == null) return;

        
        lblNick.setText(u.getNickName());
        lblDisplayNick.setText(u.getNickName());
        lblEmail.setText(u.getEmail() != null ? u.getEmail() : "No indicado");
        
        
        LocalDate birth = u.getBirthDate();
        if (birth != null) {
            lblBirthDate.setText(formatter.format(birth));
        } else {
            lblBirthDate.setText("--/--/----");
        }

        
        String avatarPath = u.getAvatarPath();
        if (avatarPath != null) {
            File f = new File(avatarPath);
            if (f.exists()) {
                
                ivAvatar.setImage(new Image(f.toURI().toString(), 60, 60, true, true));
            }
        }
    }

    @FXML
    private void irAModificarPerfil(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModificarPerfil/ModificarPerfil.fxml"));
            Parent root = loader.load();
            
            Scene nuevaEscena = new Scene(root);
            Stage stageActual = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            stageActual.setScene(nuevaEscena);
            stageActual.show();
        } catch (IOException e) {
            System.err.println("Error al cargar ModificarPerfil.fxml desde VerPerfil: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void volverAtras(ActionEvent event) {
        Stage stageActual = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stageActual.close();
    }
}