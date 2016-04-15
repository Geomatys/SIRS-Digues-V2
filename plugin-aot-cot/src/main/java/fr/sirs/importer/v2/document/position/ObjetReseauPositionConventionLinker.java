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
package fr.sirs.importer.v2.document.position;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.Session;
import fr.sirs.core.SirsCore;
import fr.sirs.core.TronconUtils;
import fr.sirs.core.component.PositionConventionRepository;
import fr.sirs.core.model.Convention;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.PositionConvention;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.ImportContext;
import fr.sirs.importer.v2.Linker;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ObjetReseauPositionConventionLinker implements Linker<ObjetReseau, PositionConvention> {

    private final String tableName = "ELEMENT_RESEAU_CONVENTION";

    private final AtomicInteger creationCount = new AtomicInteger(0);
    private final AtomicInteger bindCount = new AtomicInteger(0);

    private enum Columns {
        ID_ELEMENT_RESEAU,
        ID_CONVENTION
    }

    @Autowired
    private ImportContext context;

    @Autowired
    private Session session;

    @Override
    public Class<ObjetReseau> getTargetClass() {
        return ObjetReseau.class;
    }

    @Override
    public Class<PositionConvention> getHolderClass() {
        return PositionConvention.class;
    }

    @Override
    public void link() throws IOException, AccessDbImporterException {
        final Table table = context.inputDb.getTable(tableName);
        AbstractImporter<Convention> conventionImporter = context.importers.get(Convention.class);
        if (conventionImporter == null) {
            throw new AccessDbImporterException("No importer found for type "+Convention.class);
        }

        AbstractImporter<ObjetReseau> reseauImporter = context.importers.get(ObjetReseau.class);
        if (reseauImporter == null) {
            throw new AccessDbImporterException("No importer found for type "+ObjetReseau.class);
        }

        // TODO : bulk
        final Iterator<Row> iterator = table.iterator();
        Row next;
        Object reseauId;
        Object conventionId;
        String documentId;
        String objetReseauId;
        Element tmp;
        ObjetReseau objetReseau;
        final HashSet<Element> dataToPost = new HashSet<>(table.getRowCount());
        while (iterator.hasNext()) {
            next = iterator.next();
            reseauId = next.get(Columns.ID_ELEMENT_RESEAU.name());
            if (reseauId == null) {
                context.reportError(tableName, next, null, "Missing ID_ELEMENT_RESEAU. No link possible.");
                continue;
            }

            conventionId = next.get(Columns.ID_CONVENTION.name());
            if (conventionId == null) {
                context.reportError(tableName, next, null, "Missing ID_CONVENTION. No link possible.");
                continue;
            }

            try {
                documentId = conventionImporter.getImportedId(conventionId);
                objetReseauId = reseauImporter.getImportedId(reseauId);
            } catch (IllegalStateException e) {
                context.reportError(tableName, next, e);
                continue;
            }

            List<PositionConvention> positions = getPositions(documentId);
            tmp = session.getElement(objetReseauId).orElse(null);
            if (tmp instanceof ObjetReseau) {
                objetReseau = (ObjetReseau) tmp;
                boolean linkDone = false;
                for (final PositionConvention pos : positions) {
                    if (bind(pos, objetReseau)) {
                        linkDone = true;
                        dataToPost.add(pos);
                        bindCount.incrementAndGet();
                    }
                }

                if (!linkDone) {
                    dataToPost.add(createNewPosition(objetReseau, documentId));
                    creationCount.incrementAndGet();
                }
            }
        }

        context.executeBulk(dataToPost);

        SirsCore.LOGGER.log(Level.INFO, "Bound elements : "+bindCount);
        SirsCore.LOGGER.log(Level.INFO, "Created elements : "+creationCount);
    }

    private List<PositionConvention> getPositions(final String conventionId) {
        PositionConventionRepository repo = session.getApplicationContext().getBean(PositionConventionRepository.class);
        return repo.getByDocumentId(conventionId);
    }

    /**
     * Analyze input objects, and bind them if the given {@link PositionConvention}
     * is contained into given {@link ObjetReseau}.
     * @param pos
     * @param obj
     * @return
     */
    private boolean bind(final PositionConvention pos, final ObjetReseau obj) {
        if (pos.getObjetId() != null) {
            return false;
        } else {
            final TronconUtils.PosInfo posPosition = new TronconUtils.PosInfo(pos);
            final TronconUtils.PosInfo objPosition = new TronconUtils.PosInfo(obj);
            if (objPosition.getGeometry().contains(posPosition.getGeometry())) {
                pos.setObjetId(obj.getId());
                return true;
            } else if (objPosition.getGeoPointStart().equals(posPosition.getGeoPointStart())
                    && objPosition.getGeoPointEnd().equals(posPosition.getGeoPointEnd())) {
                pos.setObjetId(obj.getId());
                return true;
            } else {
                return false;
            }
        }
    }

    private PositionConvention createNewPosition(final ObjetReseau reseau, final String conventionId) {
        final PositionConvention position = ElementCreator.createAnonymValidElement(PositionConvention.class);

        position.setPrDebut(reseau.getPrDebut());
        position.setPrFin(reseau.getPrFin());
        position.setDate_debut(reseau.getDate_debut());
        position.setDate_fin(reseau.getDate_fin());
        position.setSystemeRepId(reseau.getSystemeRepId());
        position.setBorneDebutId(reseau.getBorneDebutId());
        position.setBorneFinId(reseau.getBorneFinId());
        position.setBorne_debut_aval(reseau.getBorne_debut_aval());
        position.setBorne_fin_aval(reseau.getBorne_fin_aval());
        position.setBorne_debut_distance(reseau.getBorne_debut_distance());
        position.setBorne_fin_distance(reseau.getBorne_fin_distance());
        position.setDesignation(reseau.getDesignation());
        position.setGeometry(reseau.getGeometry());
        position.setLatitudeMax(reseau.getLatitudeMax());
        position.setLatitudeMin(reseau.getLatitudeMin());
        position.setLongitudeMax(reseau.getLongitudeMax());
        position.setLongitudeMin(reseau.getLongitudeMin());
        position.setLinearId(reseau.getLinearId());
        position.setPositionDebut(reseau.getPositionDebut());
        position.setPositionFin(reseau.getPositionFin());

        position.setObjetId(reseau.getId());
        position.setSirsdocument(conventionId);

        return position;
    }
}
