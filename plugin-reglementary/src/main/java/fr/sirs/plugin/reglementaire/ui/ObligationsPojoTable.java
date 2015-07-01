package fr.sirs.plugin.reglementaire.ui;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.RappelObligationReglementaire;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.FXFreeTab;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

/**
 * Table présentant les obligations réglementaires.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class ObligationsPojoTable extends PojoTable {
    /**
     * 
     * @param obligationRepository Repository d'accès aux données d'obligations réglementaires
     * @param tabPane Conteneur d'onglets dans lequel afficher l'onglet de planification
     */
    public ObligationsPojoTable(final AbstractSIRSRepository obligationRepository, final TabPane tabPane) {
        super(obligationRepository, "Liste des obligations réglementaires");

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
        final BorderPane mainPane = new BorderPane();
        final PojoTable pojoTable = new PojoTable(Injector.getSession().getRepositoryForClass(RappelObligationReglementaire.class),
                "Planification(s) programmée(s)");
        mainPane.setCenter(pojoTable);
        planTab.setContent(mainPane);
        tabPane.getTabs().add(planTab);
        tabPane.getSelectionModel().select(planTab);
    }
}
