/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.plugin.document.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.plugin.document.DocumentManagementTheme;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.geotoolkit.util.FileUtilities;

/**
 *
 * @author guilhem
 */
public class DocumentsPane extends GridPane {
    
    @FXML
    private Button importDocButton;

    @FXML
    private Button deleteDocButton;

    @FXML
    private Button setFolderButton;

    @FXML
    private TreeTableView<?> tree1;

    @FXML
    private Button addDocButton;

    @FXML
    private Button addFolderButton;

    @FXML
    private Button listButton;

    private static final Image ADDF_BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/add_folder.png"));
    private static final Image ADDD_BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/add_doc.png"));
    private static final Image IMP_BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/import.png"));
    private static final Image DEL_BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/remove.png"));
    private static final Image SET_BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/set.png"));
    private static final Image LIST_BUTTON_IMAGE = new Image(DocumentManagementTheme.class.getResourceAsStream("images/list.png"));
    
    
    public DocumentsPane() {
        SIRS.loadFXML(this, DocumentsPane.class);
        Injector.injectDependencies(this);
        
        addFolderButton.setGraphic(new ImageView(ADDF_BUTTON_IMAGE));
        importDocButton.setGraphic(new ImageView(IMP_BUTTON_IMAGE));
        deleteDocButton.setGraphic(new ImageView(DEL_BUTTON_IMAGE));
        setFolderButton.setGraphic(new ImageView(SET_BUTTON_IMAGE));
        addDocButton.setGraphic(new ImageView(ADDD_BUTTON_IMAGE));
        listButton.setGraphic(new ImageView(LIST_BUTTON_IMAGE));
        
        
        
        
    }
    
}
