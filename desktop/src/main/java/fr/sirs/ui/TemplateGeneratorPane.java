package fr.sirs.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.LabelMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 *
 */
public class TemplateGeneratorPane extends VBox{
    private static final String FOREIGN_PARENT_ID = "foreignParentId";

    /**
     * Liste des classes possibles.
     */
    private final ComboBox<Class> uiClassChoice = new ComboBox<>();

    /**
     * Liste des propriétés sélectionnées pour l'export ODT.
     */
    private Set<String> selectedProperties = new HashSet<>();

    public TemplateGeneratorPane() {
        setPadding(new Insets(0, 0, 0, 10));

        final Label title = new Label("Création d'un template");
        title.getStyleClass().add("pojotable-header");
        title.setAlignment(Pos.CENTER);
        getChildren().add(title);

        final VBox vboxChoices = new VBox();
        vboxChoices.setPadding(new Insets(10));
        vboxChoices.getChildren().add(uiClassChoice);

        uiClassChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
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
                    if (FOREIGN_PARENT_ID.equals(key)) {
                        // On ne garde pas cet attribut
                        continue;
                    }
                    checkBox.setText(cvt.toString(key));
                    checkBox.setOnAction(event -> {
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

        setMinWidth(100);
        setMaxWidth(Double.MAX_VALUE);
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
