package fr.sirs.theme.ui;

import fr.sirs.SIRS;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.report.AbstractSectionRapport;
import fr.sirs.core.model.report.ModeleRapport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.Level;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.internal.GeotkFX;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class FXModeleRapportPane extends AbstractFXElementPane<ModeleRapport> {

    private static AbstractSectionRapport[] AVAILABLE_SECTIONS;

    @FXML private TextField uiName;
    @FXML private MenuButton uiAddSection;
    @FXML private VBox uiSections;

    private final ArrayList<AbstractFXElementPane<? extends AbstractSectionRapport>> sectionEditors = new ArrayList<>();

    public FXModeleRapportPane() {
        super();
        SIRS.loadFXML(this);

        uiAddSection.disableProperty().bind(
                disableFieldsProperty()
                .or(Bindings.isEmpty(uiAddSection.getItems()))
                .or(elementProperty.isNull())
        );

        final AbstractSectionRapport[] sections = getAvailableSections();
        if (sections != null && sections.length > 0) {
            for (final AbstractSectionRapport section : sections) {
                if (section == null)
                    continue;
                // Check that an editor is available for given section.
                try {
                    if (SIRS.createFXPaneForElement(section) == null) {
                        throw new IllegalArgumentException("No editor found.");
                    }
                } catch (ReflectiveOperationException | IllegalArgumentException ex) {
                    SirsCore.LOGGER.log(Level.WARNING, "No editor found for type " + section.getClass().getCanonicalName(), ex);
                    continue;
                }
                String sectionTitle = null;
                final LabelMapper mapper = LabelMapper.get(section.getClass());
                if (mapper != null)
                    sectionTitle = mapper.mapClassName();
                if (sectionTitle == null || sectionTitle.isEmpty())
                    sectionTitle = section.getClass().getSimpleName();

                final MenuItem item = new MenuItem(sectionTitle);
                uiAddSection.getItems().add(item);
                item.setOnAction((event) -> addSectionCopy(section));
            }
        }

        elementProperty.addListener(this::elementChanged);
    }

    public FXModeleRapportPane(final ModeleRapport rapport) {
        this();
        setElement(rapport);
    }

    private void elementChanged(ObservableValue<? extends ModeleRapport> obs, ModeleRapport oldModele, ModeleRapport newModele) {
        if (oldModele != null) {
            uiName.textProperty().unbindBidirectional(oldModele.libelleProperty());
            uiName.setText(null);
            uiSections.getChildren().clear();
            sectionEditors.clear();
        }

        if (newModele != null) {
            uiName.textProperty().bindBidirectional(newModele.libelleProperty());
            for (final AbstractSectionRapport section : newModele.sections) {
                addSectionEditor(section);
            }
        }
    }

    @Override
    public void preSave() throws Exception {
        final ArrayList<AbstractSectionRapport> editedSections = new ArrayList<>();
        for (final AbstractFXElementPane<? extends AbstractSectionRapport> editor : sectionEditors) {
            editor.preSave();
            editedSections.add(editor.elementProperty.get());
        }

        elementProperty.get().sections.setAll(editedSections);
    }

    /**
     * @return an empty instance of each available implementation of section objects.
     */
    private static synchronized AbstractSectionRapport[] getAvailableSections() {
        if (AVAILABLE_SECTIONS == null) {
            Iterator<Element> iterator = ServiceLoader.load(
                    Element.class,
                    Thread.currentThread().getContextClassLoader())
                    .iterator();

            final ArrayList<AbstractSectionRapport> tmpList = new ArrayList<>();
            while (iterator.hasNext()) {
                final Element next = iterator.next();
                if (next instanceof AbstractSectionRapport)
                    tmpList.add((AbstractSectionRapport) next);
            }
            AVAILABLE_SECTIONS = tmpList.toArray(new AbstractSectionRapport[tmpList.size()]);
        }
        return AVAILABLE_SECTIONS;
    }

    /**
     * Try to add a new editor for given section.
     * @param section Section to edit.
     */
    private void addSectionEditor(final AbstractSectionRapport section) {
        ArgumentChecks.ensureNonNull("Section to edit", section);
        try {
            final AbstractFXElementPane editor = SIRS.createFXPaneForElement(section);
            uiSections.getChildren().add(new SectionContainer(editor));
            sectionEditors.add(editor);

        } catch (ReflectiveOperationException | IllegalArgumentException ex) {
            SirsCore.LOGGER.log(Level.WARNING, "No editor found for type " + section.getClass().getCanonicalName(), ex);
            GeotkFX.newExceptionDialog("Impossible de créer un éditeur pour le type de section demandé.", ex);
        }
    }

    /**
     * Add a new section in currently edited model, which is a copy of given section.
     * @param original Original section to duplicate
     */
    private void addSectionCopy(final AbstractSectionRapport original) {
        final AbstractSectionRapport copy = (AbstractSectionRapport) original.copy();
        addSectionEditor(copy);
    }

    /**
     * A container for {@link AbstractSectionRapport} editors. It allow to collapse,
     * delete or duplicate an editor and the associated section.
     */
    private final class SectionContainer extends TitledPane {

        public SectionContainer(AbstractFXElementPane<? extends AbstractSectionRapport> sectionEditor) {
            super();

            ArgumentChecks.ensureNonNull("Input section editor", sectionEditor);
            sectionEditor.getStyleClass().add("transparent");
            setContent(sectionEditor);

            final Button deleteButton = new Button(null, new ImageView(GeotkFX.ICON_DELETE));
            final Button copyButton = new Button(null, new ImageView(GeotkFX.ICON_DUPLICATE));
            final HBox headerButtons = new HBox(5, copyButton, deleteButton);
            headerButtons.setMaxWidth(Double.MAX_VALUE);
            headerButtons.setAlignment(Pos.TOP_RIGHT);
            setGraphic(headerButtons);

            deleteButton.setOnAction((event) -> {
                uiSections.getChildren().remove(this);
                sectionEditors.remove(sectionEditor);
            });
            copyButton.setOnAction((event) -> addSectionCopy(sectionEditor.elementProperty.get()));
            final BooleanBinding disabled = sectionEditor.elementProperty.isNull().or(disableFieldsProperty());
            disableProperty().bind(disabled);
            copyButton.getStyleClass().add("white-with-borders");
            deleteButton.getStyleClass().add("white-with-borders");
            sectionChanged(sectionEditor.elementProperty, null, sectionEditor.elementProperty.get());
        }

        private void sectionChanged(final ObservableValue<? extends AbstractSectionRapport> obs, AbstractSectionRapport oldValue, AbstractSectionRapport newValue) {
            if (newValue == null) {
                setText(null);
            } else {
                setText(LabelMapper.get(newValue.getClass()).mapClassName());
            }
        }
    }
}
