package fr.sirs.plugin.dependance;

import fr.sirs.Injector;
import fr.sirs.core.model.AireStockageDependance;
import fr.sirs.core.model.AutreDependance;
import fr.sirs.core.model.CheminAccesDependance;
import fr.sirs.core.model.OuvrageVoirieDependance;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.SimpleFXEditMode;
import javafx.scene.Parent;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Panneau regroupant les dépendances.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class DependancesTheme extends AbstractPluginsButtonTheme {
    public DependancesTheme() {
        super("Gestion des dépendances", "Gestion des dépendances", null);
    }

    @Override
    public Parent createPane() {
        final TabPane tabPane = new TabPane();
        final Tab ouvragesTab = new Tab("Ouvrages de voirie");
        ouvragesTab.setContent(createTablePane(OuvrageVoirieDependance.class, "Liste d'ouvrages de voirie"));

        final Tab areaTab = new Tab("Aires de stockage");
        areaTab.setContent(createTablePane(AireStockageDependance.class, "Liste des aires de stockage"));

        final Tab accessPathTab = new Tab("Chemins d'accès");
        accessPathTab.setContent(createTablePane(CheminAccesDependance.class, "Liste des chemins d'accès"));

        final Tab othersTab = new Tab("Autres");
        othersTab.setContent(createTablePane(AutreDependance.class, "Liste des dépendances d'autres types"));

        tabPane.getTabs().add(ouvragesTab);
        tabPane.getTabs().add(areaTab);
        tabPane.getTabs().add(accessPathTab);
        tabPane.getTabs().add(othersTab);
        return new BorderPane(tabPane);
    }

    private BorderPane createTablePane(final Class clazz, final String title) {
        // Gestion du bouton consultation / édition pour la pojo table
        final Separator separator = new Separator();
        separator.setVisible(false);
        final SimpleFXEditMode editMode = new SimpleFXEditMode();
        final HBox topPane = new HBox(separator, editMode);
        HBox.setHgrow(separator, Priority.ALWAYS);

        final PojoTable dependancesTable = new PojoTable(Injector.getSession().getRepositoryForClass(clazz), title);
        dependancesTable.editableProperty().bind(editMode.editionState());
        return new BorderPane(dependancesTable, topPane, null, null, null);
    }
}