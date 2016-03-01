package fr.sirs.other;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.ReferenceUsageRepository;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.ReferenceType;
import fr.sirs.core.model.ReferenceUsage;
import java.io.InterruptedIOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import org.ektorp.DbAccessException;
import org.geotoolkit.gui.javafx.util.ButtonTableCell;
import org.geotoolkit.gui.javafx.util.TaskManager;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @author Aleis Manin   (Geomatys)
 */
public class FXReferenceAnalysePane extends BorderPane {

    public FXReferenceAnalysePane(final ReferenceType reference) {
        final LabelMapper mapper = LabelMapper.get(reference.getClass());
        final Label uiTitle = new Label(new StringBuilder("Usages dans la base de la référence \"").append(reference.getLibelle()).append("\" (").append(mapper.mapClassName()).append(")").toString());
        uiTitle.getStyleClass().add("pojotable-header");
        uiTitle.setAlignment(Pos.CENTER);
        uiTitle.setPadding(new Insets(5));
        uiTitle.setPrefWidth(USE_COMPUTED_SIZE);
        setTop(uiTitle);

        // Compute statistics for queried reference.
        final Task<List<ReferenceUsage>> task = new TaskManager.MockTask(
                "Recherche de l'utilisation d'une référence.",
                () -> getUsage(reference));
        task.setOnSucceeded(evt -> SIRS.fxRun(false, () -> displayReferences(task.getValue())));
        Injector.getSession().getTaskManager().submit(task);
    }

    /**
     * Display given reference usages in a table view.
     * @param referenceUsages Objects to display.
     */
    private void displayReferences(final List<ReferenceUsage> referenceUsages) {
        if (referenceUsages.isEmpty()) {
            setCenter(null);
        } else {
            final LabelMapper mapper = LabelMapper.get(ReferenceUsage.class);

            final TableView<ReferenceUsage> usages = new TableView<>(SIRS.observableList(referenceUsages));
            usages.setEditable(false);

            final TableColumn<ReferenceUsage, String> typeColumn = new TableColumn<>(mapper.mapPropertyName("type"));
            typeColumn.setCellValueFactory((TableColumn.CellDataFeatures<ReferenceUsage, String> param) -> {
                String type = param.getValue().getType();
                try {
                    LabelMapper targetMapper = LabelMapper.get(Class.forName(type, false, Thread.currentThread().getContextClassLoader()));
                    if (targetMapper != null) {
                        type = targetMapper.mapClassName();
                    }
                } catch (ClassNotFoundException e) {
                    SIRS.LOGGER.log(Level.FINE, "Cannot find title for type ".concat(type), e);
                }

                return new SimpleObjectProperty(type);
            });

            final TableColumn<ReferenceUsage, String> labelColumn = new TableColumn<>(mapper.mapPropertyName("label"));
            labelColumn.setCellValueFactory((TableColumn.CellDataFeatures<ReferenceUsage, String> param) -> new SimpleStringProperty(param.getValue().getLabel()));

            usages.getColumns().addAll(new EditColumn(), typeColumn, labelColumn);
            setCenter(usages);
        }
    }

    /**
     * Find the element pointed by input {@link ReferenceUsage#getObjectId() }
     * property, and display an editor for it when user click on the cell.
     */
    public static class EditColumn extends TableColumn<ReferenceUsage, String> {

        public EditColumn() {
            super("Edition");
            setSortable(false);
            setResizable(false);
            setPrefWidth(24);
            setMinWidth(24);
            setMaxWidth(24);
            setGraphic(new ImageView(SIRS.ICON_EDIT_BLACK));

            final Tooltip tooltip = new Tooltip("Ouvrir la fiche de l'élément");

            final Predicate<String> enabled = param -> param != null && !param.isEmpty();
            final Function<String, Boolean> openEditor = param -> {
                Injector.getSession().showEditionTab(param);
                return true;
            };

            setCellValueFactory((param) -> new SimpleStringProperty(param.getValue().getObjectId()));

            setCellFactory((TableColumn<ReferenceUsage, String> param) -> {
                final ButtonTableCell button = new ButtonTableCell(false, new ImageView(SIRS.ICON_EDIT_BLACK), o -> o != null, openEditor);
                button.setTooltip(tooltip);
                return button;
            });
        }
    }

    /**
     * Find usages of a given reference in database.
     *
     * Note : as view can be long to build, we loop over timeout errors, until a
     * result or another error is thrown.
     * @param ref The reference to find usage for in database.
     * @return List of found references.
     */
    private List<ReferenceUsage> getUsage(final ReferenceType ref) {
        final ReferenceUsageRepository repo = Injector.getSession().getReferenceUsageRepository();
        Throwable e = new TimeoutException();
        while ((e instanceof TimeoutException || e instanceof InterruptedIOException) && !Thread.currentThread().isInterrupted()) {
            try {
                return repo.getReferenceUsages(ref.getId());
            } catch (DbAccessException newException) {
                final Throwable tmp = newException.getCause() == null? newException : newException.getCause();
                tmp.addSuppressed(e);
                e = tmp;
            }
        }

        throw new DbAccessException(e);
    }
}
