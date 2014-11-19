

package fr.sirs.theme;

import fr.sirs.theme.ui.PojoTable;
import fr.sirs.core.model.ArticleJournal;
import fr.sirs.core.model.Convention;
import fr.sirs.core.model.DocumentGrandeEchelle;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Marche;
import fr.sirs.core.model.Photo;
import fr.sirs.core.model.RapportEtude;
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
        
        final PojoTable tablePhotos = new PhotoTable(Photo.class);
        final PojoTable tableConventions = new ConventionTable(Convention.class);
        final PojoTable tableJournal = new JournalTable(ArticleJournal.class);
        final PojoTable tableMarches = new MarcheTable(Marche.class);
        final PojoTable tableRapport = new RapportTable(RapportEtude.class);
        final PojoTable tableDocGrandeEchelle = new DocGrandeEchelleTable(DocumentGrandeEchelle.class);
        
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

    private static class PhotoTable extends PojoTable {

        public PhotoTable(Class pojoClass) {
            super(pojoClass,"Liste des photos");
        }

        @Override
        protected void deletePojos(Element ... pojo) {
            
        }
        
        @Override
        protected void createPojo() {
        }
    }

    private static class ConventionTable extends PojoTable {

        public ConventionTable(Class pojoClass) {
            super(pojoClass,"Liste des conventions");
        }

        @Override
        protected void deletePojos(Element ... pojo) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        @Override
        protected void createPojo() {
        }
    }

    private static class JournalTable extends PojoTable {

        public JournalTable(Class pojoClass) {
            super(pojoClass,"Liste des journaux");
        }

        @Override
        protected void deletePojos(Element ... pojo) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        @Override
        protected void createPojo() {
        }
    }

    private static class MarcheTable extends PojoTable {

        public MarcheTable(Class pojoClass) {
            super(pojoClass,"Liste des Marchés");
        }

        @Override
        protected void deletePojos(Element ... pojo) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        @Override
        protected void createPojo() {
        }
    }

    private static class RapportTable extends PojoTable {

        public RapportTable(Class pojoClass) {
            super(pojoClass,"Liste des rapports");
        }

        @Override
        protected void deletePojos(Element ... pojo) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        @Override
        protected void createPojo() {
        }
    }

    private static class DocGrandeEchelleTable extends PojoTable {

        public DocGrandeEchelleTable(Class pojoClass) {
            super(pojoClass,"Liste des documents");
        }

        @Override
        protected void deletePojos(Element ... pojo) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        @Override
        protected void createPojo() {
        }
    }
    
}
