package fr.sirs.importer.objet.desordre;

import fr.sirs.importer.objet.TypePositionImporter;
import fr.sirs.importer.TypeCoteImporter;
import fr.sirs.importer.objet.SourceInfoImporter;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import static fr.sirs.core.LinearReferencingUtilities.buildGeometry;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Desordre;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.Observation;
import fr.sirs.core.model.RefCote;
import fr.sirs.core.model.RefPosition;
import fr.sirs.core.model.RefSource;
import fr.sirs.core.model.RefTypeDesordre;
import fr.sirs.core.model.SystemeReperage;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.troncon.TronconGestionDigueImporter;
import java.io.IOException;
import java.util.ArrayList;
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
class SysEvtDesordreImporter extends GenericDesordreImporter {
    
    private final TypeDesordreImporter typeDesordreImporter;
    private final DesordreObservationImporter desordreObservationImporter;

    SysEvtDesordreImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final TronconGestionDigueImporter tronconGestionDigueImporter, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final DesordreObservationImporter desordreObservationImporter,
            final SourceInfoImporter typeSourceImporter,
            final TypePositionImporter typePositionImporter,
            final TypeCoteImporter typeCoteImporter,
            final TypeDesordreImporter typeDesordreImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter,
                typeSourceImporter, typeCoteImporter, typePositionImporter);
        this.typeDesordreImporter = typeDesordreImporter;
        this.desordreObservationImporter = desordreObservationImporter;
    }

    private enum Columns {

        ID_DESORDRE,
        //            id_nom_element,// Aucun intéret
        //            ID_SOUS_GROUPE_DONNEES,// Aucun intéret
        //            LIBELLE_SOUS_GROUPE_DONNEES,// Aucun intéret
        ID_TYPE_DESORDRE,
        //            LIBELLE_TYPE_DESORDRE,// Dans TypeDesordreImporter
        //            DECALAGE_DEFAUT, // Info d'affichage
        //            DECALAGE, // Info d'affichage
        //            LIBELLE_SOURCE, // Dans TypeSourceImporter
        //            LIBELLE_TYPE_COTE, // Dans TypeCoteImporter
        //            LIBELLE_SYSTEME_REP, // Dans SystemeRepImporter
        //            NOM_BORNE_DEBUT, //Dans BorneImporter
        //            NOM_BORNE_FIN, //Dans BorneImporter
        //            DISPARU_OUI_NON,
        //            DEJA_OBSERVE_OUI_NON,
        //            LIBELLE_TYPE_POSITION,// Dans typePositionImporter
        ID_TYPE_COTE,
        ID_TYPE_POSITION,
        ID_TRONCON_GESTION,
        ID_SOURCE,
        DATE_DEBUT_VAL,
        DATE_FIN_VAL,
        PR_DEBUT_CALCULE,
        PR_FIN_CALCULE,
        ID_SYSTEME_REP,
        ID_BORNEREF_DEBUT,
        AMONT_AVAL_DEBUT,
        DIST_BORNEREF_DEBUT,
        ID_BORNEREF_FIN,
        AMONT_AVAL_FIN,
        DIST_BORNEREF_FIN,
        LIEU_DIT_DESORDRE,
        DESCRIPTION_DESORDRE,
        //            ID_AUTO

        //Empty fields
        //     ID_PRESTATION, // obsolète ? voir table DESORDRE_PRESTATION
        //     LIBELLE_PRESTATION, // Dans l'importateur de prestations
        X_DEBUT,
        Y_DEBUT,
        X_FIN,
        Y_FIN,
//     COMMENTAIRE, // obsolète ? voir champ DESCRIPTION_DESORDRE 
    };

    @Override
    public String getTableName() {
        return SYS_EVT_DESORDRE.toString();
    }

    @Override
    public Desordre importRow(Row row) throws IOException, AccessDbImporterException {

        final TronconDigue troncon = tronconGestionDigueImporter.getTronconsDigues().get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();

        final Map<Integer, RefSource> typesSource = sourceInfoImporter.getTypeReferences();
        final Map<Integer, RefCote> typesCote = typeCoteImporter.getTypeReferences();
        final Map<Integer, RefPosition> typesPosition = typePositionImporter.getTypeReferences();

        final Map<Integer, RefTypeDesordre> typesDesordre = typeDesordreImporter.getTypeReferences();
        final Map<Integer, List<Observation>> observations = desordreObservationImporter.getObservationsByDesordreId();

        final Desordre desordre = createAnonymValidElement(Desordre.class);
        
        desordre.setLinearId(troncon.getId());
        desordre.setDesignation(String.valueOf(row.getInt(Columns.ID_DESORDRE.toString())));

        if (row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()) != null) {
            final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()).doubleValue());
            if (b != null) {
                desordre.setBorneDebutId(b.getId());
            }
        }
        if (row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()) != null) {
            desordre.setBorne_debut_distance(row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()).floatValue());
        }
        if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
            desordre.setPrDebut(row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue());
        }

        if (row.getDouble(Columns.ID_BORNEREF_FIN.toString()) != null) {
            final BorneDigue b = bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue());
            if (b != null) {
                desordre.setBorneFinId(b.getId());
            }
        }
        if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
            desordre.setBorne_fin_distance(row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue());
        }
        if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
            desordre.setPrFin(row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue());
        }

        if (row.getInt(Columns.ID_SYSTEME_REP.toString()) != null) {
            desordre.setSystemeRepId(systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString())).getId());
        }

        desordre.setBorne_debut_aval(row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString()));
        desordre.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL_FIN.toString()));
        desordre.setLieuDit(row.getString(Columns.LIEU_DIT_DESORDRE.toString()));

        if (row.getInt(Columns.ID_TYPE_DESORDRE.toString()) != null) {
            desordre.setTypeDesordreId(typesDesordre.get(row.getInt(Columns.ID_TYPE_DESORDRE.toString())).getId());
        }

        if (row.getInt(Columns.ID_SOURCE.toString()) != null) {
            desordre.setSourceId(typesSource.get(row.getInt(Columns.ID_SOURCE.toString())).getId());
        }

        if (row.getInt(Columns.ID_TYPE_POSITION.toString()) != null) {
            desordre.setPositionId(typesPosition.get(row.getInt(Columns.ID_TYPE_POSITION.toString())).getId());
        }

        if (row.getInt(Columns.ID_TYPE_COTE.toString()) != null) {
            desordre.setCoteId(typesCote.get(row.getInt(Columns.ID_TYPE_COTE.toString())).getId());
        }

        if (row.getDate(Columns.DATE_DEBUT_VAL.toString()) != null) {
            desordre.setDate_debut(DbImporter.parseLocalDate(row.getDate(Columns.DATE_DEBUT_VAL.toString()), dateTimeFormatter, desordre));
        }
        
        if (row.getDate(Columns.DATE_FIN_VAL.toString()) != null) {
            desordre.setDate_fin(DbImporter.parseLocalDate(row.getDate(Columns.DATE_FIN_VAL.toString()), dateTimeFormatter, desordre));
        }

        if (row.getString(Columns.DESCRIPTION_DESORDRE.toString()) != null) {
            desordre.setCommentaire(row.getString(Columns.DESCRIPTION_DESORDRE.toString()));
        }

        GeometryFactory geometryFactory = new GeometryFactory();
        final MathTransform lambertToRGF;
        try {
            lambertToRGF = CRS.findMathTransform(DbImporter.IMPORT_CRS, getOutputCrs(), true);

            try {

                if (row.getDouble(Columns.X_DEBUT.toString()) != null && row.getDouble(Columns.Y_DEBUT.toString()) != null) {
                    desordre.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                            row.getDouble(Columns.X_DEBUT.toString()),
                            row.getDouble(Columns.Y_DEBUT.toString()))), lambertToRGF));
                }
            } catch (MismatchedDimensionException | TransformException ex) {
                Logger.getLogger(SysEvtDesordreImporter.class.getName()).log(Level.WARNING, null, ex);
            }

            try {

                if (row.getDouble(Columns.X_FIN.toString()) != null && row.getDouble(Columns.Y_FIN.toString()) != null) {
                    desordre.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
                            row.getDouble(Columns.X_FIN.toString()),
                            row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF));
                }
            } catch (MismatchedDimensionException | TransformException ex) {
                Logger.getLogger(SysEvtDesordreImporter.class.getName()).log(Level.WARNING, null, ex);
            }
        } catch (FactoryException ex) {
            Logger.getLogger(SysEvtDesordreImporter.class.getName()).log(Level.WARNING, null, ex);
        }

        if (observations.get(row.getInt(Columns.ID_DESORDRE.toString())) != null) {
            desordre.setObservations(observations.get(row.getInt(Columns.ID_DESORDRE.toString())));
        }
        
        desordre.setGeometry(buildGeometry(troncon.getGeometry(), desordre, tronconGestionDigueImporter.getBorneDigueRepository()));
        
        return desordre;
    }

    @Override
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }
}
