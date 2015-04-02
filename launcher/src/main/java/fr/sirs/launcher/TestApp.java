package fr.sirs.launcher;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.linearref.LengthLocationMap;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.math.Vector2D;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import fr.sirs.SIRS;
import fr.sirs.util.FXDirectoryTextField;
import fr.sirs.util.FXFileTextField;
import fr.sirs.util.property.SirsPreferences;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import org.apache.sis.geometry.Envelope2D;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.CommonCRS;
import org.geotoolkit.display2d.GO2Utilities;
import org.geotoolkit.internal.Loggers;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class TestApp extends Application {

        @Override
        public void start(Stage primaryStage) throws Exception {
            
            ObservableList<String> myList = FXCollections.observableArrayList("first", "second", "third", "fourth");
            final ComboBox myCBox = new ComboBox();
            myCBox.setItems(myList);

            SIRS.initCombo(myCBox, myList, "first");
            
//            FXDirectoryTextField dirField = new FXDirectoryTextField();
//            dirField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
//                SirsPreferences.INSTANCE.setProperty(SirsPreferences.PROPERTIES.DOCUMENT_ROOT.name(), newValue);
//            });
            
            BorderPane borderPane = new BorderPane(myCBox);
            //borderPane.setTop(dirField);
            final Stage stage = new Stage();
            stage.setScene(new Scene(borderPane));
            stage.setWidth(400);
            stage.setHeight(600);
            stage.setTitle("test stage");
            stage.setOnCloseRequest((WindowEvent event) -> {
                System.exit(0);
            });
            stage.show();
            
        }

        public static void main(final String[] args) {
            launch(args);
        }
        
        public static void testLinear() {
            final Point pt = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(10, 10));
            final LineString segment = GO2Utilities.JTS_FACTORY.createLineString(new Coordinate[]{new Coordinate(7, 6), new Coordinate(12, 4)});
            Coordinate[] nearestPoints = DistanceOp.nearestPoints(pt, segment);
            for (int i = 0; i < nearestPoints.length; i++) {
                Loggers.REFERENCING.info("Index "+i+" : "+nearestPoints[i]);
            }
            Loggers.REFERENCING.info(GO2Utilities.JTS_FACTORY.createLineString(nearestPoints).toText());
            
            final Coordinate projectedPt = nearestPoints[1];
            
            LineString subSegment = GO2Utilities.JTS_FACTORY.createLineString(new Coordinate[]{projectedPt, segment.getEndPoint().getCoordinate()});
            
            LengthIndexedLine index = new LengthIndexedLine(segment);
            Loggers.REFERENCING.info("Distance of projected point from start : "+index.indexOf(projectedPt));
            Loggers.REFERENCING.info("Computed from vector : "+new Vector2D(segment.getStartPoint().getCoordinate(), projectedPt).length());            
        }    
}
