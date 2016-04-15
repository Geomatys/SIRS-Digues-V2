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
package fr.sirs.importer.v2.linear;

import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.BORNE_DIGUE;
import fr.sirs.importer.v2.CorruptionLevel;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.ErrorReport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.geotoolkit.display2d.GO2Utilities;
import org.opengis.referencing.operation.TransformException;
import org.springframework.stereotype.Component;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
@Component
public class BorneDigueImporter extends AbstractImporter<BorneDigue> {

    enum Columns {
        ID_BORNE,
        ID_TRONCON_GESTION,
        NOM_BORNE,
        X_POINT,
        Y_POINT,
        Z_POINT,
        FICTIVE,
//        X_POINT_ORIGINE,
//        Y_POINT_ORIGINE,
    };

    @Override
    public Class<BorneDigue> getElementClass() {
        return BorneDigue.class;
    }

    @Override
    public BorneDigue importRow(Row row, BorneDigue output) throws IOException, AccessDbImporterException {
        output = super.importRow(row, output);
        output.setLibelle(DbImporter.cleanNullString(row.getString(Columns.NOM_BORNE.toString())));

        output.setFictive(row.getBoolean(Columns.FICTIVE.toString()));

        try {
            final Double pointZ = row.getDouble(Columns.Z_POINT.toString());
            final Point point;
            if (pointZ != null) {
                final double[] coord = new double[]{
                    row.getDouble(Columns.X_POINT.toString()),
                    row.getDouble(Columns.Y_POINT.toString()),
                    pointZ
                };
                context.geoTransform.transform(coord, 0, coord, 0, 1);
                point = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(coord[0], coord[1], coord[2]));
            } else {

                final double[] coord = new double[]{
                    row.getDouble(Columns.X_POINT.toString()),
                    row.getDouble(Columns.Y_POINT.toString())
                };
                context.geoTransform.transform(coord, 0, coord, 0, 1);
                point = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(coord[0], coord[1]));
            }
            output.setGeometry(point);
        } catch (TransformException ex) {
            context.reportError(new ErrorReport(ex, row, getTableName(), null, output, "geometry", "Cannot set position of a borne.", CorruptionLevel.FIELD));
        }

        return output;
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_BORNE.name();
    }

    @Override
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for(Columns c : Columns.values())
            columns.add(c.toString());
        return columns;
    }

    @Override
    public String getTableName() {
        return BORNE_DIGUE.toString();
    }
}
