/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.mapper.point;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.PointDZ;
import fr.sirs.importer.AccessDbImporterException;
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
public class PointDZMapper extends AbstractMapper<PointDZ> {

    private enum Columns {
        DISTANCE,
        PR_SAISI
    }

    private final String fieldName;

    public PointDZMapper(Table table, final String fieldName) {
        super(table);
        this.fieldName = fieldName;
    }

    @Override
    public void map(Row input, PointDZ output) throws IllegalStateException, IOException, AccessDbImporterException {
        Double pr = input.getDouble(fieldName);
        if (pr != null) {
            output.setD(pr);
        }
    }

    @Component
    public static class Spi implements MapperSpi<PointDZ> {

        @Override
        public Optional<Mapper<PointDZ>> configureInput(Table inputType) throws IllegalStateException {
            for (final Columns c : Columns.values()) {
                if (inputType.getColumn(c.name()) != null) {
                    return Optional.of(new PointDZMapper(inputType, c.name()));
                }
            }
            return Optional.empty();
        }

        @Override
        public Class<PointDZ> getOutputClass() {
            return PointDZ.class;
        }
    }

}
