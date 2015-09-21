/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.mapper.document.profil;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.DZLeveProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.mapper.AbstractMapper;
import fr.sirs.importer.v2.mapper.Mapper;
import fr.sirs.importer.v2.mapper.MapperSpi;
import java.io.IOException;
import java.util.Optional;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class DZProfilTraversMapper extends AbstractMapper<DZLeveProfilTravers> {

    private enum Columns {
        DISTANCE
    }

    public DZProfilTraversMapper(Table table) {
        super(table);
    }

    @Override
    public void map(Row input, DZLeveProfilTravers output) throws IllegalStateException, IOException, AccessDbImporterException {
        Double d = input.getDouble(Columns.DISTANCE.name());
        if (d != null) {
            output.setD(d);
        }
    }

    public static class Spi implements MapperSpi<DZLeveProfilTravers> {

        @Override
        public Optional<Mapper<DZLeveProfilTravers>> configureInput(Table inputType) throws IllegalStateException {
            for (final Columns c : Columns.values()) {
                if (inputType.getColumn(c.name()) == null) {
                    return Optional.empty();
                }
            }
            return Optional.of(new DZProfilTraversMapper(inputType));
        }

        @Override
        public Class<DZLeveProfilTravers> getOutputClass() {
            return DZLeveProfilTravers.class;
        }
    }

}
