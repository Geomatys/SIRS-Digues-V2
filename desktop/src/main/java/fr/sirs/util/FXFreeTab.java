
package fr.sirs.util;

import fr.sirs.Printable;
import fr.sirs.SIRS;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
public class FXFreeTab extends Tab implements FXTextAbregeable, Printable {
    
    private static boolean DEFAULT_ABREGEABLE = true;
    private static int DEFAULT_NB_AFFICHABLE = 25;
    private final ObjectProperty printElements = new SimpleObjectProperty();
    
    private FXFreeTab(String text, boolean abregeable, int nbAffichable) {
        super();
        setAbregeable(abregeable);
        setNbAffichable(nbAffichable);
        setTextAbrege(text);
        
        final MenuItem item = new MenuItem("Détacher");
        item.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                
                final TabPane tabs = FXFreeTab.this.tabPaneProperty().get();
                tabs.getTabs().remove(FXFreeTab.this);
                
                final Stage stage = new Stage();
                stage.getIcons().add(SIRS.ICON);
                stage.setTitle(getText());
                final TabPane newPane = new TabPane();
                newPane.getTabs().add(FXFreeTab.this);
                final Scene scene = new Scene(newPane);
                stage.setScene(scene);
                stage.setOnHidden((WindowEvent event1) -> {
                    newPane.getTabs().remove(FXFreeTab.this);
                    tabs.getTabs().add(FXFreeTab.this);
                });
                
                stage.show();
            }
        });
        
        final ContextMenu menu = new ContextMenu(item);
        setContextMenu(menu);

        selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(Boolean.TRUE.equals(newValue)){
                    if(getContent()!=null){
                        getContent().requestFocus();
                    }
                }
            }
        });
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

    @Override
    public ObjectProperty getPrintableElements() {
        return printElements;
    }
    
}
