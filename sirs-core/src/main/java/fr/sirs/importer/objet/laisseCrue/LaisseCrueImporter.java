package fr.sirs.importer.objet.laisseCrue;

import fr.sirs.core.SirsCore;

import fr.sirs.importer.objet.TypeRefHeauImporter;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.LaisseCrue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.objet.SourceInfoImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class LaisseCrueImporter extends GenericLaisseCrueImporter {
    
    private final SysEvtLaisseCrueImporter sysEvtLaisseCrueImporter;

    public LaisseCrueImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final IntervenantImporter intervenantImporter,
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter,
            final SourceInfoImporter typeSourceImporter,
            final TypeRefHeauImporter typeRefHeauImporter) {
        super(accessDatabase, couchDbConnector, 
                systemeReperageImporter, borneDigueImporter, 
                intervenantImporter, evenementHydrauliqueImporter, 
                typeSourceImporter, typeRefHeauImporter);
        sysEvtLaisseCrueImporter = new SysEvtLaisseCrueImporter(
                accessDatabase, couchDbConnector, 
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
        return DbImporter.TableName.LAISSE_CRUE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        this.structures = sysEvtLaisseCrueImporter.getById();
        this.structuresByTronconId = sysEvtLaisseCrueImporter.getByTronconId();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final LaisseCrue objet;
            final boolean nouvelObjet;
            
            if(structures.get(row.getInt(Columns.ID_LAISSE_CRUE.toString()))!=null){
                objet = structures.get(row.getInt(Columns.ID_LAISSE_CRUE.toString()));
                nouvelObjet=false;
            }
            else{
                SirsCore.LOGGER.log(Level.FINE, "Nouvel objet !!");
                objet = importRow(row);
                nouvelObjet=true;
            }
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                objet.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            if (nouvelObjet) {
            
                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                structures.put(row.getInt(Columns.ID_LAISSE_CRUE.toString()), objet);

                // Set the list ByTronconId
                List<LaisseCrue> listByTronconId = structuresByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
                if (listByTronconId == null) {
                    listByTronconId = new ArrayList<>();
                    structuresByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
                }
                listByTronconId.add(objet);
            }
        }
    }
}
