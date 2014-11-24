
package fr.sirs.theme.ui;

import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.model.LeveeProfilTravers;
import fr.sirs.core.model.ProfilTravers;
import java.lang.reflect.Constructor;
import java.util.Map;
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
import org.geotoolkit.gui.javafx.util.FXDateField;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXThemePane extends BorderPane {
    
    private final Object object;
    private Node specificThemePane;
    protected Map<String, Object> resources;
//    private TronconDigue troncon;
//    private TronconDigue newTroncon = null;
    
    @FXML private ScrollPane uiEditDetailTronconTheme;
      
    @FXML private Label mode;

    @FXML private FXDateField date_maj;

    @FXML private Label id;

    @FXML private BorderPane uiBorderPane;

    @FXML private Label mode1;
    @FXML private Label uiTitleLabel;
    
    @FXML private ToggleButton uiEdit;
    @FXML private ToggleButton uiConsult;
    @FXML private Button uiSave;


    public FXThemePane(final Object theme){
        SIRS.loadFXML(this);
        this.object = theme;
        
        initFields();
        
        initSubPane();
        
        final ToggleGroup group = new ToggleGroup();
        uiConsult.setToggleGroup(group);
        uiEdit.setToggleGroup(group);
        group.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
            if(newValue==null) group.selectToggle(uiConsult);
        });
    }
    
    public FXThemePane(final Object theme, final Map<String, Object> resources){
        SIRS.loadFXML(this);
        this.object = theme;
        
        initFields();
        
        this.resources=resources;
        
        initSubPane();
        
        final ToggleGroup group = new ToggleGroup();
        uiConsult.setToggleGroup(group);
        uiEdit.setToggleGroup(group);
        group.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
            if(newValue==null) group.selectToggle(uiConsult);
        });
    }
    
    
    @FXML
    void save(ActionEvent event) {
        final Session session = Injector.getBean(Session.class);
        
        if(specificThemePane instanceof ThemePane){
            ((ThemePane) specificThemePane).preSave();
            
//            if(((ThemePane) specificThemePane).tronconChangedProperty().get()){
//                ((ThemePane) specificThemePane).tronconChangedProperty().set(false);
//                for(final Objet str : troncon.getStructures()){
//                    if(str.getId().equals(structure.getId())){
//                        troncon.getStructures().remove(str);
//                        break;
//                    }
//                }
//                newTroncon = session.getTronconDigueRepository().get(structure.getTroncon());
//                newTroncon.getStructures().add(structure);
//                structure.setDateMaj(LocalDateTime.now());
//                newTroncon.setDateMaj(LocalDateTime.now());
//                session.getTronconDigueRepository().update(newTroncon);
//            } else{
//                for(final Objet str : troncon.getStructures()){
//                    if(str.getId().equals(structure.getId())){
//                        troncon.getStructures().set(troncon.getStructures().indexOf(str), structure);
//                        break;
//                    }
//                }
//            }
        }
        else {
            throw new UnsupportedOperationException("The sub-pane must implement "+ThemePane.class.getCanonicalName()+" interface.");
        }
        
//        troncon.setDateMaj(LocalDateTime.now());
//        session.getTronconDigueRepository().update(troncon);
//        
//        if(newTroncon!=null){
//            troncon=newTroncon;
//            newTroncon=null;
//        }
    }
    
    @FXML
    private void showOnMap(){
//        final Session session = Injector.getBean(Session.class);
//        final FXMapTab tab = session.getFrame().getMapTab();
//        tab.show();
//        final FXMap map = tab.getMap().getUiMap();
//        try {
//            map.getCanvas().setVisibleArea(JTS.toEnvelope(structure.getGeometry()));
//        } catch (NoninvertibleTransformException | TransformException ex) {
//            SIRS.LOGGER.log(Level.WARNING, ex.getMessage(),ex);
//        }
    }
    
    private void initFields(){
        uiTitleLabel.setText("Information sur une instance de "+object.getClass().getSimpleName());
        if(object instanceof ProfilTravers){
            id.setText(((ProfilTravers)object).getId());
            date_maj.valueProperty().bindBidirectional(((ProfilTravers)object).dateMajProperty());
        } else if (object instanceof LeveeProfilTravers){
            id.setText(((LeveeProfilTravers)object).getId());
            date_maj.valueProperty().bindBidirectional(((LeveeProfilTravers)object).dateMajProperty());
        }
        date_maj.setDisable(true);
    }

    private void initSubPane() {
        
        try{
            // Choose the pane adapted to the specific structure.
            final String className = "fr.sirs.theme.ui.FX"+object.getClass().getSimpleName()+"SubPane";
            final Class controllerClass = Class.forName(className);
            final Constructor cstr;
            if(resources==null){
                cstr = controllerClass.getConstructor(object.getClass());
                specificThemePane = (Node) cstr.newInstance(object);
            } else {
                cstr = controllerClass.getConstructor(object.getClass(), Map.class);
                specificThemePane = (Node) cstr.newInstance(object, resources);
            }
            
            uiEditDetailTronconTheme.setContent(specificThemePane);
        }catch(Exception ex){
            throw new UnsupportedOperationException("Failed to load panel : "+ex.getMessage(),ex);
        }
        
        //mode edition
        final BooleanBinding editBind = uiEdit.selectedProperty().not();
        uiSave.disableProperty().bind(editBind);
        
        if(specificThemePane instanceof ThemePane){
            ((ThemePane) specificThemePane).disableFieldsProperty().bind(editBind);
        }
        else {
            throw new UnsupportedOperationException("The sub-pane must implement "+ThemePane.class.getCanonicalName()+" interface.");
        }
    }
    
}
