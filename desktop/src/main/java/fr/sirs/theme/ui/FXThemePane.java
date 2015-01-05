
package fr.sirs.theme.ui;

import fr.sirs.FXEditMode;
import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import static fr.sirs.Role.ADMIN;
import static fr.sirs.Role.EXTERNE;
import static fr.sirs.Role.USER;
import fr.sirs.core.component.TronconDigueRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.LeveeProfilTravers;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.map.FXMapTab;
import java.awt.geom.NoninvertibleTransformException;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.gui.javafx.render2d.FXMap;
import org.geotoolkit.gui.javafx.util.FXDateField;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
public class FXThemePane<T extends Element> extends AbstractFXElementPane<T> {
    
    protected Node specificThemePane;
    
    @FXML protected ScrollPane uiEditDetailTronconTheme;

    @FXML private FXDateField date_maj;

    @FXML private Label id;
    
    @FXML private Label uiTitleLabel;
    
    @FXML private FXEditMode uiMode;
    @FXML private Button uiShowOnMapButton;

    private TronconDigue troncon;
    
    public FXThemePane(final T theme) {
        SIRS.loadFXML(this);
        
        date_maj.setDisable(true);
        
        uiMode.setAllowedRoles(ADMIN, USER, EXTERNE);
        uiMode.setSaveAction(this::save);
        BooleanBinding NotEdit = uiMode.editionState().not();
        uiEditDetailTronconTheme.disableProperty().bind(NotEdit); // Provisoire car limite la lisibilité et bloque le défilement du ScrollPane.
        uiEditDetailTronconTheme.getStyleClass().add("element-pane");
        
        elementProperty.addListener((ObservableValue<? extends Element> observable, Element oldValue, Element newValue) -> {
            initPane();
        });
        setElement((T) theme);
    }
    
    public void setShowOnMapButton(final boolean isShown){
        uiShowOnMapButton.setVisible(isShown);
    }
    
    @FXML
    void save() {
        preSave();
        
        // If we've got an object, troncon is updated.
        if (troncon != null) {
            LocalDateTime now = LocalDateTime.now();
            final Objet structure = (Objet) elementProperty.get();
            final String tronconId = structure.getTroncon();
            
            if (tronconId == null) {
                new Alert(Alert.AlertType.INFORMATION, "Un objet ne peut être sauvegardé sans tronçon valide.", ButtonType.OK);
                return;
            }
            
            Session session = Injector.getBean(Session.class);
            TronconDigueRepository tronconRepo = session.getTronconDigueRepository();
            
            if (!tronconId.equals(troncon.getId())) {
                troncon.getStructures().remove(structure);
                final TronconDigue newTroncon = tronconRepo.get(tronconId);
                newTroncon.getStructures().add(structure);
                // Update old troncon to remove current object
                troncon.setDateMaj(now);
                tronconRepo.update(troncon);
                // Say that current troncon is the new one.
                troncon = newTroncon;
            }
            
            troncon.setDateMaj(now);
            tronconRepo.update(troncon);
        }
    }
    
    @FXML
    private void showOnMap(){
        final Element object = elementProperty.get();
        if (object instanceof Positionable) {
            final Session session = Injector.getBean(Session.class);
            final FXMapTab tab = session.getFrame().getMapTab();
            tab.show();
            final FXMap map = tab.getMap().getUiMap();
            try {
                map.getCanvas().setVisibleArea(JTS.toEnvelope(((Positionable) object).getGeometry()));
            } catch (NoninvertibleTransformException | TransformException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            new Alert(Alert.AlertType.INFORMATION, "L'élément courant n'est pas positionable sur la carte.", ButtonType.OK).show();
        }
    }
    
    protected void initPane() {
        final Element object = elementProperty.get();
        if (object instanceof Objet) {
            troncon = (TronconDigue) ((Objet) elementProperty.get()).getParent();
        } else {
            troncon = null;
        }
        if (object == null) {
            uiTitleLabel.setText("Aucune information à afficher");
            id.setText("");
            date_maj.valueProperty().unbind();
            
            uiEditDetailTronconTheme.setContent(new Label("Pas d'éditeur disponible."));
            specificThemePane = null;

        } else {
            uiTitleLabel.setText("Information sur une instance de " + object.getClass().getSimpleName());
            id.setText(object.getId());
            // TODO : make a "WithDateMaj" interface, or something similar.
            if (object instanceof ProfilTravers) {
                date_maj.valueProperty().bindBidirectional(((ProfilTravers) object).dateMajProperty());
            } else if (object instanceof LeveeProfilTravers) {
                date_maj.valueProperty().bindBidirectional(((LeveeProfilTravers) object).dateMajProperty());
            } else if (object instanceof Objet) {
                date_maj.valueProperty().bindBidirectional(((Objet) object).dateMajProperty());
            } else {
                date_maj.valueProperty().unbind();
            }
            
            try {
                // Choose the pane adapted to the specific structure.
                final String className = "fr.sirs.theme.ui.FX" + object.getClass().getSimpleName() + "Pane";
                final Class controllerClass = Class.forName(className);
                final Constructor cstr = controllerClass.getConstructor(object.getClass());
                specificThemePane = (Node) cstr.newInstance(object);
//                ((FXElementPane) specificThemePane).editableProperty().bind(uiMode.editionState());
                ((AbstractFXElementPane) specificThemePane).editableProperty().bind(uiMode.editionState());
                uiEditDetailTronconTheme.setContent(specificThemePane);
            } catch (Exception ex) {
                throw new UnsupportedOperationException("Failed to load panel : " + ex.getMessage(), ex);
            }
        }
        
        uiShowOnMapButton.setVisible(object instanceof Positionable);
    }

    @Override
    final public void setElement(T element) {
        elementProperty.set(element);       
    }

    @Override
    public void preSave() {
        if (specificThemePane instanceof FXElementPane) {
            ((FXElementPane) specificThemePane).preSave();
        }
    }
}
