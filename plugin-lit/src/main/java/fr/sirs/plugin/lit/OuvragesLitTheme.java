
package fr.sirs.plugin.lit;

import fr.sirs.core.model.AutreOuvrageLit;
import fr.sirs.core.model.PlageDepotLit;
import fr.sirs.core.model.SeuilLit;
import fr.sirs.plugin.lit.ui.AbstractDescriptionPane;
import fr.sirs.plugin.lit.util.TabContent;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class OuvragesLitTheme extends AbstractDescriptionTheme {
    
    public OuvragesLitTheme() {
        super("Ouvrages dans le lit", "Ouvrages dans le lit");
    }
    
    @Override
    public Parent createPane() {
        List<TabContent> content = new ArrayList<>();
        content.add(new TabContent("Seuils", "Tableau des seuils", SeuilLit.class));
        content.add(new TabContent("Plage de depôt", "Tableau des plages de depôt", PlageDepotLit.class));
        content.add(new TabContent("Autres ouvrages", "Tableau des ouvrages complementaire", AutreOuvrageLit.class));
        
        final BorderPane borderPane = new AbstractDescriptionPane(content);
        return borderPane;
    }
}
