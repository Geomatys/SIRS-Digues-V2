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
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.ParametreHydrauliqueProfilLong;
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
public class ParamHydroPLongMapper extends AbstractMapper<ParametreHydrauliqueProfilLong> {

    private final AbstractImporter<EvenementHydraulique> hydroImporter;

    private enum Columns {
        ID_EVENEMENT_HYDRAU,
        PR_DEBUT_SAISI,
        PR_FIN_SAISI,
        PREMIER_DEBORDEMENT_DEBIT_M3S,
    }

    public ParamHydroPLongMapper(Table table) {
        super(table);
        hydroImporter = context.importers.get(EvenementHydraulique.class);
    }

    @Override
    public void map(Row input, ParametreHydrauliqueProfilLong output) throws IllegalStateException, IOException, AccessDbImporterException {
        final Integer idHydro = input.getInt(Columns.ID_EVENEMENT_HYDRAU.toString());
        if (idHydro != null) {
            output.setEvenementHydrauliqueId(hydroImporter.getImportedId(idHydro));
        }

        final Double startPr = input.getDouble(Columns.PR_DEBUT_SAISI.toString());
        if (startPr != null) {
            output.setPrDebut(startPr.floatValue());
        }

        final Double endPr = input.getDouble(Columns.PR_FIN_SAISI.toString());
        if (endPr != null) {
            output.setPrFin(endPr.floatValue());
        }

        final Double debit = input.getDouble(Columns.PREMIER_DEBORDEMENT_DEBIT_M3S.toString());
        if (debit != null) {
            output.setDebitPremerDebordement(debit.floatValue());
        }
    }

    @Component
    public static class Spi implements MapperSpi<ParametreHydrauliqueProfilLong> {

        @Override
        public Optional<Mapper<ParametreHydrauliqueProfilLong>> configureInput(Table inputType) throws IllegalStateException {
            for (final Columns c : Columns.values()) {
                if (!ImportContext.columnExists(inputType, c.name())) {
                    return Optional.empty();
                }
            }
            return Optional.of(new ParamHydroPLongMapper(inputType));
        }

        @Override
        public Class<ParametreHydrauliqueProfilLong> getOutputClass() {
            return ParametreHydrauliqueProfilLong.class;
        }
    }
}
