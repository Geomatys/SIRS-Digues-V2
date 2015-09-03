
package fr.sirs.plugin.berge;

import fr.sirs.core.model.LaisseCrue;
import fr.sirs.core.model.LigneEau;
import fr.sirs.core.model.MonteeEaux;
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
public class MesureEventHydroTheme extends AbstractDescriptionTheme {
    
    public MesureEventHydroTheme() {
        super("Mesures d'événement hydro", "Mesures d'événement hydro");
    }
    
    @Override
    public Parent createPane() {
        List<TabContent> content = new ArrayList<>();
        content.add(new TabContent("Laisse de crue", "Tableau des laisses de crue", LaisseCrue.class));
        content.add(new TabContent("Montée des eaux", "Tableau des montées des eaux", MonteeEaux.class));
        content.add(new TabContent("Ligne d'eau", "Tableau des lignes d'eau", LigneEau.class));
        
        final BorderPane borderPane = new AbstractDescriptionPane(content);
        return borderPane;
    }
}
