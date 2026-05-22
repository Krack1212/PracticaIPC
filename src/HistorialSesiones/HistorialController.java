package HistorialSesiones;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

// Importaciones de la librería del caso práctico
import upv.ipc.sportlib.Session;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

public class HistorialController {

    private TableView<Session> tablaSesiones;
    private TableColumn<Session, LocalDateTime> colInicio;
    private TableColumn<Session, Duration> colDuracion;
    private TableColumn<Session, Integer> colImportadas;
    private TableColumn<Session, Integer> colVisualizadas;
    private TableColumn<Session, Integer> colAnotaciones;

    private Label lblTotalTiempo;
    private Label lblTotalImportadas;
    private Label lblTotalVistas;
    private Label lblTotalAnotaciones;

    private SportActivityApp app;
    private ObservableList<Session> listaSesiones = FXCollections.observableArrayList();
    @FXML
    private Button btnVolver;
    @FXML
    private ListView<?> listViewHistorial;

    public void initialize() {
        // 1. Obtener la instancia singleton de la aplicación
        app = SportActivityApp.getInstance();
        
        // 2. Configurar las columnas usando los métodos getter exactos de la clase Session
        colInicio.setCellValueFactory(new PropertyValueFactory<>("startTime"));      // getStartTime()
        colDuracion.setCellValueFactory(new PropertyValueFactory<>("duration"));    // getDuration()
        colImportadas.setCellValueFactory(new PropertyValueFactory<>("importedActivities")); // getImportedActivities()
        colVisualizadas.setCellValueFactory(new PropertyValueFactory<>("viewedActivities")); // getViewedActivities()
        colAnotaciones.setCellValueFactory(new PropertyValueFactory<>("annotationsCreated")); // getAnnotationsCreated()

        // 3. Cargar los datos del usuario actual si hay sesión iniciada
        User usuarioActual = app.getCurrentUser();
        if (usuarioActual != null) {
            // Obtiene las sesiones ordenadas por fecha descendente usando el método de la app
            List<Session> sesiones = app.getSessionsByUser(usuarioActual);
            listaSesiones.addAll(sesiones);
            tablaSesiones.setItems(listaSesiones);
            
            // 4. Calcular y mostrar los totales acumulados requisitados
            calcularTotales(sesiones);
        }
    }

    private void calcularTotales(List<Session> sesiones) {
        long totalSegundos = 0;
        int totalImportadas = 0;
        int totalVistas = 0;
        int totalAnotaciones = 0;

        for (Session s : sesiones) {
            if (s.getDuration() != null) {
                totalSegundos += s.getDuration().getSeconds();
            }
            totalImportadas += s.getImportedActivities();
            totalVistas += s.getViewedActivities();
            totalAnotaciones += s.getAnnotationsCreated();
        }

        // Formatear la duración total (Horas:Minutos:Segundos)
        long horas = totalSegundos / 3600;
        long minutos = (totalSegundos % 3600) / 60;
        long segundos = totalSegundos % 60;
        
        lblTotalTiempo.setText(String.format("%02d:%02d:%02d", horas, minutos, segundos));
        lblTotalImportadas.setText(String.valueOf(totalImportadas));
        lblTotalVistas.setText(String.valueOf(totalVistas));
        lblTotalAnotaciones.setText(String.valueOf(totalAnotaciones));
    }

    @FXML
    void onVolverClick() {
        // Lógica recomendada por la práctica: sustituir el área correspondiente del BorderPane principal
        System.out.println("Regresando al menú principal");
    }
}