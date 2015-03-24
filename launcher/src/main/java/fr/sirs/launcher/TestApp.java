package fr.sirs.launcher;

import fr.sirs.util.FXDirectoryTextField;
import fr.sirs.util.FXFileTextField;
import fr.sirs.util.property.SirsPreferences;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class TestApp extends Application {

        @Override
        public void start(Stage primaryStage) throws Exception {
            FXDirectoryTextField dirField = new FXDirectoryTextField();
            dirField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                SirsPreferences.INSTANCE.setProperty(SirsPreferences.PROPERTIES.DOCUMENT_ROOT.name(), newValue);
            });
            BorderPane borderPane = new BorderPane(new FXFileTextField());
            borderPane.setTop(dirField);
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
    
}
