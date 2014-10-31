/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.theme.detail;

import fr.sym.Symadrem;
import fr.symadrem.sirs.core.model.Crete;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.HTMLEditor;
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
    
    @FXML HTMLEditor uiComment;
    @FXML FXDateField uiDebut;
    @FXML FXDateField uiFin;
    @FXML FXNumberSpinner uiEpaisseur;
    
    @FXML ComboBox<String> uiBorneDebut;
    @FXML FXNumberSpinner uiDistanceDebut;
    @FXML FXNumberSpinner uiPRDebut;
    
    @FXML ChoiceBox<String> uiBorneFin;
    @FXML FXNumberSpinner uiDistanceFin;
    @FXML FXNumberSpinner uiPRFin;
    
    @FXML FXNumberSpinner uiCouches;
    
    private DetailCretePane(){
        Symadrem.loadFXML(this);
        disableFields = new SimpleBooleanProperty();
    }
    
    public DetailCretePane(final Crete crete){
        this();
        this.crete = crete;
        initFields();
    }
    
    
    
    
//    
//    ID_TRONCON_GESTION,
////        DATE_DEBUT_VAL,
////        DATE_FIN_VAL,
//        PR_DEBUT_CALCULE,
//        PR_FIN_CALCULE,
//        ID_SYSTEME_REP,
//        ID_BORNEREF_DEBUT,
//        AMONT_AVAL_DEBUT,
//        DIST_BORNEREF_DEBUT,
//        ID_BORNEREF_FIN,
//        AMONT_AVAL_FIN,
//        DIST_BORNEREF_FIN,
////        COMMENTAIRE,
//        N_COUCHE,
////        ID_TYPE_MATERIAU,
////        ID_TYPE_NATURE,
////        ID_TYPE_FONCTION,
////        EPAISSEUR,
            
            
            
    private void initFields(){
        uiComment.setHtmlText(crete.getCommentaire());
        uiComment.disableProperty().bind(disableFields);
        
        uiDebut.valueProperty().bindBidirectional(crete.date_debutProperty());
        uiDebut.disableProperty().bind(disableFields);
        
        uiFin.valueProperty().bindBidirectional(crete.date_finProperty());
        uiFin.disableProperty().bind(disableFields);
        
        uiEpaisseur.valueProperty().bindBidirectional(crete.epaisseurProperty());
        uiEpaisseur.disableProperty().bind(disableFields);
        
        uiBorneDebut.setValue(crete.getBorne_debut());
        uiBorneDebut.disableProperty().bind(disableFields);
        
        uiDistanceDebut.valueProperty().bindBidirectional(crete.borne_debut_distanceProperty());
        uiDistanceDebut.disableProperty().bind(disableFields);
        
        uiPRDebut.valueProperty().bindBidirectional(crete.pR_debutProperty());
        uiPRDebut.disableProperty().bind(disableFields);
        
        uiBorneFin.setValue(crete.getBorne_fin());
        uiBorneFin.disableProperty().bind(disableFields);
        
        uiDistanceFin.valueProperty().bindBidirectional(crete.borne_fin_distanceProperty());
        uiDistanceFin.disableProperty().bind(disableFields);
        
        uiPRFin.valueProperty().bindBidirectional(crete.pR_finProperty());
        uiPRFin.disableProperty().bind(disableFields);
        
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
    }
}
