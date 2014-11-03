/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.theme.detail;

import fr.sym.Session;
import fr.sym.Symadrem;
import fr.sym.digue.Injector;
import fr.symadrem.sirs.core.component.BorneDigueRepository;
import fr.symadrem.sirs.core.component.SystemeReperageRepository;
import fr.symadrem.sirs.core.component.TronconDigueRepository;
import fr.symadrem.sirs.core.model.BorneDigue;
import fr.symadrem.sirs.core.model.Crete;
import fr.symadrem.sirs.core.model.SystemeReperage;
import fr.symadrem.sirs.core.model.TronconDigue;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.HTMLEditor;
import javafx.util.StringConverter;
import jidefx.scene.control.field.NumberField;
import org.geotoolkit.gui.javafx.util.FXDateField;
import org.geotoolkit.gui.javafx.util.FXNumberSpinner;

/**
 *
 * @author Samuel Andrés
 */
public class DetailCretePane extends BorderPane implements DetailThemePane {
    
    private final ObjectProperty<Crete> crete;
    private final BooleanProperty disableFields;
    private final BooleanProperty tronconChanged;
    
    private final TronconDigueRepository tronconDigueRepository;
    
    // Propriétés de Positionnable
    @FXML DetailPositionnablePane uiPositionnable;
    
    // Propriétés de Structure
    @FXML HTMLEditor uiComment;
    @FXML FXDateField uiDebut;
    @FXML FXDateField uiFin;
    @FXML ComboBox<TronconDigue> uiTroncons;
    
    // Propriétés de Crête
    @FXML FXNumberSpinner uiEpaisseur;
    @FXML FXNumberSpinner uiCouches;
    
    private DetailCretePane(){
        Symadrem.loadFXML(this);
        disableFields = new SimpleBooleanProperty();
        tronconChanged = new SimpleBooleanProperty(false);
        final Session session = Injector.getBean(Session.class);
        tronconDigueRepository = session.getTronconDigueRepository();
        this.crete = new SimpleObjectProperty<>();
    }
    
    public DetailCretePane(final Crete crete){
        this();
        this.crete.set(crete);
        initFields();
    }       
            
    private void initFields(){
        
        // Propriétés héritées de Positionnable
        uiPositionnable.positionableProperty().bindBidirectional(crete);
        
        // Propriétés héritées de Structure
        final StringConverter<TronconDigue> tronconsConverter = new StringConverter<TronconDigue>() {

            @Override
            public String toString(TronconDigue object) {
                if(object == null) return "Pas de tronçon.";
                return object.getNom()+ " ("+object.getId()+ ")";
            }

            @Override
            public TronconDigue fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        
        final ObservableList<TronconDigue> troncons = FXCollections.observableArrayList();
        TronconDigue troncon=null;
        
        for(final TronconDigue t : tronconDigueRepository.getAll()){
            troncons.add(t);
            if(t.getId().equals(crete.get().getTroncon())) troncon=t;
        }
        troncons.add(null);
        uiTroncons.setConverter(tronconsConverter);
        uiTroncons.setItems(troncons);
        uiTroncons.setValue(troncon);
        uiTroncons.disableProperty().bind(disableFields);
        uiTroncons.valueProperty().addListener(new ChangeListener<TronconDigue>() {

            @Override
            public void changed(ObservableValue<? extends TronconDigue> observable, TronconDigue oldValue, TronconDigue newValue) {
                tronconChanged.set(true);
            }
        });
        
        uiComment.setHtmlText(crete.get().getCommentaire());
        uiComment.disableProperty().bind(disableFields);
        
        uiDebut.valueProperty().bindBidirectional(crete.get().date_debutProperty());
        uiDebut.disableProperty().bind(disableFields);
        
        uiFin.valueProperty().bindBidirectional(crete.get().date_finProperty());
        uiFin.disableProperty().bind(disableFields);
        
        
        // Propriétés propres à la Crête
        uiEpaisseur.valueProperty().bindBidirectional(crete.get().epaisseurProperty());
        uiEpaisseur.disableProperty().bind(disableFields);
        
        uiCouches.numberTypeProperty().set(NumberField.NumberType.Integer);
        uiCouches.minValueProperty().set(0);
        uiCouches.valueProperty().bindBidirectional(crete.get().num_coucheProperty());
        uiCouches.disableProperty().bind(disableFields);
    }

    @Override
    public BooleanProperty disableFieldsProperty() {
        return disableFields;
    }

    @Override
    public void preSave() {
        crete.get().setCommentaire(uiComment.getHtmlText());
        if(uiTroncons.getValue()!=null){
            crete.get().setTroncon(uiTroncons.getValue().getId());
        }
        else {
            crete.get().setTroncon(null);
        }
    }

    @Override
    public BooleanProperty tronconChangedProperty() {
        return this.tronconChanged;
    }
}
