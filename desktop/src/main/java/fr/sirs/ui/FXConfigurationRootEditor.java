/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.ui;

import fr.sirs.SIRS;
import fr.sirs.core.SirsCore;
import fr.sirs.util.SaveableConfiguration;
import fr.sirs.util.property.ConfigurationRoot;
import java.io.File;
import java.util.prefs.BackingStoreException;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.util.logging.Level;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.scene.control.Label;

/**
 * An editor to update configuration root path.
 *
 * @author maximegavens
 */
public class FXConfigurationRootEditor extends BorderPane implements SaveableConfiguration {
    
    @FXML
    public TextField rootFolderField;
    
    @FXML
    private Button chooRootButton;
    
    @FXML
    private Label warningLabel;
    
    public FXConfigurationRootEditor() {
        SIRS.loadFXML(this);

        final String oldRoot = ConfigurationRoot.getRoot();
        rootFolderField.setText(oldRoot);
        rootFolderField.setDisable(true);
        
        warningLabel.setVisible(false);
    }
    
    @FXML
    public void chooseRootFile(ActionEvent event) {
        
        final DirectoryChooser fileChooser = new DirectoryChooser();
        final File file = fileChooser.showDialog(null);
        if (file != null) {
            final String oldPath = ConfigurationRoot.getRoot();
            final String newPath = file.getPath();
            rootFolderField.setText(newPath);
            if (!oldPath.equals(newPath)) {
                SirsCore.LOGGER.log(Level.INFO, "The new path is different, need to reload the application.");
                warningLabel.setVisible(true);
            } else {
                warningLabel.setVisible(false);
            }
        }
    }

    @Override
    public String getTitle() {
        return "Répertoire de configuration";
    }

    @Override
    public void save() throws BackingStoreException, IOException {
        final String oldRoot = ConfigurationRoot.getRoot();
        final String confRootStr = rootFolderField.getText();
        if (confRootStr == null || confRootStr.isEmpty()) {
            throw new RuntimeException("Unexpected behaviour. The configuration directory text field can't be null or empty.");
        } else {
            ConfigurationRoot.setRootAndMove(oldRoot, confRootStr);
        }

        ConfigurationRoot.flush();
        if (!oldRoot.equals(confRootStr)) {
            SirsCore.LOGGER.log(Level.INFO, "The configuration folder path has changed. Application needs to reload global variables");
            System.exit(0);
        }
    }
}
