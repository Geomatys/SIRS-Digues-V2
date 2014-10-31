/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.theme.detail;

import fr.sym.Symadrem;
import fr.symadrem.sirs.core.model.Crete;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.HTMLEditor;
import org.geotoolkit.gui.javafx.util.FXDateField;
import org.geotoolkit.gui.javafx.util.FXNumberSpinner;

/**
 *
 * @author Samuel Andrés
 */
public class DetailCretePane extends BorderPane {
    
    private Crete crete;
    
    @FXML HTMLEditor uiComment;
    @FXML FXDateField uiDebut;
    @FXML FXDateField uiFin;
    @FXML FXNumberSpinner uiEpaisseur;
    
    private DetailCretePane(){
        Symadrem.loadFXML(this);
    }
    
    public DetailCretePane(final Crete crete){
        this();
        this.crete = crete;
        initFields();
    }
    
    
    
    
    
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
        uiDebut.valueProperty().bindBidirectional(crete.date_debutProperty());
        uiFin.valueProperty().bindBidirectional(crete.date_finProperty());
        uiEpaisseur.valueProperty().bindBidirectional(crete.epaisseurProperty());
    }
}
