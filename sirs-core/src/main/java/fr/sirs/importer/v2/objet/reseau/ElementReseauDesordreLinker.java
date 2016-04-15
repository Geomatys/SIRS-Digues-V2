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

import fr.sirs.importer.v2.*;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.SessionCore;
import fr.sirs.core.component.AbstractSIRSRepository;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.EchelleLimnimetrique;
import fr.sirs.core.model.Element;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.OuvrageParticulier;
import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.core.model.OuvrageVoirie;
import fr.sirs.core.model.ReseauHydrauliqueCielOuvert;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.core.model.VoieDigue;
import fr.sirs.importer.AccessDbImporterException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import org.springframework.beans.factory.annotation.Autowired;

import static fr.sirs.importer.DbImporter.TableName.DESORDRE_ELEMENT_RESEAU;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.springframework.stereotype.Component;

/**
 *
 * Create links between object using an MS-Access join table.
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class ElementReseauDesordreLinker implements Linker<ObjetReseau, Desordre>, WorkMeasurable {

    @Override
    public Class<ObjetReseau> getTargetClass() {
        return ObjetReseau.class;
    }

    @Override
    public Class<Desordre> getHolderClass() {
        return Desordre.class;
    }

    private enum Columns {
        ID_DESORDRE,
        ID_ELEMENT_RESEAU
    }

    @Autowired
    protected ImportContext context;

    @Autowired
    protected SessionCore session;

    @Autowired
    ReseauRegistry registry;
    
    private final SimpleIntegerProperty count = new SimpleIntegerProperty(0);

    public void link() throws AccessDbImporterException, IOException {
        Iterator<Row> iterator = context.inputDb.getTable(DESORDRE_ELEMENT_RESEAU.name()).iterator();

        final AbstractImporter<ObjetReseau> reseauImporter = context.importers.get(ObjetReseau.class);
        if (reseauImporter == null) {
            throw new AccessDbImporterException("No importer found for type " + ObjetReseau.class.getCanonicalName());
        }

        final AbstractImporter<Desordre> desordreImporter = context.importers.get(Desordre.class);
        if (desordreImporter == null) {
            throw new AccessDbImporterException("No importer found for type " + Desordre.class.getCanonicalName());
        }

        final AbstractSIRSRepository<Desordre> desordreRepo = session.getRepositoryForClass(Desordre.class);
        if (desordreRepo == null) {
            throw new AccessDbImporterException("No repository available to get/update objects of type " + Desordre.class.getCanonicalName());
        }

        final HashSet<Element> toUpdate = new HashSet<>();

        String reseauId, desordreId;
        Class elementType;
        ObjetReseau objetReseau;
        Desordre desordre;
        Row current;
        AbstractSIRSRepository<ObjetReseau> reseauRepo;
        while (iterator.hasNext()) {

            // Split execution in bulks
            while (iterator.hasNext() && toUpdate.size() < context.bulkLimit) {
                current = iterator.next();

                // Those fields should be SQL join table keys, so they should never be null.
                reseauId = reseauImporter.getImportedId(current.getInt(Columns.ID_ELEMENT_RESEAU.name()));
                elementType = registry.getElementType(current);
                desordreId = desordreImporter.getImportedId(current.getInt(Columns.ID_DESORDRE.name()));
                if (reseauId == null) {
                    context.reportError(new ErrorReport(null, current, DESORDRE_ELEMENT_RESEAU.name(), Columns.ID_ELEMENT_RESEAU.name(), null, null, "No imported object found for input Id.", CorruptionLevel.ROW));
                    continue;
                } else if (desordreId == null) {
                    context.reportError(new ErrorReport(null, current, DESORDRE_ELEMENT_RESEAU.name(), Columns.ID_DESORDRE.name(), null, null, "No imported object found for input Id.", CorruptionLevel.ROW));
                    continue;
                }

                reseauRepo = session.getRepositoryForClass(elementType);
                objetReseau = reseauRepo.get(reseauId);
                desordre = desordreRepo.get(desordreId);
                if (link(objetReseau, desordre)) {
                    toUpdate.add(desordre);
                }
            }

            context.executeBulk(toUpdate);
            toUpdate.clear();
        }
        count.set(1);
    }

    @Override
    public int getTotalWork() {
        return 1;
    }

    @Override
    public IntegerProperty getWorkDone() {
        return count;
    }

    private boolean link(final ObjetReseau elementReseau, final Desordre desordre) {
        if (elementReseau instanceof VoieDigue) {
            desordre.getVoieDigueIds().add(elementReseau.getId());
        } else if (elementReseau instanceof EchelleLimnimetrique) {
            desordre.getEchelleLimnimetriqueIds().add(elementReseau.getId());
        } else if (elementReseau instanceof OuvrageParticulier) {
            desordre.getOuvrageParticulierIds().add(elementReseau.getId());
        }else if (elementReseau instanceof OuvrageHydrauliqueAssocie) {
            desordre.getOuvrageHydrauliqueAssocieIds().add(elementReseau.getId());
        } else if (elementReseau instanceof OuvrageTelecomEnergie) {
            desordre.getOuvrageTelecomEnergieIds().add(elementReseau.getId());
        } else if (elementReseau instanceof OuvrageVoirie) {
            desordre.getOuvrageVoirieIds().add(elementReseau.getId());
        } else if (elementReseau instanceof ReseauTelecomEnergie) {
            desordre.getReseauTelecomEnergieIds().add(elementReseau.getId());
        } else if (elementReseau instanceof ReseauHydrauliqueCielOuvert) {
            desordre.getReseauHydrauliqueCielOuvertIds().add(elementReseau.getId());
        } else if (elementReseau instanceof ReseauHydrauliqueFerme) {
            desordre.getReseauHydrauliqueFermeIds().add(elementReseau.getId());
        } else {
            return false;
        }
        return true;
    }
}
