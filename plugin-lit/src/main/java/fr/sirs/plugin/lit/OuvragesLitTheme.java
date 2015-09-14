
package fr.sirs.plugin.lit;

import fr.sirs.core.model.Seuil;
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
        content.add(new TabContent("Seuils", "Tableau des seuils", Seuil.class));
        content.add(new TabContent("Plage de depôt", "Tableau des plages de depôt", Seuil.class)); // TODO
        content.add(new TabContent("Autres ouvrages", "Tableau des ouvrages complementaire", Seuil.class)); //TODO
        
        final BorderPane borderPane = new AbstractDescriptionPane(content);
        return borderPane;
    }
}
