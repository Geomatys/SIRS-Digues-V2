/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.mapper.document;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.Marche;
import fr.sirs.core.model.Organisme;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.mapper.AbstractMapper;
import fr.sirs.importer.v2.mapper.Mapper;
import fr.sirs.importer.v2.mapper.MapperSpi;
import java.io.IOException;
import java.util.Optional;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class MarcheMapper extends AbstractMapper<Marche> {

    private final AbstractImporter<Organisme> orgImporter;

    private enum Columns {
        ID_MAITRE_OUVRAGE,
        MONTANT_MARCHE,
        N_OPERATION
    };

    public MarcheMapper(Table table) {
        super(table);
        orgImporter = context.importers.get(Organisme.class);
        if (orgImporter == null) {
            throw new IllegalStateException("No importer found for class Organisme !");
        }
    }

    @Override
    public void map(Row input, Marche output) throws IllegalStateException, IOException, AccessDbImporterException {
        final Integer maitreOuvrage = input.getInt(Columns.ID_MAITRE_OUVRAGE.toString());
        if (maitreOuvrage != null) {
            final String importedId = orgImporter.getImportedId(maitreOuvrage);
            if (importedId == null) {
                throw new AccessDbImporterException("No organism found for ID "+maitreOuvrage);
            }
            output.setMaitreOuvrageId(importedId);
        }

        final Double montant = input.getDouble(Columns.MONTANT_MARCHE.toString());
        if (montant != null) {
            output.setMontant(montant.floatValue());
        }

        final Integer operation = input.getInt(Columns.N_OPERATION.toString());
        if (operation != null) {
            output.setNumOperation(operation);
        }
    }

    public static class Spi implements MapperSpi<Marche> {

        @Override
        public Optional<Mapper<Marche>> configureInput(Table inputType) throws IllegalStateException {
            for (final Columns c : Columns.values()) {
                if (inputType.getColumn(c.name()) == null) {
                    return Optional.empty();
                }
            }
            return Optional.of(new MarcheMapper(inputType));
        }

        @Override
        public Class<Marche> getOutputClass() {
            return Marche.class;
        }
    }
}
