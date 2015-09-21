
package fr.sirs.plugin.berge;

import fr.sirs.core.model.EpiBerge;
import fr.sirs.core.model.OuvrageRevancheBerge;
import fr.sirs.core.model.PiedBerge;
import fr.sirs.core.model.SommetBerge;
import fr.sirs.core.model.TalusBerge;
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
public class StructureElementTheme extends AbstractDescriptionTheme {

    public StructureElementTheme() {
        super("Elements de structure", "Elements de structure");
    }

    @Override
    public Parent createPane() {
        List<TabContent> content = new ArrayList<>();
        content.add(new TabContent("Sommet", "Tableau des sommets", SommetBerge.class));
        content.add(new TabContent("Talus", "Tableau des talus", TalusBerge.class));
        content.add(new TabContent("Pieds de berge", "Tableau des pieds de berge", PiedBerge.class));
        content.add(new TabContent("Epis", "Tableau des épis", EpiBerge.class));
        content.add(new TabContent("Ouvrage de revanche", "Tableau des ouvrage de revanche", OuvrageRevancheBerge.class));
        
        final BorderPane borderPane = new AbstractDescriptionPane(content);
        return borderPane;
    }
}
