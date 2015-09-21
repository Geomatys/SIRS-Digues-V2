/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.mapper.document.profil;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.PrZProfilLong;
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
public class PrZMapper extends AbstractMapper<PrZProfilLong> {

    private enum Columns {
        PR_SAISI
    }

    public PrZMapper(Table table) {
        super(table);
    }

    @Override
    public void map(Row input, PrZProfilLong output) throws IllegalStateException, IOException, AccessDbImporterException {
        Double pr = input.getDouble(Columns.PR_SAISI.name());
        if (pr != null) {
            output.setD(pr);
        }
    }

    public static class Spi implements MapperSpi<PrZProfilLong> {

        @Override
        public Optional<Mapper<PrZProfilLong>> configureInput(Table inputType) throws IllegalStateException {
            for (final Columns c : Columns.values()) {
                if (inputType.getColumn(c.name()) == null) {
                    return Optional.empty();
                }
            }
            return Optional.of(new PrZMapper(inputType));
        }

        @Override
        public Class<PrZProfilLong> getOutputClass() {
            return PrZProfilLong.class;
        }
    }

}
