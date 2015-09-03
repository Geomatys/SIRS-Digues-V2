
package fr.sirs.plugin.berge;

import fr.sirs.core.model.Desordre;
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
public class DesordresTheme extends AbstractDescriptionTheme {
    
    public DesordresTheme() {
        super("Désordres", "Désordres");
    }
    
    @Override
    public Parent createPane() {
        List<TabContent> content = new ArrayList<>();
        content.add(new TabContent("", "Tableau des désordres", Desordre.class));
        final BorderPane borderPane = new AbstractDescriptionPane(content);
        return borderPane;
    }
    
}
