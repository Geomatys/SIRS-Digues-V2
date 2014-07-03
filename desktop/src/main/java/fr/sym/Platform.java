

package fr.sym;

import java.io.IOException;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class Platform extends Application {

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        
        // perform initialization and plugin loading tasks
        final Task initTask = new Task() {
            @Override
            protected Object call() throws InterruptedException {
                
                for(int i=0;i<10;i++){
                    Thread.sleep(400);
                    updateProgress(i, 10);
                    updateMessage("Load plugin . . . " + i);
                }
                Thread.sleep(400);
                updateMessage("All plugins loaded.");
                updateProgress(10,10);
                return null;
            }
        };
        
        
        showLoadingStage(initTask);
        new Thread(initTask).start();
        
        showMainStage();
    }
    
    private void showLoadingStage(Task task) throws IOException {
        final Stage splashStage = new Stage();
        
        //task.messageProperty().
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/sym/splashscreen.fxml"));
        final Parent root = loader.load();
        final FXSplashController controller = loader.getController();
//        root.setEffect(new DropShadow());
        
        controller.uiProgressLabel.textProperty().bind(task.messageProperty());
        controller.uiProgressBar.progressProperty().bind(task.progressProperty());
        
        System.out.println(controller);
        
        final Scene scene = new Scene(root);
        scene.setFill(null);
        scene.getStylesheets().add("/fr/sym/splashscreen.css");
        
        
        splashStage.setTitle("Symadrem");
        splashStage.initStyle(StageStyle.TRANSPARENT);
        splashStage.setScene(scene);
        splashStage.show();
    }

    private void showMainStage() {
    }
    
    
}
