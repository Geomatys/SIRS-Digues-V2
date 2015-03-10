
package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.Session;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.PreviewLabel;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.AvecLibelle;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.query.ElementHit;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.StringConverter;
import org.opengis.feature.PropertyType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Try to return a simple and readable name for any element given as argument.
 * 
 * @author Alexis Manin
 * @author Johann Sorel
 */
public class SirsStringConverter extends StringConverter {

    private final WeakHashMap<String, Object> fromString = new WeakHashMap<>();
    private final WeakHashMap<String, LabelMapper> labelMappers = new WeakHashMap<>();
    
    /**
     * Find a simple name for input object.
     * @param item The object to find a name for.
     * @return A title for input item, or a null or empty value, if none have been found.
     */
    @Override
    public String toString(Object item) {
        if(item instanceof SystemeReperageBorne){
            final SystemeReperageBorne srb = (SystemeReperageBorne) item;
            final Session session = Injector.getBean(Session.class);
            item = session.getBorneDigueRepository().get(srb.getBorneId());
        }

        String text = "";
        if (item instanceof AvecLibelle) {
            text = ((AvecLibelle)item).getLibelle();
        } else if (item instanceof ElementHit) {
            text = ((ElementHit) item).getLibelle();
        } else if (item instanceof PreviewLabel) {
            final PreviewLabel label = (PreviewLabel) item;
            final LabelMapper labelMapper = getLabelMapperForClass(label.getType());
            text = (labelMapper==null) ? "" : (labelMapper.mapPropertyName("classAbrege") + " - ");            
            text += (label.getPseudoId()==null) ? "" : (label.getPseudoId() + " : ");
            text += (label.getLabel()==null) ? "" : label.getLabel();
        } else if (item instanceof Contact) {
            final Contact c = (Contact) item;
            text = c.getNom() + " " + c.getPrenom();
        } else if (item instanceof Organisme) {
            text = ((Organisme)item).getNom();
        } else if (item instanceof Element) {
            final LabelMapper labelMapper = getLabelMapperForClass(item.getClass());
            // TODO : make a commodity method in label mapper ?
            text = labelMapper.mapPropertyName("classAbrege") + ((((Element)item).getPseudoId()==null) ? "" : ((Element)item).getPseudoId());
        } else if (item instanceof String) {
            text = (String) item;
        } else if (item instanceof CoordinateReferenceSystem) {
            text = ((CoordinateReferenceSystem) item).getName().toString();
        } else if (item instanceof PropertyType) {
            text = ((PropertyType) item).getName().tip().toString();
        }
        
        if (text != null && !text.isEmpty()) {
            fromString.put(text, item);
        }
        return text;
    }

    @Override
    public Object fromString(String string) {
        return fromString.get(string);
    }
    
    /**
     * 
     * @param clazz
     * @return the label mapper for the given class.
     */
    private LabelMapper getLabelMapperForClass(final Class clazz){
        if(labelMappers.get(clazz.getName())==null)
            labelMappers.put(clazz.getName(), new LabelMapper(clazz));
        return labelMappers.get(clazz.getName());
    }
    
    /**
     * 
     * @param className
     * @return the labelMapper for the given class name, or null if there is no
     * class for the given name.
     */
    private LabelMapper getLabelMapperForClass(final String className){
        try {
            return getLabelMapperForClass(Class.forName(className));
        } catch (ClassNotFoundException ex) {
            SIRS.LOGGER.log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
