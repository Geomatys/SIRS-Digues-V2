
package fr.sirs.theme.ui;

import fr.sirs.Session;
import fr.sirs.SIRS;
import fr.sirs.Injector;
import fr.sirs.core.component.*;
import fr.sirs.core.model.*;
import fr.sirs.util.FXFileTextField;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.HTMLEditor;
import javafx.event.ActionEvent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import org.geotoolkit.gui.javafx.util.FXDateField;

import java.util.logging.Level;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.Border;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Olivier Nouguier (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FXPhotoPane extends AbstractFXElementPane<Photo> {

    private final Previews previewRepository;

    // Document parent
    @FXML Label ui_parent_label;
    @FXML HBox ui_parent_box;
    @FXML ComboBox ui_parent_choice;
    @FXML Button ui_parent_link;

    /** A reference to the parent of the edited element. We keep it to be able to update old parent if a new one is set. */
    private Element originalParent;
    // Propriétés de Positionable
    @FXML FXPositionablePane uiPositionable;

    // Propriétés de Photo
    @FXML DatePicker ui_date;
    @FXML FXFileTextField ui_chemin;
    @FXML TextField ui_libelle;
    @FXML HTMLEditor ui_commentaire;
    @FXML ComboBox ui_orientationPhoto;
    @FXML Button ui_orientationPhoto_link;
    @FXML ComboBox ui_coteId;
    @FXML Button ui_coteId_link;
    @FXML ComboBox ui_photographeId;
    @FXML Button ui_photographeId_link;

    // Propriétés de SIRSFileReference
    @FXML ImageView ui_photo;

    @FXML ScrollPane ui_scroll_pane;
    @FXML StackPane ui_photo_stack;
    @FXML HBox ui_hbox_container;

    /**
     * Constructor. Initialize part of the UI which will not require update when element edited change.
     */
    private FXPhotoPane() {
        SIRS.loadFXML(this, Photo.class);
        previewRepository = Injector.getBean(Session.class).getPreviews();
        elementProperty().addListener(this::initFields);

        ui_parent_box.managedProperty().bind(ui_parent_box.visibleProperty());
        ui_parent_link.disableProperty().bind(ui_parent_choice.getSelectionModel().selectedItemProperty().isNull());
        ui_parent_link.setGraphic(new ImageView(SIRS.ICON_LINK));
        ui_parent_link.setOnAction((ActionEvent e)->Injector.getSession().showEditionTab(ui_parent_choice.getSelectionModel().getSelectedItem()));
        ui_parent_choice.disableProperty().bind(disableFieldsProperty());
        uiPositionable.disableFieldsProperty().bind(disableFieldsProperty());
        uiPositionable.positionableProperty().bind(elementProperty());

	/*
	 * Disabling rules.
	 */
        ui_date.disableProperty().bind(disableFieldsProperty());
        ui_chemin.disableFieldsProperty.bind(disableFieldsProperty());
        ui_libelle.disableProperty().bind(disableFieldsProperty());
        ui_commentaire.disableProperty().bind(disableFieldsProperty());
        ui_orientationPhoto.disableProperty().bind(disableFieldsProperty());
        ui_orientationPhoto_link.setVisible(false);
        ui_coteId.disableProperty().bind(disableFieldsProperty());
        ui_coteId_link.setVisible(false);
        ui_photographeId.disableProperty().bind(disableFieldsProperty());
        ui_photographeId_link.disableProperty().bind(ui_photographeId.getSelectionModel().selectedItemProperty().isNull());
        ui_photographeId_link.setGraphic(new ImageView(SIRS.ICON_LINK));
        ui_photographeId_link.setOnAction((ActionEvent e)->Injector.getSession().showEditionTab(ui_photographeId.getSelectionModel().getSelectedItem()));

        ui_scroll_pane.setMinWidth(USE_PREF_SIZE);
        ui_scroll_pane.setPrefWidth(USE_COMPUTED_SIZE);
        ui_scroll_pane.setBorder(Border.EMPTY);

        ui_hbox_container.setFillHeight(true);

        ui_photo.setPreserveRatio(true);
        ui_chemin.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue != null) {
                ui_photo.setImage(new Image(ui_chemin.getURI().toString()));
                ui_photo.minWidth(0);
                ui_photo.minHeight(0);
                // Compute height when parent size or padding change.
                ui_photo.fitHeightProperty().bind(Bindings.createDoubleBinding(() -> {
                    double height = ui_hbox_container.getHeight();
                    final Insets padding = ui_photo_stack.getPadding();
                    if (padding != null) {
                        height -= padding.getBottom() + padding.getTop();
                    }
                    return Math.max(0, height);
                }, ui_hbox_container.heightProperty(), ui_photo_stack.paddingProperty()));

                // Compute height when parent size or padding change.
                ui_photo.fitWidthProperty().bind(Bindings.createDoubleBinding(() -> {
                    double width = getWidth() - ui_scroll_pane.getWidth() - ui_hbox_container.getSpacing();
                    final Insets padding = ui_photo_stack.getPadding();
                    if (padding != null) {
                        width -= padding.getBottom() + padding.getTop();
                    }
                    return Math.max(0, width);
                }, widthProperty(), ui_scroll_pane.widthProperty(), ui_hbox_container.spacingProperty()));
            } else {
                ui_photo.setImage(null);
            }
        });
    }

    public FXPhotoPane(final Photo photo) {
        this();
        this.elementProperty().set(photo);
    }

    /**
     * Initialize fields at element setting.
     */
    private void initFields(ObservableValue<? extends Photo > observableElement, Photo oldElement, Photo newElement) {
        // Unbind fields bound to previous element.
        if (oldElement != null) {
            // Propriétés de Photo
            ui_date.valueProperty().unbindBidirectional(oldElement.dateProperty());
            ui_chemin.textProperty().unbindBidirectional(oldElement.cheminProperty());
            ui_libelle.textProperty().unbindBidirectional(oldElement.libelleProperty());
        }

        final Session session = Injector.getBean(Session.class);
        originalParent = newElement.getParent();
        if (originalParent == null && newElement.getDocumentId()!=null) {
            final Preview preview = previewRepository.get(newElement.getDocumentId());
            final AbstractSIRSRepository<Element> repository = session.getRepositoryForType(preview.getElementClass());
            originalParent = repository.get(newElement.getDocumentId());
        }

        /* Try to build a list of possible parents. If parent element is not the CouchDb document (depth level > 1),
         * we will just display current parent, and won't allow user to change it, because it's too unpredictible.
         */
        final String documentId = newElement.getDocumentId();
        if (originalParent != null && !originalParent.getId().equals(documentId)) {
            ui_parent_choice.disableProperty().unbind();
            ui_parent_choice.setDisable(true);
            SIRS.initCombo(ui_parent_choice, FXCollections.singletonObservableList(originalParent), originalParent);

        } else if (documentId != null) {
            final Preview parentLabel = previewRepository.get(documentId);
            SIRS.initCombo(ui_parent_choice, FXCollections.observableArrayList(
                    previewRepository.getByClass(parentLabel.getElementClass())), parentLabel);
        } else if (Objet.class.isAssignableFrom(newElement.getClass())
                    || PositionDocument.class.isAssignableFrom(newElement.getClass())) {
            SIRS.initCombo(ui_parent_choice, FXCollections.observableArrayList(previewRepository.getByClass(TronconDigue.class)), null);
        }
        // Prepare parent edition. If we've got a list of potential parents, we display combobox, and notify edited element on selection change.
        ObservableList items = ui_parent_choice.getItems();
        if (items == null || items.isEmpty()) {
            ui_parent_box.setVisible(false);
        } else {
            if (newElement.parentProperty() != null) {
                ui_parent_choice.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue observable, Object oldValue, Object newValue) -> {
                            if (newValue == null) {
                                newElement.setParent(null);
                            } else if (newValue instanceof Element) {
                                newElement.setParent((Element) newValue);
                            } else if (newValue instanceof Preview) {
                                final Preview tmpLabel = (Preview) newValue;
                                newElement.setParent((Element) session.getRepositoryForType(tmpLabel.getElementClass())
                                        .get(tmpLabel.getElementId()));
                            }
                        });
            }
            ui_parent_box.setVisible(true);

            // We've got a non-null parent list, we'll try display a more precise label.
            Object tmpParent = ui_parent_choice.getSelectionModel().getSelectedItem();
            if (tmpParent == null) {
                tmpParent = items.get(0);
            }
            final String parentType = Injector.getSession().getElementType(tmpParent);
            if (parentType != null) {
                try {
                    ui_parent_label.setText(LabelMapper.get(Class.forName(parentType)).mapClassName());
                } catch (ClassNotFoundException e) {
                    SIRS.LOGGER.log(Level.WARNING, "No class for input type " + parentType, e);
                }
            }
        }

        /*
         * Bind control properties to Element ones.
         */
        // Propriétés de Photo
        // * date
        ui_date.valueProperty().bindBidirectional(newElement.dateProperty());
        // * chemin
        ui_chemin.textProperty().bindBidirectional(newElement.cheminProperty());
        // * libelle
        ui_libelle.textProperty().bindBidirectional(newElement.libelleProperty());
        // * commentaire
        ui_commentaire.setHtmlText(newElement.getCommentaire());

        SIRS.initCombo(ui_orientationPhoto, FXCollections.observableArrayList(
            previewRepository.getByClass(RefOrientationPhoto.class)),
            newElement.getOrientationPhoto() == null? null : previewRepository.get(newElement.getOrientationPhoto()));
        SIRS.initCombo(ui_coteId, FXCollections.observableArrayList(
            previewRepository.getByClass(RefCote.class)),
            newElement.getCoteId() == null? null : previewRepository.get(newElement.getCoteId()));
        SIRS.initCombo(ui_photographeId, FXCollections.observableArrayList(
            previewRepository.getByClass(Contact.class)),
            newElement.getPhotographeId() == null? null : previewRepository.get(newElement.getPhotographeId()));
    }

    @Override
    public void preSave() {
        final Session session = Injector.getBean(Session.class);
        final Photo element = (Photo) elementProperty().get();

        final Object selectedParent = ui_parent_choice.getSelectionModel().selectedItemProperty().get();
        if (selectedParent == null) {
            throw new IllegalStateException("L'élément ne peut être enregistré sans parent valide.");
        }

        final Element newParent;
        if (selectedParent instanceof Preview) {
            final Preview tmpLabel = (Preview) selectedParent;
            newParent = (Element) session.getRepositoryForType(tmpLabel.getElementClass()).get(tmpLabel.getElementId());
        } else if (selectedParent instanceof Element) {
            newParent = (Element) selectedParent;
        } else {
            throw new IllegalStateException("L'élément parent est de type inconnu. Sauvegarde impossible.");
        }

        // If parent has changed, we have to dereference our object from the old one, and
        if (originalParent == null || !originalParent.getId().equals(newParent.getId())) {
            newParent.addChild(element);
            if (originalParent != null) {
                originalParent.removeChild(element);
            }
            originalParent = newParent;
        }

        element.setCommentaire(ui_commentaire.getHtmlText());

        uiPositionable.preSave();

        Object cbValue;
        cbValue = ui_orientationPhoto.getValue();
        if (cbValue instanceof Preview) {
            element.setOrientationPhoto(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setOrientationPhoto(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setOrientationPhoto(null);
        }
        cbValue = ui_coteId.getValue();
        if (cbValue instanceof Preview) {
            element.setCoteId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setCoteId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setCoteId(null);
        }
        cbValue = ui_photographeId.getValue();
        if (cbValue instanceof Preview) {
            element.setPhotographeId(((Preview)cbValue).getElementId());
        } else if (cbValue instanceof Element) {
            element.setPhotographeId(((Element)cbValue).getId());
        } else if (cbValue == null) {
            element.setPhotographeId(null);
        }
    }
}
