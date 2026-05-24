package PantallaPrincipal;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mapademo.MapaDemo;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.Annotation;
import upv.ipc.sportlib.AnnotationType;
import static upv.ipc.sportlib.AnnotationType.CIRCLE;
import static upv.ipc.sportlib.AnnotationType.LINE;
import static upv.ipc.sportlib.AnnotationType.POINT;
import static upv.ipc.sportlib.AnnotationType.TEXT;
import upv.ipc.sportlib.GeoPoint;
import upv.ipc.sportlib.MapProjection;
import upv.ipc.sportlib.MapRegion;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.TrackPoint;
import upv.ipc.sportlib.User;


public class PantallaPrincipalController implements Initializable {

    
    @FXML private ListView<Activity>         listActividades;
    @FXML private ScrollPane                 mapScrollPane;
    @FXML private Slider                     zoomSlider;
    @FXML private Label                      lblBienvenido;
    @FXML private ImageView                  ivUserAvatar;
    @FXML private Label                      lblMapaActual;
    @FXML private Label                      lblMouseCoords;
    @FXML private Label                      lblEstado;
    @FXML private Label                      lblSesionInfo;
    @FXML private Label                      lblPlaceholder;
    @FXML private Label                      lblStatsPlaceholder;
    @FXML private GridPane                   gridStats;
    @FXML private AreaChart<Number, Number>  elevationChart;
    @FXML private NumberAxis                 chartXAxis;
    @FXML private NumberAxis                 chartYAxis;
    @FXML private CheckBox                   chkVelocidad;

    
    @FXML private Label lblDist, lblDuracion, lblVelMedia, lblRitmo;
    @FXML private Label lblDesnPos, lblDesnNeg, lblAltMin, lblAltMax;
    @FXML private Label lblAcumDist, lblAcumTiempo, lblAcumDesn;

    
    private Group       zoomGroup;
    private Pane        mapPane;
    private Group       routeGroup;           
    private MapProjection projection;
    private Activity    currentActivity;
    private Circle      hoverMarker;          
    private boolean     showVelocity = false;

    
    private AnnotationType pendingType        = null;
    private GeoPoint       pendingFirstPoint  = null;
    private String         pendingText        = null;
    private String         pendingColor       = "#e74c3c";
    private double         pendingStrokeWidth = 2.0;

    private ContextMenu mapContextMenu;

    private final SportActivityApp      app  = SportActivityApp.getInstance();
    private final LocalDateTime         sessionStart = LocalDateTime.now();
    private static final DateTimeFormatter FMT_DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarZoom();
        configurarListView();
        configurarContextMenu();
        cargarInfoUsuario();
        cargarActividades();
        actualizarSesionInfo();
    }

    private void configurarZoom() {
        zoomSlider.setMin(0.3);
        zoomSlider.setMax(3.0);
        zoomSlider.setValue(1.0);
        zoomSlider.valueProperty().addListener((o, ov, nv) -> aplicarZoom(nv.doubleValue()));
    }

    private void configurarListView() {
        listActividades.setCellFactory(lv -> new ListCell<Activity>() {
            @Override
            protected void updateItem(Activity act, boolean empty) {
                super.updateItem(act, empty);
                if (empty || act == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String fecha = act.getStartTime() != null
                        ? act.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yy"))
                        : "?";
                    double km = act.getTotalDistance() / 1000.0;
                    setText(String.format("%-16s\n  %.2f km • %s",
                        act.getName(), km, fecha));
                    setStyle("-fx-text-fill: #ecf0f1; -fx-background-color: transparent;");
                }
            }
        });

        listActividades.getSelectionModel().selectedItemProperty().addListener(
            (o, ov, nv) -> { if (nv != null) mostrarActividad(nv); }
        );
    }

    private void configurarContextMenu() {
        MenuItem miPunto  = new MenuItem("📍 Añadir punto");
        MenuItem miTexto  = new MenuItem("📝 Añadir texto");
        MenuItem miLinea  = new MenuItem("📏 Añadir línea");
        MenuItem miCirc   = new MenuItem("⭕ Añadir círculo");

        miPunto.setOnAction(e -> iniciarAnotacion(AnnotationType.POINT));
        miTexto.setOnAction(e -> iniciarAnotacion(AnnotationType.TEXT));
        miLinea.setOnAction(e -> iniciarAnotacion(AnnotationType.LINE));
        miCirc.setOnAction(e  -> iniciarAnotacion(AnnotationType.CIRCLE));

        mapContextMenu = new ContextMenu(miPunto, miTexto,
            new SeparatorMenuItem(), miLinea, miCirc);
    }

    private void cargarInfoUsuario() {
        User u = app.getCurrentUser();
        if (u == null) return;
        lblBienvenido.setText("  " + u.getNickName());
        if (u.getAvatarPath() != null) {
            File f = new File(u.getAvatarPath());
            if (f.exists()) {
                ivUserAvatar.setImage(new Image(f.toURI().toString(), 34, 34, true, true));
            }
        }
    }

    private void cargarActividades() {
        List<Activity> acts = app.getUserActivities();
        listActividades.setItems(FXCollections.observableArrayList(acts));
        actualizarAcumuladoMes(acts);
    }

    private void actualizarSesionInfo() {
        lblSesionInfo.setText("Sesión iniciada: " + sessionStart.format(FMT_DT));
    }

    

    @FXML private void zoomIn()    { zoomSlider.setValue(zoomSlider.getValue() + 0.15); }
    @FXML private void zoomOut()   { zoomSlider.setValue(zoomSlider.getValue() - 0.15); }
    @FXML private void zoomReset() { zoomSlider.setValue(1.0); }

    private void aplicarZoom(double scale) {
        if (zoomGroup == null) return;
        double h = mapScrollPane.getHvalue();
        double v = mapScrollPane.getVvalue();
        zoomGroup.setScaleX(scale);
        zoomGroup.setScaleY(scale);
        mapScrollPane.setHvalue(h);
        mapScrollPane.setVvalue(v);
    }

    

    private void mostrarActividad(Activity act) {
        currentActivity = act;
        pendingType       = null;
        pendingFirstPoint = null;

        MapRegion region = act.getSuggestedMap();
        if (region == null) {
            mostrarAlerta("Sin mapa", "No se encontró ningún mapa adecuado para esta actividad.");
            return;
        }

        construirMapa(region);
        dibujarRuta(act, region);
        dibujarAnotaciones(act);
        mostrarEstadisticas(act);
        dibujarGraficaDesnivel(act);

        lblMapaActual.setText(region.getName());
        lblPlaceholder.setVisible(false);
        lblPlaceholder.setManaged(false);
        lblEstado.setText("Actividad: " + act.getName());
    }

    

    private void construirMapa(MapRegion region) {
        File imgFile = new File(region.getImagePath());
        if (!imgFile.exists()) {
            mapScrollPane.setContent(
                new Label("Imagen de mapa no encontrada: " + imgFile.getPath()));
            return;
        }

        Image img = new Image(imgFile.toURI().toString());
        double W = img.getWidth();
        double H = img.getHeight();

        
        mapPane = new Pane();
        mapPane.setPrefSize(W, H);
        mapPane.setMinSize(W, H);
        mapPane.setMaxSize(W, H);
        ImageView iv = new ImageView(img);
        iv.setFitWidth(W);
        iv.setFitHeight(H);
        mapPane.getChildren().add(iv);

       
        routeGroup = new Group();
        routeGroup.setMouseTransparent(true);
        mapPane.getChildren().add(routeGroup);

        
        hoverMarker = new Circle(6, Color.YELLOW);
        hoverMarker.setStroke(Color.ORANGE);
        hoverMarker.setStrokeWidth(2);
        hoverMarker.setVisible(false);
        hoverMarker.setMouseTransparent(true);
        mapPane.getChildren().add(hoverMarker);

        
        projection = new MapProjection(region, W, H);

        
        mapPane.setOnMouseClicked(e -> onMapClicked(e));
        mapPane.setOnMouseMoved(e -> {
            if (projection != null) {
                GeoPoint gp = projection.unproject(e.getX(), e.getY());
                lblMouseCoords.setText(
                    String.format("Lat: %.5f  Lon: %.5f", gp.getLatitude(), gp.getLongitude()));
            }
        });

        
        zoomGroup   = new Group(mapPane);
        Group contentGroup = new Group(zoomGroup);

        double z = zoomSlider.getValue();
        zoomGroup.setScaleX(z);
        zoomGroup.setScaleY(z);

        mapScrollPane.setContent(contentGroup);
    }

    

    private void onMapClicked(javafx.scene.input.MouseEvent e) {
        if (currentActivity == null) return;
        mapContextMenu.hide();

        
        if (pendingType != null && pendingFirstPoint != null &&
            e.getButton() == MouseButton.PRIMARY) {
            GeoPoint secondPoint = projection.unproject(e.getX(), e.getY());
            guardarAnotacion(List.of(pendingFirstPoint, secondPoint));
            pendingType = null;
            pendingFirstPoint = null;
            mapPane.setCursor(javafx.scene.Cursor.DEFAULT);
            return;
        }

        if (e.getButton() == MouseButton.SECONDARY) {
            
            pendingFirstPoint = projection.unproject(e.getX(), e.getY());
            mapContextMenu.show(mapPane.getScene().getWindow(),
                mapPane.localToScreen(e.getX(), e.getY()).getX(),
                mapPane.localToScreen(e.getX(), e.getY()).getY());
        }
    }

    

    private void iniciarAnotacion(AnnotationType type) {
        if (currentActivity == null || pendingFirstPoint == null) return;

        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nueva anotación — " + type.name());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField tfTexto = new TextField();
        tfTexto.setPromptText("Texto de la anotación (opcional)");
        ColorPicker cp = new ColorPicker(Color.web(pendingColor));

        VBox content = new VBox(8,
            new Label("Texto:"), tfTexto,
            new Label("Color:"), cp
        );
        content.setPrefWidth(280);
        dialog.getDialogPane().setContent(content);
        dialog.initOwner(mapScrollPane.getScene().getWindow());

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            pendingType = null;
            pendingFirstPoint = null;
            return;
        }

        pendingText  = tfTexto.getText().trim();
        pendingColor = toHex(cp.getValue());
        pendingType  = type;

        if (type == AnnotationType.POINT || type == AnnotationType.TEXT) {
            
            guardarAnotacion(List.of(pendingFirstPoint));
            pendingType = null;
            pendingFirstPoint = null;
        } else {
            
            lblEstado.setText("Haz clic izquierdo para colocar el segundo punto de la " + type.name());
            mapPane.setCursor(javafx.scene.Cursor.CROSSHAIR);
        }
    }

    private void guardarAnotacion(List<GeoPoint> puntos) {
        Annotation ann = new Annotation(pendingType, pendingText, pendingColor,
            pendingStrokeWidth, puntos);
        Annotation saved = app.addAnnotation(currentActivity, ann);
        if (saved != null) {
            dibujarUnaAnotacion(saved);
            lblEstado.setText("Anotación guardada.");
        } else {
            lblEstado.setText("Error al guardar la anotación.");
        }
    }

    private void dibujarAnotaciones(Activity act) {
        for (Annotation ann : act.getAnnotations()) {
            dibujarUnaAnotacion(ann);
        }
    }

    private void dibujarUnaAnotacion(Annotation ann) {
        if (projection == null || mapPane == null) return;
        List<GeoPoint> pts = ann.getGeoPoints();
        Color color = Color.web(ann.getColor());

        switch (ann.getType()) {
            case POINT: {
                Point2D p = projection.project(pts.get(0));
                Circle c = new Circle(p.getX(), p.getY(), 8, color);
                c.setStroke(color.darker());
                c.setStrokeWidth(1.5);
                mapPane.getChildren().add(c);
                if (!ann.getText().isEmpty()) {
                    Text t = new Text(p.getX() + 11, p.getY() + 4, ann.getText());
                    t.setFill(Color.BLACK);
                    t.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");
                    mapPane.getChildren().add(t);
                }
                break;
            }
            case TEXT: {
                Point2D p = projection.project(pts.get(0));
                Label lbl = new Label(ann.getText());
                lbl.setLayoutX(p.getX());
                lbl.setLayoutY(p.getY() - 14);
                lbl.setStyle(String.format(
                    "-fx-background-color: %s; -fx-text-fill: white; " +
                    "-fx-padding: 2 6; -fx-background-radius: 3; -fx-font-size: 12;",
                    ann.getColor()));
                mapPane.getChildren().add(lbl);
                break;
            }
            case LINE: {
                if (pts.size() < 2) break;
                Point2D p1 = projection.project(pts.get(0));
                Point2D p2 = projection.project(pts.get(1));
                Line l = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                l.setStroke(color);
                l.setStrokeWidth(ann.getStrokeWidth());
                mapPane.getChildren().add(l);
                break;
            }
            case CIRCLE: {
                if (pts.size() < 2) break;
                Point2D pC = projection.project(pts.get(0));
                Point2D pB = projection.project(pts.get(1));
                double radius = pC.distance(pB);
                Circle c = new Circle(pC.getX(), pC.getY(), radius);
                c.setFill(Color.TRANSPARENT);
                c.setStroke(color);
                c.setStrokeWidth(ann.getStrokeWidth());
                mapPane.getChildren().add(c);
                break;
            }
        }
    }

    

    private void dibujarRuta(Activity act, MapRegion region) {
        List<TrackPoint> tps = act.getTrackPoints();
        if (tps.isEmpty()) return;

        if (showVelocity) {
            dibujarRutaVelocidad(tps);
        } else {
            dibujarRutaNormal(tps);
        }

        
        Point2D inicio = projection.project(tps.get(0));
        Point2D fin    = projection.project(tps.get(tps.size() - 1));

        Circle cIni = new Circle(inicio.getX(), inicio.getY(), 7, Color.LIMEGREEN);
        cIni.setStroke(Color.DARKGREEN); cIni.setStrokeWidth(2);
        Circle cFin = new Circle(fin.getX(), fin.getY(), 7, Color.CRIMSON);
        cFin.setStroke(Color.DARKRED); cFin.setStrokeWidth(2);

        routeGroup.getChildren().addAll(cIni, cFin);
    }

    private void dibujarRutaNormal(List<TrackPoint> tps) {
        Polyline ruta = new Polyline();
        for (TrackPoint tp : tps) {
            Point2D p = projection.project(tp);
            ruta.getPoints().addAll(p.getX(), p.getY());
        }
        ruta.setStroke(Color.DODGERBLUE);
        ruta.setStrokeWidth(2.5);
        ruta.setFill(Color.TRANSPARENT);
        routeGroup.getChildren().add(ruta);
    }

    private void dibujarRutaVelocidad(List<TrackPoint> tps) {
        if (tps.size() < 2) { dibujarRutaNormal(tps); return; }

        
        double vMin = Double.MAX_VALUE, vMax = 0;
        double[] speeds = new double[tps.size() - 1];
        for (int i = 0; i < tps.size() - 1; i++) {
            speeds[i] = tps.get(i).speedTo(tps.get(i + 1));
            if (speeds[i] < vMin) vMin = speeds[i];
            if (speeds[i] > vMax) vMax = speeds[i];
        }

        for (int i = 0; i < tps.size() - 1; i++) {
            Point2D p1 = projection.project(tps.get(i));
            Point2D p2 = projection.project(tps.get(i + 1));
            double t   = (vMax > vMin) ? (speeds[i] - vMin) / (vMax - vMin) : 0.5;

            
            Color c = t < 0.5
                ? Color.GREEN.interpolate(Color.YELLOW, t * 2)
                : Color.YELLOW.interpolate(Color.RED, (t - 0.5) * 2);

            Line seg = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            seg.setStroke(c);
            seg.setStrokeWidth(3);
            routeGroup.getChildren().add(seg);
        }
    }

    

    private void mostrarEstadisticas(Activity act) {
        double km = act.getTotalDistance() / 1000.0;
        Duration dur = act.getDuration();

        lblDist.setText(String.format("%.2f km", km));
        lblDuracion.setText(formatDuracion(dur));
        lblVelMedia.setText(String.format("%.1f km/h", act.getAverageSpeed()));
        lblRitmo.setText(String.format("%.2f min/km", act.getAveragePace()));
        lblDesnPos.setText(String.format("%.0f m", act.getElevationGain()));
        lblDesnNeg.setText(String.format("%.0f m", act.getElevationLoss()));
        lblAltMin.setText(String.format("%.0f m", act.getMinElevation()));
        lblAltMax.setText(String.format("%.0f m", act.getMaxElevation()));

        gridStats.setVisible(true);
        gridStats.setManaged(true);
        lblStatsPlaceholder.setVisible(false);
        lblStatsPlaceholder.setManaged(false);
    }

    

    private void dibujarGraficaDesnivel(Activity act) {
        elevationChart.getData().clear();
        List<TrackPoint> tps = act.getTrackPoints();
        if (tps.isEmpty()) return;

        XYChart.Series<Number, Number> serie = new XYChart.Series<>();
        serie.setName("Altitud");

        double distAcum = 0;
        for (int i = 0; i < tps.size(); i++) {
            if (i > 0) {
                distAcum += tps.get(i - 1).distanceTo(tps.get(i)) / 1000.0;
            }
            serie.getData().add(
                new XYChart.Data<>(distAcum, tps.get(i).getElevation()));
        }

        elevationChart.getData().add(serie);
        elevationChart.setVisible(true);
        elevationChart.setManaged(true);

        
        final List<TrackPoint> tpsFinal = tps;
        final List<Double> distancias = new ArrayList<>();
        double d = 0;
        distancias.add(0.0);
        for (int i = 1; i < tpsFinal.size(); i++) {
            d += tpsFinal.get(i - 1).distanceTo(tpsFinal.get(i)) / 1000.0;
            distancias.add(d);
        }
        double maxDist = distancias.get(distancias.size() - 1);

        elevationChart.setOnMouseMoved(ev -> {
            if (projection == null || hoverMarker == null) return;
            
            double pct = Math.max(0, Math.min(1, ev.getX() / elevationChart.getWidth()));
            double distObj = pct * maxDist;

            
            int idx = 0;
            double menorDif = Math.abs(distancias.get(0) - distObj);
            for (int i = 1; i < distancias.size(); i++) {
                double dif = Math.abs(distancias.get(i) - distObj);
                if (dif < menorDif) { menorDif = dif; idx = i; }
            }

            Point2D p = projection.project(tpsFinal.get(idx));
            hoverMarker.setCenterX(p.getX());
            hoverMarker.setCenterY(p.getY());
            hoverMarker.setVisible(true);
        });

        elevationChart.setOnMouseExited(ev -> {
            if (hoverMarker != null) hoverMarker.setVisible(false);
        });
    }

    

    private void actualizarAcumuladoMes(List<Activity> todas) {
        int mesActual = LocalDateTime.now().getMonthValue();
        int anioActual = LocalDateTime.now().getYear();

        double distTotal = 0;
        long   durSeg    = 0;
        double gainTotal = 0;
        double lossTotal = 0;

        for (Activity act : todas) {
            if (act.getStartTime() == null) continue;
            if (act.getStartTime().getMonthValue() == mesActual &&
                act.getStartTime().getYear()       == anioActual) {
                distTotal += act.getTotalDistance() / 1000.0;
                if (act.getDuration() != null) durSeg += act.getDuration().getSeconds();
                gainTotal += act.getElevationGain();
                lossTotal += act.getElevationLoss();
            }
        }

        lblAcumDist.setText(String.format("%.1f km", distTotal));
        lblAcumTiempo.setText(formatDuracion(Duration.ofSeconds(durSeg)));
        lblAcumDesn.setText(String.format("+%.0f / -%.0f m", gainTotal, lossTotal));
    }

    

    @FXML
    private void importarActividad() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar fichero GPX");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Ficheros GPX", "*.gpx"));
        fc.setInitialDirectory(new File("src/gpx/gpx").exists()
            ? new File("src/gpx/gpx") : new File("."));

        File gpxFile = fc.showOpenDialog(mapScrollPane.getScene().getWindow());
        if (gpxFile == null) return;

        lblEstado.setText("Importando actividad...");
        try {
            Activity act = app.importActivity(gpxFile);
            if (act != null) {
                cargarActividades();
                listActividades.getSelectionModel().select(act);
                lblEstado.setText("Actividad importada: " + act.getName());
            } else {
                lblEstado.setText("Error al importar el fichero GPX.");
            }
        } catch (Exception ex) {
            lblEstado.setText("Error: " + ex.getMessage());
        }
    }

    @FXML
    private void eliminarActividad() {
        Activity act = listActividades.getSelectionModel().getSelectedItem();
        if (act == null) {
            mostrarAlerta("Sin selección", "Selecciona una actividad de la lista.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar actividad");
        confirm.setHeaderText("¿Eliminar \"" + act.getName() + "\"?");
        confirm.setContentText("Esta acción no se puede deshacer.");
        confirm.initOwner(mapScrollPane.getScene().getWindow());

        Optional<ButtonType> resp = confirm.showAndWait();
        if (resp.isPresent() && resp.get() == ButtonType.OK) {
            app.removeActivity(act);
            currentActivity = null;
            mapScrollPane.setContent(null);
            ocultarStatsYChart();
            lblPlaceholder.setVisible(true);
            lblPlaceholder.setManaged(true);
            cargarActividades();
            lblEstado.setText("Actividad eliminada.");
        }
    }

    @FXML
    private void toggleVelocidad() {
        showVelocity = chkVelocidad.isSelected();
        if (currentActivity != null) {
            
            limpiarCapasRuta();
            if (currentActivity.getSuggestedMap() != null) {
                dibujarRuta(currentActivity, currentActivity.getSuggestedMap());
            }
        }
    }

    @FXML
    private void cerrarSesion() {
        app.logout();
        MapaDemo.primaryStage.setMaximized(false);
        MapaDemo.cargarVista("/autenticarse/Autenticarse.fxml", 480, 560, false);
    }

    @FXML
    private void abrirHistorial() {
        abrirVentanaModal("/HistorialSesiones/HistorialSesiones.fxml",
            "Historial de sesiones", 700, 420);
    }

    @FXML
    private void abrirModificarPerfil() {
        abrirVentanaModal("/ModificarPerfil/Modificar.fxml",
            "Modificar perfil", 500, 560);
        
        cargarInfoUsuario();
    }

    @FXML
    private void anadirMapa() {
        dialogAnadirMapa();
    }

    @FXML
    private void gestionarMapas() {
        dialogGestionarMapas();
    }

    @FXML
    private void acercaDe() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Acerca de");
        a.setHeaderText("Running la Safor — IPC 2026");
        a.setContentText(
            "En caso de haber algún problema contactar con: \nrlargra@upv.edu.es \nimareng@upv.edu.es \nosanjim@upv.edu.es"  );
        a.initOwner(mapScrollPane.getScene().getWindow());
        a.showAndWait();
    }

   

    private void dialogAnadirMapa() {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Añadir mapa");
        dlg.setHeaderText("Introduce los datos del nuevo mapa");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.initOwner(mapScrollPane.getScene().getWindow());

        TextField tfNombre  = new TextField();  tfNombre.setPromptText("Nombre del mapa");
        TextField tfLatMin  = new TextField();  tfLatMin.setPromptText("Latitud mínima");
        TextField tfLatMax  = new TextField();  tfLatMax.setPromptText("Latitud máxima");
        TextField tfLonMin  = new TextField();  tfLonMin.setPromptText("Longitud mínima");
        TextField tfLonMax  = new TextField();  tfLonMax.setPromptText("Longitud máxima");
        Label     lblImg    = new Label("Sin imagen seleccionada");
        final File[] imgRef = {null};

        Button btnImg = new Button("Seleccionar JPG...");
        btnImg.setOnAction(ev -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.jpeg", "*.png"));
            File f = fc.showOpenDialog(dlg.getOwner());
            if (f != null) { imgRef[0] = f; lblImg.setText(f.getName()); }
        });

        GridPane gp = new GridPane();
        gp.setHgap(10); gp.setVgap(8);
        gp.addRow(0, new Label("Nombre:"), tfNombre);
        gp.addRow(1, new Label("Imagen:"), new HBox(8, btnImg, lblImg));
        gp.addRow(2, new Label("Lat. mín:"), tfLatMin);
        gp.addRow(3, new Label("Lat. máx:"), tfLatMax);
        gp.addRow(4, new Label("Lon. mín:"), tfLonMin);
        gp.addRow(5, new Label("Lon. máx:"), tfLonMax);
        dlg.getDialogPane().setContent(gp);

        Optional<ButtonType> res = dlg.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) return;

        try {
            String nombre = tfNombre.getText().trim();
            double latMin = Double.parseDouble(tfLatMin.getText().trim());
            double latMax = Double.parseDouble(tfLatMax.getText().trim());
            double lonMin = Double.parseDouble(tfLonMin.getText().trim());
            double lonMax = Double.parseDouble(tfLonMax.getText().trim());

            if (nombre.isEmpty() || imgRef[0] == null) {
                mostrarAlerta("Datos incompletos", "Debes indicar nombre e imagen.");
                return;
            }

            MapRegion r = app.addMapRegion(nombre, imgRef[0], latMin, latMax, lonMin, lonMax);
            if (r != null) {
                lblEstado.setText("Mapa añadido: " + r.getName());
            } else {
                mostrarAlerta("Error", "No se pudo añadir el mapa. Comprueba los datos.");
            }
        } catch (NumberFormatException ex) {
            mostrarAlerta("Datos inválidos", "Las coordenadas deben ser números decimales.");
        }
    }

   

    private void dialogGestionarMapas() {
        List<MapRegion> regiones = app.getMapRegions();

        StringBuilder sb = new StringBuilder("Mapas registrados:\n\n");
        for (MapRegion r : regiones) {
            sb.append(String.format("• %s  [%.4f–%.4f lat, %.4f–%.4f lon]\n",
                r.getName(), r.getLatMin(), r.getLatMax(), r.getLonMin(), r.getLonMax()));
        }

        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Gestión de mapas");
        a.setHeaderText("Mapas disponibles en el sistema");
        a.setContentText(sb.toString());
        a.initOwner(mapScrollPane.getScene().getWindow());
        a.getDialogPane().setMinWidth(500);
        a.showAndWait();
    }

    

    private void ocultarStatsYChart() {
        gridStats.setVisible(false);
        gridStats.setManaged(false);
        elevationChart.setVisible(false);
        elevationChart.setManaged(false);
        lblStatsPlaceholder.setVisible(true);
        lblStatsPlaceholder.setManaged(true);
    }

    
    private void limpiarCapasRuta() {
        if (routeGroup != null) {
            routeGroup.getChildren().clear();
        }
    }

    private void abrirVentanaModal(String fxmlPath, String titulo, int w, int h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), w, h);
            scene.getStylesheets().add(
                getClass().getResource("/Style/Registro.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(mapScrollPane.getScene().getWindow());
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            lblEstado.setText("Error al abrir: " + titulo);
        }
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.initOwner(mapScrollPane.getScene().getWindow());
        a.showAndWait();
    }

    private static String formatDuracion(Duration dur) {
        if (dur == null) return "—";
        long h = dur.toHours();
        long m = dur.toMinutesPart();
        long s = dur.toSecondsPart();
        return String.format("%d:%02d:%02d", h, m, s);
    }

    private static String toHex(Color c) {
        return String.format("#%02X%02X%02X",
            (int)(c.getRed()   * 255),
            (int)(c.getGreen() * 255),
            (int)(c.getBlue()  * 255));
    }
}
