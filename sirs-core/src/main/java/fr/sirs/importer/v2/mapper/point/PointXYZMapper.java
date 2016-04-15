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
package fr.sirs.importer.v2.mapper.point;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import fr.sirs.core.model.PointXYZ;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.ImportContext;
import fr.sirs.importer.v2.mapper.AbstractMapper;
import fr.sirs.importer.v2.mapper.Mapper;
import fr.sirs.importer.v2.mapper.MapperSpi;
import java.io.IOException;
import java.util.Optional;
import org.opengis.referencing.operation.TransformException;
import org.springframework.stereotype.Component;

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

    @Component
    public static class Spi implements MapperSpi<PointXYZ> {

        @Override
        public Optional<Mapper<PointXYZ>> configureInput(Table inputType) throws IllegalStateException {
            for (final Columns c : Columns.values()) {
                if (!ImportContext.columnExists(inputType, c.name())) {
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
