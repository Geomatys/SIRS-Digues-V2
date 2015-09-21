/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.mapper.document.profil;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.PointZ;
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
public class PointZMapper extends AbstractMapper<PointZ> {

    private enum Columns {
        Z
    }

    public PointZMapper(Table table) {
        super(table);
    }

    @Override
    public void map(Row input, PointZ output) throws IllegalStateException, IOException, AccessDbImporterException {
        final Double z = input.getDouble(Columns.Z.toString());
        if (z != null) {
            output.setZ(z);
        }
    }

    public static class Spi implements MapperSpi<PointZ> {

        @Override
        public Optional<Mapper<PointZ>> configureInput(Table inputType) throws IllegalStateException {
            for (final Columns c : Columns.values()) {
                if (inputType.getColumn(c.name()) == null) {
                    return Optional.empty();
                }
            }
            return Optional.of(new PointZMapper(inputType));
        }

        @Override
        public Class<PointZ> getOutputClass() {
            return PointZ.class;
        }
    }
}
