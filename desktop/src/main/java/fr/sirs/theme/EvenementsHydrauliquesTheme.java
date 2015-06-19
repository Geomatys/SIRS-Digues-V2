package fr.sirs.theme;

import fr.sirs.Injector;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.theme.ui.PojoTable;
import javafx.scene.Parent;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class EvenementsHydrauliquesTheme extends Theme {
        
    public EvenementsHydrauliquesTheme() {
        super("Evènements hydrauliques", Type.UNLOCALIZED);
    }

    @Override
    public Parent createPane() {
        return new PojoTable(Injector.getSession().getRepositoryForClass(EvenementHydraulique.class), null);
    }
    
}
