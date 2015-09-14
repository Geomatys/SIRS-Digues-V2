
package fr.sirs.plugin.lit;

import fr.sirs.core.model.Desordre;
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
public class IleBancTheme extends AbstractDescriptionTheme {
    
    public IleBancTheme() {
        super("Iles et bancs", "Iles et bancs");
    }
    
    @Override
    public Parent createPane() {
        List<TabContent> content = new ArrayList<>();
        content.add(new TabContent("", "Tableau des îles et bancs", Desordre.class)); // TODO
        final BorderPane borderPane = new AbstractDescriptionPane(content);
        return borderPane;
    }
    
}
