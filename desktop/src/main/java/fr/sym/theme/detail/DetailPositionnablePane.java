
package fr.sym.theme.detail;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import fr.sym.Session;
import fr.sym.Symadrem;
import fr.sym.digue.Injector;
import fr.symadrem.sirs.core.LinearReferencingUtilities;
import fr.symadrem.sirs.core.model.BorneDigue;
import fr.symadrem.sirs.core.model.Positionable;
import fr.symadrem.sirs.core.model.SystemeReperage;
import fr.symadrem.sirs.core.model.SystemeReperageBorne;
import fr.symadrem.sirs.core.model.TronconDigue;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import org.apache.sis.referencing.CommonCRS;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.util.FXNumberSpinner;
import org.geotoolkit.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class DetailPositionnablePane extends BorderPane {
    
    private static final CoordinateReferenceSystem CRS_WGS84 = CommonCRS.WGS84.normalizedGeographic();
    private static final CoordinateReferenceSystem CRS_RGF93 = Session.PROJECTION;
    
    public static final Image ICON_IMPORT  = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_DOWNLOAD,22,Color.WHITE),null);
    public static final Image ICON_VIEWOTHER  = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_BARS,22,Color.WHITE),null);
    
    @FXML private Button uiImport;
    @FXML private Button uiView;
    
    @FXML private ToggleButton uiTypeBorne;
    @FXML private ToggleButton uiTypeCoord;
    @FXML private ProgressIndicator uiLoading;
    @FXML private BorderPane uiPane;
    
    //Mode borne
    @FXML private GridPane uiBornePane;
    @FXML private ComboBox<String> uiSRs;
    @FXML private ComboBox<String> uiBorneStart;
    @FXML private ComboBox<String> uiBorneEnd;
    @FXML private CheckBox uiAvalSart;
    @FXML private CheckBox uiAvalEnd;
    @FXML private FXNumberSpinner uiDistanceStart;
    @FXML private FXNumberSpinner uiDistanceEnd;    
    //Mode coordonées
    @FXML private GridPane uiCoordPane;
    @FXML private ComboBox<CoordinateReferenceSystem> uiCRSs;
    @FXML private FXNumberSpinner uiLongitudeStart;
    @FXML private FXNumberSpinner uiLongitudeEnd;
    @FXML private FXNumberSpinner uiLatitudeStart;
    @FXML private FXNumberSpinner uiLatitudeEnd;
    
    
    private final ObjectProperty<Positionable> positionableProperty = new SimpleObjectProperty<>();
    private final BooleanProperty disableFieldsProperty = new SimpleBooleanProperty(true);
    private boolean initializing = false;
    private final Map<String,SystemeReperage> cacheSystemeReperage = new HashMap<>();
    private final Map<String,BorneDigue> cacheBorneDigue = new HashMap<>();
            
    public DetailPositionnablePane(){
        try{
            final Class cdtClass = getClass();
            final String fxmlpath = "/fr/sym/theme/detail/DetailPositionnablePane.fxml";
            final FXMLLoader loader = new FXMLLoader(cdtClass.getResource(fxmlpath));
            loader.setController(this);
            loader.setRoot(this);
            loader.setClassLoader(cdtClass.getClassLoader());
            try {
                loader.load();
            } catch (IOException ex) {
                throw new IllegalArgumentException(ex.getMessage(), ex);
            }
        }catch(Throwable ex){
            ex.printStackTrace();
        }
        
        positionableProperty.addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                updateField();
            }
        });
        
        uiImport.setGraphic(new ImageView(ICON_IMPORT));
        uiView.setGraphic(new ImageView(ICON_VIEWOTHER));
     
        uiSRs.setCellFactory((ListView<String> param) -> new ObjectListCell());
        uiSRs.setButtonCell(new ObjectListCell());
        uiCRSs.setCellFactory((ListView<CoordinateReferenceSystem> param) -> new ObjectListCell());
        uiCRSs.setButtonCell(new ObjectListCell());
        uiBorneStart.setCellFactory((ListView<String> param) -> new ObjectListCell());
        uiBorneStart.setButtonCell(new ObjectListCell());
        uiBorneEnd.setCellFactory((ListView<String> param) -> new ObjectListCell());
        uiBorneEnd.setButtonCell(new ObjectListCell());
        
        //liste par défaut des systemes de coordonnées
        final ObservableList<CoordinateReferenceSystem> crss = FXCollections.observableArrayList();
        crss.add(CRS_WGS84);
        crss.add(CRS_RGF93);
        uiCRSs.setItems(crss);
        uiCRSs.getSelectionModel().clearAndSelect(1);
        
        
        //affichage des panneaux coord/borne
        final ToggleGroup group = new ToggleGroup();
        uiTypeBorne.setToggleGroup(group);
        uiTypeCoord.setToggleGroup(group);
        group.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
            if(newValue==null) group.selectToggle(uiTypeCoord);
        });
        
        uiCoordPane.visibleProperty().bind(uiTypeCoord.selectedProperty());
        uiBornePane.visibleProperty().bind(uiTypeBorne.selectedProperty());
        
        //binding de l'etat editable
        final BooleanProperty binding = disableFieldsProperty;
        uiImport.visibleProperty().bind(disableFieldsProperty.not());
        
        uiSRs.disableProperty().bind(binding);
        uiBorneStart.disableProperty().bind(binding);
        uiBorneEnd.disableProperty().bind(binding);
        uiAvalSart.disableProperty().bind(binding);
        uiAvalEnd.disableProperty().bind(binding);
        uiDistanceStart.disableProperty().bind(binding);
        uiDistanceEnd.disableProperty().bind(binding);
        
        uiLongitudeStart.disableProperty().bind(binding);
        uiLatitudeStart.disableProperty().bind(binding);
        uiLongitudeEnd.disableProperty().bind(binding);
        uiLatitudeEnd.disableProperty().bind(binding);
        
        uiCRSs.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<CoordinateReferenceSystem>() {

            @Override
            public void changed(ObservableValue<? extends CoordinateReferenceSystem> observable, CoordinateReferenceSystem oldValue, CoordinateReferenceSystem newValue) {
                updateGeoCoord();
            }
        });
        uiSRs.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                updateBorneList();
            }
        });
        
    }
    
    public ObjectProperty<Positionable> positionableProperty(){
        return positionableProperty;
    }
    
    public Positionable getPositionable(){
        return positionableProperty.get();
    }
    
    public void setPositionable(Positionable positionable){
        positionableProperty.set(positionable);
    }
    
    public BooleanProperty disableFieldsProperty(){
        return disableFieldsProperty;
    }
        
    @FXML
    void importCoord(ActionEvent event) {

    }

    @FXML
    void viewAllSR(ActionEvent event) {
        final Positionable pos = (Positionable) positionableProperty.get();
        if(pos==null) return;
        
        final Session session = Injector.getBean(Session.class);
        final TronconDigue troncon = session.getTronconDigueRepository().get(pos.getDocumentId());
        final Geometry linear = troncon.getGeometry();
        
        //calcule de la position geographique
        Point startPoint = pos.getPositionDebut();
        Point endPoint = pos.getPositionFin();
        if(startPoint==null){
            //calcule a partir des bornes
            
            final BorneDigue borneStart = cacheBorneDigue.get(pos.borne_debutProperty().get());
            final Point borneStartPoint = borneStart.getPositionBorne();
            double distStart = pos.getBorne_debut_distance();
            if(pos.getBorne_debut_aval()) distStart -= -1;
            
            final BorneDigue borneEnd = cacheBorneDigue.get(pos.borne_finProperty().get());
            final Point borneEndPoint = borneEnd.getPositionBorne();            
            double distEnd = pos.getBorne_fin_distance();
            if(pos.getBorne_fin_aval()) distEnd -= -1;
            
            startPoint = LinearReferencingUtilities.calculateCoordinate(linear, borneStartPoint, distStart, 0);
            endPoint = LinearReferencingUtilities.calculateCoordinate(linear, borneEndPoint, distEnd, 0);
            
        }
        
        
        final StringBuilder page = new StringBuilder();
        page.append("<html><body>");
        
        
        //RGF93 coord
        page.append("<h2>Projection française (RGF-93, EPSG:2154)</h2>");
        page.append("<b>Début</b><br/>");
        page.append("X : ").append(startPoint.getX()).append("<br/>");
        page.append("Y : ").append(startPoint.getY()).append("<br/>");
        page.append("<b>Fin</b><br/>");
        page.append("X : ").append(endPoint.getX()).append("<br/>");
        page.append("Y : ").append(endPoint.getY()).append("<br/>");
        page.append("<br/>");
        
        
        //WGS84 coord
        try {
            final MathTransform trs = CRS.findMathTransform(CRS_RGF93, CRS_WGS84, true);
            Point ptStart = (Point) JTS.transform(startPoint, trs);
            Point ptEnd = (Point) JTS.transform(endPoint, trs);
            
            page.append("<h2>Coordonnées géographique (WGS-84, EPSG:4326)</h2>");
            page.append("<b>Début</b><br/>");
            page.append("Longitude : ").append(ptStart.getX()).append("<br/>");
            page.append("Latitude : ").append(ptStart.getY()).append("<br/>");
            page.append("<b>Fin</b><br/>");
            page.append("Longitude : ").append(ptEnd.getX()).append("<br/>");
            page.append("Latitude : ").append(ptEnd.getY()).append("<br/>");
            page.append("<br/>");
        } catch (FactoryException | TransformException ex) {
            Symadrem.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        
        
        //pour chaque systeme de reperage
        for(SystemeReperage sr : cacheSystemeReperage.values()){
            final List<BorneDigue> bornes = new ArrayList<>();
            final List<Point> references = new ArrayList<>();
            for(SystemeReperageBorne srb : sr.systemereperageborneId){
                final String bid = srb.getBorneId();
                final BorneDigue bd = session.getBorneDigueRepository().get(bid);
                if(bd!=null){ 
                    bornes.add(bd);
                    references.add(bd.getPositionBorne());
                }
            }
            
            final Entry<Integer,double[]> startRef = LinearReferencingUtilities.calculateRelative(linear, references.toArray(new Point[0]), startPoint);
            final BorneDigue startBorne = bornes.get(startRef.getKey());
            double distanceStartBorne = startRef.getValue()[0];
            boolean startAval = false;
            if(distanceStartBorne<0){
                distanceStartBorne = -distanceStartBorne;
                startAval = true;
            }
            
            final Entry<Integer,double[]> endRef = LinearReferencingUtilities.calculateRelative(linear, references.toArray(new Point[0]), endPoint);
            final BorneDigue endBorne = bornes.get(endRef.getKey());
            double distanceEndBorne = endRef.getValue()[0];
            boolean endAval = false;
            if(distanceEndBorne<0){
                distanceEndBorne = -distanceEndBorne;
                endAval = true;
            }
            
            
            page.append("<h2>SR : ").append(sr.getNom()).append("</h2>");
            page.append("<b>Début </b>");
            page.append(startBorne.getNom()).append(' ');
            page.append(distanceStartBorne).append("m ");
            page.append(startAval ? "en amont":"en aval");
            page.append("<br/>");
            page.append("<b>Fin </b>");
            page.append(endBorne.getNom()).append(' ');
            page.append(distanceEndBorne).append("m ");
            page.append(endAval ? "en amont":"en aval");
            page.append("<br/><br/>");
        }
        
        
        page.append("</html></body>");
        
        final WebView view = new WebView();        
        view.getEngine().loadContent(page.toString());
        
        final Dialog dialog = new Dialog();
        final DialogPane pane = new DialogPane();
        pane.setContent(view);
        pane.getButtonTypes().add(ButtonType.CLOSE);
        dialog.setDialogPane(pane);
        dialog.setTitle("Position");
        dialog.setOnCloseRequest((Event event1) -> {dialog.hide();});
        dialog.showAndWait();
        
    }

    private void updateGeoCoord() {
        if(initializing) return;
        final CoordinateReferenceSystem crs = uiCRSs.getSelectionModel().getSelectedItem();
        
        Point ptStart = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(
            fxNumberValue(uiLongitudeStart),
            fxNumberValue(uiLatitudeStart)
        ));
        Point ptEnd = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(
            fxNumberValue(uiLongitudeEnd),
            fxNumberValue(uiLatitudeEnd)
        ));
        
        if(crs==CRS_WGS84){
            //conversion vers WGS84
            try {
                final MathTransform trs = CRS.findMathTransform(CRS_RGF93, CRS_WGS84, true);
                ptStart = (Point) JTS.transform(ptStart, trs);
                ptEnd = (Point) JTS.transform(ptEnd, trs);
            } catch (FactoryException | TransformException ex) {
                Symadrem.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }
        }else if(crs==CRS_RGF93){
            //conversion vers RGF93
            try {
                final MathTransform trs = CRS.findMathTransform(CRS_WGS84, CRS_RGF93, true);
                ptStart = (Point) JTS.transform(ptStart, trs);
                ptEnd = (Point) JTS.transform(ptEnd, trs);
            } catch (FactoryException | TransformException ex) {
                Symadrem.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        
        uiLongitudeStart.valueProperty().set(ptStart.getX());
        uiLatitudeStart.valueProperty().set(ptStart.getY());
        uiLongitudeEnd.valueProperty().set(ptEnd.getX());
        uiLatitudeEnd.valueProperty().set(ptEnd.getY());
        
    }
    
    private double fxNumberValue(FXNumberSpinner spinner){
        if(spinner.valueProperty().get()==null) return 0;
        return spinner.valueProperty().get().doubleValue();
    }
    
    private void updateSRList(){
        cacheSystemeReperage.clear();
        
        final Positionable pos = (Positionable) positionableProperty.get();
        if(pos==null) return;
        
        final Session session = Injector.getBean(Session.class);
        final TronconDigue troncon = session.getTronconDigueRepository().get(pos.getDocumentId());
        final List<SystemeReperage> srs = session.getSystemeReperageRepository().getByTroncon(troncon);      
        
        for(SystemeReperage sr : srs){
            cacheSystemeReperage.put(sr.getId(), sr);
        }
        
    }
    
    private void updateBorneList(){
        final Positionable pos = (Positionable) positionableProperty.get();
        if(pos==null) return;
        
        cacheBorneDigue.clear();
        final List<String> bornes = new ArrayList<>();
        if(pos.getSysteme_rep_id()!=null && !pos.getSysteme_rep_id().isEmpty()){
            final Session session = Injector.getBean(Session.class);
            final SystemeReperage sr = session.getSystemeReperageRepository().get(pos.getSysteme_rep_id());
            if(sr!=null){
                for(SystemeReperageBorne srb : sr.systemereperageborneId){
                    final String bid = srb.getBorneId();
                    final BorneDigue bd = session.getBorneDigueRepository().get(bid);
                    if(bd!=null){
                        cacheBorneDigue.put(bid, bd);
                        bornes.add(bid);
                    }
                }
            }
        }
        
        uiBorneStart.setItems(FXCollections.observableList(bornes));
        uiBorneEnd.setItems(FXCollections.observableList(bornes));
    }
    
    private void updateField(){
        final Positionable pos = (Positionable) positionableProperty.get();
        if(pos==null) return;
        
        initializing = true;
        uiLoading.setVisible(true);
        uiPane.setDisable(true);
        
        new Thread(){
            @Override
            public void run() {
                //creation de la liste des systemes de reperage
                updateSRList();
                uiSRs.setItems(FXCollections.observableArrayList(cacheSystemeReperage.keySet()));
                updateBorneList();
                
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        //Bindings
                        uiSRs.valueProperty().bindBidirectional(pos.systeme_rep_idProperty());
                        uiBorneStart.valueProperty().bindBidirectional(pos.borne_debutProperty());
                        uiBorneEnd.valueProperty().bindBidirectional(pos.borne_finProperty());
                        uiAvalSart.selectedProperty().bindBidirectional(pos.borne_debut_avalProperty());
                        uiAvalEnd.selectedProperty().bindBidirectional(pos.borne_fin_avalProperty());
                        uiDistanceStart.valueProperty().bindBidirectional(pos.borne_debut_distanceProperty());
                        uiDistanceEnd.valueProperty().bindBidirectional(pos.borne_fin_distanceProperty());

                        //selectionner RGF93 par defaut
                        uiCRSs.getSelectionModel().clearAndSelect(1);
                        final Point startPos = pos.getPositionDebut();
                        final Point endPos = pos.getPositionFin();

                        if(startPos != null){
                            uiLongitudeStart.valueProperty().set(startPos.getX());
                            uiLatitudeStart.valueProperty().set(startPos.getY());
                        }
                        if(endPos != null){
                            uiLongitudeEnd.valueProperty().set(endPos.getX());
                            uiLatitudeEnd.valueProperty().set(endPos.getY());
                        }

                        //on active le panneau qui a le positionnement
                        if(startPos!=null || endPos!=null){
                            uiTypeCoord.setSelected(true);
                        }else{
                            uiTypeBorne.setSelected(true);
                        }

                        uiPane.setDisable(false);
                        uiLoading.setVisible(false);
                        initializing = false;
                    }
                });
                
            }
        }.start();
    }
        
    public void preSave() {
        final Positionable pos = (Positionable) positionableProperty.get();
        if(pos==null) return;
        
        if(uiTypeCoord.isSelected()){
            //sauvegarde de la position geographique
            Point ptStart = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(
                fxNumberValue(uiLongitudeStart),
                fxNumberValue(uiLatitudeStart)
            ));
            Point ptEnd = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(
                fxNumberValue(uiLongitudeEnd),
                fxNumberValue(uiLatitudeEnd)
            ));
            
            if(uiCRSs.getSelectionModel().getSelectedItem() == CRS_WGS84){
                //conversion vers RGF93
                try {
                    final MathTransform trs = CRS.findMathTransform(CRS_WGS84, CRS_RGF93, true);
                    ptStart = (Point) JTS.transform(ptStart, trs);
                    ptEnd = (Point) JTS.transform(ptEnd, trs);
                } catch (FactoryException | TransformException ex) {
                    Symadrem.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                }
            }
            
            //on sauvegarde la position geo
            pos.setPositionDebut(ptStart);
            pos.setPositionFin(ptEnd);
            //on efface la position par rapport aux bornes
            //les propriétés sont bindées            
            pos.borne_debut_avalProperty().set(false);
            pos.borne_fin_avalProperty().set(false);
            pos.borne_debutProperty().set(null);
            pos.borne_finProperty().set(null);
            pos.borne_debut_distanceProperty().set(0);
            pos.borne_fin_distanceProperty().set(0);
        }else{
            //sauvegarde de la position par borne
            //les propriétés sont bindées            
            pos.setPositionDebut(null);
            pos.setPositionFin(null);
        }
    }
    
    private final class ObjectListCell extends ListCell{

        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if(empty){
                setText("");
                return;
            }
            
            if(item instanceof String){
                if(cacheBorneDigue.containsKey(item)) item = cacheBorneDigue.get(item);
                if(cacheSystemeReperage.containsKey(item)) item = cacheSystemeReperage.get(item);
            }
            
            
            if(item instanceof CoordinateReferenceSystem){
                setText(((CoordinateReferenceSystem)item).getName().toString());
            }else if(item instanceof SystemeReperage){
                setText(((SystemeReperage)item).getNom());
            }else if(item instanceof BorneDigue){
                setText(((BorneDigue)item).getNom());
            }else{
                setText("");
            }
                        
        }
    
    }
    
    
}
