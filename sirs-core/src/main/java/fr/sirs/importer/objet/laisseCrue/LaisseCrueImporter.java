package fr.sirs.importer.objet.laisseCrue;


import fr.sirs.importer.objet.TypeRefHeauImporter;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.LaisseCrue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.objet.SourceInfoImporter;
import fr.sirs.importer.troncon.TronconGestionDigueImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class LaisseCrueImporter extends GenericLaisseCrueImporter {
    
    private final SysEvtLaisseCrueImporter sysEvtLaisseCrueImporter;

    public LaisseCrueImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final IntervenantImporter intervenantImporter,
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter,
            final SourceInfoImporter typeSourceImporter,
            final TypeRefHeauImporter typeRefHeauImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, 
                intervenantImporter, evenementHydrauliqueImporter, 
                typeSourceImporter, typeRefHeauImporter);
        sysEvtLaisseCrueImporter = new SysEvtLaisseCrueImporter(
                accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter,
                intervenantImporter, evenementHydrauliqueImporter,
                typeSourceImporter, typeRefHeauImporter);
    }

    @Override
    public LaisseCrue importRow(Row row) throws IOException, AccessDbImporterException {
        return sysEvtLaisseCrueImporter.importRow(row);
    }

    private enum Columns {
        ID_LAISSE_CRUE,
//        ID_EVENEMENT_HYDRAU,
        ID_TRONCON_GESTION,
//        PR_DEBUT_CALCULE,
//        PR_FIN_CALCULE,
//        X_DEBUT,
//        Y_DEBUT,
//        X_FIN,
//        Y_FIN,
//        ID_SYSTEME_REP,
//        ID_BORNEREF_DEBUT,
//        AMONT_AVAL_DEBUT,
//        DIST_BORNEREF_DEBUT,
//        ID_BORNEREF_FIN,
//        AMONT_AVAL_FIN,
//        DIST_BORNEREF_FIN,
//        COMMENTAIRE,
//        DATE,
//        ID_TYPE_REF_HEAU,
//        HAUTEUR_EAU,
//        ID_INTERV_OBSERVATEUR,
//        ID_SOURCE,
//        POSITION,
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
        return LAISSE_CRUE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        objets = new HashMap<>();
        objetsByTronconId = new HashMap<>();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final LaisseCrue objet = importRow(row);
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                objet.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            
                if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                    objet.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
                }
            
                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                objets.put(row.getInt(Columns.ID_LAISSE_CRUE.toString()), objet);

                // Set the list ByTronconId
                List<LaisseCrue> listByTronconId = objetsByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
                if (listByTronconId == null) {
                    listByTronconId = new ArrayList<>();
                    objetsByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
                }
                listByTronconId.add(objet);
            }
        }
        couchDbConnector.executeBulk(objets.values());
    }
}
