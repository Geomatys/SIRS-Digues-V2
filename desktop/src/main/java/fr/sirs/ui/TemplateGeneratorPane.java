package fr.sirs.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.util.SirsStringConverter;
import java.awt.Color;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.odftoolkit.simple.TextDocument;


/**
 * Formulaire de génération de template ODT.
 *
 * @author Cédric Briançon (Geomatys)
 */
public class TemplateGeneratorPane extends VBox{
    private static final Image ICON_DOWNLOAD =
            SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_DOWNLOAD, 16, Color.BLACK), null);

    private static final String CSS_WHITE_WITH_BORDERS = "white-with-borders";
    private static final String CSS_POJOTABLE_HEADER = "pojotable-header";

    /**
     * Attributs que l'on ne souhaite pas garder dans le formulaire.
     */
    private static final List<String> FIELDS_TO_IGNORE = Arrays.asList(new String[] {SIRS.AUTHOR_FIELD, SIRS.VALID_FIELD, SIRS.FOREIGN_PARENT_ID_FIELD});

    /**
     * Liste des classes possibles.
     */
    private final ComboBox<Class> uiClassChoice = new ComboBox<>();

    private final Label progressLabel = new Label();

    /**
     * Liste des propriétés sélectionnées pour l'export ODT.
     */
    private Set<String> selectedProperties = new HashSet<>();

    public TemplateGeneratorPane() {
        setPadding(new Insets(0, 10, 0, 10));
        setSpacing(10);

        final Label title = new Label("Générateur de modèles .odt");
        title.getStyleClass().add(CSS_POJOTABLE_HEADER);
        title.setAlignment(Pos.CENTER);
        final BorderPane titlePane = new BorderPane(title);
        titlePane.setMaxWidth(Double.MAX_VALUE);
        getChildren().add(titlePane);

        final VBox vboxChoices = new VBox();
        vboxChoices.setPadding(new Insets(10));
        vboxChoices.getChildren().add(uiClassChoice);
        vboxChoices.getStyleClass().add(CSS_WHITE_WITH_BORDERS);

        uiClassChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            progressLabel.setText("");
            if (newValue != null) {
                // Rafraichissement de la liste des cases à cocher par rapport à la nouvelle classe choisie
                if (vboxChoices.getChildren().size() > 1 && vboxChoices.getChildren().get(1) instanceof GridPane) {
                    vboxChoices.getChildren().remove(1);
                }

                final GridPane gridBoxes = new GridPane();
                gridBoxes.setHgap(5);
                gridBoxes.setVgap(5);
                gridBoxes.setPadding(new Insets(10));

            final Function<String, String> titleBuilder = createPropertyNameMapper(uiClassChoice.getValue());
                final Map<String, PropertyDescriptor> props;
                try {
                    props = SIRS.listSimpleProperties(newValue);
                } catch (IntrospectionException ex) {
                    SirsCore.LOGGER.warning("Impossible de charger les propriétés de l'objet : " + newValue.getName());
                    return;
                }
                int i = 0, j = 0;
                for (final Map.Entry<String, PropertyDescriptor> entry : props.entrySet()) {
                    // Ajout des checkbox pour la nouvelle classe choisie
                    final CheckBox checkBox = new CheckBox();
                    final String key = entry.getKey();
                    if (FIELDS_TO_IGNORE.contains(key)) {
                        // On ne garde pas cet attribut
                        continue;
                    }
                    checkBox.setText(titleBuilder.apply(key));
                    checkBox.setOnAction(event -> {
                        progressLabel.setText("");
                        if (checkBox.isSelected()) {
                            selectedProperties.add(key);
                        } else if (selectedProperties.contains(key)) {
                            selectedProperties.remove(key);
                        }
                    });
                    // Placement dans la grille sur 2 colonnes
                    if (i == 0) {
                        gridBoxes.add(checkBox, i, j);
                        i++;
                    } else {
                        gridBoxes.add(checkBox, i, j);
                        i = 0;
                        j++;
                    }
                }
                vboxChoices.getChildren().add(gridBoxes);
            }
        });

        // Build model class list.
        final ObservableList<Class> clazz = FXCollections.observableArrayList(Injector.getSession().getAvailableModels());
        Collections.sort(clazz, (o1, o2) -> {
            final SirsStringConverter converter = new SirsStringConverter();
            return Collator.getInstance().compare(converter.toString(o1), converter.toString(o2));
        });
        SIRS.initCombo(uiClassChoice, clazz, null);
        getChildren().add(vboxChoices);

        // Action launched on template generation.
        final Button generateBtn = new Button("Générer le template");
        generateBtn.setOnAction(event -> {
            try {
                generate();
            } catch (IOException ex) {
                SirsCore.LOGGER.log(Level.WARNING, "Erreur à la génération du document", ex);
            }
        });
        generateBtn.setGraphic(new ImageView(ICON_DOWNLOAD));
        generateBtn.visibleProperty().bind(uiClassChoice.getSelectionModel().selectedItemProperty().isNotNull());
        final BorderPane generateButtonPane = new BorderPane();
        generateButtonPane.setRight(generateBtn);
        getChildren().add(generateButtonPane);

        getChildren().add(new BorderPane(progressLabel));

        setMinWidth(100);
        setMaxWidth(Double.MAX_VALUE);
    }

    /**
     * Génère le fichier ODT de template avec les propriétés choisies.
     *
     * @throws IOException si une erreur est survenue à l'écriture du fichier ODT.
     */
    private void generate() throws IOException {
        final FileChooser chooser = new FileChooser();
        final FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Open Document Format", "*.odt");
        chooser.getExtensionFilters().add(filter);
        chooser.setSelectedExtensionFilter(filter);
        chooser.setInitialFileName("template.odt");

        final File outputFile = chooser.showSaveDialog(null);
        if(outputFile==null) return;

        final TextDocument doc;
        try {
            doc = TextDocument.newTextDocument();

            final Function<String, String> titleBuilder = createPropertyNameMapper(uiClassChoice.getValue());
            final StringBuilder sb = new StringBuilder();
            for (final String prop : selectedProperties) {
                sb.append(titleBuilder.apply(prop)).append(" : ${").append(prop).append("}\n");
            }

            doc.addParagraph(sb.toString());
            doc.save(outputFile);
            progressLabel.setText("Document ODT généré avec succès");
        } catch (Exception ex) {
            progressLabel.setText("Erreur à la génération du document !");
            throw new IOException(ex);
        }
    }

    /**
     * Create a function giving display name for properties of a given class.
     * @param propertyHolder Class holding properties to translate.
     * @return A function which take a property name, and return a display name for it.
     */
    public static Function<String, String> createPropertyNameMapper(final Class propertyHolder) {
        final LabelMapper mapper = LabelMapper.get(propertyHolder);
        if (mapper == null) {
            /*
             * If we cannot find any mapper, we try to build a decent name by putting space on word end / beginning.
             */
            return (input) -> input.replaceAll("([A-Z0-9][^A-Z0-9])", " $1").replaceAll("([^A-Z0-9\\s])([A-Z0-9])", "$1 $2");
        } else {
            return (input) -> mapper.mapPropertyName(input);
        }
    }
}
