package fr.sirs.other;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.Repository;
import fr.sirs.core.component.ValiditySummaryRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ValiditySummary;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.geotoolkit.font.FontAwesomeIcons;
import org.geotoolkit.font.IconBuilder;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;
import org.geotoolkit.internal.GeotkFX;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXPseudoIdAnalysePane extends BorderPane {
    
    public static final Image ICON_CHECK_CIRCLE = SwingFXUtils.toFXImage(IconBuilder.createImage(FontAwesomeIcons.ICON_CHECK_CIRCLE, 16, Color.decode("#00aa00")),null);
    
    private TableView<ValiditySummary> pseudoIds;
    private final Session session = Injector.getSession();
    private final Map<String, ResourceBundle> bundles = new HashMap<>();

    public FXPseudoIdAnalysePane(final Class type) {
        final ResourceBundle bundle = ResourceBundle.getBundle(ValiditySummary.class.getName());

        final ValiditySummaryRepository valididySummaryRepository = session.getValiditySummaryRepository();
        final List<ValiditySummary> validitySummaries = valididySummaryRepository.getPseudoIdsForClass(type);

        if (validitySummaries != null && !validitySummaries.isEmpty()) {
            pseudoIds = new TableView<>(FXCollections.observableArrayList(validitySummaries));
            pseudoIds.setEditable(false);

            pseudoIds.getColumns().add(new StateColumn());

            final TableColumn<ValiditySummary, String> propertyColumn = new TableColumn<>(bundle.getString("pseudoId"));
            propertyColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ValiditySummary, String>, ObservableValue<String>>() {

                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<ValiditySummary, String> param) {
                    return new SimpleObjectProperty<>(param.getValue().getPseudoId());
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
            final Label uiTitle = new Label("Occurrences des pseudo-identifiants pour les entités " + topBundle.getString("class"));
            uiTitle.getStyleClass().add("pojotable-header");
            uiTitle.setAlignment(Pos.CENTER);
            uiTitle.setPadding(new Insets(5));
            uiTitle.setPrefWidth(USE_COMPUTED_SIZE);
            setTop(uiTitle);

        }
    }

    public class StateButtonTableCell extends ButtonTableCell<ValiditySummary, Object> {

        private final Node defaultGraphic;

        public StateButtonTableCell(Node graphic) {
            super(false, graphic, (Object t) -> true, new Function<Object, Object>() {
                @Override
                public Object apply(Object t) {

                    if (t != null && t instanceof ValiditySummary) {
                        final Session session = Injector.getSession();
                        final Repository repo = session.getRepositoryForType(((ValiditySummary) t).getDocClass());
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
