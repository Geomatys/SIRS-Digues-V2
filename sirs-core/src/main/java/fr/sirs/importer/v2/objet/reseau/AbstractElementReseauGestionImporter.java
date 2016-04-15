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
package fr.sirs.importer.v2.objet.reseau;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.PeriodeObjet;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.AbstractUpdater;
import java.io.IOException;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public abstract class AbstractElementReseauGestionImporter<T extends PeriodeObjet> extends AbstractUpdater<T, ObjetReseau> {

    private enum Columns {
        ID_ELEMENT_RESEAU,
        ID_INTERV_GARDIEN,
        DATE_DEBUT_GARDIEN,
        DATE_FIN_GARDIEN,
        DATE_DERNIERE_MAJ
    }

    private AbstractImporter<ObjetReseau> reseauImporter;

    @Override
    protected void postCompute() {
        super.postCompute();
        reseauImporter = null;
    }

    @Override
    protected void preCompute() throws AccessDbImporterException {
        super.preCompute();
        reseauImporter = context.importers.get(ObjetReseau.class);
    }

    @Override
    protected ObjetReseau getDocument(Object rowId, Row input, T output) {
        Integer reseauId = null;
        try {
            reseauId = input.getInt(Columns.ID_ELEMENT_RESEAU.name());
            final String importedId = reseauImporter.getImportedId(reseauId);
            final Element element = session.getElement(importedId).orElse(null);
            if (element instanceof ObjetReseau) {
                return (ObjetReseau) element;
            } else {
                throw new IllegalArgumentException("No document found for " + Columns.ID_ELEMENT_RESEAU.name() + " " + reseauId);
            }
        } catch (IOException | AccessDbImporterException ex) {
            throw new IllegalStateException("No document found for " + Columns.ID_ELEMENT_RESEAU.name(), ex);
        } catch (IllegalStateException e) {
            context.reportError(getTableName(), input, new IllegalArgumentException("No document found for " + Columns.ID_ELEMENT_RESEAU.name() + " " + reseauId));
            return null;
        }
    }
}
