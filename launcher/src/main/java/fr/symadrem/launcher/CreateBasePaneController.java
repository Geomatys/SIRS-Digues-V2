/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.symadrem.launcher;

import com.healthmarketscience.jackcess.DatabaseBuilder;
import fr.sym.util.importer.AccessDbImporterException;
import fr.sym.util.importer.DbImporter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.ektorp.CouchDbConnector;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class CreateBasePaneController implements Initializable {

    @FXML private Button avortButton;
    @FXML private Button chooseMainDbButton;
    @FXML private Button chooseCartoDbButton;
    
    @FXML private TextField mainDbPath;
    @FXML private TextField cartoDbPath;
    @FXML private Button validateButton;
    
    private File mainDbFile;
    private File cartoDbFile;
    
    @FXML
    private void validate(final ActionEvent event) throws IOException, AccessDbImporterException{
        System.out.println("validation");
        final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:/symadrem/spring/import-context.xml");
        final CouchDbConnector couchDbConnector = applicationContext.getBean(CouchDbConnector.class);
        DbImporter importer = new DbImporter(couchDbConnector);
        importer.setDatabase(DatabaseBuilder.open(mainDbFile),
                DatabaseBuilder.open(cartoDbFile));
        importer.cleanDb();
        importer.importation();
        ((Stage) validateButton.getScene().getWindow()).close();
    }
    
    @FXML
    private void avort(final ActionEvent event){
        System.out.println("Annulation");
        ((Stage) avortButton.getScene().getWindow()).close();
    }
    
    @FXML
    private void chooseMainDb(final ActionEvent event){
        final FileChooser fileChooser = new FileChooser();
        mainDbFile = fileChooser.showOpenDialog(chooseMainDbButton.getScene().getWindow());
        mainDbPath.setText(mainDbFile.getAbsolutePath());
    }
    
    @FXML
    private void chooseCartoDb(final ActionEvent event){
        final FileChooser fileChooser = new FileChooser();
        cartoDbFile = fileChooser.showOpenDialog(chooseMainDbButton.getScene().getWindow());
        cartoDbPath.setText(cartoDbFile.getAbsolutePath());
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        
        System.out.println("Initialise !");
        System.out.println(resources);
        
        
        
    }
}
