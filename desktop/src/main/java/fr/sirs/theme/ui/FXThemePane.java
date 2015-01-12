
package fr.sirs.theme.ui;

import fr.sirs.FXEditMode;
import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import static fr.sirs.Role.ADMIN;
import static fr.sirs.Role.EXTERNE;
import static fr.sirs.Role.USER;
import fr.sirs.core.Repository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.LeveeProfilTravers;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.map.FXMapTab;
import java.awt.geom.NoninvertibleTransformException;
import java.time.LocalDateTime;
import javafx.beans.value.ObservableValue;
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
    
    private final Session session = Injector.getSession();
    protected FXElementPane specificThemePane;
    
    @FXML protected ScrollPane uiEditDetailTronconTheme;
    @FXML private FXDateField date_maj;
    @FXML private Label id;
    @FXML private Label uiTitleLabel;
    @FXML private FXEditMode uiMode;
    @FXML private Button uiShowOnMapButton;

    private Element couchDbDocument;
    
    public FXThemePane(final T theme) {
        SIRS.loadFXML(this);
        
        date_maj.setDisable(true);
        
        uiMode.setAllowedRoles(ADMIN, USER, EXTERNE);
        uiMode.setSaveAction(this::save);
        disableFieldsProperty().bind(uiMode.editionState().not());
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
        
        LocalDateTime now = LocalDateTime.now();
        
        Element elementDocument = elementProperty.get().getCouchDBDocument();
        if (elementDocument == null) {
            new Alert(Alert.AlertType.INFORMATION, "Un objet ne peut être sauvegardé sans tronçon valide.", ButtonType.OK);
            return;
        }
        
        final Repository repo = session.getRepositoryForClass(elementDocument.getClass());
        if (couchDbDocument == null) {
            couchDbDocument = elementDocument;
        } else if (!couchDbDocument.equals(elementDocument)) {
            // TODO : manage following case : edited element has been moved to another document.
            repo.update(couchDbDocument);
            couchDbDocument = elementDocument;
        }
        
        repo.update(couchDbDocument);
    }
    
    @FXML
    private void showOnMap(){
        final Element object = elementProperty.get();
        if (object instanceof Positionable) {
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
        couchDbDocument = object.getCouchDBDocument();
        
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
                specificThemePane = SIRS.generateEditionPane(object);
                specificThemePane.disableFieldsProperty().bind(disableFieldsProperty());
                uiEditDetailTronconTheme.setContent((Node)specificThemePane);
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
            specificThemePane.preSave();
        }
    }
}
