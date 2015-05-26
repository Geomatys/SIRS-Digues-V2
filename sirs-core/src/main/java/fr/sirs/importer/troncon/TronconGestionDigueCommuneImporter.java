package fr.sirs.importer.troncon;

import com.healthmarketscience.jackcess.Database;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.TypeCoteImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TronconGestionDigueCommuneImporter extends GenericPeriodeLocaliseeImporter {
    
    private final CommuneImporter communeImporter;
    private final TypeCoteImporter typeCoteImporter;

    TronconGestionDigueCommuneImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final BorneDigueImporter borneDigueImporter, 
            final CommuneImporter communeImporter,
            final TypeCoteImporter typeCoteImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter, systemeReperageImporter, borneDigueImporter);
        this.communeImporter = communeImporter;
        this.typeCoteImporter = typeCoteImporter;
    }

    private enum Columns {
        ID_TRONCON_COMMUNE,
        ID_TRONCON_GESTION,
        ID_COMMUNE,
        DATE_DEBUT,
        DATE_FIN,
        ID_TYPE_COTE,
        PR_DEBUT_CALCULE,
        PR_FIN_CALCULE,
        X_DEBUT,
        X_FIN,
        Y_DEBUT,
        Y_FIN,
        ID_BORNEREF_DEBUT,
        ID_BORNEREF_FIN,
        ID_SYSTEME_REP,
        DIST_BORNEREF_DEBUT,
        DIST_BORNEREF_FIN,
        AMONT_AVAL_DEBUT,
        AMONT_AVAL_FIN,
        COMMENTAIRE,
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
        return TRONCON_GESTION_DIGUE_COMMUNE.toString();
    }

    @Override
    public void compute() throws IOException, AccessDbImporterException {
//        communesByTronconId = new HashMap<>();
//        communesByTronconCommuneId = new HashMap<>();
//
//        final Map<Integer, BorneDigue> bornes = borneDigueImporter.getBorneDigue();
//        final Map<Integer, SystemeReperage> systemesReperage = systemeReperageImporter.getSystemeRepLineaire();
//        
//        final Map<Integer, RefCote> typesCote = typeCoteImporter.getTypeReferences();
//        
//        final Map<Integer, Commune> communes = communeImporter.getCommunes();
//        
//        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
//        while (it.hasNext()) {
//            final Row row = it.next();
//            final PeriodeCommune periode = createAnonymValidElement(PeriodeCommune.class);
//
//            if (row.getDate(Columns.DATE_DEBUT.toString()) != null) {
//                periode.setDate_debut(DbImporter.parse(row.getDate(Columns.DATE_DEBUT.toString()), dateTimeFormatter));
//            }
//            
//            if (row.getDate(Columns.DATE_FIN.toString()) != null) {
//                periode.setDate_fin(DbImporter.parse(row.getDate(Columns.DATE_FIN.toString()), dateTimeFormatter));
//            }
//            
//            if(row.getInt(Columns.ID_TYPE_COTE.toString())!=null){
//                periode.setCoteId(typesCote.get(row.getInt(Columns.ID_TYPE_COTE.toString())).getId());
//            }
//            
//            if (row.getDouble(Columns.PR_DEBUT_CALCULE.toString()) != null) {
//                periode.setPR_debut(row.getDouble(Columns.PR_DEBUT_CALCULE.toString()).floatValue());
//            }
//            
//            if (row.getDouble(Columns.PR_FIN_CALCULE.toString()) != null) {
//                periode.setPR_fin(row.getDouble(Columns.PR_FIN_CALCULE.toString()).floatValue());
//            }
//            
//            GeometryFactory geometryFactory = new GeometryFactory();
//            final MathTransform lambertToRGF;
//            try {
//                lambertToRGF = CRS.findMathTransform(CRS.decode("EPSG:27563"), getOutputCrs(), true);
//
//                try {
//
//                    if (row.getDouble(Columns.X_DEBUT.toString()) != null && row.getDouble(Columns.Y_DEBUT.toString()) != null) {
//                        periode.setPositionDebut((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
//                                row.getDouble(Columns.X_DEBUT.toString()),
//                                row.getDouble(Columns.Y_DEBUT.toString()))), lambertToRGF));
//                    }
//                } catch (MismatchedDimensionException | TransformException ex) {
//                    Logger.getLogger(TronconGestionDigueCommuneImporter.class.getName()).log(Level.SEVERE, null, ex);
//                }
//
//                try {
//
//                    if (row.getDouble(Columns.X_FIN.toString()) != null && row.getDouble(Columns.Y_FIN.toString()) != null) {
//                        periode.setPositionFin((Point) JTS.transform(geometryFactory.createPoint(new Coordinate(
//                                row.getDouble(Columns.X_FIN.toString()),
//                                row.getDouble(Columns.Y_FIN.toString()))), lambertToRGF));
//                    }
//                } catch (MismatchedDimensionException | TransformException ex) {
//                    Logger.getLogger(TronconGestionDigueCommuneImporter.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            } catch (FactoryException ex) {
//                Logger.getLogger(TronconGestionDigueCommuneImporter.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            
//            if (row.getInt(Columns.ID_SYSTEME_REP.toString()) != null) {
//                periode.setSystemeRepId(systemesReperage.get(row.getInt(Columns.ID_SYSTEME_REP.toString())).getId());
//            }
//            
//            if (row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()) != null) {
//                periode.setBorneDebutId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_DEBUT.toString()).doubleValue()).getId());
//            }
//            
//            periode.setBorne_debut_aval(row.getBoolean(Columns.AMONT_AVAL_DEBUT.toString()));
//            
//            if (row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()) != null) {
//                periode.setBorne_debut_distance(row.getDouble(Columns.DIST_BORNEREF_DEBUT.toString()).floatValue());
//            }
//            
//            if (row.getDouble(Columns.ID_BORNEREF_FIN.toString()) != null) {
//                periode.setBorneFinId(bornes.get((int) row.getDouble(Columns.ID_BORNEREF_FIN.toString()).doubleValue()).getId());
//            }
//            
//            periode.setBorne_fin_aval(row.getBoolean(Columns.AMONT_AVAL_FIN.toString()));
//            
//            if (row.getDouble(Columns.DIST_BORNEREF_FIN.toString()) != null) {
//                periode.setBorne_fin_distance(row.getDouble(Columns.DIST_BORNEREF_FIN.toString()).floatValue());
//            }
//            
//            periode.setCommentaire(row.getString(Columns.COMMENTAIRE.toString()));
//            
//            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
//                periode.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
//            }
//
//            // Set the references.
//            final Commune commune = communes.get(row.getInt(Columns.ID_COMMUNE.toString()));
//            if (commune.getId() != null) {
//                periode.setCommuneId(commune.getId());
//            } else {
//                throw new AccessDbImporterException("L'organisme " + commune + " n'a pas encore d'identifiant CouchDb !");
//            }
//            
//            periode.setDesignation(String.valueOf(row.getInt(Columns.ID_TRONCON_COMMUNE.toString())));
//            
//            communesByTronconCommuneId.put(row.getInt(Columns.ID_TRONCON_COMMUNE.toString()), periode);
//
//            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
//            List<PeriodeCommune> listeCommunes = communesByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
//            if(listeCommunes == null){
//                listeCommunes = new ArrayList<>();
//            }
//            listeCommunes.add(periode);
//            communesByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listeCommunes);
//        }
    }
}
