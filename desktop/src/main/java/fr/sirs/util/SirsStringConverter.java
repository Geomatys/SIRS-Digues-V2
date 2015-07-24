
package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import static fr.sirs.SIRS.BUNDLE_KEY_CLASS_ABREGE;
import fr.sirs.Session;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.SystemeReperageBorne;
import fr.sirs.core.model.AvecLibelle;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.LabelMapper;
import fr.sirs.core.model.PositionDocument;
import fr.sirs.core.model.Preview;
import fr.sirs.core.model.Role;
import static fr.sirs.core.model.Role.ADMIN;
import static fr.sirs.core.model.Role.EXTERN;
import static fr.sirs.core.model.Role.GUEST;
import static fr.sirs.core.model.Role.USER;
import fr.sirs.core.model.SQLQuery;
import fr.sirs.index.ElementHit;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.StringConverter;
import org.apache.sis.util.ArgumentChecks;
import org.opengis.feature.PropertyType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Try to return a simple and readable name for any element given as argument.
 *
 * @author Alexis Manin
 * @author Johann Sorel
 */
public class SirsStringConverter extends StringConverter {

    private final WeakHashMap<String, Object> FROM_STRING = new WeakHashMap<>();

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
            item = session.getRepositoryForClass(BorneDigue.class).get(srb.getBorneId());
        }

        StringBuilder text = new StringBuilder();
        // Start title with element designation
        if (item instanceof Element) {
            text.append(getDesignation((Element)item));
        }  else if (item instanceof Preview) {
            text.append(getDesignation((Preview) item));
        }  else if (item instanceof SQLQuery) {
            text.append(((SQLQuery)item).getLibelle());
        }

        // Search for a name or label associated to input object
        if (item instanceof Contact) {
            final Contact c = (Contact) item;
            if (c.getNom() != null && !c.getNom().isEmpty()) {
                text.append(" : ").append(c.getNom());
            }
            if (c.getPrenom() != null && !c.getPrenom().isEmpty()) {
                text.append(' ').append(c.getPrenom());
            }
        } else if (item instanceof Organisme) {
            final Organisme o = (Organisme)item;
            if (o.getNom() != null && !o.getNom().isEmpty()) {
                text.append(" : ").append(o.getNom());
            }
        } else if (item instanceof ElementHit) {
            text.append(((ElementHit) item).getLibelle());
        } else if (item instanceof String) {
            text.append((String) item);
        } else if (item instanceof CoordinateReferenceSystem) {
            text.append(((CoordinateReferenceSystem) item).getName().toString());
        } else if (item instanceof PropertyType) {
            text = text.append(((PropertyType) item).getName().tip().toString());
        } else if (item instanceof Role){
            if(item==ADMIN) text.append("Administrateur");
            else if(item==USER) text.append("Utilisateur");
            else if(item==GUEST) text.append("Invité");
            else if(item==EXTERN) text.append("Externe");
        } else if (item instanceof Class) {
            if(Element.class.isAssignableFrom((Class) item)){
                text.append(LabelMapper.get((Class) item).mapClassName());
            }
        } else if(item!=null && item.getClass().isEnum()){
            text.append(((Enum)item).name());
        }

        // Whatever object we've got, if we can append a libelle, we do.
        if (item instanceof AvecLibelle) {
            final AvecLibelle libelle = (AvecLibelle) item;
            if (text.length() > 0
                    && libelle.getLibelle() != null
                    && !libelle.getLibelle().isEmpty()) {
                text.append(" : ").append(libelle.getLibelle());
            }
        }

        final String result = text.toString();
        if (result != null && !result.isEmpty()) {
            FROM_STRING.put(result, item);
        }
        return result;
    }

    public static String getDesignation(final Preview source) {
        ArgumentChecks.ensureNonNull("Preview to get designation for", source);
        final LabelMapper labelMapper = source.getElementClass() == null? null : getLabelMapperForClass(source.getElementClass());
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
        final LabelMapper labelMapper =  LabelMapper.get(source.getClass());
        String prefixedDesignation = (labelMapper == null) ? "" : labelMapper.mapPropertyName(BUNDLE_KEY_CLASS_ABREGE);
        if(source.getDesignation()!=null){
            if(!"".equals(prefixedDesignation)){
                prefixedDesignation+=" - ";
            }
            prefixedDesignation+=source.getDesignation();
        }

        if(source instanceof PositionDocument){
            if(((PositionDocument)source).getSirsdocument()!=null){
                final Preview preview = Injector.getSession().getPreviews().get(((PositionDocument)source).getSirsdocument());
                if(preview!=null && preview.getElementClass()!=null){
                    try {
                        final LabelMapper documentLabelMapper =  LabelMapper.get(Class.forName(preview.getElementClass(), true, Thread.currentThread().getContextClassLoader()));
                        if(documentLabelMapper!=null){
                            prefixedDesignation+=" ["+documentLabelMapper.mapClassName()+"] ";
                        }
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(SirsStringConverter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        return prefixedDesignation;
    }

    @Override
    public Object fromString(String string) {
        return FROM_STRING.get(string);
    }

    /**
     *
     * @param className
     * @return the labelMapper for the given class name, or null if there is no
     * class for the given name.
     */
    private static LabelMapper getLabelMapperForClass(final String className){
        try {
            return  LabelMapper.get(Class.forName(className,  true, Thread.currentThread().getContextClassLoader()));
        } catch (ClassNotFoundException ex) {
            SIRS.LOGGER.log(Level.WARNING, null, ex);
        }
        return null;
    }

}
