package fr.sirs.importer.troncon;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import static fr.sirs.core.LinearReferencingUtilities.buildGeometry;
import fr.sirs.core.model.BorneDigue;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.SyndicTroncon;
import fr.sirs.core.model.Syndicat;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.SystemeReperageImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ektorp.CouchDbConnector;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class TronconGestionDigueSyndicatImporter extends GenericPeriodeLocaliseeImporter<SyndicTroncon> {
    
    private final SyndicatImporter syndicatImporter;

    public TronconGestionDigueSyndicatImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final BorneDigueImporter borneDigueImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, 
                systemeReperageImporter, borneDigueImporter);
        this.syndicatImporter = new SyndicatImporter(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_TRONCON_SYNDICAT,
        ID_TRONCON_GESTION,
        ID_SYNDICAT,
        DATE_DEBUT,
        DATE_FIN,
//        ID_TYPE_COTE, // Pas dans le nouveau modèle
        PR_DEBUT_CALCULE, // Pas dans le nouveau modèle
        PR_FIN_CALCULE, // Pas dans le nouveau modèle
        X_DEBUT, // Pas dans le nouveau modèle
        X_FIN, // Pas dans le nouveau modèle
        Y_DEBUT, // Pas dans le nouveau modèle
        Y_FIN, // Pas dans le nouveau modèle
        ID_BORNEREF_DEBUT, // Pas dans le nouveau modèle
        ID_BORNEREF_FIN, // Pas dans le nouveau modèle
        ID_SYSTEME_REP, // Pas dans le nouveau modèle
        DIST_BORNEREF_DEBUT, // Pas dans le nouveau modèle
        DIST_BORNEREF_FIN, // Pas dans le nouveau modèle
        AMONT_AVAL_DEBUT, // Pas dans le nouveau modèle
        AMONT_AVAL_FIN, // Pas dans le nouveau modèle
//        COMMENTAIRE, // Pas dans le nouveau modèle
        DATE_DERNIERE_MAJ
    };

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
        return TRONCON_GESTION_DIGUE_SYNDICAT.toString();
    }

    @Override
    public void compute() throws IOException, AccessDbImporterException {
        objets = new HashMap<>();

        final Map<Integer, Syndicat> syndicats = syndicatImporter.getSyndicats();
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final SyndicTroncon periodeSyndicale = createAnonymValidElement(SyndicTroncon.class);
            
            final TronconDigue troncon = tronconGestionDigueImporter.getTronconsDigues().get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            periodeSyndicale.setLinearId(troncon.getId());
            
            periodeSyndicale.setDesignation(String.valueOf(row.getInt(Columns.ID_TRONCON_SYNDICAT.toString())));

            if (row.getDate(Columns.DATE_DEBUT.toString()) != null) {
                periodeSyndicale.setDate_debut(DbImporter.parse(row.getDate(Columns.DATE_DEBUT.toString()), dateTimeFormatter));
            }
            if (row.getDate(Columns.DATE_FIN.toString()) != null) {
                periodeSyndicale.setDate_fin(DbImporter.parse(row.getDate(Columns.DATE_FIN.toString()), dateTimeFormatter));
            }
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                periodeSyndicale.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            
            if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
                periodeSyndicale.setPR_debut(row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue());
            }

            if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
                periodeSyndicale.setPR_fin(row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue());
            }

            if (row.getInt(Columns.ID_SYSTEME_REP.toString()) != null) {
                periodeSyndicale.setSystemeRepId(systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString())).getId());
            }

            if (row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()) != null) {
                periodeSyndicale.setBorneDebutId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()).doubleValue()).getId());
            }

            periodeSyndicale.setBorne_debut_aval(row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString()));

            if (row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()) != null) {
                periodeSyndicale.setBorne_debut_distance(row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()).floatValue());
            }

            if (row.getDouble(Columns.ID_BORNEREF_FIN.toString()) != null) {
                periodeSyndicale.setBorneFinId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
            }

            periodeSyndicale.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL_FIN.toString()));

            if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
                periodeSyndicale.setBorne_fin_distance(row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue());
            }
            
            GeometryFactory geometryFactory = new GeometryFactory();
            final MathTransform lambertToRGF;
            try {
                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), getOutputCrs(), true);

                try {

                    if (row.getDouble(Columns.X_DEBUT.toString()) != null && row.getDouble(Columns.Y_DEBUT.toString()) != null) {
                        periodeSyndicale.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X_DEBUT.toString()),
                                row.getDouble(Columns.Y_DEBUT.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(ProprietaireTronconGestionImporter.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {

                    if (row.getDouble(Columns.X_FIN.toString()) != null && row.getDouble(Columns.Y_FIN.toString()) != null) {
                        periodeSyndicale.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                                row.getDouble(Columns.X_FIN.toString()),
                                row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF));
                    }
                } catch (MismatchedDimensionException | TransformException ex) {
                    Logger.getLogger(ProprietaireTronconGestionImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(ProprietaireTronconGestionImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            periodeSyndicale.setGeometry(buildGeometry(troncon.getGeometry(), periodeSyndicale, tronconGestionDigueImporter.getBorneDigueRepository()));

            // Set the references.
            if(row.getInt(Columns.ID_SYNDICAT.toString())!=null){
                final Syndicat syndicat = syndicats.get(row.getInt(Columns.ID_SYNDICAT.toString()));
                if (syndicat.getId() != null) {
                    periodeSyndicale.setSyndicatId(syndicat.getId());
                } else {
                    throw new AccessDbImporterException("Le contact " + syndicat + " n'a pas encore d'identifiant CouchDb !");
                }
            }
            
            objets.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), periodeSyndicale);
        }
        couchDbConnector.executeBulk(objets.values());
    }
}
