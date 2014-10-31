/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.theme.detail;

import fr.sym.Symadrem;
import fr.symadrem.sirs.core.model.Crete;
import fr.symadrem.sirs.core.model.Structure;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Samuel Andrés
 */
public class DetailTronconThemePane extends BorderPane {
    
    @FXML private VBox uiEditDetailTronconTheme;
    
    public DetailTronconThemePane(final Structure structure){
        Symadrem.loadFXML(this);
        if(structure instanceof Crete) uiEditDetailTronconTheme.getChildren().add(new DetailCretePane((Crete) structure));
    }
    
}
