package fr.sirs.plugin.aot.cot;

import fr.sirs.Injector;
import fr.sirs.core.model.Convention;
import fr.sirs.theme.ui.AbstractPluginsButtonTheme;
import fr.sirs.theme.ui.PojoTable;
import fr.sirs.util.SimpleFXEditMode;
import javafx.scene.Parent;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Bouton de suivi d'AOT / COT.
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class SuiviAotCotTheme extends AbstractPluginsButtonTheme {
    private static final Image BUTTON_IMAGE = new Image(
            SuiviAotCotTheme.class.getResourceAsStream("images/aot-suivi.png"));
    
    public SuiviAotCotTheme() {
        super("Suivi AOT/COT", "Suivi AOT/COT", BUTTON_IMAGE);
    }

    @Override
    public Parent createPane() {
        final Separator separator = new Separator();
        separator.setVisible(false);
        final SimpleFXEditMode editMode = new SimpleFXEditMode();
        final HBox topPane = new HBox(separator, editMode);
        HBox.setHgrow(separator, Priority.ALWAYS);

        PojoTable pojoTable = new PojoTable(Injector.getSession().getRepositoryForClass(Convention.class), getName());
        pojoTable.editableProperty().bind(editMode.editionState());
        return new BorderPane(pojoTable, topPane, null, null, null);
    }
}