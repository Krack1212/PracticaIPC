package PantallaPrincipal;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseEvent;

public class PantallaPrincipalController implements Initializable {

    @FXML
    private Slider zoom_slider;
    @FXML
    private Label mousePosition;
    @FXML
    private SplitPane splitPane;
    @FXML
    private ListView<?> map_listview;
    @FXML
    private ScrollPane map_scrollpane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }    

    @FXML
    private void atencionALCliente(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
     alert.setTitle("Atención al cliente");
     alert.setHeaderText("AYUDA");
     alert.setContentText("Contacta a osanjim@upv.edu.es, imareng@upv.edu.es o rlargra@upv.edu.es");
     alert.showAndWait();
    }

    @FXML
    private void zoomOut(ActionEvent event) {
    }

    @FXML
    private void zoomIn(ActionEvent event) {
    }

    @FXML
    private void listClicked(MouseEvent event) {
    }

    @FXML
    private void showPosition(MouseEvent event) {
    }

    @FXML
    private void ModificarPerfil(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader miCargador = new javafx.fxml.FXMLLoader(getClass().getResource("/ModificarPerfil/Modificar.fxml"));
            javafx.scene.Parent root = miCargador.load();
                    
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            
            // 🟢 SOLUCIÓN: Accedemos al Stage de forma segura para MenuItems
            javafx.scene.control.MenuItem item = (javafx.scene.control.MenuItem) event.getSource();
            // Conseguimos la ventana a través del estilo de la barra interna del menú
            javafx.stage.Stage stage = (javafx.stage.Stage) item.getParentPopup().getOwnerWindow();
                    
            stage.setScene(scene);
            stage.setTitle("Modificar Perfil");
            stage.show();
        } catch (java.io.IOException e) {
            System.out.println("Error al cargar el FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
}
