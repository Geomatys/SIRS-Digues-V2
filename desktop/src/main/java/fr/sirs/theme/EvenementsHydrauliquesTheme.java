package fr.sirs.theme;

import fr.sirs.Injector;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.SimpleFXEditMode;
import javafx.scene.Parent;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class EvenementsHydrauliquesTheme extends Theme {

    public EvenementsHydrauliquesTheme() {
        super("Evènements hydrauliques", Type.UNLOCALIZED);
    }

    @Override
    public Parent createPane() {
        final Separator separator = new Separator();
        separator.setVisible(false);
        final SimpleFXEditMode editMode = new SimpleFXEditMode();
        final HBox topPane = new HBox(separator, editMode);
        HBox.setHgrow(separator, Priority.ALWAYS);

        PojoTable pojoTable = new PojoTable(Injector.getSession().getRepositoryForClass(EvenementHydraulique.class), null);
        pojoTable.editableProperty().bind(editMode.editionState());
        return new BorderPane(pojoTable, topPane, null, null, null);
    }

}
