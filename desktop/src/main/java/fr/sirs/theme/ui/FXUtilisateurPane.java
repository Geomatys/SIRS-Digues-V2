package fr.sirs.theme.ui;

import fr.sirs.SIRS;
import static fr.sirs.SIRS.hexaMD5;
import fr.sirs.Session;
import fr.sirs.core.component.UtilisateurRepository;
import fr.sirs.core.model.*;
import fr.sirs.util.SirsStringConverter;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 * @author Samuel Andrés (Geomatys)
 */
public class FXUtilisateurPane extends AbstractFXElementPane<Utilisateur> {

    private final BooleanProperty administrableProperty = new SimpleBooleanProperty(this, "administrableProperty", false);

    @Autowired
    private Session session;

    @Autowired
    private UtilisateurRepository repository;

    // Propriétés de Utilisateur
    @FXML TextField ui_login;
    @FXML Label ui_labelLogin;
    @FXML PasswordField ui_password;
    @FXML PasswordField ui_passwordConfirm;
    @FXML Label ui_labelConfirm;
    @FXML ComboBox<Role> ui_role;
    @FXML CheckBox ui_passwordChange;

    private String currentEditedUserLogin;

    /**
     * Constructor. Initialize part of the UI which will not require update when
     * element edited change.
     */
    private FXUtilisateurPane() throws NoSuchAlgorithmException {
        this(null, false);
    }

    public FXUtilisateurPane(final Utilisateur utilisateur) throws NoSuchAlgorithmException {
        this(utilisateur, false);
    }

    public FXUtilisateurPane(final Utilisateur utilisateur, final boolean administrable) throws NoSuchAlgorithmException {

        SIRS.loadFXML(this, Utilisateur.class);

        elementProperty().addListener(this::initFields);

        this.elementProperty().set(utilisateur);
        if(utilisateur!=null){
            currentEditedUserLogin = utilisateur.getLogin();
        }
        administrableProperty().set(administrable);

        ui_role.disableProperty().bind(new SecurityBinding());
        ui_login.disableProperty().bind(disableFieldsProperty());
        ui_login.editableProperty().bind(administrableProperty());
        ui_passwordChange.disableProperty().bind(disableFieldsProperty());
        ui_password.disableProperty().bind(disableFieldsProperty());
        ui_passwordConfirm.disableProperty().bind(disableFieldsProperty());
        ui_password.editableProperty().bind(ui_passwordChange.selectedProperty());
        ui_passwordConfirm.editableProperty().bind(ui_passwordChange.selectedProperty());

        ui_role.getItems().addAll(Role.values());
        ui_role.setConverter(new SirsStringConverter());
    }

    public final BooleanProperty administrableProperty(){return administrableProperty;}
    public boolean isAdministrable(){return administrableProperty.get();}
    public void setAdministrable(final boolean administrable){
        administrableProperty.set(administrable);
    }

    /**
     * Initialize fields at element setting.
     */
    private void initFields(ObservableValue<? extends Utilisateur> observable, Utilisateur oldValue, Utilisateur newValue) {
        if (oldValue != null) {
            ui_login.textProperty().unbindBidirectional(oldValue.loginProperty());
            ui_role.valueProperty().unbindBidirectional(oldValue.roleProperty());
        }

        // * password
        ui_password.setText("");
        ui_passwordConfirm.setText("");

        if (newValue == null) return;
        /*
         * Bind control properties to Element ones.
         */
        // Propriétés de Utilisateur
        // * login
        ui_login.textProperty().bindBidirectional(newValue.loginProperty());

        // * role
        ui_role.valueProperty().bindBidirectional(newValue.roleProperty());
    }

    private class SecurityBinding extends BooleanBinding{

        SecurityBinding(){
            super.bind(disableFieldsProperty(), elementProperty(), session.utilisateurProperty());
        }
        @Override
        protected boolean computeValue() {
            return disableFieldsProperty().get() || elementProperty().get().equals(session.utilisateurProperty().get());
        }

    }

    @Override
    public void preSave() throws Exception {
        final String inputLogin = ui_login.getText();
        // Interdiction d'un indentifiant vide.
        if(ui_login == null
                || inputLogin==null
                || "".equals(inputLogin)){
            ui_labelLogin.setTextFill(Color.RED);
            final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Vous devez renseigner l'identifiant.", ButtonType.CLOSE);
            alert.setResizable(true);
            alert.showAndWait();
            throw new Exception("L'identifiant utilisateur n'a pas été renseigné ! Modification non enregistrée.");
        }

        // Sinon, si on est susceptible d'avoir modifié le login.
        else if(!inputLogin.equals(currentEditedUserLogin)){

            repository.clearCache();
            final List<Utilisateur> utilisateurs = repository.getByLogin(inputLogin);
            for(final Utilisateur utilisateur : utilisateurs){
                if(inputLogin.equals(utilisateur.getLogin())){
                    ui_labelLogin.setTextFill(Color.RED);
                    final Alert alert = new Alert(Alert.AlertType.INFORMATION, "L'identifiant "+inputLogin+" existe déjà dans la base locale.", ButtonType.CLOSE);
                    alert.setResizable(true);
                    alert.showAndWait();
                    throw new Exception("L'identifiant "+inputLogin+" existe déjà dans la base locale. ! Modification non enregistrée.");
                }
            }
            ui_labelLogin.setTextFill(Color.BLACK);
            currentEditedUserLogin = inputLogin;
            elementProperty.get().setLogin(inputLogin);
        }

        // Vérification du mot de passe.
        if(ui_passwordChange.isSelected()){// On vérifie que l'utilisateur a bien spécifié explicitement qu'il désirait changer de mot de passe.
            if(ui_password == null
                    || ui_passwordConfirm == null
                    || !ui_password.getText().equals(ui_passwordConfirm.getText())){
                ui_labelConfirm.setTextFill(Color.RED);
                final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Le mot de passe et sa confirmation ne correspondent pas.", ButtonType.CLOSE);
                alert.setResizable(true);
                alert.showAndWait();
                throw new Exception("Les mots de passe ne correspondent pas ! Modification non enregistrée.");
            }
            else{
                elementProperty.get().setPassword(hexaMD5(ui_password.getText()));
                ui_labelConfirm.setTextFill(Color.BLACK);
            }
        }
    }

}
