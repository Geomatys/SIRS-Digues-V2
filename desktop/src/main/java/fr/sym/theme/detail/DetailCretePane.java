/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.theme.detail;

import fr.sym.Symadrem;
import fr.symadrem.sirs.core.model.Crete;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author samuel
 */
public class DetailCretePane extends BorderPane {
    
    private DetailCretePane(){
        Symadrem.loadFXML(this);
    }
    
    public DetailCretePane(final Crete crete){
        
    }
}
