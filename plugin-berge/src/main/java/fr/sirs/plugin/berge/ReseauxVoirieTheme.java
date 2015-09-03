
package fr.sirs.plugin.berge;

import fr.sirs.core.model.OuvrageVoirie;
import fr.sirs.core.model.VoieAcces;
import fr.sirs.core.model.VoieDigue;
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
public class ReseauxVoirieTheme extends AbstractDescriptionTheme {
    
    public ReseauxVoirieTheme() {
        super("Réseaux de voirie", "Réseaux de voirie");
    }
    
    @Override
    public Parent createPane() {
        List<TabContent> content = new ArrayList<>();
        content.add(new TabContent("Voie d'accés", "Tableau des voies d'accès", VoieAcces.class));
        content.add(new TabContent("Voie sur berges", "Tableau des voies sur berge", VoieDigue.class));
        content.add(new TabContent("Ouvrages de voirie", "Tableau des ouvrages de voirie", OuvrageVoirie.class));
        
        final BorderPane borderPane = new AbstractDescriptionPane(content);
        return borderPane;
    }
}
