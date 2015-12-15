
package fr.sirs.other;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import static fr.sirs.SIRS.BUNDLE_KEY_CLASS;
import fr.sirs.Session;
import fr.sirs.core.component.ReferenceUsageRepository;
import fr.sirs.core.model.AvecLibelle;
import fr.sirs.core.model.ReferenceType;
import fr.sirs.core.model.ReferenceUsage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import javafx.util.Callback;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXReferenceAnalysePane extends BorderPane {

    private TableView<ReferenceUsage> usages;
    private final Session session = Injector.getSession();
    private final Map<String, ResourceBundle> bundles = new HashMap<>();

    public FXReferenceAnalysePane(final ReferenceType reference) {
        final ResourceBundle bundle = ResourceBundle.getBundle(ReferenceUsage.class.getName(), Locale.getDefault(),
                Thread.currentThread().getContextClassLoader());

        try {
            final Method getId = reference.getClass().getMethod("getId");
            final String id = (String) getId.invoke(reference);
            final ReferenceUsageRepository referenceUsageRepository = session.getReferenceUsageRepository();
            final List<ReferenceUsage> referenceUsages = new ArrayList<>();

            final Task task = new Task() {

                @Override
                protected Object call() throws Exception {
                    updateMessage("Recherche de l'utilisation d'une référence.");
                    referenceUsages.addAll(referenceUsageRepository.getReferenceUsages(id));
                    return null;
                }
            };

            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

                @Override
                public void handle(WorkerStateEvent event) {
                    if(!referenceUsages.isEmpty()){
                        usages = new TableView<>(SIRS.observableList(referenceUsages));
                        usages.setEditable(false);

                        final TableColumn<ReferenceUsage, String> propertyColumn = new TableColumn<>(bundle.getString("property"));
                        propertyColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ReferenceUsage, String>, ObservableValue<String>>() {

                            @Override
                            public ObservableValue<String> call(TableColumn.CellDataFeatures<ReferenceUsage, String> param) {
                                return new SimpleObjectProperty<>(param.getValue().getProperty());
                            }
                        });
                        usages.getColumns().add(propertyColumn);

                        final TableColumn<ReferenceUsage, String> objectIdColumn = new TableColumn<>(bundle.getString("objectId"));
                        objectIdColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ReferenceUsage, String>, ObservableValue<String>>() {

                            @Override
                            public ObservableValue<String> call(TableColumn.CellDataFeatures<ReferenceUsage, String> param) {
                                return new SimpleObjectProperty(param.getValue().getObjectId());
                            }
                        });
                        usages.getColumns().add(objectIdColumn);

                        final TableColumn<ReferenceUsage, String> typeColumn = new TableColumn<>(bundle.getString("type"));
                        typeColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ReferenceUsage, String>, ObservableValue<String>>() {

                            @Override
                            public ObservableValue<String> call(TableColumn.CellDataFeatures<ReferenceUsage, String> param) {
                                String type = param.getValue().getType();
                                if(bundles.get(type)==null) bundles.put(type, ResourceBundle.getBundle(type, Locale.getDefault(), Thread.currentThread().getContextClassLoader()));
                                type = bundles.get(type).getString(BUNDLE_KEY_CLASS);
                                return new SimpleObjectProperty(type);
                            }
                        });
                        usages.getColumns().add(typeColumn);

                        final TableColumn<ReferenceUsage, String> labelColumn = new TableColumn<>(bundle.getString("label"));
                        labelColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ReferenceUsage, String>, ObservableValue<String>>() {

                            @Override
                            public ObservableValue<String> call(TableColumn.CellDataFeatures<ReferenceUsage, String> param) {
                                return new SimpleObjectProperty(param.getValue().getLabel());
                            }
                        });
                        usages.getColumns().add(labelColumn);
                        setCenter(usages);

                        final ResourceBundle topBundle = ResourceBundle.getBundle(reference.getClass().getName(), Locale.getDefault(), Thread.currentThread().getContextClassLoader());
                        final Label uiTitle = new Label("Usages dans la base de la référence \""+ ((AvecLibelle)reference).getLibelle()+"\" ("+topBundle.getString(BUNDLE_KEY_CLASS)+")");
                        uiTitle.getStyleClass().add("pojotable-header");
                        uiTitle.setAlignment(Pos.CENTER);
                        uiTitle.setPadding(new Insets(5));
                        uiTitle.setPrefWidth(USE_COMPUTED_SIZE);
                        setTop(uiTitle);

                    }
                }
            });

            Injector.getSession().getTaskManager().submit(task);

        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            SIRS.LOGGER.log(Level.SEVERE, null, ex);
        }
    }


}
