package fr.sirs.importer.v2.linear;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import fr.sirs.core.model.Digue;
import fr.sirs.core.model.RefRive;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.AbstractImporter;
import fr.sirs.importer.v2.AbstractImporter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.geotoolkit.data.shapefile.shp.ShapeHandler;
import org.geotoolkit.data.shapefile.shp.ShapeType;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.referencing.LinearReferencing;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class TronconGestionDigueImporter extends AbstractImporter<TronconDigue> {

    private static final String GEOM_TABLE = "CARTO_TRONCON_GESTION_DIGUE";
    private static final String GEOM_COLUMN = "SHAPE";

    private AbstractImporter<RefRive> typeRiveImporter;
    //private final TronconGestionDigueGestionnaireImporter tronconGestionDigueGestionnaireImporter;
    private AbstractImporter<Digue> digueImporter;

    private Column idColumn;
    private Cursor geometryCursor;

    enum Columns {
        ID_TRONCON_GESTION,
//        ID_ORG_GESTIONNAIRE, //Dans les gestions ?
        ID_DIGUE,
        ID_TYPE_RIVE,
        DATE_DEBUT_VAL_TRONCON,
        DATE_FIN_VAL_TRONCON,
        NOM_TRONCON_GESTION,
        COMMENTAIRE_TRONCON,
//        DATE_DEBUT_VAL_GESTIONNAIRE_D, //Dans les gestions ?
//        DATE_FIN_VAL_GESTIONNAIRE_D, //Dans les gestions ?
        ID_SYSTEME_REP_DEFAUT,
        DATE_DERNIERE_MAJ
    };

    @Override
    protected Class<TronconDigue> getDocumentClass() {
        return TronconDigue.class;
    }

    @Override
    public String getRowIdFieldName() {
        return Columns.ID_TRONCON_GESTION.name();
    }

    @Override
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return TRONCON_GESTION_DIGUE.toString();
    }

    @Override
    protected void postCompute() {
        super.postCompute();
        typeRiveImporter = null;
        digueImporter = null;
        geometryCursor = null;
        idColumn = null;
    }

    @Override
    protected void preCompute() throws AccessDbImporterException {
        super.preCompute();
        typeRiveImporter = context.importers.get(RefRive.class);
        digueImporter = context.importers.get(Digue.class);
        try {
            final Table t = context.inputCartoDb.getTable(GEOM_TABLE);
            geometryCursor = t.getDefaultCursor();
            idColumn = t.getColumn(getRowIdFieldName());
        } catch (Exception e) {
            throw new AccessDbImporterException("Cannot get geometry data for TronconDigue.", e);
        }
    }

    @Override
    public TronconDigue importRow(Row row, TronconDigue tronconDigue) throws IOException, AccessDbImporterException {
        tronconDigue = super.importRow(row, tronconDigue);

        tronconDigue.setLibelle(row.getString(Columns.NOM_TRONCON_GESTION.toString()));
        tronconDigue.setCommentaire(row.getString(Columns.COMMENTAIRE_TRONCON.toString()));

        final Date DATE_DERNIERE_MAJ = row.getDate(Columns.DATE_DERNIERE_MAJ.toString());
        if (DATE_DERNIERE_MAJ != null) {
            tronconDigue.setDateMaj(context.convertData(DATE_DERNIERE_MAJ, LocalDate.class));
        }

        final Date DATE_DEBUT_VAL_TRONCON = row.getDate(Columns.DATE_DEBUT_VAL_TRONCON.toString());
        if (DATE_DEBUT_VAL_TRONCON != null) {
            tronconDigue.setDate_debut(context.convertData(DATE_DEBUT_VAL_TRONCON, LocalDate.class));
        }

        final Date DATE_FIN_VAL_TRONCON = row.getDate(Columns.DATE_FIN_VAL_TRONCON.toString());
        if (DATE_FIN_VAL_TRONCON != null) {
            tronconDigue.setDate_fin(context.convertData(DATE_FIN_VAL_TRONCON, LocalDate.class));
        }

        final Integer riveId = row.getInt(Columns.ID_TYPE_RIVE.toString());
        if (riveId != null) {
            tronconDigue.setTypeRiveId(typeRiveImporter.getImportedId(riveId));
        }

        Integer digueId = row.getInt(Columns.ID_DIGUE.toString());
        if (digueId != null) {
            tronconDigue.setDigueId(digueImporter.getImportedId(digueId));
        }

        final Integer tronconId = row.getInt(getRowIdFieldName());
        if (tronconId == null) {
            throw new AccessDbImporterException("No id set in current row !");
        }

        tronconDigue.setGeometry(computeGeometry(tronconId));

        return tronconDigue;
    }

    /**
     * Search for the last valid geometry which has been submitted for a given {@link TronconDigue}
     *
     * @param tronconId Id of the source {@link TronconDigue } we want a geometry for.
     * @return A line string registered for the given object, or null if we cannot find any.
     */
    private LineString computeGeometry(final int tronconId) throws IOException {
        // Only take the last submitted geometry for the object.
        geometryCursor.afterLast();
        while (geometryCursor.findNextRow(idColumn, tronconId)) {
            final Row currentRow = geometryCursor.getCurrentRow();
            final byte[] bytes = currentRow.getBytes(GEOM_COLUMN);
            try {
                if (bytes == null || bytes.length <= 0) {
                    continue;
                }
                final ByteBuffer bb = ByteBuffer.wrap(bytes);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                final int id = bb.getInt();
                final ShapeType shapeType = ShapeType.forID(id);
                final ShapeHandler handler = shapeType.getShapeHandler(false);
                final Geometry tmpGeometry = JTS.transform((Geometry) handler.read(bb, shapeType), context.geoTransform);
                final LineString geom = LinearReferencing.asLineString(tmpGeometry);
                if (geom != null)
                    return geom;
            } catch (Exception e) {
                context.reportError(GEOM_TABLE, currentRow, e, "A geometry cannot be read.");
            }
        }
        return null;
    }
}
