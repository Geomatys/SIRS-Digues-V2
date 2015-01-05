

package fr.sirs.theme;

import fr.sirs.Injector;
import fr.sirs.Role;
import fr.sirs.Session;
import fr.sirs.theme.ui.PojoTable;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ContactsTheme extends Theme {

    public ContactsTheme() {
        super("Contacts", Type.OTHER);
    }

    @Override
    public Parent createPane() {
        final BorderPane uiCenter = new BorderPane();
        final TabPane tabPane = new TabPane();
        
        final Session session = Injector.getSession();
        final boolean editable = ((session.getRole()==Role.ADMIN) 
                || (session.getRole()==Role.USER) 
                || (session.getRole()==Role.EXTERNE));
        final PojoTable tableContact = new PojoTable(session.getContactRepository(),"Liste des contacts");
        tableContact.editableProperty().set(editable);
        final PojoTable tableOrganisme = new PojoTable(session.getOrganismeRepository(),"Liste des organismes");
        tableOrganisme.editableProperty().set(editable);
        
        final Tab tabIntervenant = new Tab("Intervenant");
        tabIntervenant.setContent(tableContact);
        final Tab tabOrganisme = new Tab("Organisme");
        tabOrganisme.setContent(tableOrganisme);
        
        tabPane.getTabs().add(tabIntervenant);
        tabPane.getTabs().add(tabOrganisme);
        
        uiCenter.setCenter(tabPane);
        return tabPane;
    }
    
}
