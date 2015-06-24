
package fr.sirs.theme.ui;

import fr.sirs.FXEditMode;
import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import static fr.sirs.core.model.Role.ADMIN;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.AvecDateMaj;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.Positionable;
import fr.sirs.map.FXMapTab;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.logging.Level;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.geotoolkit.gui.javafx.util.FXLocalDateTimeField;
import org.geotoolkit.internal.GeotkFX;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
public class FXElementContainerPane<T extends Element> extends AbstractFXElementPane<T> {
    
    private final Session session = Injector.getSession();
    protected FXElementPane specificThemePane;
    
    @FXML private Label uiHeaderLabel;
    @FXML private Label uiDateMajLabel;
    @FXML private TextField uiPseudoId;
    @FXML private FXLocalDateTimeField date_maj;
    @FXML private FXEditMode uiMode;
    @FXML private Button uiShowOnMapButton;

    private Element couchDbDocument;
    
    public FXElementContainerPane(final T element) {
        SIRS.loadFXML(this);
        date_maj.setDisable(true);
        
        uiMode.setSaveAction(this::save);
        uiMode.requireEditionForElement(element);
        disableFieldsProperty().bind(uiMode.editionState().not());
        
        uiPseudoId.disableProperty().bind(disableFieldsProperty());
        
        elementProperty.addListener(this::initPane);
        
        setElement((T) element);
    }
    
    public void setShowOnMapButton(final boolean isShown){
        uiShowOnMapButton.setVisible(isShown);
    }
    
    @FXML
    void save() {
        try {
            preSave();

            LocalDateTime now = LocalDateTime.now();

            Element elementDocument = elementProperty.get().getCouchDBDocument();
            if (elementDocument == null) {
                final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Un objet ne peut être sauvegardé sans tronçon valide.", ButtonType.OK);
                alert.setResizable(true);
                alert.show();
                return;
            }

            final AbstractSIRSRepository repo = session.getRepositoryForClass(elementDocument.getClass());
            if (couchDbDocument == null) {
                couchDbDocument = elementDocument;
            } else if (!couchDbDocument.equals(elementDocument)) {
                if (couchDbDocument instanceof AvecDateMaj) {
                    ((AvecDateMaj)couchDbDocument).dateMajProperty().set(now);
                }
                repo.update(couchDbDocument);
                couchDbDocument = elementDocument;
            }

            if (couchDbDocument instanceof AvecDateMaj) {
                ((AvecDateMaj)couchDbDocument).dateMajProperty().set(now);
            }

            if (elementProperty.get() instanceof AvecDateMaj) {
                ((AvecDateMaj)elementProperty.get()).dateMajProperty().set(now);
            }
            
            repo.update(couchDbDocument);
        } catch (Exception e) {
            GeotkFX.newExceptionDialog("L'élément ne peut être sauvegardé.", e).show();
            SIRS.LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }
    
    @FXML
    private void showOnMap() {
        final Element object = elementProperty.get();
        if (object instanceof Positionable) {
            final FXMapTab tab = session.getFrame().getMapTab();

            tab.getMap().focusOnElement(object);
            tab.show();
        } else {
            final Alert alert = new Alert(Alert.AlertType.INFORMATION, "L'élément courant n'est pas positionable sur la carte.", ButtonType.OK);
            alert.setResizable(true);
            alert.show();
        }
    }
    
    protected void initPane(ObservableValue<? extends Element> observable, Element oldValue, Element newValue) {
        // unbind all mono-directional
        date_maj.valueProperty().unbind();
        uiMode.validProperty().unbind();
        uiMode.authorIDProperty().unbind();
        
        if (oldValue != null) {
            //unbind all bidirectionnal
            uiPseudoId.textProperty().unbindBidirectional(oldValue.designationProperty());
        }
        
        if (newValue == null) {
            uiHeaderLabel.setText("Aucune information disponible");
            setCenter(new Label("Pas d'éditeur disponible."));
            specificThemePane = null;
        } else {
            try {
                uiHeaderLabel.setText("Informations sur un(e) "+new LabelMapper(newValue.getClass()).mapClassName());
            } catch (Exception e) {
                SIRS.LOGGER.log(Level.WARNING, "Header label cannot be updated.", e);
                uiHeaderLabel.setText("Informations sur un ouvrage");
            }
            // Keep parent reference, so we can now if it has been switched at save.
            couchDbDocument = newValue.getCouchDBDocument();
            
            // maj
            if (newValue instanceof AvecDateMaj) {
                date_maj.valueProperty().bind(((AvecDateMaj) newValue).dateMajProperty());
                date_maj.setVisible(true);
                uiDateMajLabel.setVisible(true);
            } else {
                date_maj.setVisible(false);
                uiDateMajLabel.setVisible(false);
            }
            
            //validation
            uiMode.validProperty().bind(newValue.validProperty());
            uiMode.authorIDProperty().bind(newValue.authorProperty());
            Injector.getSession().getPrintManager().prepareToPrint(newValue);
            
            uiPseudoId.textProperty().bindBidirectional(newValue.designationProperty());
            
            // If we previously edited same type of element, we recycle edition panel.
            if (specificThemePane != null && oldValue != null && oldValue.getClass().equals(newValue.getClass())) {
                specificThemePane.setElement(newValue);
            } else {
                try {
                    // Choose the pane adapted to the specific structure.
                    final String className = "fr.sirs.theme.ui.FX" + newValue.getClass().getSimpleName() + "Pane";
                    final Class controllerClass = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
                    final Constructor cstr = controllerClass.getConstructor(newValue.getClass());
                    specificThemePane = (FXElementPane) cstr.newInstance(newValue);
                    specificThemePane.disableFieldsProperty().bind(disableFieldsProperty());
                    if (specificThemePane instanceof FXUtilisateurPane) {
                        ((FXUtilisateurPane) specificThemePane).setAdministrable(ADMIN.equals(session.getRole()));
                    }
                    setCenter((Node) specificThemePane);
                } catch (Exception ex) {
                    throw new UnsupportedOperationException("Failed to load panel : " + ex.getMessage(), ex);
                }
            }
        }
        
        uiShowOnMapButton.setVisible(newValue instanceof Positionable);
    }

    @Override
    final public void setElement(T element) {
        elementProperty.set(element);
    }

    @Override
    public void preSave() throws Exception {
        specificThemePane.preSave();
    }
}
