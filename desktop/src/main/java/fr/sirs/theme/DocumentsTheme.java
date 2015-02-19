

package fr.sirs.theme;

import fr.sirs.Injector;
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
public class DocumentsTheme extends Theme {

    public DocumentsTheme() {
        super("Documents", Type.OTHER);
    }

    @Override
    public Parent createPane() {
        final BorderPane uiCenter = new BorderPane();
        final TabPane tabPane = new TabPane();
        
        final Session session = Injector.getSession();
//        final Tab tabPhoto = new Tab("Photo");
//        tabPhoto.setContent(tablePhotos);
        final Tab tabConvention = new Tab("Conventions");
        tabConvention.setContent(new PojoTable(session.getConventionRepository(), "Conventions"));
        final Tab tabJournal = new Tab("Articles de journal");
        tabJournal.setContent(new PojoTable(session.getArticleJournalRepository(), "Articles de journal"));
        final Tab tabMarche = new Tab("Marchés");
        tabMarche.setContent(new PojoTable(session.getMarcheRepository(), "Marchés"));
        final Tab tabRapport = new Tab("Rapports d'étude");
        tabRapport.setContent(new PojoTable(session.getRapportEtudeRepository(), "Rapports d'étude"));
        final Tab tabDocGrandeEchelle = new Tab("Documents à grande échelle");
        tabDocGrandeEchelle.setContent(new PojoTable(session.getDocumentGrandeEchelleRepository(), "Documents à grande échelle"));
        
//        tabPane.getTabs().add(tabPhoto);
        tabPane.getTabs().add(tabConvention);
        tabPane.getTabs().add(tabJournal);
        tabPane.getTabs().add(tabMarche);
        tabPane.getTabs().add(tabRapport);
        tabPane.getTabs().add(tabDocGrandeEchelle);
        
        uiCenter.setCenter(tabPane);
        return tabPane;
    }
    
}
