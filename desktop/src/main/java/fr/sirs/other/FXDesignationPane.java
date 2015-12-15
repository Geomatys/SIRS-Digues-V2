package fr.sirs.other;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import static fr.sirs.SIRS.PREVIEW_BUNDLE_KEY_DESIGNATION;
import static fr.sirs.SIRS.PREVIEW_BUNDLE_KEY_LIBELLE;
import fr.sirs.Session;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.Preview;
import fr.sirs.util.FXPreviewToElementDesignationTableColumn;
import fr.sirs.util.FXPreviewToElementTableColumn;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXDesignationPane extends BorderPane {

    private TableView<Preview> table;
    private final Session session = Injector.getSession();
    private final Previews repository = session.getPreviews();
    private final ChoiceBox<Choice> choiceBox = new ChoiceBox<>(FXCollections.observableArrayList(Choice.values()));

    private List<Preview> previews;

    private enum Choice {DOUBLON, ALL, EMPTY};

    public FXDesignationPane(final Class type) {
        final ResourceBundle previewBundle = ResourceBundle.getBundle(Preview.class.getName(),
                Locale.getDefault(), Thread.currentThread().getContextClassLoader());

        previews = repository.getByClass(type);

        table = new TableView<>(SIRS.observableList(previews));
        table.setEditable(true);

        table.getColumns().add(new FXPreviewToElementTableColumn());
        table.getColumns().add(new FXPreviewToElementDesignationTableColumn(previewBundle.getString(PREVIEW_BUNDLE_KEY_DESIGNATION)));

        final TableColumn<Preview, String> labelColumn = new TableColumn<>(previewBundle.getString(PREVIEW_BUNDLE_KEY_LIBELLE));
        labelColumn.setCellValueFactory((TableColumn.CellDataFeatures<Preview, String> param) -> {
                return new SimpleObjectProperty(param.getValue().getLibelle());
        });
        labelColumn.setEditable(false);
        table.getColumns().add(labelColumn);
        setCenter(table);

        choiceBox.setConverter(new StringConverter<Choice>() {

            @Override
            public String toString(Choice object) {
                final String result;
                switch (object) {
                    case DOUBLON:
                        result = "Doublons";
                        break;
                    case EMPTY:
                        result = "Sans désignation";
                        break;
                    case ALL:
                    default:
                        result = "Tous";
                }
                return result;
            }

            @Override
            public Choice fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        choiceBox.setValue(Choice.ALL);
        choiceBox.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Choice> observable, Choice oldValue, Choice newValue) -> {
                fillTable();
            });

        final Button reload = new Button("Recharger", new ImageView(SIRS.ICON_ROTATE_LEFT_ALIAS));
        reload.setOnAction((ActionEvent e) -> {previews = repository.getByClass(type); fillTable();});

        final HBox hBox = new HBox(choiceBox, reload);
        hBox.setAlignment(Pos.CENTER);
        hBox.setPadding(new Insets(20));
        hBox.setSpacing(100);

        setTop(hBox);

    }

    private void fillTable(){
        final List<Preview> referenceUsages;
        switch (choiceBox.getSelectionModel().getSelectedItem()) {
            case DOUBLON:
                referenceUsages = doublons();
                break;
            case EMPTY:
                referenceUsages = empty();
                break;
            case ALL:
            default:
                referenceUsages = previews;
        }
        table.setItems(SIRS.observableList(referenceUsages));
    }

    private List<Preview> empty() {

        // Maintenant on sait quels sont les id doublons
        final List<Preview> referenceUsages = new ArrayList<>();

        for (final Preview preview : previews) {
            if (preview.getDesignation() == null || "".equals(preview.getDesignation()) || " ".equals(preview.getDesignation())){
                referenceUsages.add(preview);
            }
        }
        return referenceUsages;
    }

    private List<Preview> doublons() {

        final List<String> doubleids = new ArrayList<>();

        // Détection des identifiants doublons
        final List<String> ids = new ArrayList<>();
        for (final Preview preview : previews) {

            if (preview.getDesignation() != null) {
                if (!ids.contains(preview.getDesignation())) {
                    ids.add(preview.getDesignation());
                } else if (!doubleids.contains(preview.getDesignation())) {
                    doubleids.add(preview.getDesignation());
                }
            }
            ids.add(preview.getDesignation());
        }

        // Maintenant on sait quels sont les id doublons
        final List<Preview> referenceUsages = new ArrayList<>();

        for (final Preview preview : previews) {
            if (preview.getDesignation() != null && doubleids.contains(preview.getDesignation())) {
                referenceUsages.add(preview);
            }
        }
        return referenceUsages;
    }

}
