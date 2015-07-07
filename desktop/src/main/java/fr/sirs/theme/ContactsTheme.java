package fr.sirs.theme;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.Organisme;
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
 *
 * @author Johann Sorel (Geomatys)
 */
public class ContactsTheme extends Theme {

    public ContactsTheme() {
        super("Organismes et personnes physiques", Type.UNLOCALIZED);
    }

    @Override
    public Parent createPane() {
        final BorderPane uiCenter = new BorderPane();
        final TabPane tabPane = new TabPane();

        final Session session = Injector.getSession();
        final PojoTable tableContact = new PojoTable(session.getRepositoryForClass(Contact.class),"Personnes physiques");
        final PojoTable tableOrganisme = new PojoTable(session.getRepositoryForClass(Organisme.class),"Organismes");

        final Tab tabIntervenant = new Tab("Personnes physiques");
        tabIntervenant.setContent(createTabContent(tableContact));
        final Tab tabOrganisme = new Tab("Organismes");
        tabOrganisme.setContent(createTabContent(tableOrganisme));

        tabPane.getTabs().add(tabIntervenant);
        tabPane.getTabs().add(tabOrganisme);

        uiCenter.setCenter(tabPane);
        return tabPane;
    }

    public Parent createTabContent(final PojoTable table) {
        final Separator separator = new Separator();
        separator.setVisible(false);
        final SimpleFXEditMode editMode = new SimpleFXEditMode();
        final HBox topPane = new HBox(separator, editMode);
        HBox.setHgrow(separator, Priority.ALWAYS);

        table.editableProperty().bind(editMode.editionState());

        return new BorderPane(table, topPane, null, null, null);
    }
}
