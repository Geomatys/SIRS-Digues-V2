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
package fr.sirs.core.model;

import static fr.sirs.core.SirsCore.BUNDLE_KEY_CLASS;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.collection.Cache;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class LabelMapper {

    /**
     * Cache.
     */
    private static final Cache<Class,LabelMapper> MAPPERS = new Cache<>(12, 0, false);

    private final Class modelClass;
    private final ResourceBundle bundle;

    /**
     * Return a mapper which give translation for the attribute names of the given class.
     * @param clazz Class to get translations for.
     * @return A mapper, or null if we have no bundle for input class.
     */
    public static synchronized LabelMapper get(Class clazz){
        try {
            return MAPPERS.getOrCreate(clazz, () -> new LabelMapper(clazz));
        } catch (Exception ex) {
            Logger.getLogger(LabelMapper.class.getName()).log(Level.WARNING, "No label mapper found for class "+clazz.getCanonicalName(), ex);
        }
        return null;
    }

    private LabelMapper(final Class modelClass) throws MissingResourceException {
        ArgumentChecks.ensureNonNull("Input model class", modelClass);
        this.modelClass = modelClass;
        bundle = ResourceBundle.getBundle(modelClass.getName(), Locale.getDefault(), Thread.currentThread().getContextClassLoader());
    }

    public Class getModelClass() {return this.modelClass;}

    public String mapPropertyName(final String property) {
        try {
            return bundle.getString(property);
        } catch (NullPointerException | MissingResourceException e) {
            return property;
        }
    }

    public static String mapPropertyName(final Class modelClass, final String property) {
        return get(modelClass).mapPropertyName(property);
    }

    /**
     *
     * @return The bundle propety mapping the class name (singular).
     */
    public String mapClassName() {
        String name = null;
        try{
            name = bundle.getString(BUNDLE_KEY_CLASS);
        }catch(MissingResourceException ex){
            //not important
        }
        return name!=null ? name : modelClass.getSimpleName();
    }

    /**
     *
     * @return The bundle property mapping the class name (plural).
     */
    public String mapClassNamePlural() {
        String name = null;
        try{
            name = bundle.getString("classPlural");
        }catch(MissingResourceException ex){
            //not important
        }
        return name!=null ? name : modelClass.getSimpleName();
    }

    /**
     *
     * @param plural must be true to return the plural class name; false otherwise.
     * @return The bundle property mapping the class name.
     */
    public String mapClassName(final boolean plural){
        if(plural) return mapClassNamePlural();
        else return mapClassName();
    }
}
