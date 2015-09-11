package fr.sirs.importer.objet.prestation;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Prestation;
import fr.sirs.core.model.RefSource;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.objet.SourceInfoImporter;
import fr.sirs.importer.TypeCoteImporter;
import fr.sirs.importer.objet.TypePositionImporter;
import fr.sirs.importer.documentTroncon.document.marche.MarcheImporter;
import fr.sirs.importer.troncon.TronconGestionDigueImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class PrestationImporter extends GenericPrestationImporter {

    private final TypePrestationImporter typePrestationImporter;
    private final SysEvtPrestationImporter sysEvtPrestationImporter;

    public PrestationImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter,
            final BorneDigueImporter borneDigueImporter,
            final MarcheImporter marcheImporter,
            final SourceInfoImporter typeSourceImporter,
            final TypeCoteImporter typeCoteImporter,
            final TypePositionImporter typePositionImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, marcheImporter,
                typeSourceImporter, typePositionImporter, typeCoteImporter);
        this.typePrestationImporter = new TypePrestationImporter(accessDatabase,
                couchDbConnector);
        this.sysEvtPrestationImporter = new SysEvtPrestationImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, marcheImporter,
                typePositionImporter, typeCoteImporter, typePrestationImporter);
    }

    private enum Columns {
        ID_PRESTATION,
        ID_TRONCON_GESTION,
//        LIBELLE_PRESTATION,
////        ID_MARCHE,
        REALISATION_INTERNE,
//        ID_TYPE_PRESTATION,
        COUT_AU_METRE,//
        COUT_GLOBAL,//
//        ID_TYPE_COTE,
//        ID_TYPE_POSITION,
////        ID_INTERV_REALISATEUR, // Ne sert à rien : voir la table PRESTATION_INTERVENANT
//        DESCRIPTION_PRESTATION,
//        DATE_DEBUT_VAL,
//        DATE_FIN_VAL,
        ID_SOURCE,//
//        PR_DEBUT_CALCULE,
//        PR_FIN_CALCULE,
//        X_DEBUT,
//        Y_DEBUT,
//        X_FIN,
//        Y_FIN,
//        ID_BORNEREF_DEBUT,
//        ID_BORNEREF_FIN,
//        ID_SYSTEME_REP,
//        DIST_BORNEREF_DEBUT,
//        DIST_BORNEREF_FIN,
//        AMONT_AVAL_DEBUT,
//        AMONT_AVAL_FIN,
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
        return PRESTATION.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {

        objets = new HashMap<>();
        objetsByTronconId = new HashMap<>();

        final Map<Integer, RefSource> typesSource = sourceInfoImporter.getTypeReferences();


        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final Prestation objet = importRow(row);

            if(objet!=null){

                {
                    final boolean realisation = row.getBoolean(Columns.REALISATION_INTERNE.toString());
                    objet.setRealisationInterne(realisation);
                }

                if (row.getDouble(Columns.COUT_AU_METRE.toString()) != null) {
                    objet.setCoutMetre(row.getDouble(Columns.COUT_AU_METRE.toString()).floatValue());
                }

                if (row.getDouble(Columns.COUT_GLOBAL.toString()) != null) {
                    objet.setCoutGlobal(row.getDouble(Columns.COUT_GLOBAL.toString()).floatValue());
                }

                if(row.getInt(Columns.ID_SOURCE.toString())!=null){
                    final RefSource typeSource = sourceInfoImporter.getImportedId(row.getInt(Columns.ID_SOURCE.toString()));
                    if(typeSource!=null){
                        if(objet.getSourceId()==null){
                            objet.setSourceId(typeSource.getId());
                        }
                    }
                }

                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                objets.put(row.getInt(Columns.ID_PRESTATION.toString()), objet);

                // Set the list ByTronconId
                List<Prestation> listByTronconId = objetsByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
                if (listByTronconId == null) {
                    listByTronconId = new ArrayList<>();
                    objetsByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
                }
                listByTronconId.add(objet);
            }
        }
        context.outputDb.executeBulk(objets.values());
    }

    @Override
    public public  importRow(Row row) throws IOException, AccessDbImporterException {
        return sysEvtPrestationImporter.importRow(row);
    }
}
