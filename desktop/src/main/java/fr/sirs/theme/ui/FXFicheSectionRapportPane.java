package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.SQLQuery;
import fr.sirs.core.model.report.FicheSectionRapport;
import fr.sirs.core.model.report.ModeleElement;
import fr.sirs.query.FXSearchPane;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import org.ektorp.DbAccessException;

/**
 * TODO : model edition
 * @author Alexis Manin (Geomatys)
 */
public class FXFicheSectionRapportPane extends AbstractFXElementPane<FicheSectionRapport> {

    private static enum PhotoChoice {
        NONE("Aucune", 0),
        LAST("La dernière", 1),
        CHOOSE("Les n dernières", -1),
        ALL("Toutes", Short.MAX_VALUE);

        public final String title;
        public final int number;
        PhotoChoice(final String title, final int number) {
            this.title = title;
            this.number = number;
        }
    }

    @FXML private TextField uiTitle;
    @FXML private Label uiQueryTitle;
    @FXML private Label uiModelTitle;
    @FXML private ChoiceBox<PhotoChoice> uiNbPhotoChoice;
    @FXML private Spinner<Integer> uiNbPhotoSpinner;

    private final ObjectProperty<SQLQuery> queryProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<ModeleElement> modelProperty = new SimpleObjectProperty<>();

    public FXFicheSectionRapportPane() {
        super();
        SIRS.loadFXML(this);

        elementProperty.addListener(this::elementChanged);
        queryProperty.addListener(this::queryChanged);
        modelProperty.addListener(this::modelChanged);
        uiNbPhotoChoice.valueProperty().addListener((ObservableValue<? extends PhotoChoice> observable, PhotoChoice oldValue, PhotoChoice newValue) -> {
            uiNbPhotoSpinner.setVisible(newValue != null && newValue.number < 0);
        });
        uiNbPhotoChoice.setItems(FXCollections.observableArrayList(PhotoChoice.values()));
        uiNbPhotoChoice.setValue(PhotoChoice.NONE);

        // TODO : tooltips.
    }

    /**
     * Called when element edited change. We must update all UI to manage the new one.
     * @param obs
     * @param oldValue
     * @param newValue
     */
    private void elementChanged(ObservableValue<? extends FicheSectionRapport> obs, FicheSectionRapport oldValue, FicheSectionRapport newValue) {
        // Start by clearing bindings from old element.
        if (oldValue != null) {
            uiTitle.textProperty().unbindBidirectional(oldValue.libelleProperty());
        }

        if (newValue != null) {
            uiTitle.textProperty().bindBidirectional(newValue.libelleProperty());
            if (newValue.getRequeteId() != null) {
                try {
                    queryProperty.set(Injector.getSession().getRepositoryForClass(SQLQuery.class).get(newValue.getRequeteId()));
                } catch (DbAccessException e) {
                    queryProperty.set(null);
                }
            }

            if (newValue.getModeleElementId()!= null) {
                try {
                    modelProperty.set(Injector.getSession().getRepositoryForClass(ModeleElement.class).get(newValue.getModeleElementId()));
                } catch (DbAccessException e) {
                    modelProperty.set(null);
                }
            }

            // Analyze number of photos chosen, to pick adapted UI element (combo-box or spinner)
            final int nbPhotos = newValue.getNbPhotos();
            uiNbPhotoChoice.setValue(null);
            for (final PhotoChoice c : PhotoChoice.values()) {
                if (c.number == nbPhotos) {
                    uiNbPhotoChoice.setValue(c);
                }
            }

            if (uiNbPhotoChoice.getValue() == null) {
                if (nbPhotos > 0) {
                    uiNbPhotoChoice.setValue(PhotoChoice.CHOOSE);
                    uiNbPhotoSpinner.getValueFactory().setValue(nbPhotos);
                } else {
                    uiNbPhotoChoice.setValue(PhotoChoice.NONE);
                }
            }
        } else {
            queryProperty.set(null);
            modelProperty.set(null);
            uiNbPhotoChoice.setValue(PhotoChoice.NONE);
        }
    }

    /**
     * Update query label when user change its value.
     */
    private void queryChanged(ObservableValue<? extends SQLQuery> obs, SQLQuery oldValue, SQLQuery newValue) {
        if (newValue == null)
            uiQueryTitle.setText("N/A");
        else
            uiQueryTitle.setText(newValue.getLibelle());
    }

    /**
     * Change model label whan user modify it value.
     */
    private void modelChanged(ObservableValue<? extends ModeleElement> obs, ModeleElement oldValue, ModeleElement newValue) {
        if (newValue == null)
            uiModelTitle.setText("N/A");
        else
            uiModelTitle.setText(newValue.getLibelle());
    }

    @Override
    public void preSave() throws Exception {
        if (queryProperty.get() != null) {
            elementProperty.get().setRequeteId(queryProperty.get().getId());
        } else {
            elementProperty.get().setRequeteId(null);
        }

        final PhotoChoice photoChoice = uiNbPhotoChoice.getValue();
        if (photoChoice.number < 0) {
            elementProperty.get().setNbPhotos(Math.max(0, uiNbPhotoSpinner.getValue()));
        } else {
            elementProperty.get().setNbPhotos(photoChoice.number);
        }
    }

    /**
     * Action to perform when user want to select a query.
     */
    @FXML
    private void chooseQuery(final ActionEvent e) {
        final Optional<SQLQuery> query = FXSearchPane.chooseSQLQuery(Injector.getSession().getRepositoryForClass(SQLQuery.class).getAll());
        if (query.isPresent())
            queryProperty.set(query.get());
    }

    /**
     * Triggered when user clean query value.
     */
    @FXML
    private void deleteQuery(final ActionEvent e) {
        queryProperty.set(null);
    }

    /**
     * Action to perform when user want to select a model.
     */
    @FXML
    private void chooseModel(final ActionEvent e) {
        // TODO
    }

    /**
     * Action performed when user remove model.
     */
    @FXML
    private void deleteModel(final ActionEvent e) {
        modelProperty.set(null);
    }
}
