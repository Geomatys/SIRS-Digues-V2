package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

/**
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 * @author Samuel Andrés (Geomatys)
 */
public class FXUtilisateurPane extends AbstractFXElementPane<Utilisateur> {

    private final MessageDigest messageDigest;
    private final BooleanProperty administrableProperty = new SimpleBooleanProperty(this, "administrableProperty", false);

    // Propriétés de Utilisateur
    @FXML TextField ui_login;
    @FXML Label ui_labelLogin;
    @FXML PasswordField ui_password;
    @FXML PasswordField ui_passwordConfirm;
    @FXML Label ui_labelConfirm;
    @FXML TextField ui_role;

    /**
     * Constructor. Initialize part of the UI which will not require update when
     * element edited change.
     */
    private FXUtilisateurPane() throws NoSuchAlgorithmException {
        SIRS.loadFXML(this, Utilisateur.class);
        elementProperty().addListener((ObservableValue<? extends Utilisateur> observable, Utilisateur oldValue, Utilisateur newValue) -> {
            initFields();
        });
        messageDigest = MessageDigest.getInstance("MD5");

    }

    public FXUtilisateurPane(final Utilisateur utilisateur) throws NoSuchAlgorithmException {
        this(utilisateur, false);
    }

    public FXUtilisateurPane(final Utilisateur utilisateur, final boolean administrable) throws NoSuchAlgorithmException {
        this();
        this.elementProperty().set(utilisateur);
        this.administrableProperty().set(administrable);
    }
    
    public BooleanProperty administrableProperty(){return administrableProperty;}
    public boolean isAdministrable(){return administrableProperty.get();}
    public void setAdministrable(final boolean administrable){
        administrableProperty.set(administrable);
    }

    /**
     * Initialize fields at element setting.
     */
    private void initFields() {

        final Utilisateur element = (Utilisateur) elementProperty().get();

        /*
         * Bind control properties to Element ones.
         */
        // Propriétés de Utilisateur
        // * login
        ui_login.textProperty().bindBidirectional(element.loginProperty());
        ui_login.disableProperty().bind(disableFieldsProperty());
        ui_login.editableProperty().bind(administrableProperty());

        // * password
        ui_password.setText("");
        ui_passwordConfirm.setText("");

        ui_password.disableProperty().bind(disableFieldsProperty());
        ui_passwordConfirm.disableProperty().bind(disableFieldsProperty());

        // * role
        ui_role.textProperty().bindBidirectional(element.roleProperty());
        ui_role.disableProperty().bind(disableFieldsProperty());
        ui_role.editableProperty().bind(administrableProperty());

    }

    private String digest(final String toEncrypt) {
        if (messageDigest == null) {
            return toEncrypt;
        } else {
            return new String(messageDigest.digest((toEncrypt).getBytes()));
        }
    }

    @Override
    public void preSave() throws Exception {
        
        // Vérification de l'identifiant.
        if(ui_login == null 
                || ui_login.getText()==null 
                || "".equals(ui_login.getText())){
            ui_labelLogin.setTextFill(Color.RED);
            new Alert(Alert.AlertType.INFORMATION, "Vous devez renseigner l'identifiant.", ButtonType.CLOSE).showAndWait();
            throw new Exception("L'identifiant utilisateur n'a pas été renseigné ! Modification non enregistrée.");
        }
        else{
            final List<Utilisateur> utilisateurs = Injector.getSession().getUtilisateurRepository().getAll();
            for(final Utilisateur utilisateur : utilisateurs){
                if(ui_login.getText().equals(utilisateur.getLogin())){
                    ui_labelLogin.setTextFill(Color.RED);
                    new Alert(Alert.AlertType.INFORMATION, "L'identifiant "+ui_login.getText()+" existe déjà dans la base locale.", ButtonType.CLOSE).showAndWait();
                    throw new Exception("L'identifiant "+ui_login.getText()+" existe déjà dans la base locale. ! Modification non enregistrée.");
                }
            }
            ui_labelLogin.setTextFill(Color.BLACK);
        }
        
        // Vérification du mot de passe.
        if(ui_password == null
                || ui_passwordConfirm == null
                || !ui_password.getText().equals(ui_passwordConfirm.getText())){
            ui_labelConfirm.setTextFill(Color.RED);
            new Alert(Alert.AlertType.INFORMATION, "Le mot de passe et sa confirmation ne correspondent pas.", ButtonType.CLOSE).showAndWait();
            throw new Exception("Les mots de passe ne correspondent pas ! Modification non enregistrée.");
        } 
        else{
            elementProperty.get().setPassword(digest(ui_password.getText()));
            ui_labelConfirm.setTextFill(Color.BLACK);
        }
    }
}
