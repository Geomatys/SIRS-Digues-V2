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
package fr.sirs.importer.v2.linear.management;

import fr.sirs.core.model.Element;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public abstract class GenericPeriodeLocaliseeImporter<T extends Element> extends AbstractImporter<T> {

    protected AbstractImporter<TronconDigue> tdImporter;

    @Override
    protected void preCompute() throws AccessDbImporterException {
        super.preCompute();
        tdImporter = context.importers.get(TronconDigue.class);
    }

    @Override
    protected void postCompute() {
        super.postCompute();
        tdImporter = null;
    }
}
