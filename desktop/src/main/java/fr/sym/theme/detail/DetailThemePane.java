/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.theme.detail;

import javafx.beans.property.BooleanProperty;

/**
 *
 * @author Samuel Andrés
 */
public interface DetailThemePane {
    
    /**
     * Binds enabling/disabling state of Pane edition elements.
     * @return 
     */
    BooleanProperty disableFieldsProperty();
    
    /**
     * Detects if the troncon has changed.
     * @return 
     */
    BooleanProperty tronconChangedProperty();
    
    /**
     * Record unbinded fields changes before saving.
     */
    void preSave();
}
