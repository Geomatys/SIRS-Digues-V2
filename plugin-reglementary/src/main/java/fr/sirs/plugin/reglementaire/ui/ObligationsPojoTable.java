package fr.sirs.plugin.reglementaire.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.SystemeEndiguementRepository;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.core.model.PlanificationObligationReglementaire;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.FXFreeTab;
import fr.sirs.util.SimpleFXEditMode;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.geotoolkit.filter.DefaultPropertyIsNull;
import org.geotoolkit.filter.DefaultPropertyName;
import org.geotoolkit.filter.binarylogic.DefaultAnd;
import org.geotoolkit.gui.javafx.util.FXTableCell;
import org.opengis.filter.Filter;

/**
 * Table présentant les obligations réglementaires.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class ObligationsPojoTable extends PojoTable {
    /**
     * Nom de la colonne à ajouter pour la table.
     */
    private static final String CLASS_PROP_NAME = "Classe";

    private final CheckBox uiHideRealizedCheckBox = new CheckBox("Masquer les étapes réalisées");

    /**
     * Création de la table présentant les obligations réglementaires.
     *
     * @param clazz   Classe d'objets affichés par cette table
     * @param tabPane Conteneur d'onglets dans lequel afficher l'onglet de planification
     */
    public ObligationsPojoTable(final Class clazz, final TabPane tabPane) {
        super(clazz, "Liste des obligations réglementaires");

        getUiTable().getColumns().add(5, new SEClassTableColumn());

        final Button uiPlanificationBtn = new Button(null, new ImageView(SIRS.ICON_CLOCK_WHITE));
        uiPlanificationBtn.getStyleClass().add(BUTTON_STYLE);
        uiPlanificationBtn.setTooltip(new Tooltip("Planification automatique"));
        uiPlanificationBtn.setOnMouseClicked(event -> showPlanificationTable(tabPane));
        searchEditionToolbar.getChildren().add(2, uiPlanificationBtn);

        if (getFilterUI() instanceof VBox) {
            final VBox vbox = (VBox) getFilterUI();
            vbox.getChildren().add(vbox.getChildren().size() - 1, uiHideRealizedCheckBox);
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
     * Ouvre un onglet sur la table des planifications.
     *
     * @param tabPane Le conteneur d'onglet dans lequel ajouter ce nouvel onglet.
     */
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


    /**
     * Colonne représentant la classe du système d'endiguement.
     */
    private class SEClassTableColumn extends TableColumn<ObligationReglementaire, String> {
        public SEClassTableColumn() {
            super(CLASS_PROP_NAME);

            setCellValueFactory(param -> param.getValue().systemeEndiguementIdProperty());
            setCellFactory(param -> new SEClassCell());

            setEditable(false);
        }
    }

    /**
     * Cellule représentant la classe du système d'endiguement.
     */
    private class SEClassCell extends FXTableCell<ObligationReglementaire, String> {
        public SEClassCell() {
            super();
            setAlignment(Pos.CENTER);
        }

        /**
         * Garde la correspondance entre la classe du système d'endiguement affichée dans la cellule et la propriété
         * correspondante dans le système d'endiguement.
         *
         * @param item La propriété id du système d'endiguement
         * @param empty Vrai si la cellule est vide
         */
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (!empty && item != null) {
                textProperty().bind(Injector.getBean(SystemeEndiguementRepository.class).get(item).classementProperty());
            } else {
                textProperty().unbind();
                setText(null);
            }
        }
    }
}
