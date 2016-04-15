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
package fr.sirs.importer.v2.mapper.objet;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.Photo;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefOrientationPhoto;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.CorruptionLevel;
import fr.sirs.importer.v2.ErrorReport;
import fr.sirs.importer.v2.mapper.AbstractMapper;
import fr.sirs.importer.v2.mapper.Mapper;
import fr.sirs.importer.v2.mapper.MapperSpi;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotoolkit.display2d.GO2Utilities;
import org.opengis.referencing.operation.TransformException;
import org.springframework.stereotype.Component;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
@Component
public class PhotoMapperSpi implements MapperSpi<Photo> {

    @Override
    public Optional<Mapper<Photo>> configureInput(Table inputType) throws IllegalStateException {
        boolean geoMissing = false, linearMissing = false;
        for (final PhotoColumns col : PhotoColumns.values()) {
            try {
                inputType.getColumn(col.name());
            } catch (IllegalArgumentException e) {
                if (PhotoColumns.X_PHOTO.equals(col) || PhotoColumns.Y_PHOTO.equals(col)) {
                    geoMissing = true;
                } else if (PhotoColumns.ID_BORNEREF.equals(col)
                        || PhotoColumns.ID_SYSTEME_REP.equals(col)
                        || PhotoColumns.DIST_BORNEREF.equals(col)
                        || PhotoColumns.AMONT_AVAL.equals(col)
                        || PhotoColumns.PR_PHOTO.equals(col)) {
                    linearMissing = true;
                } else {
                    return Optional.empty();
                }
            }
        }
        return Optional.of(new PhotoMapper(inputType, geoMissing, linearMissing));
    }

    @Override
    public Class<Photo> getOutputClass() {
        return Photo.class;
    }

    private static class PhotoMapper extends AbstractMapper<Photo> {

        protected AbstractImporter<Contact> contactImporter;
        protected AbstractImporter<RefOrientationPhoto> orientationImporter;
        protected AbstractImporter<RefCote> coteImporter;
        protected AbstractImporter<BorneDigue> borneImporter;
        protected AbstractImporter<SystemeReperage> srImporter;
        final boolean geoMissing;
        final boolean linearMissing;

        private PhotoMapper(final Table t, final boolean geoMissing, final boolean linearMissing) {
            super(t);
            this.geoMissing = geoMissing;
            this.linearMissing = linearMissing;

            contactImporter = context.importers.get(Contact.class);
            if (contactImporter == null) {
                throw new IllegalStateException("Cannot retrieve needed Contact importer for position imports.");
            }

            orientationImporter = context.importers.get(RefOrientationPhoto.class);
            if (orientationImporter == null) {
                throw new IllegalStateException("Cannot retrieve needed RefOrientationPhoto importer for position imports.");
            }

            coteImporter = context.importers.get(RefCote.class);
            if (coteImporter == null) {
                throw new IllegalStateException("Cannot retrieve needed RefCote importer for position imports.");
            }

            borneImporter = context.importers.get(BorneDigue.class);
            if (borneImporter == null) {
                throw new IllegalStateException("Cannot retrieve needed BorneDigue importer for position imports.");
            }

            srImporter = context.importers.get(SystemeReperage.class);
            if (srImporter == null) {
                throw new IllegalStateException("Cannot retrieve needed SystemeReperage importer for position imports.");
            }
        }

        @Override
        public void map(Row input, Photo output) throws IllegalStateException, IOException, AccessDbImporterException {

            final Object contact = input.get(PhotoColumns.ID_INTERV_PHOTOGRAPH.name());
            if (contact != null) {
                output.setPhotographeId(contactImporter.getImportedId(contact));
            }

            final Object orientation = input.get(PhotoColumns.ID_ORIENTATION.name());
            if (orientation != null) {
                output.setOrientationPhoto(orientationImporter.getImportedId(orientation));
            }

            final Object typeCote = input.get(PhotoColumns.ID_TYPE_COTE.name());
            if (typeCote != null) {
                output.setCoteId(coteImporter.getImportedId(typeCote));
            }

            final Date date = input.getDate(PhotoColumns.DATE_PHOTO.name());
            if (date != null) {
                output.setDate(context.convertData(date, LocalDate.class));
            }

            if (!geoMissing) {
                final Double x = input.getDouble(PhotoColumns.X_PHOTO.name());
                final Double y = input.getDouble(PhotoColumns.Y_PHOTO.name());

                if (x != null && y != null) {
                    final double[] point = new double[]{x, y};
                    try {
                        context.geoTransform.transform(point, 0, point, 0, 1);
                        final Point position = GO2Utilities.JTS_FACTORY.createPoint(new Coordinate(point[0], point[1]));
                        output.setPositionDebut(position);
                        output.setPositionFin(position);
                    } catch (TransformException ex) {
                        Logger.getLogger(PhotoMapperSpi.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            if (!linearMissing) {
                Object startId = input.get(PhotoColumns.ID_BORNEREF.toString());
                boolean isNumber = startId instanceof Number;
                if (isNumber) {
                    startId = ((Number) startId).intValue();
                }
                if (startId != null && (!isNumber || ((Number) startId).intValue() > 0)) {
                    final String bId = borneImporter.getImportedId(startId);
                    if (bId != null) {
                        output.setBorneDebutId(bId);
                    } else {
                        context.reportError(new ErrorReport(null, input, tableName, PhotoColumns.ID_BORNEREF.name(), output, "borneDebutId", "Cannot set linear referencing. No borne imported for ID : " + startId, CorruptionLevel.FIELD));
                    }
                }

                final Double startDistance = input.getDouble(PhotoColumns.DIST_BORNEREF.toString());
                if (startDistance != null) {
                    output.setBorne_debut_distance(startDistance.floatValue());
                }
                final Double startPr = input.getDouble(PhotoColumns.PR_PHOTO.toString());
                if (startPr != null) {
                    output.setPrDebut(startPr.floatValue());
                }

                output.setBorne_debut_aval(input.getBoolean(PhotoColumns.AMONT_AVAL.toString()));

                // SR
                final Object srid = input.get(PhotoColumns.ID_SYSTEME_REP.toString());
                if (srid != null) {
                    output.setSystemeRepId(srImporter.getImportedId(srid));
                }
            }
        }
    }
}
