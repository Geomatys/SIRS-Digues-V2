

package fr.sym.theme;

import fr.symadrem.sirs.core.model.ArticleJournal;
import fr.symadrem.sirs.core.model.Convention;
import fr.symadrem.sirs.core.model.DocumentGrandeEchelle;
import fr.symadrem.sirs.core.model.Element;
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
        
        final AbstractPojoTable tablePhotos = new PhotoTable(Photo.class);
        final AbstractPojoTable tableConventions = new ConventionTable(Convention.class);
        final AbstractPojoTable tableJournal = new JournalTable(ArticleJournal.class);
        final AbstractPojoTable tableMarches = new MarcheTable(Marche.class);
        final AbstractPojoTable tableRapport = new RapportTable(RapportEtude.class);
        final AbstractPojoTable tableDocGrandeEchelle = new DocGrandeEchelleTable(DocumentGrandeEchelle.class);
        
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

    private static class PhotoTable extends AbstractPojoTable {

        public PhotoTable(Class pojoClass) {
            super(pojoClass);
        }

        @Override
        protected void deletePojo(Element pojo) {
            
        }

        @Override
        protected void editPojo(Element pojo) {
            
        }
    }

    private static class ConventionTable extends AbstractPojoTable {

        public ConventionTable(Class pojoClass) {
            super(pojoClass);
        }

        @Override
        protected void deletePojo(Element pojo) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void editPojo(Element pojo) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static class JournalTable extends AbstractPojoTable {

        public JournalTable(Class pojoClass) {
            super(pojoClass);
        }

        @Override
        protected void deletePojo(Element pojo) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void editPojo(Element pojo) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static class MarcheTable extends AbstractPojoTable {

        public MarcheTable(Class pojoClass) {
            super(pojoClass);
        }

        @Override
        protected void deletePojo(Element pojo) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void editPojo(Element pojo) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static class RapportTable extends AbstractPojoTable {

        public RapportTable(Class pojoClass) {
            super(pojoClass);
        }

        @Override
        protected void deletePojo(Element pojo) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void editPojo(Element pojo) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static class DocGrandeEchelleTable extends AbstractPojoTable {

        public DocGrandeEchelleTable(Class pojoClass) {
            super(pojoClass);
        }

        @Override
        protected void deletePojo(Element pojo) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void editPojo(Element pojo) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
}
