package HistorialSesiones;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import upv.ipc.sportlib.Session;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

/**
 * Controlador de la pantalla de historial de sesiones.
 *
 * Carga todas las sesiones del usuario autenticado y calcula totales acumulados.
 */
public class HistorialController implements Initializable {

    @FXML private TableView<Session>                tablaSesiones;
    @FXML private TableColumn<Session, String>      colInicio;
    @FXML private TableColumn<Session, String>      colFin;
    @FXML private TableColumn<Session, String>      colDuracion;
    @FXML private TableColumn<Session, String>      colImport;
    @FXML private TableColumn<Session, String>      colVistas;
    @FXML private TableColumn<Session, String>      colAnotac;

    @FXML private Label lblTotalSesiones;
    @FXML private Label lblTotalImport;
    @FXML private Label lblTotalVistas;
    @FXML private Label lblTotalAnotac;

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        cargarSesiones();
    }

    private void configurarColumnas() {
        colInicio.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getStartTime() != null
                ? c.getValue().getStartTime().format(FMT) : "-"));

        colFin.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getEndTime() != null
                ? c.getValue().getEndTime().format(FMT) : "-"));

        colDuracion.setCellValueFactory(c -> {
            java.time.Duration d = c.getValue().getDuration();
            if (d == null) return new SimpleStringProperty("-");
            long h = d.toHours();
            long m = d.toMinutesPart();
            long s = d.toSecondsPart();
            return new SimpleStringProperty(String.format("%d:%02d:%02d", h, m, s));
        });

        colImport.setCellValueFactory(c ->
            new SimpleStringProperty(String.valueOf(c.getValue().getImportedActivities())));

        colVistas.setCellValueFactory(c ->
            new SimpleStringProperty(String.valueOf(c.getValue().getViewedActivities())));

        colAnotac.setCellValueFactory(c ->
            new SimpleStringProperty(String.valueOf(c.getValue().getAnnotationsCreated())));
    }

    private void cargarSesiones() {
        SportActivityApp app = SportActivityApp.getInstance();
        User user = app.getCurrentUser();
        if (user == null) return;

        List<Session> sesiones = app.getSessionsByUser(user);
        tablaSesiones.setItems(FXCollections.observableArrayList(sesiones));

        // Totales acumulados
        long totalImport  = sesiones.stream().mapToLong(Session::getImportedActivities).sum();
        long totalVistas   = sesiones.stream().mapToLong(Session::getViewedActivities).sum();
        long totalAnotac  = sesiones.stream().mapToLong(Session::getAnnotationsCreated).sum();

        lblTotalSesiones.setText(String.valueOf(sesiones.size()));
        lblTotalImport.setText(String.valueOf(totalImport));
        lblTotalVistas.setText(String.valueOf(totalVistas));
        lblTotalAnotac.setText(String.valueOf(totalAnotac));
    }

    /** Cierra esta ventana de historial (si se abre como Stage separado). */
    @FXML
    private void cerrar() {
        Stage stage = (Stage) tablaSesiones.getScene().getWindow();
        stage.close();
    }
}
