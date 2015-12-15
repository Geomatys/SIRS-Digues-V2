
package fr.sirs.util;

import fr.sirs.SIRS;
import fr.sirs.ui.Growl;
import java.lang.ref.WeakReference;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class FXFreeTab extends Tab implements FXTextAbregeable {

    private static boolean DEFAULT_ABREGEABLE = true;
    private static int DEFAULT_NB_AFFICHABLE = 25;

    private static final String UNBIND = "Détacher";
    private static final String BIND = "Rattacher";

    /**
     * Last pane this tab has been bound to.
     */
    private WeakReference<TabPane> previous;
    private final MenuItem bindAction;

    private FXFreeTab(String text, boolean abregeable, int nbAffichable) {
        super();
        setAbregeable(abregeable);
        setNbAffichable(nbAffichable);
        setTextAbrege(text);

        bindAction = new MenuItem(UNBIND);
        bindAction.setOnAction(this::unbind);
        setContextMenu(new ContextMenu(bindAction));
    }

    public FXFreeTab(String text, int nbAffichable) {
        this(text, DEFAULT_ABREGEABLE, nbAffichable);
    }

    public FXFreeTab(String text, boolean abregeable) {
        this(text, abregeable, DEFAULT_NB_AFFICHABLE);
    }

    public FXFreeTab(String text) {
        this(text, DEFAULT_ABREGEABLE);
    }

    public FXFreeTab(){
        this(null, DEFAULT_ABREGEABLE);
    }

    private BooleanProperty abregeableProperty;

    @Override
    public final BooleanProperty abregeableProperty(){
        if(abregeableProperty==null){
            abregeableProperty=new SimpleBooleanProperty(this, "abregeable");
        }
        return abregeableProperty;
    }
    @Override
    public final boolean isAbregeable(){return abregeableProperty().get();}
    @Override
    public final void setAbregeable(final boolean abregeable){abregeableProperty().set(abregeable);}

    private IntegerProperty nbAffichableProperty;

    @Override
    public final IntegerProperty nbAffichableProperty(){
        if(nbAffichableProperty==null){
            nbAffichableProperty = new SimpleIntegerProperty(this, "nbAffichable");
        }
        return nbAffichableProperty;
    }
    @Override
    public final int getNbAffichable(){return nbAffichableProperty().get();}
    @Override
    public final void setNbAffichable(final int nbAffichable){nbAffichableProperty().set(nbAffichable);}

    public final void setTextAbrege(final String text){
        if(text!=null
            && isAbregeable()
            && text.length()>getNbAffichable()){
            setText(text.substring(0, getNbAffichable())+"...");
            setTooltip(new Tooltip(text));
        }
        else{
            setText(text);
        }
    }

    private void unbind(final ActionEvent evt) {
        final TabPane tabPane = this.getTabPane();
        previous = new WeakReference<>(tabPane);
        tabPane.getTabs().remove(this);

        final Stage stage = new Stage();
        stage.getIcons().add(SIRS.ICON);
        stage.titleProperty().bind(textProperty());

        final TabPane newPane = new TabPane(this);
        newPane.getStylesheets().add(SIRS.CSS_PATH);
        stage.setScene(new Scene(newPane));
        stage.setOnHidden((WindowEvent event1) -> {
            newPane.getTabs().remove(FXFreeTab.this);
            final TabPane tmp = previous == null? null : previous.get();
            if (tmp != null) {
                tmp.getTabs().add(FXFreeTab.this);
            }
        });

        stage.sizeToScene();
        stage.show();

        bindAction.setText(BIND);
        bindAction.setOnAction(this::bind);
    }

    private void bind(final ActionEvent evt) {
        final TabPane tmpPane = previous == null ? null : previous.get();
        if (tmpPane == null) {
            new Growl(Growl.Type.WARNING, "Le panneau d'origine n'existe plus. Impossible de raccrocher l'onglet.").showAndFade();
            getContextMenu().getItems().remove(bindAction); // No more binding is possible now.
        } else {
            this.getTabPane().getScene().getWindow().hide();

            bindAction.setText(UNBIND);
            bindAction.setOnAction(this::unbind);
        }
    }
}
