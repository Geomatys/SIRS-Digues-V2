package fr.sirs.theme.ui;

import com.sun.tools.javac.util.ServiceLoader;
import fr.sirs.SIRS;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.report.AbstractSectionRapport;
import fr.sirs.core.model.report.ModeleRapport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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

    private final ArrayList<AbstractFXElementPane> sectionEditors = new ArrayList<>();

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
            for (final AbstractSectionRapport rapport : sections) {
                if (rapport == null)
                    continue;
                // Check that an editor is available for given section.
                try {
                    if (SIRS.createFXPaneForElement(rapport) == null) {
                        throw new IllegalArgumentException("No editor found.");
                    }
                } catch (ReflectiveOperationException | IllegalArgumentException ex) {
                    SirsCore.LOGGER.log(Level.WARNING, "No editor found for type " + rapport.getClass().getCanonicalName(), ex);
                    continue;
                }
                String sectionTitle = null;
                final LabelMapper mapper = LabelMapper.get(rapport.getClass());
                if (mapper != null)
                    sectionTitle = mapper.mapClassName();
                if (sectionTitle == null || sectionTitle.isEmpty())
                    sectionTitle = rapport.getClass().getSimpleName();

                final MenuItem item = new MenuItem(sectionTitle);
                item.setOnAction((event) -> addSection(rapport));
            }
        }
    }

    public FXModeleRapportPane(final ModeleRapport rapport) {
        this();
        setElement(rapport);
    }

    private void elementChanged(ObservableValue<? extends ModeleRapport> obs, ModeleRapport oldModele, ModeleRapport newModele) {
        if (oldModele != null) {
            oldModele.libelleProperty().unbindBidirectional(uiName.textProperty());
            uiSections.getChildren().clear();
        }

        if (newModele != null) {
            newModele.libelleProperty().bindBidirectional(uiName.textProperty());
        }
    }

    @Override
    public void preSave() throws Exception {
        for (final AbstractFXElementPane editor : sectionEditors) {
            editor.preSave();
        }
    }

    /**
     * @return an empty instance of each available implementation of section objects.
     */
    private static AbstractSectionRapport[] getAvailableSections() {
        if (AVAILABLE_SECTIONS == null) {
            Iterator<AbstractSectionRapport> iterator = ServiceLoader.load(
                    AbstractSectionRapport.class,
                    Thread.currentThread().getContextClassLoader())
                    .iterator();

            final ArrayList<AbstractSectionRapport> tmpList = new ArrayList<>();
            while (iterator.hasNext()) {
                tmpList.add(iterator.next());
            }
            AVAILABLE_SECTIONS = tmpList.toArray(new AbstractSectionRapport[tmpList.size()]);
        }
        return AVAILABLE_SECTIONS;
    }

    /**
     * Try to add a new editor for a new section created by copying given one.
     * @param template Default implementation of the section tto add / edit.
     */
    private void addSection(final AbstractSectionRapport template) {
        ArgumentChecks.ensureNonNull("Section template", template);
        try {
            final AbstractSectionRapport copy = (AbstractSectionRapport) template.copy();
            final AbstractFXElementPane editor = SIRS.createFXPaneForElement(copy);
            uiSections.getChildren().add(new SectionContainer(copy, editor));
            // If (and only if) we succeed acquisition of an editor, we add he new section.
            elementProperty.get().sections.add(copy);
            sectionEditors.add(editor);

        } catch (ReflectiveOperationException | IllegalArgumentException ex) {
            SirsCore.LOGGER.log(Level.WARNING, "No editor found for type " + template.getClass().getCanonicalName(), ex);
            GeotkFX.newExceptionDialog("Impossible de créer un éditeur pour le type de section demandé.", ex);
        }
    }

    /**
     * A container for {@link AbstractSectionRapport} editors. It allow to collapse,
     * delete or duplicate an editor and the associated section.
     */
    private final class SectionContainer extends TitledPane {

        private final AbstractSectionRapport section;

        public SectionContainer(AbstractSectionRapport input, Node sectionEditor) {
            super();

            ArgumentChecks.ensureNonNull("Input section object", input);
            ArgumentChecks.ensureNonNull("Input section editor", sectionEditor);
            section = input;
            setContent(sectionEditor);

            final Button deleteButton = new Button(null, new ImageView(GeotkFX.ICON_DELETE));
            final Button copyButton = new Button(null, new ImageView(GeotkFX.ICON_DUPLICATE));
            final Separator sep = new Separator();
            sep.setMaxWidth(Double.MAX_VALUE);
            sep.setMinWidth(0);
            HBox.setHgrow(sep, Priority.ALWAYS);
            final HBox headerButtons = new HBox(5, sep, copyButton, deleteButton);

            setGraphic(headerButtons);

            deleteButton.setOnAction((event) -> {
                uiSections.getChildren().remove(this);
                sectionEditors.remove(getContent());
                elementProperty.get().sections.remove(section);
            });

            copyButton.setOnAction((event) -> addSection(section));
        }
    }
}
