package fr.sirs.theme.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.AvecForeignParent;
import fr.sirs.core.model.ParcelleVegetation;
import static fr.sirs.plugin.vegetation.PluginVegetation.isCoherent;
import fr.sirs.theme.AbstractTheme;
import fr.sirs.theme.TronconTheme;
import fr.sirs.util.SimpleFXEditMode;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class FXVegetationTronconThemePane extends FXTronconThemePane {

    public FXVegetationTronconThemePane(TronconTheme.ThemeManager ... groups) {
        super(groups);
    }

    protected class VegetationTronconThemePojoTable<T extends AvecForeignParent> extends TronconThemePojoTable<T>{

        public VegetationTronconThemePojoTable(TronconTheme.ThemeManager<T> group) {
            super(group);

            final TableColumn<ParcelleVegetation, ParcelleVegetation> alertColumn = new AlertTableColumn();
            getTable().getColumns().add((TableColumn) alertColumn);
        }
    }

    @Override
    protected Parent createContent(AbstractTheme.ThemeManager manager) {
        final Separator separator = new Separator();
        separator.setVisible(false);
        final SimpleFXEditMode editMode = new SimpleFXEditMode();
        final HBox topPane = new HBox(separator, editMode);
        HBox.setHgrow(separator, Priority.ALWAYS);

        final VegetationTronconThemePojoTable table = new VegetationTronconThemePojoTable(manager);
        table.setDeletor(manager.getDeletor());
        table.editableProperty().bind(editMode.editionState());
        table.foreignParentProperty().bindBidirectional(linearIdProperty());

        return new BorderPane(table, topPane, null, null, null);
    }

    private static class AlertTableColumn extends TableColumn<ParcelleVegetation, ParcelleVegetation> {

        public AlertTableColumn(){
            setGraphic(new ImageView(SIRS.ICON_EXCLAMATION_TRIANGLE_BLACK));
            setPrefWidth(24);
            setMinWidth(24);
            setMaxWidth(24);
            setCellValueFactory((TableColumn.CellDataFeatures<ParcelleVegetation, ParcelleVegetation> param) -> new SimpleObjectProperty(param.getValue()));
            setCellFactory((TableColumn<ParcelleVegetation, ParcelleVegetation> param) -> new AlertTableCell());
        }
    }


    private static class AlertTableCell extends TableCell<ParcelleVegetation, ParcelleVegetation>{
        @Override
        protected void updateItem(final ParcelleVegetation item, boolean empty){
            super.updateItem(item, empty);

            if(item!=null && item.getPlanId()!=null){

                final Runnable cellUpdater = () -> {
                    final ImageView image = isCoherent(item) ? null : new ImageView(SIRS.ICON_EXCLAMATION_TRIANGLE);
                    Platform.runLater(() -> setGraphic(image));
                };

                Injector.getSession().getTaskManager().submit("Vérification de la cohérence de traitement de la parcelle "+item.getDesignation(), cellUpdater);
            }
        }
    }
}
