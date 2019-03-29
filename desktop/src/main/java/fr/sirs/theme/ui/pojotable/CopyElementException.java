/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.theme.ui.pojotable;

import javafx.scene.control.Alert;

/**
 *
 * @author matthieu
 */
public class CopyElementException extends Exception {

    public CopyElementException() {
        super();
    }

    public CopyElementException(String message) {
        super(message);
    }
    
    public CopyElementException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Permet d'afficher le message de la CopieElementException dans une fenêtre
     * JavaFx.
     */
    public void openAlertWindow() {
        final Alert alert = new Alert(Alert.AlertType.WARNING, this.getMessage());
        alert.setResizable(true);
        alert.showAndWait();
    }
    
    

}
