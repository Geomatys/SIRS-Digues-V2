/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.mapper.point;

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

        Z,
        HAUTEUR_EAU
    }

    private final String fieldName;

    public PointZMapper(Table table, String fieldName) {
        super(table);
        this.fieldName = fieldName;
    }

    @Override
    public void map(Row input, PointZ output) throws IllegalStateException, IOException, AccessDbImporterException {
        final Double z = input.getDouble(fieldName);
        if (z != null) {
            output.setZ(z);
        }
    }

    public static class Spi implements MapperSpi<PointZ> {

        @Override
        public Optional<Mapper<PointZ>> configureInput(Table inputType) throws IllegalStateException {
            for (final Columns c : Columns.values()) {
                if (inputType.getColumn(c.name()) != null) {
                    return Optional.of(new PointZMapper(inputType, c.name()));
                }
            }
            return Optional.empty();
        }

        @Override
        public Class<PointZ> getOutputClass() {
            return PointZ.class;
        }
    }
}
