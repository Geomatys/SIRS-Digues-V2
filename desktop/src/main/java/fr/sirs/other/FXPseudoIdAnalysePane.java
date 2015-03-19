package fr.sirs.other;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import static fr.sirs.SIRS.ICON_CHECK_CIRCLE;
import fr.sirs.Session;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.ValiditySummaryRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ValiditySummary;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;
import org.geotoolkit.internal.GeotkFX;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXPseudoIdAnalysePane extends BorderPane {

    private TableView<ValiditySummary> pseudoIds;
    private final Session session = Injector.getSession();
    final ValiditySummaryRepository valididySummaryRepository = session.getValiditySummaryRepository();
    final List<ValiditySummary> validitySummaries;
    private final Map<String, ResourceBundle> bundles = new HashMap<>();

    private enum ValiditySummaryChoice {

        DOUBLON, ALL
    };

    public FXPseudoIdAnalysePane(final Class type) {
        final ResourceBundle bundle = ResourceBundle.getBundle(ValiditySummary.class.getName());

        validitySummaries = valididySummaryRepository.getDesignationsForClass(type);

        pseudoIds = new TableView<>(FXCollections.observableArrayList(validitySummaries));
        pseudoIds.setEditable(false);

        pseudoIds.getColumns().add(new StateColumn());

        final TableColumn<ValiditySummary, String> propertyColumn = new TableColumn<>(bundle.getString("pseudoId"));
        propertyColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ValiditySummary, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ValiditySummary, String> param) {
                return new SimpleObjectProperty<>(param.getValue().getDesignation());
            }
        });
        pseudoIds.getColumns().add(propertyColumn);

        final TableColumn<ValiditySummary, String> objectIdColumn = new TableColumn<>(bundle.getString("elementId"));
        objectIdColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ValiditySummary, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ValiditySummary, String> param) {
                return new SimpleObjectProperty(param.getValue().getElementId());
            }
        });
        pseudoIds.getColumns().add(objectIdColumn);
//                
        final TableColumn<ValiditySummary, String> labelColumn = new TableColumn<>(bundle.getString("label"));
        labelColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ValiditySummary, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ValiditySummary, String> param) {
                return new SimpleObjectProperty(param.getValue().getLabel());
            }
        });
        pseudoIds.getColumns().add(labelColumn);
        setCenter(pseudoIds);

        final ResourceBundle topBundle = ResourceBundle.getBundle(type.getName());
        final Label uiTitle = new Label("Occurrences des désignations pour les entités " + topBundle.getString("class"));
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
                pseudoIds.setItems(FXCollections.observableArrayList(referenceUsages));
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

    public class StateButtonTableCell extends ButtonTableCell<ValiditySummary, Object> {

        private final Node defaultGraphic;

        public StateButtonTableCell(Node graphic) {
            super(false, graphic, (Object t) -> true, new Function<Object, Object>() {
                @Override
                public Object apply(Object t) {

                    if (t != null && t instanceof ValiditySummary) {
                        final Session session = Injector.getSession();
                        final AbstractSIRSRepository repo = session.getRepositoryForType(((ValiditySummary) t).getDocClass());
                        final Element docu = (Element) repo.get(((ValiditySummary) t).getDocId());

                        // Si l'elementid est null, c'est que l'élément est le document lui-même
                        if (((ValiditySummary) t).getElementId() == null) {
                            session.getFrame().addTab(session.getOrCreateElementTab(docu));
                        } // Sinon, c'est que l'élément est inclus quelque part dans le document et il faut le rechercher.
                        else {
                            final Element elt = docu.getChildById(((ValiditySummary) t).getElementId());
                            session.getFrame().addTab(session.getOrCreateElementTab(elt));
                        }
                    }
                    return t;
                }
            });
            defaultGraphic = graphic;
        }

        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null) {
                button.setGraphic(new ImageView(SIRS.ICON_EYE));
            }
        }
    }

    private class StateColumn extends TableColumn<ValiditySummary, Object> {

        public StateColumn() {
            super("Détail");
            setEditable(false);
            setSortable(false);
            setResizable(true);
            setPrefWidth(70);
//            setPrefWidth(24);
//            setMinWidth(24);
//            setMaxWidth(24);
            setGraphic(new ImageView(GeotkFX.ICON_MOVEUP));

            setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ValiditySummary, Object>, ObservableValue<Object>>() {
                @Override
                public ObservableValue<Object> call(TableColumn.CellDataFeatures<ValiditySummary, Object> param) {
                    return new SimpleObjectProperty<>(param.getValue());
                }
            });

            setCellFactory(new Callback<TableColumn<ValiditySummary, Object>, TableCell<ValiditySummary, Object>>() {

                @Override
                public TableCell<ValiditySummary, Object> call(TableColumn<ValiditySummary, Object> param) {

                    return new StateButtonTableCell(new ImageView(ICON_CHECK_CIRCLE));
                }
            });
        }
    }

}
