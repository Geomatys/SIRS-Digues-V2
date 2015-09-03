
package fr.sirs.plugin.berge;

import fr.sirs.core.model.ArticleJournal;
import fr.sirs.core.model.DocumentGrandeEchelle;
import fr.sirs.core.model.Marche;
import fr.sirs.core.model.ProfilLong;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.core.model.RapportEtude;
import fr.sirs.plugin.berge.ui.AbstractDescriptionPane;
import fr.sirs.plugin.berge.util.TabContent;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DocumentTheme extends AbstractDescriptionTheme {
    
    public DocumentTheme() {
        super("Documents", "Documents");
    }
    
    @Override
    public Parent createPane() {
        List<TabContent> content = new ArrayList<>();
        content.add(new TabContent("Articles", "Tableau des articles de journaux", ArticleJournal.class));
        content.add(new TabContent("Marchés", "Tableau des marchés", Marche.class));
        content.add(new TabContent("Rapports d'étude", "Tableau des rapports d'étude", RapportEtude.class));
        content.add(new TabContent("Documents à grande échelle", "Tableau des documents à grande échelle", DocumentGrandeEchelle.class));
        content.add(new TabContent("Profils en long", "Tableau des profils en long", ProfilLong.class));
        content.add(new TabContent("Profils en travers", "Tableau des profils en travers", ProfilTravers.class));
        
        final BorderPane borderPane = new AbstractDescriptionPane(content);
        return borderPane;
    }
}
