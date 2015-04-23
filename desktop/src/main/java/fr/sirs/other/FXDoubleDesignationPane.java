package fr.sirs.other;

import fr.sirs.Injector;
import static fr.sirs.SIRS.BUNDLE_KEY_CLASS;
import fr.sirs.Session;
import fr.sirs.core.component.ValiditySummaryRepository;
import fr.sirs.core.model.ValiditySummary;
import fr.sirs.util.FXValiditySummaryToElementTableColumn;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXDoubleDesignationPane extends BorderPane {

    private TableView<ValiditySummary> designations;
    private final Session session = Injector.getSession();
    private final ValiditySummaryRepository valididySummaryRepository = session.getValiditySummaryRepository();
    private final List<ValiditySummary> validitySummaries;

    private enum ValiditySummaryChoice {
        DOUBLON, ALL
    };

    public FXDoubleDesignationPane(final Class type) {
        final ResourceBundle vsBundle = ResourceBundle.getBundle(ValiditySummary.class.getName());

        validitySummaries = valididySummaryRepository.getDesignationsForClass(type);

        designations = new TableView<>(FXCollections.observableArrayList(validitySummaries));
        designations.setEditable(false);

        designations.getColumns().add(new FXValiditySummaryToElementTableColumn());

        final TableColumn<ValiditySummary, String> designationColumn = new TableColumn<>(vsBundle.getString("pseudoId"));
        designationColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ValiditySummary, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ValiditySummary, String> param) {
                return new SimpleObjectProperty<>(param.getValue().getDesignation());
            }
        });
        designations.getColumns().add(designationColumn);
        
        final TableColumn<ValiditySummary, String> labelColumn = new TableColumn<>(vsBundle.getString("label"));
        labelColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ValiditySummary, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ValiditySummary, String> param) {
                return new SimpleObjectProperty(param.getValue().getLabel());
            }
        });
        designations.getColumns().add(labelColumn);
        setCenter(designations);

        final ResourceBundle topBundle = ResourceBundle.getBundle(type.getName());
        final Label uiTitle = new Label("Occurrences des désignations pour les entités " + topBundle.getString(BUNDLE_KEY_CLASS));
        uiTitle.getStyleClass().add("pojotable-header");
        uiTitle.setAlignment(Pos.CENTER);
        uiTitle.setPadding(new Insets(5));
        uiTitle.setPrefWidth(USE_COMPUTED_SIZE);

        final ChoiceBox<ValiditySummaryChoice> choiceBox = new ChoiceBox<>(FXCollections.observableArrayList(ValiditySummaryChoice.values()));
        choiceBox.setConverter(new StringConverter<ValiditySummaryChoice>() {

            @Override
            public String toString(ValiditySummaryChoice object) {
                final String result;
                switch (object) {
                    case DOUBLON:
                        result = "Doublons";
                        break;
                    case ALL:
                    default:
                        result = "Tous";
                }
                return result;
            }

            @Override
            public ValiditySummaryChoice fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        choiceBox.setValue(ValiditySummaryChoice.ALL);
        choiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ValiditySummaryChoice>() {

            @Override
            public void changed(ObservableValue<? extends ValiditySummaryChoice> observable, ValiditySummaryChoice oldValue, ValiditySummaryChoice newValue) {
                final List<ValiditySummary> referenceUsages;
                switch (choiceBox.getSelectionModel().getSelectedItem()) {
                    case DOUBLON:
                        referenceUsages = doublons();
                        break;
                    case ALL:
                    default:
                        referenceUsages = validitySummaries;
                }
                designations.setItems(FXCollections.observableArrayList(referenceUsages));
            }
        });

        final HBox hBox = new HBox(choiceBox);
        hBox.setAlignment(Pos.CENTER);
        hBox.setPadding(new Insets(20));
        hBox.setSpacing(100);

        final VBox vBox = new VBox(uiTitle, hBox);

        setTop(vBox);

    }

    private List<ValiditySummary> doublons() {

        final List<String> doubleids = new ArrayList<>();

        // Détection des identifiants doublons
        final List<String> ids = new ArrayList<>();
        for (final ValiditySummary validitySummary : validitySummaries) {

            if (validitySummary.getDesignation() != null) {
                if (!ids.contains(validitySummary.getDesignation())) {
                    ids.add(validitySummary.getDesignation());
                } else if (!doubleids.contains(validitySummary.getDesignation())) {
                    doubleids.add(validitySummary.getDesignation());
                }
            }
            ids.add(validitySummary.getDesignation());
        }

        // Maintenant on sait quels sont les id doublons
        final List<ValiditySummary> referenceUsages = new ArrayList<>();

        for (final ValiditySummary validitySummary : validitySummaries) {
            if (validitySummary.getDesignation() != null && doubleids.contains(validitySummary.getDesignation())) {
                referenceUsages.add(validitySummary);
            }
        }
        return referenceUsages;
    }

}
