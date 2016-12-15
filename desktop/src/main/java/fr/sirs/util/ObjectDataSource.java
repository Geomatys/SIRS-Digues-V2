/**
 * This file is part of SIRS-Digues 2.
 *
 * Copyright (C) 2016, FRANCE-DIGUES,
 * 
 * SIRS-Digues 2 is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * SIRS-Digues 2 is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SIRS-Digues 2. If not, see <http://www.gnu.org/licenses/>
 */
package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.SIRS;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.AbstractObservation;
import fr.sirs.core.model.AbstractPhoto;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ReferenceType;
import fr.sirs.util.property.Reference;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.ObjectConverters;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
public class ObjectDataSource<T> implements JRDataSource {

    protected final Iterator<T> iterator;
    protected T currentObject;
    protected final Previews previewRepository;
    protected final SirsStringConverter stringConverter;

    public ObjectDataSource(final Iterable<T> iterable){
        this(iterable, null);
    }

    public ObjectDataSource(final Iterable<T> iterable, final Previews previewLabelRepository){
        this(iterable, previewLabelRepository, null);
    }

    public ObjectDataSource(final Iterable<T> iterable, final Previews previewsRepository, final SirsStringConverter stringConverter){
        ArgumentChecks.ensureNonNull("iterable", iterable);
        iterator = iterable.iterator();
        this.previewRepository = previewsRepository;
        this.stringConverter = stringConverter;
    }

    @Override
    public boolean next() throws JRException {
        if(iterator.hasNext()){
            currentObject = iterator.next();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Object getFieldValue(final JRField jrf) throws JRException {
        final String name = jrf.getName();
        try {
            final Method getter = currentObject.getClass().getMethod("get" + name.substring(0, 1).toUpperCase() + name.substring(1));
            final Object propertyValue = getter.invoke(currentObject);
            if (propertyValue != null) {
                final Reference ref = getter.getAnnotation(Reference.class);
                return parsePropertyValue(propertyValue, ref == null ? null : ref.ref(), jrf.getValueClass());
            }
        } catch (Exception ex) {
            SIRS.LOGGER.log(Level.WARNING, "Impossible to print a field value.", ex);
        }

        //No field that match this name, looks like the feature type
        //used is not the exact one returned by the JasperReportservice.
        //This is not necessarly an error if for exemple someone ignore
        //some attribut from the template because he doesn't need them.
        return null;
    }

    /**
     * Extract information from input object to put it in an object of queried type.
     * @param propertyValue The object to get data from.
     * @param refClass If input object is a reference to an element, this class give the pointed element type. Can be null.
     * @param outputClass The type of object to return.
     * @return Extracted information, or null if analysis failed.
     */
    protected Object parsePropertyValue(Object propertyValue, final Class refClass, final Class outputClass) {
        if (propertyValue instanceof Collection) {
            final PrintableArrayList resultList = new PrintableArrayList(propertyValue instanceof List);
            for (final Object data : (Collection) propertyValue) {
                resultList.add(parsePropertyValue(data, refClass, String.class));
            }

            return resultList;
        }

        if (refClass != null) {
            if (!refClass.isAssignableFrom(propertyValue.getClass()) && (propertyValue instanceof String)) {
                if (ReferenceType.class.isAssignableFrom(refClass)) {
                    propertyValue = Injector.getSession().getRepositoryForClass(refClass).get((String)propertyValue);
                } else {
                    propertyValue = previewRepository.get((String)propertyValue);
                }
            }
        }

        if (outputClass.isAssignableFrom(propertyValue.getClass()))
            return propertyValue;

        if (String.class.isAssignableFrom(outputClass)) {
            return stringConverter.toString(propertyValue);
        } else {
            return ObjectConverters.convert(propertyValue, outputClass);
        }
    }
    
    /**
     * Pour le classement des observations de la plus récente à la plus ancienne.
     */
    static final Comparator<AbstractObservation> OBSERVATION_COMPARATOR = (o1, o2) -> {
        if(o1==null && o2==null) return 0;
        else if(o1==null || o2==null) return (o1==null) ? -1 : 1;
        else if(o1.getDate()==null && o2.getDate()==null) return 0;
        else if(o1.getDate()==null || o2.getDate()==null) return (o1.getDate()==null) ? 1 : -1;
        else return -o1.getDate().compareTo(o2.getDate());
    };
    
    /**
     * Pour le classement des photographies de la plus récente à la plus ancienne.
     */
    static final Comparator<AbstractPhoto> PHOTO_COMPARATOR = (p1, p2) -> {
        if(p1==null && p2==null) return 0;
        else if(p1==null || p2==null) return (p1==null) ? -1 : 1;
        else if(p1.getDate()==null && p2.getDate()==null) return 0;
        else if(p1.getDate()==null || p2.getDate()==null) return (p1.getDate()==null) ? 1 : -1;
        else return -p1.getDate().compareTo(p2.getDate());
    };
    
    /**
     * Pour le classement des éléments par désignation (ordre alphabétique).
     */
    static final Comparator<Element> ELEMENT_COMPARATOR = (p1, p2) -> {
        if(p1==null && p2==null) return 0;
        else if(p1==null || p2==null) return (p1==null) ? -1 : 1;
        else if(p1.getDesignation()==null && p2.getDesignation()==null) return 0;
        else if(p1.getDesignation()==null || p2.getDesignation()==null) return (p1.getDesignation()==null) ? 1 : -1;
        else return p1.getDesignation().compareTo(p2.getDesignation());
    };
}
