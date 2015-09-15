package fr.sirs.plugin.reglementaire.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.model.EtapeObligationReglementaire;
import fr.sirs.core.model.PlanificationObligationReglementaire;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.FXFreeTab;
import fr.sirs.util.SimpleFXEditMode;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.geotoolkit.filter.DefaultPropertyIsNull;
import org.geotoolkit.filter.DefaultPropertyName;
import org.geotoolkit.filter.binarylogic.DefaultAnd;
import org.opengis.filter.Filter;

/**
 *
 */
public class EtapesPojoTable extends PojoTable {

    private final CheckBox uiHideRealizedCheckBox = new CheckBox("Masquer les étapes réalisées");

    public EtapesPojoTable(final TabPane tabPane) {
        super(EtapeObligationReglementaire.class, "Etapes d'obligations réglementaires");

        final Button uiPlanificationBtn = new Button(null, new ImageView(SIRS.ICON_CLOCK_WHITE));
        uiPlanificationBtn.getStyleClass().add(BUTTON_STYLE);
        uiPlanificationBtn.setTooltip(new Tooltip("Planification automatique"));
        uiPlanificationBtn.setOnMouseClicked(event -> showPlanificationTable(tabPane));
        searchEditionToolbar.getChildren().add(1, uiPlanificationBtn);

        if (getFilterUI() instanceof VBox) {
            final VBox vbox = (VBox) getFilterUI();
            vbox.getChildren().add(vbox.getChildren().size() - 1, uiHideRealizedCheckBox);
            uiHideRealizedCheckBox.setStyle("-fx-text-fill: #FFF");
        }
    }

    @Override
    public Filter getFilter() {
        if (uiHideRealizedCheckBox == null || !uiHideRealizedCheckBox.isSelected()) {
            return super.getFilter();
        }

        final Filter filterNotRealized = new DefaultPropertyIsNull(new DefaultPropertyName("dateRealisation"));
        final Filter filter = super.getFilter();
        if (filter == null) {
            return filterNotRealized;
        }

        return new DefaultAnd(filterNotRealized, filter);
    }

    @Override
    public void resetFilter(final VBox filterContent) {
        super.resetFilter(filterContent);

        uiHideRealizedCheckBox.setSelected(false);
    }

    /**
     //     * Ouvre un onglet sur la table des planifications.
     //     *
     //     * @param tabPane Le conteneur d'onglet dans lequel ajouter ce nouvel onglet.
     //     */
    private void showPlanificationTable(final TabPane tabPane) {
        final FXFreeTab planTab = new FXFreeTab("Panification");
        // Gestion du bouton consultation / édition pour la pojo table
        final Separator separator = new Separator();
        separator.setVisible(false);
        final SimpleFXEditMode editMode = new SimpleFXEditMode();
        final HBox topPane = new HBox(separator, editMode);
        HBox.setHgrow(separator, Priority.ALWAYS);
        final PojoTable pojoTable = new PojoTable(Injector.getSession().getRepositoryForClass(
                PlanificationObligationReglementaire.class), "Planification(s) programmée(s)");
        pojoTable.editableProperty().bind(editMode.editionState());
        planTab.setContent(new BorderPane(pojoTable, topPane, null, null, null));
        tabPane.getTabs().add(planTab);
        tabPane.getSelectionModel().select(planTab);
    }
}
