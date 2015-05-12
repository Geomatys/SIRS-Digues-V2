

package fr.sirs.theme;

import fr.sirs.core.model.FrontFrancBord;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.LargeurFrancBord;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.PiedFrontFrancBord;
import fr.sirs.core.model.TronconDigue;


/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class FrancBordTheme extends AbstractTronconTheme {
    
    public FrancBordTheme() {
        super("Franc-bord");
        
        final String title1 = new LabelMapper(FrontFrancBord.class).mapClassName();
        final ThemeGroup group1 = new ThemeGroup(title1, FrontFrancBord.class,
            (TronconDigue t) -> t.structures.filtered((Objet t1) -> t1 instanceof FrontFrancBord), 
            (TronconDigue t, Object c) -> t.structures.remove(c));
        
        final String title2 = new LabelMapper(PiedFrontFrancBord.class).mapClassName();
        final ThemeGroup group2 = new ThemeGroup(title2, PiedFrontFrancBord.class,
            (TronconDigue t) -> t.structures.filtered((Objet t1) -> t1 instanceof PiedFrontFrancBord),
            (TronconDigue t, Object c) -> t.structures.remove(c));
        
        final String title3 = new LabelMapper(LargeurFrancBord.class).mapClassName();
        final ThemeGroup group3 = new ThemeGroup(title3, LargeurFrancBord.class,
            (TronconDigue t) -> t.structures.filtered((Objet t1) -> t1 instanceof LargeurFrancBord),
            (TronconDigue t, Object c) -> t.structures.remove(c));
        
        setGroups(new ThemeGroup[]{group1, group2, group3});
    }
    
}
