

package fr.sym.theme;

import fr.symadrem.sirs.core.model.ArticleJournal;
import fr.symadrem.sirs.core.model.Convention;
import fr.symadrem.sirs.core.model.DocumentGrandeEchelle;
import fr.symadrem.sirs.core.model.Marche;
import fr.symadrem.sirs.core.model.Photo;
import fr.symadrem.sirs.core.model.RapportEtude;
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
        
        final AbstractPojoTable tablePhotos = new AbstractPojoTable(Photo.class);
        final AbstractPojoTable tableConventions = new AbstractPojoTable(Convention.class);
        final AbstractPojoTable tableJournal = new AbstractPojoTable(ArticleJournal.class);
        final AbstractPojoTable tableMarches = new AbstractPojoTable(Marche.class);
        final AbstractPojoTable tableRapport = new AbstractPojoTable(RapportEtude.class);
        final AbstractPojoTable tableDocGrandeEchelle = new AbstractPojoTable(DocumentGrandeEchelle.class);
        
        final Tab tabPhoto = new Tab("Photo");
        tabPhoto.setContent(tablePhotos);
        final Tab tabConvention = new Tab("Convention");
        tabConvention.setContent(tableConventions);
        final Tab tabJournal = new Tab("Article de journal");
        tabJournal.setContent(tableJournal);
        final Tab tabMarche = new Tab("Marché");
        tabMarche.setContent(tableMarches);
        final Tab tabRapport = new Tab("Rapport d'étude");
        tabRapport.setContent(tableRapport);
        final Tab tabDocGrandeEchelle = new Tab("Document à grande échelle");
        tabDocGrandeEchelle.setContent(tableDocGrandeEchelle);
        
        tabPane.getTabs().add(tabPhoto);
        tabPane.getTabs().add(tabConvention);
        tabPane.getTabs().add(tabJournal);
        tabPane.getTabs().add(tabMarche);
        tabPane.getTabs().add(tabRapport);
        tabPane.getTabs().add(tabDocGrandeEchelle);
        
        uiCenter.setCenter(tabPane);
        return tabPane;
    }
    
}
