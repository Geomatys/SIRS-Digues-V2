package fr.sirs.importer.v2.mapper.objet;

import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import fr.sirs.core.model.BorneDigue;
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
import java.beans.IntrospectionException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotoolkit.display2d.GO2Utilities;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class PhotoMapperSpi implements MapperSpi<Photo> {

    private final HashMap<String, String> bindings;

    public PhotoMapperSpi() throws IntrospectionException {
        bindings = new HashMap<>(2);
        bindings.put(PhotoColumns.ID_ORIENTATION.name(), "orientationPhoto");
        bindings.put(PhotoColumns.ID_TYPE_COTE.name(), "coteId");
        bindings.put(PhotoColumns.ID_SYSTEME_REP.name(), "systemeRepId");
        bindings.put(PhotoColumns.PR_PHOTO.name(), "prDebut");
        bindings.put(PhotoColumns.X_PHOTO.name(), "coteId");
        bindings.put(PhotoColumns.ID_TYPE_COTE.name(), "coteId");
    }

    @Override
    public Optional<Mapper<Photo>> configureInput(Table inputType) throws IllegalStateException {
        boolean geoMissing = false, linearMissing = false;
        for (final PhotoColumns col : PhotoColumns.values()) {
            try {
                inputType.getColumn(col.name());
            } catch (IllegalArgumentException e) {
                if (PhotoColumns.X_PHOTO.equals(col) || PhotoColumns.X_PHOTO.equals(col)) {
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
            final Integer orientation = input.getInt(PhotoColumns.ID_ORIENTATION.name());
            if (orientation != null) {
                output.setOrientationPhoto(orientationImporter.getImportedId(orientation));
            }

            final Integer typeCote = input.getInt(PhotoColumns.ID_TYPE_COTE.name());
            if (typeCote != null) {
                output.setCoteId(coteImporter.getImportedId(typeCote));
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
                final Double startId = input.getDouble(PhotoColumns.ID_BORNEREF.toString());
                if (startId != null) {
                    final String bId = borneImporter.getImportedId(startId.intValue());
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
                final Integer srid = input.getInt(PhotoColumns.ID_SYSTEME_REP.toString());
                if (srid != null) {
                    output.setSystemeRepId(srImporter.getImportedId(srid));
                }
            }
        }
    }
}
