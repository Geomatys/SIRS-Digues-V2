

package fr.sym.theme;

import fr.symadrem.sirs.core.model.Element;
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
        
        final AbstractPojoTable tableIntervenant = new IntervenantTable(IntervenantPrestation.class);
        final AbstractPojoTable tableOrganisme = new OrgnismeTable(Organisme.class);
        
        final Tab tabIntervenant = new Tab("Intervenant");
        tabIntervenant.setContent(tableIntervenant);
        final Tab tabOrganisme = new Tab("Organisme");
        tabOrganisme.setContent(tableOrganisme);
        
        tabPane.getTabs().add(tabIntervenant);
        tabPane.getTabs().add(tabOrganisme);
        
        uiCenter.setCenter(tabPane);
        return tabPane;
    }

    private static class IntervenantTable extends AbstractPojoTable {

        public IntervenantTable(Class pojoClass) {
            super(pojoClass);
        }

        @Override
        protected void deletePojo(Element pojo) {
            
        }

        @Override
        protected void editPojo(Element pojo) {
            
        }
    }

    private static class OrgnismeTable extends AbstractPojoTable {

        public OrgnismeTable(Class pojoClass) {
            super(pojoClass);
        }

        @Override
        protected void deletePojo(Element pojo) {
            
        }

        @Override
        protected void editPojo(Element pojo) {
            
        }
    }
    
}
