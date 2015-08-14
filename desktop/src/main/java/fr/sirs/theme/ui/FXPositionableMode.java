
package fr.sirs.theme.ui;

import com.vividsolutions.jts.geom.Geometry;
import fr.sirs.Injector;
import fr.sirs.core.model.AvecForeignParent;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.Positionable;
import fr.sirs.core.model.TronconDigue;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public interface FXPositionableMode {

    Node getFXNode();

    ObjectProperty<Positionable> positionableProperty();

    BooleanProperty disablingProperty();


    /**
     * Searche recursively the troncon of the positionable.
     *
     * @param pos
     * @return
     */
    public static TronconDigue getTronconFromPositionable(final Positionable pos){
        final Element currentElement = getTronconFromElement(pos);
        if(currentElement instanceof TronconDigue) return (TronconDigue) currentElement;
        else return null;
    }

    public static Element getTronconFromElement(final Element element){
        Element candidate = null;

        // Si on arrive sur un Troncon, on renvoie le troncon.
        if(element instanceof TronconDigue){
            candidate = element;
        }

        // Sinon on cherche un troncon dans les parents
        else {
            // On privilégie le chemin AvecForeignParent
            if(element instanceof AvecForeignParent){
                String id = ((AvecForeignParent) element).getForeignParentId();
                candidate = getTronconFromElement(Injector.getSession().getRepositoryForClass(TronconDigue.class).get(id));
            }
            // Si on n'a pas (ou pas trouvé) de troncon via la référence ForeignParent on cherche via le conteneur
            if (candidate==null && element.getParent()!=null) {
                candidate = getTronconFromElement(element.getParent());
            }
        }
        return candidate;
    }

    public static double fxNumberValue(ObjectProperty<Double> spinnerNumber){
        if(spinnerNumber.get()==null) return 0;
        return spinnerNumber.get();
    }
    
}
