package fr.sirs.plugin.reglementaire.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.component.SystemeEndiguementRepository;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.ObligationReglementaire;
import fr.sirs.core.model.RappelObligationReglementaire;
import fr.sirs.core.model.SystemeEndiguement;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.FXFreeTab;
import fr.sirs.util.SimpleFXEditMode;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import org.geotoolkit.gui.javafx.util.FXStringCell;

/**
 * Table présentant les obligations réglementaires.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class ObligationsPojoTable extends PojoTable {
    /**
     * Création de la table présentant les obligations réglementaires.
     *
     * @param obligationRepository Repository d'accès aux données d'obligations réglementaires
     * @param tabPane              Conteneur d'onglets dans lequel afficher l'onglet de planification
     */
    public ObligationsPojoTable(final AbstractSIRSRepository obligationRepository, final TabPane tabPane) {
        super(obligationRepository, "Liste des obligations réglementaires");

        getUiTable().getColumns().add(5, new ClassTableColumn());

        final Button uiPlanificationBtn = new Button(null, new ImageView(SIRS.ICON_CLOCK_WHITE));
        uiPlanificationBtn.getStyleClass().add(BUTTON_STYLE);
        uiPlanificationBtn.setTooltip(new Tooltip("Planification automatique"));
        searchEditionToolbar.getChildren().add(2, uiPlanificationBtn);

        uiPlanificationBtn.setOnMouseClicked(event -> showPlanificationTable(tabPane));
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
        final PojoTable pojoTable = new PojoTable(Injector.getSession().getRepositoryForClass(RappelObligationReglementaire.class),
                "Planification(s) programmée(s)");
        pojoTable.editableProperty().bind(editMode.editionState());
        planTab.setContent(new BorderPane(pojoTable, topPane, null, null, null));
        tabPane.getTabs().add(planTab);
        tabPane.getSelectionModel().select(planTab);
    }

    /**
     * Colonne représentant la classe du système d'endiguement
     */
    private class ClassTableColumn extends TableColumn<ObligationReglementaire, String> {
        public ClassTableColumn() {
            super("Classe");

            setCellValueFactory(param -> {
                final ObligationReglementaire obligation = param.getValue();
                if (obligation.getSystemeEndiguementId() != null) {
                    final SystemeEndiguement se = Injector.getBean(SystemeEndiguementRepository.class).get(obligation.getSystemeEndiguementId());
                    return new SimpleStringProperty(se.getClassement());
                }
                return new SimpleStringProperty();
            });

            setCellFactory(param -> new FXStringCell());

            setEditable(false);
        }
    }
}
