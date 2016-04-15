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
package fr.sirs.importer.v2;

import fr.sirs.core.model.Element;
import fr.sirs.importer.AccessDbImporterException;
import java.io.IOException;

/**
 * A class whose aim is to update an already existing document by putting id of
 * another already existing document into it.
 *
 * @author Alexis Manin (Geomatys)
 * @param <T> Target element type : type pointed by the link.
 * @param <U> Type of object which will contain the reference.
 */
public interface Linker<T extends Element, U extends Element> {

    /**
     * @return Type of the object which will pointed by the link.
     */
    Class<T> getTargetClass();

    /**
     * @return Type of the object which will contain the link.
     */
    Class<U> getHolderClass();

    /**
     * Bind target objects to link holders.
     * @throws java.io.IOException If an error occurs while accessing data.
     * @throws fr.sirs.importer.AccessDbImporterException If an error occurs while processing a link.
     */
    void link() throws IOException, AccessDbImporterException;

}
