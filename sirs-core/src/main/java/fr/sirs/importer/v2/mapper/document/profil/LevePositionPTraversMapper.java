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
package fr.sirs.importer.v2.mapper.document.profil;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.LevePositionProfilTravers;
import fr.sirs.core.model.LeveProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.ImportContext;
import fr.sirs.importer.v2.mapper.AbstractMapper;
import fr.sirs.importer.v2.mapper.Mapper;
import fr.sirs.importer.v2.mapper.MapperSpi;
import java.io.IOException;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class LevePositionPTraversMapper extends AbstractMapper<LevePositionProfilTravers> {

    private final AbstractImporter<LeveProfilTravers> leveImporter;

    private enum Columns {
        ID_PROFIL_EN_TRAVERS_LEVE,
        COTE_RIVIERE_Z_NGF_PIED_DE_DIGUE,
        COTE_RIVIERE_Z_NGF_SOMMET_RISBERME,
        CRETE_Z_NGF,
        COTE_TERRE_Z_NGF_SOMMET_RISBERME,
        COTE_TERRE_Z_NGF_PIED_DE_DIGUE,
        CRETE_LARGEUR
    }

    public LevePositionPTraversMapper(Table table) {
        super(table);
        leveImporter = context.importers.get(LeveProfilTravers.class);
    }

    @Override
    public void map(Row input, LevePositionProfilTravers output) throws IllegalStateException, IOException, AccessDbImporterException {
        final Object leveId = input.get(Columns.ID_PROFIL_EN_TRAVERS_LEVE.name());
        if (leveId == null) {
            throw new AccessDbImporterException("No ID set for foreign key "+Columns.ID_PROFIL_EN_TRAVERS_LEVE.name() + "in table "+table.getName());
        }
        final String importedId = leveImporter.getImportedId(leveId);
        if (importedId == null) {
            throw new AccessDbImporterException("No imported object found for foreign key "+Columns.ID_PROFIL_EN_TRAVERS_LEVE.name()+" with value "+leveId);
        }
        output.setLeveId(importedId);

        final Double cotePied = input.getDouble(Columns.COTE_RIVIERE_Z_NGF_PIED_DE_DIGUE.toString());
        if (cotePied != null) {
            output.setCotePiedDigueRiviere(cotePied);
        }

        final Double coteRiviere = input.getDouble(Columns.COTE_RIVIERE_Z_NGF_SOMMET_RISBERME.toString());
        if (coteRiviere != null) {
            output.setCoteSommetRisbermeRiviere(coteRiviere);
        }

        final Double crete = input.getDouble(Columns.CRETE_Z_NGF.toString());
        if (crete != null) {
            output.setCoteCrete(crete);
        }

        final Double sommet = input.getDouble(Columns.COTE_TERRE_Z_NGF_SOMMET_RISBERME.toString());
        if (sommet != null) {
            output.setCoteSommetRisbermeTerre(sommet);
        }

        final Double pied = input.getDouble(Columns.COTE_TERRE_Z_NGF_PIED_DE_DIGUE.toString());
        if (pied != null) {
            output.setCotePiedDigueTerre(pied);
        }

        final Double largeur = input.getDouble(Columns.CRETE_LARGEUR.toString());
        if (largeur != null) {
            output.setLargeurCrete(largeur);
        }
    }

    @Component
    public static class Spi implements MapperSpi<LevePositionProfilTravers> {

        @Override
        public Optional<Mapper<LevePositionProfilTravers>> configureInput(Table inputType) throws IllegalStateException {
            for (final Columns c : Columns.values()) {
                if (!ImportContext.columnExists(inputType, c.name())) {
                    return Optional.empty();
                }
            }
            return Optional.of(new LevePositionPTraversMapper(inputType));
        }

        @Override
        public Class<LevePositionProfilTravers> getOutputClass() {
            return LevePositionProfilTravers.class;
        }
    }
}
