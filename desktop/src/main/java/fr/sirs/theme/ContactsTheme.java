

package fr.sirs.theme;

import fr.sirs.theme.ui.AbstractPojoTable;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.IntervenantPrestation;
import fr.sirs.core.model.Organisme;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
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
            super(pojoClass,"Liste des intervenants");
        }

        @Override
        protected void deletePojos(Element ... pojo) {
            
        }

        @Override
        protected void elementEdited(TableColumn.CellEditEvent<Element, Object> event) {
        }

        @Override
        protected void createPojo() {
        }
    }

    private static class OrgnismeTable extends AbstractPojoTable {

        public OrgnismeTable(Class pojoClass) {
            super(pojoClass,"Liste des organismes");
        }

        @Override
        protected void deletePojos(Element ... pojo) {
            
        }

        @Override
        protected void elementEdited(TableColumn.CellEditEvent<Element, Object> event) {
        }
        
        @Override
        protected void createPojo() {
        }
    }
    
}
