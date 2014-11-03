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
import fr.symadrem.sirs.core.component.TronconDigueRepository;
import fr.symadrem.sirs.core.model.BorneDigue;
import fr.symadrem.sirs.core.model.Crete;
import fr.symadrem.sirs.core.model.TronconDigue;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
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
    
    private Crete crete;
    private final BooleanProperty disableFields;
    private final BooleanProperty tronconChanged;
    
    private final BorneDigueRepository borneDigueRepository;
    private final TronconDigueRepository tronconDigueRepository;
    
    // Propriétés de Positionnable
    @FXML ComboBox<BorneDigue> uiBorneDebut;
    @FXML FXNumberSpinner uiDistanceDebut;
    @FXML FXNumberSpinner uiPRDebut;
    
    @FXML ComboBox<BorneDigue> uiBorneFin;
    @FXML FXNumberSpinner uiDistanceFin;
    @FXML FXNumberSpinner uiPRFin;
    
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
        borneDigueRepository = session.getBorneDigueRepository();
        tronconDigueRepository = session.getTronconDigueRepository();
    }
    
    public DetailCretePane(final Crete crete){
        this();
        this.crete = crete;
        initFields();
    }
    
    
    
    
    
//    ID_TRONCON_GESTION,
////        DATE_DEBUT_VAL,
////        DATE_FIN_VAL,
////        PR_DEBUT_CALCULE,
////        PR_FIN_CALCULE,
//        ID_SYSTEME_REP,
////        ID_BORNEREF_DEBUT,
//        AMONT_AVAL_DEBUT,
////        DIST_BORNEREF_DEBUT,
////        ID_BORNEREF_FIN,
//        AMONT_AVAL_FIN,
////        DIST_BORNEREF_FIN,
////        COMMENTAIRE,
////        N_COUCHE,
////        ID_TYPE_MATERIAU,
////        ID_TYPE_NATURE,
////        ID_TYPE_FONCTION,
////        EPAISSEUR,
            
            
            
    private void initFields(){
        
        // Propriétés héritées de Positionnable
        final StringConverter<BorneDigue> bornesConverter = new StringConverter<BorneDigue>() {

            @Override
            public String toString(BorneDigue object) {
                if(object == null) return "Pas de borne.";
                return object.getNom()+ " ("+object.getId()+ ")";
            }

            @Override
            public BorneDigue fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        
        final ObservableList<BorneDigue> bornes = FXCollections.observableArrayList();
        BorneDigue borneDebut=null, borneFin=null;
        
        for(final BorneDigue b : borneDigueRepository.getAll()){
            bornes.add(b);
            if(b.getId().equals(crete.getBorne_debut())) borneDebut=b;
            if(b.getId().equals(crete.getBorne_fin())) borneFin=b;
        }
        bornes.add(null);
        
        uiBorneDebut.setConverter(bornesConverter);
        uiBorneDebut.setItems(bornes);
        uiBorneDebut.setValue(borneDebut);
        uiBorneDebut.disableProperty().bind(disableFields);
        
        uiBorneFin.setConverter(bornesConverter);
        uiBorneFin.setItems(bornes);
        uiBorneFin.setValue(borneFin);
        uiBorneFin.disableProperty().bind(disableFields);
        
        uiDistanceDebut.valueProperty().bindBidirectional(crete.borne_debut_distanceProperty());
        uiDistanceDebut.disableProperty().bind(disableFields);
        
        uiPRDebut.valueProperty().bindBidirectional(crete.pR_debutProperty());
        uiPRDebut.disableProperty().bind(disableFields);
        
        uiDistanceFin.valueProperty().bindBidirectional(crete.borne_fin_distanceProperty());
        uiDistanceFin.disableProperty().bind(disableFields);
        
        uiPRFin.valueProperty().bindBidirectional(crete.pR_finProperty());
        uiPRFin.disableProperty().bind(disableFields);
        
        
        
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
            if(t.getId().equals(crete.getTroncon())) troncon=t;
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
        
        uiComment.setHtmlText(crete.getCommentaire());
        uiComment.disableProperty().bind(disableFields);
        
        uiDebut.valueProperty().bindBidirectional(crete.date_debutProperty());
        uiDebut.disableProperty().bind(disableFields);
        
        uiFin.valueProperty().bindBidirectional(crete.date_finProperty());
        uiFin.disableProperty().bind(disableFields);
        
        
        // Propriétés propres à la Crête
        uiEpaisseur.valueProperty().bindBidirectional(crete.epaisseurProperty());
        uiEpaisseur.disableProperty().bind(disableFields);
        
        uiCouches.numberTypeProperty().set(NumberField.NumberType.Integer);
        uiCouches.minValueProperty().set(0);
        uiCouches.valueProperty().bindBidirectional(crete.num_coucheProperty());
        uiCouches.disableProperty().bind(disableFields);
    }

    @Override
    public BooleanProperty disableFieldsProperty() {
        return disableFields;
    }

    @Override
    public void preSave() {
        crete.setCommentaire(uiComment.getHtmlText());
        if(uiBorneDebut.getValue()!=null){
            crete.setBorne_debut(uiBorneDebut.getValue().getId());
        }
        else {
            crete.setBorne_debut(null);
        }
        if(uiBorneFin.getValue()!=null){
            crete.setBorne_fin(uiBorneFin.getValue().getId());
        }
        else {
            crete.setBorne_fin(null);
        }
        if(uiTroncons.getValue()!=null){
            crete.setTroncon(uiTroncons.getValue().getId());
        }
        else {
            crete.setTroncon(null);
        }
    }

    @Override
    public BooleanProperty tronconChangedProperty() {
        return this.tronconChanged;
    }
}
