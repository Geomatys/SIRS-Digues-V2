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
package fr.sirs.index;

import fr.sirs.core.SirsCore;
import fr.sirs.core.model.LabelMapper;
import java.util.logging.Level;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public final class ElementHit {
    private String elementId = "";
    private String clazz = "";
    private String libelle = "";

    public ElementHit(SearchHit hit) {
        elementId = hit.getId();
        final SearchHitField fieldClass = hit.field("@class");
        final SearchHitField fieldLibelle = hit.field("libelle");
        if (fieldClass != null) {
            clazz = fieldClass.getValue();
        }
        if (fieldLibelle != null) {
            libelle = fieldLibelle.getValue();
        }
    }

    public String getDocumentId() {
        return elementId;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getElementClassName() {
        return clazz;
    }

    public String getElementClassTitle() {
        try {
            return LabelMapper.get(getElementClass()).mapClassName();
        } catch (Exception e) {
            SirsCore.LOGGER.log(Level.FINE, "No valid type for found element.", e);
            return clazz == null || clazz.isEmpty()? "N/A" : clazz.substring(clazz.lastIndexOf('.')+1);
        }
    }

    public Class getElementClass() throws ClassNotFoundException {
        return Class.forName(clazz, true, Thread.currentThread().getContextClassLoader());
    }

}
