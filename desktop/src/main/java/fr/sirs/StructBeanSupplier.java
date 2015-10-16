
package fr.sirs;

import fr.sirs.core.SirsCore;
import fr.sirs.core.component.DocumentListener;
import fr.sirs.core.model.Element;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.geotoolkit.data.bean.BeanFeatureSupplier;
import org.geotoolkit.display2d.GO2Utilities;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;

/**
 * A data supplier for {@link BeanStore}. It listens on application {@link DocumentChangeEmiter} to be notified when data is updated.
 *
 * @author Johann Sorel (Geomatys)
 */
public class StructBeanSupplier extends BeanFeatureSupplier implements DocumentListener {

    private static final FilterFactory2 FF = GO2Utilities.FILTER_FACTORY;

    public StructBeanSupplier(Class clazz, final Supplier<Iterable> callable) {
        super(clazz, "id", hasField(clazz,"geometry")?"geometry":null, CorePlugin.MAP_PROPERTY_PREDICATE, null, Injector.getSession().getProjection(), callable::get);
        try {
            Injector.getDocumentChangeEmiter().addListener(this);
        } catch (Exception e) {
            SirsCore.LOGGER.warning("Feature store supplier for class "+ clazz.getCanonicalName() +" cannot listen on database changes.");
        }
    }

    private static boolean hasField(Class clazz, String field){
        try {
            for(PropertyDescriptor desc : Introspector.getBeanInfo(clazz).getPropertyDescriptors()){
                if(desc.getName().equals(field)){
                    return true;
                }
            }
        } catch (IntrospectionException e) {
            return false;
        }
        return false;
    }

    @Override
    public void documentCreated(Map<Class, List<Element>> added) {
        if (added == null) {
            return;
        }
        final Id filter = getIdFilter(added);
        if (filter != null) {
            fireFeaturesAdded(filter);
        }
    }

    @Override
    public void documentChanged(Map<Class, List<Element>> changed) {
        if (changed == null) {
            return;
        }
        final Id filter = getIdFilter(changed);
        if (filter != null) {
            fireFeaturesUpdated(filter);
        }
    }

    @Override
    public void documentDeleted(Map<Class, List<Element>> deleteObject) {
        if (deleteObject == null) {
            return;
        }
        final Id filter = getIdFilter(deleteObject);
        if (filter != null) {
            fireFeaturesDeleted(filter);
        }
    }

    private final Id getIdFilter(final Map<Class, List<Element>> elementMap) {
        final List<Element> elements = elementMap.get(getBeanClass());
        if (elements == null || elements.isEmpty()) {
            return null;
        }
        final HashSet<FeatureId> fIds = new HashSet<>();
        for (Element e : elements) {
            fIds.add(FF.featureId(e.getId()));
        }
        return FF.id(fIds);
    }

}
