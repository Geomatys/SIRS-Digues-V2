

package fr.sirs.theme;

import fr.sirs.core.model.Crete;
import fr.sirs.core.model.OuvrageRevanche;
import fr.sirs.core.model.TalusDigue;
import fr.sirs.core.model.TalusRisberme;
import fr.sirs.core.model.SommetRisberme;
import fr.sirs.core.model.PiedDigue;
import fr.sirs.core.model.Fondation;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.TronconDigue;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class StructuresTheme extends AbstractTronconTheme {

    private static final ThemeGroup GROUP1 = new ThemeGroup("Crête", "Table des "+LabelMapper.mapClassName(Crete.class, true), Crete.class,               
            (TronconDigue t) -> t.structures.filtered((Objet t1) -> t1 instanceof Crete),
            (TronconDigue t, Object c) -> t.structures.remove(c));
    private static final ThemeGroup GROUP2 = new ThemeGroup("Ouvrage de revanche", "Table des "+LabelMapper.mapClassName(OuvrageRevanche.class, true), OuvrageRevanche.class,       
            (TronconDigue t) -> t.structures.filtered((Objet t1) -> t1 instanceof OuvrageRevanche),
            (TronconDigue t, Object c) -> t.structures.remove(c));
    private static final ThemeGroup GROUP3 = new ThemeGroup("Talus de digue", "Table des "+LabelMapper.mapClassName(TalusDigue.class, true), TalusDigue.class,    
            (TronconDigue t) -> t.structures.filtered((Objet t1) -> t1 instanceof TalusDigue),
            (TronconDigue t, Object c) -> t.structures.remove(c));
    private static final ThemeGroup GROUP4 = new ThemeGroup("Sommet Risberme", "Table des "+LabelMapper.mapClassName(SommetRisberme.class, true), SommetRisberme.class,         
            (TronconDigue t) -> t.structures.filtered((Objet t1) -> t1 instanceof SommetRisberme),
            (TronconDigue t, Object c) -> t.structures.remove(c));
    private static final ThemeGroup GROUP5 = new ThemeGroup("Talus Risberme", "Table des "+LabelMapper.mapClassName(TalusRisberme.class, true), TalusRisberme.class,         
            (TronconDigue t) -> t.structures.filtered((Objet t1) -> t1 instanceof TalusRisberme),
            (TronconDigue t, Object c) -> t.structures.remove(c));
    private static final ThemeGroup GROUP6 = new ThemeGroup("Pied de digue", "Table des "+LabelMapper.mapClassName(PiedDigue.class, true), PiedDigue.class,         
            (TronconDigue t) -> t.structures.filtered((Objet t1) -> t1 instanceof PiedDigue),
            (TronconDigue t, Object c) -> t.structures.remove(c));    
    private static final ThemeGroup GROUP7 = new ThemeGroup("Fondation", "Table des "+LabelMapper.mapClassName(Fondation.class, true), Fondation.class,        
            (TronconDigue t) -> t.structures.filtered((Objet t1) -> t1 instanceof Fondation),
            (TronconDigue t, Object c) -> t.structures.remove(c));
    
    
    public StructuresTheme() {
        super("Structure", GROUP1,GROUP2,GROUP3,GROUP4,GROUP5,GROUP6,GROUP7);
    }
    
}
