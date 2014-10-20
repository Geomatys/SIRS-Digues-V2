

package fr.sym.theme;

import fr.symadrem.sirs.core.model.IntervenantPrestation;
import fr.symadrem.sirs.core.model.Organisme;
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
        
        final AbstractPojoTable tableIntervenant = new AbstractPojoTable(IntervenantPrestation.class);
        final AbstractPojoTable tableOrganisme = new AbstractPojoTable(Organisme.class);
        
        final Tab tabIntervenant = new Tab("Intervenant");
        tabIntervenant.setContent(tableIntervenant);
        final Tab tabOrganisme = new Tab("Organisme");
        tabOrganisme.setContent(tableOrganisme);
        
        tabPane.getTabs().add(tabIntervenant);
        tabPane.getTabs().add(tabOrganisme);
        
        uiCenter.setCenter(tabPane);
        return tabPane;
    }
    
}
