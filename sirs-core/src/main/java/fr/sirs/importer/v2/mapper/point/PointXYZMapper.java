/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2.mapper.point;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.PointXYZ;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.mapper.AbstractMapper;
import fr.sirs.importer.v2.mapper.Mapper;
import fr.sirs.importer.v2.mapper.MapperSpi;
import java.io.IOException;
import java.util.Optional;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class PointXYZMapper extends AbstractMapper<PointXYZ> {

    private enum Columns {
        X,
        Y
    }

    public PointXYZMapper(Table table) {
        super(table);
    }

    @Override
    public void map(Row input, PointXYZ output) throws IllegalStateException, IOException, AccessDbImporterException {
            Double x = input.getDouble(Columns.X.name());
            Double y = input.getDouble(Columns.Y.name());
            if (x == null || y == null) {
                throw new AccessDbImporterException("Point XYZ : An ordinate of the row is null.");
            }

        try {
            final double[] point = new double[]{x,y};
            context.geoTransform.transform(point, 0, point, 0, 1);
            output.setX(point[0]);
            output.setY(point[1]);
        } catch (TransformException ex) {
            throw new AccessDbImporterException("Impossible to transform an XYZ point.", ex);
        }
    }

    public static class Spi implements MapperSpi<PointXYZ> {

        @Override
        public Optional<Mapper<PointXYZ>> configureInput(Table inputType) throws IllegalStateException {
            for (final Columns c : Columns.values()) {
                if (inputType.getColumn(c.name()) == null) {
                    return Optional.empty();
                }
            }
            return Optional.of(new PointXYZMapper(inputType));
        }

        @Override
        public Class<PointXYZ> getOutputClass() {
            return PointXYZ.class;
        }
    }
}
