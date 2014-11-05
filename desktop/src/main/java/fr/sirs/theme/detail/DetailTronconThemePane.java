/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.theme.detail;

import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.digue.Injector;
import fr.sirs.map.FXMapTab;
import fr.sirs.core.model.Crete;
import fr.sirs.core.model.Structure;
import fr.sirs.core.model.TronconDigue;
import java.awt.geom.NoninvertibleTransformException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.util.FXDateField;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author Samuel Andrés
 */
public class DetailTronconThemePane extends BorderPane {
    
    private final Structure structure;
    private Node specificThemePane;
    private TronconDigue troncon;
    private TronconDigue newTroncon = null;
    
    @FXML private ScrollPane uiEditDetailTronconTheme;
      
    @FXML
    private Label mode;

    @FXML
    private FXDateField date_maj;

    @FXML
    private Label id;

    @FXML
    private BorderPane uiBorderPane;

    @FXML
    private Label mode1;
    
    @FXML private ToggleButton uiEdit;
    @FXML private ToggleButton uiConsult;
    @FXML private Button uiSave;


    @FXML
    void save(ActionEvent event) {
        
        final Session session = Injector.getBean(Session.class);
        
        if(specificThemePane instanceof DetailThemePane){
            ((DetailThemePane) specificThemePane).preSave();
            
            if(((DetailThemePane) specificThemePane).tronconChangedProperty().get()){
                ((DetailThemePane) specificThemePane).tronconChangedProperty().set(false);
                for(final Structure str : troncon.getStructures()){
                    if(str.getId().equals(structure.getId())){
                        troncon.getStructures().remove(str);
                        break;
                    }
                }
                newTroncon = session.getTronconDigueRepository().get(structure.getTroncon());
                newTroncon.getStructures().add(structure);
                structure.setDateMaj(LocalDateTime.now());
                newTroncon.setDateMaj(LocalDateTime.now());
                session.getTronconDigueRepository().update(newTroncon);
            } else{
                for(final Structure str : troncon.getStructures()){
                    if(str.getId().equals(structure.getId())){
                        troncon.getStructures().set(troncon.getStructures().indexOf(str), structure);
                        break;
                    }
                }
            }
        }
        else {
            throw new UnsupportedOperationException("The sub-pane must implement "+DetailThemePane.class.getCanonicalName()+" interface.");
        }
        
        troncon.setDateMaj(LocalDateTime.now());
        session.getTronconDigueRepository().update(troncon);
        
        if(newTroncon!=null){
            troncon=newTroncon;
            newTroncon=null;
        }
    }
    
    @FXML
    private void showOnMap(){
        final Session session = Injector.getBean(Session.class);
        final FXMapTab tab = session.getFrame().getMapTab();
        tab.show();
        final FXMap map = tab.getMap().getUiMap();
        try {
            map.getCanvas().setVisibleArea(JTS.toEnvelope(structure.getGeometry()));
        } catch (NoninvertibleTransformException | TransformException ex) {
            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
        }
    }
    
    
    public DetailTronconThemePane(final Structure structure){
        SIRS.loadFXML(this);
        this.structure = structure;
        final Session session = Injector.getBean(Session.class);
        troncon = session.getTronconDigueRepository().get(structure.getTroncon());
        
        initFields();
        
        initSubPane();
        
        final ToggleGroup group = new ToggleGroup();
        uiConsult.setToggleGroup(group);
        uiEdit.setToggleGroup(group);
        group.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
            if(newValue==null) group.selectToggle(uiConsult);
        });
    }
    
    
    private void initFields(){
        id.setText(structure.getId());
        date_maj.valueProperty().bindBidirectional(structure.dateMajProperty());
        date_maj.setDisable(true);
    }

    private void initSubPane() {
        
        // Choose the pane adapted to the specific structure.
        if(structure instanceof Crete) {
            specificThemePane = new DetailCretePane((Crete) structure);
        }
        else {
            throw new UnsupportedOperationException("Unknown theme class.");
        }
        
        uiEditDetailTronconTheme.setContent(specificThemePane);
        
        //mode edition
        final BooleanBinding editBind = uiEdit.selectedProperty().not();
        uiSave.disableProperty().bind(editBind);
        
        if(specificThemePane instanceof DetailThemePane){
            ((DetailThemePane) specificThemePane).disableFieldsProperty().bind(editBind);
        }
        else {
            throw new UnsupportedOperationException("The sub-pane must implement "+DetailThemePane.class.getCanonicalName()+" interface.");
        }
    }
    
}
