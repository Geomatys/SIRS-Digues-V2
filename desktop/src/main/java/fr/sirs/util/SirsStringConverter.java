
package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import static fr.sirs.SIRS.BUNDLE_KEY_CLASS_ABREGE;
import fr.sirs.Session;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.PreviewLabel;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.AvecLibelle;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.Role;
import static fr.sirs.core.model.Role.ADMIN;
import static fr.sirs.core.model.Role.EXTERN;
import static fr.sirs.core.model.Role.GUEST;
import static fr.sirs.core.model.Role.USER;
import fr.sirs.query.ElementHit;
import java.util.WeakHashMap;
import java.util.logging.Level;
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
    private static final WeakHashMap<String, LabelMapper> LABEL_MAPPERS = new WeakHashMap<>();
    
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
        if (item instanceof Contact) {
            final Contact c = (Contact) item;
            text = getDesignation((Element)item)+" : "+c.getNom() + " " + c.getPrenom();
        } else if (item instanceof Organisme) {
            text = getDesignation((Element)item)+" : "+((Organisme)item).getNom();
        } else if (item instanceof Element) {
            text = getDesignation((Element)item);
        } else if (item instanceof ElementHit) {
            text = ((ElementHit) item).getLibelle();
        } else if (item instanceof PreviewLabel) {
            final PreviewLabel label = (PreviewLabel) item;
            text = getDesignation(label);
            if (label.getLabel() != null) {
                if (!text.isEmpty())  text += " : ";
                text += label.getLabel();
            }
        } else if (item instanceof String) {
            text = (String) item;
        } else if (item instanceof CoordinateReferenceSystem) {
            text = ((CoordinateReferenceSystem) item).getName().toString();
        } else if (item instanceof PropertyType) {
            text = ((PropertyType) item).getName().tip().toString();
        } else if (item instanceof Role){
            if(item==ADMIN) text="Administrateur";
            else if(item==USER) text="Utilisateur";
            else if(item==GUEST) text="Invité";
            else if(item==EXTERN) text="Externe";
            else text="";
        }
        
        // Whatever object we've got, if we can append a libelle, we do.
        if (item instanceof AvecLibelle) {
            final AvecLibelle libelle = (AvecLibelle) item;
            if (!text.isEmpty() 
                    && libelle.getLibelle()!=null 
                    && !libelle.getLibelle().isEmpty()) {
                text += " : ";
            }
            if (libelle.getLibelle()!=null 
                    && !libelle.getLibelle().isEmpty()) {
                text += libelle.getLibelle();
            }
        }
            
        if (text != null && !text.isEmpty()) {
            fromString.put(text, item);
        }
        return text;
    }

    public static String getDesignation(final PreviewLabel source) {
        final LabelMapper labelMapper = getLabelMapperForClass(source.getType());
        String prefixedDesignation = (labelMapper == null) ? "" : labelMapper.mapPropertyName(BUNDLE_KEY_CLASS_ABREGE);
        if(source.getDesignation()!=null){
            if(!"".equals(prefixedDesignation)){
                prefixedDesignation+=" - ";
            }
            prefixedDesignation+=source.getDesignation();
        }
        return prefixedDesignation;
    }
    
    public static String getDesignation(final Element source) {
        final LabelMapper labelMapper = getLabelMapperForClass(source.getClass());
        String prefixedDesignation = (labelMapper == null) ? "" : labelMapper.mapPropertyName(BUNDLE_KEY_CLASS_ABREGE);
        if(source.getDesignation()!=null){
            if(!"".equals(prefixedDesignation)){
                prefixedDesignation+=" - ";
            }
            prefixedDesignation+=source.getDesignation();
        }
        return prefixedDesignation;
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
    private static LabelMapper getLabelMapperForClass(final Class clazz){
        if(LABEL_MAPPERS.get(clazz.getName())==null)
            LABEL_MAPPERS.put(clazz.getName(), new LabelMapper(clazz));
        return LABEL_MAPPERS.get(clazz.getName());
    }
    
    /**
     * 
     * @param className
     * @return the labelMapper for the given class name, or null if there is no
     * class for the given name.
     */
    private static LabelMapper getLabelMapperForClass(final String className){
        try {
            return getLabelMapperForClass(Class.forName(className));
        } catch (ClassNotFoundException ex) {
            SIRS.LOGGER.log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
