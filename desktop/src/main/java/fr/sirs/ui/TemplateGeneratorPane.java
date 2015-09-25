package fr.sirs.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.LabelMapper;
import java.awt.Color;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
import javafx.util.StringConverter;
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
    private static final String FOREIGN_PARENT_ID = "foreignParentId";
    private static final String AUTHOR = "author";
    private static final String VALID = "valid";
    private static final String DATE_MAJ = "dateMaj";

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

        final Label title = new Label("Création d'un template");
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

                final AttributeConverter cvt = new AttributeConverter();
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
                    if (FOREIGN_PARENT_ID.equals(key) || AUTHOR.equals(key) || VALID.equals(key) || DATE_MAJ.equals(key)) {
                        // On ne garde pas cet attribut
                        continue;
                    }
                    checkBox.setText(cvt.toString(key));
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

        final ObservableList<Class> clazz = FXCollections.observableArrayList(Injector.getSession().getAvailableModels());
        Collections.sort(clazz, (o1, o2) ->
                Collator.getInstance().compare(LabelMapper.get(o1).mapClassName(), LabelMapper.get(o2).mapClassName()));
        SIRS.initCombo(uiClassChoice, clazz, null);
        getChildren().add(vboxChoices);

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

            final AttributeConverter cvt = new AttributeConverter();
            final StringBuilder sb = new StringBuilder();
            for (final String prop : selectedProperties) {
                sb.append(cvt.toString(prop)).append(" : ${").append(prop).append("}\n");
            }

            doc.addParagraph(sb.toString());
            doc.save(outputFile);
            progressLabel.setText("Document ODT généré avec succès");
        } catch (Exception ex) {
            progressLabel.setText("Erreur à la génération du document !");
            throw new IOException(ex);
        }
    }

    private class AttributeConverter extends StringConverter<String> {

        @Override
        public String toString(String object) {
            return LabelMapper.get(uiClassChoice.getSelectionModel().getSelectedItem()).mapPropertyName(object);
        }

        @Override
        public String fromString(String string) {
            return string;
        }
    }
}
