package fr.sirs.importer.objet.ligneEau;

import fr.sirs.core.SirsCore;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.LigneEau;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.objet.TypeRefHeauImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class LigneEauImporter extends GenericLigneEauImporter {
    
    private final LigneEauMesuresPrzImporter ligneEauMesuresPrzImporter;
    private final LigneEauMesuresXyzImporter ligneEauMesuresXyzImporter;
    private final SysEvtLigneEauImporter sysEvtLigneEauImporter;

    public LigneEauImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter,
            final TypeRefHeauImporter typeRefHeauImporter) {
        super(accessDatabase, couchDbConnector, 
                systemeReperageImporter, borneDigueImporter, 
                evenementHydrauliqueImporter, typeRefHeauImporter);
        ligneEauMesuresPrzImporter = new LigneEauMesuresPrzImporter(
                accessDatabase, couchDbConnector);
        ligneEauMesuresXyzImporter = new LigneEauMesuresXyzImporter(
                accessDatabase, couchDbConnector);
        sysEvtLigneEauImporter = new SysEvtLigneEauImporter(
                accessDatabase, couchDbConnector, 
                systemeReperageImporter, borneDigueImporter, 
                evenementHydrauliqueImporter, ligneEauMesuresPrzImporter, 
                ligneEauMesuresXyzImporter, typeRefHeauImporter);
    }

    @Override
    public LigneEau importRow(Row row) throws IOException, AccessDbImporterException {
        return sysEvtLigneEauImporter.importRow(row);
    }

    private enum Columns {
        ID_LIGNE_EAU,
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
//        ID_TYPE_REF_HEAU,
//        ID_SYSTEME_REP_PRZ,
//        DATE,
//        COMMENTAIRE,
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
        return DbImporter.TableName.LIGNE_EAU.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        structures = new HashMap<>();
        structuresByTronconId = new HashMap<>();
        
        // Commenté pour ignorer la table d'événements.
//        this.structures = sysEvtLigneEauImporter.getById();
//        this.structuresByTronconId = sysEvtLigneEauImporter.getByTronconId();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final LigneEau objet;
            final boolean nouvelObjet;
            
            if(structures.get(row.getInt(Columns.ID_LIGNE_EAU.toString()))!=null){
                objet = structures.get(row.getInt(Columns.ID_LIGNE_EAU.toString()));
                nouvelObjet=false;
            }
            else{
                SirsCore.LOGGER.log(Level.FINE, "Nouvel objet !!");
                objet = importRow(row);
                nouvelObjet=true;
            }
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                objet.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            
            if (nouvelObjet) {
            
                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                structures.put(row.getInt(Columns.ID_LIGNE_EAU.toString()), objet);

                // Set the list ByTronconId
                List<LigneEau> listByTronconId = structuresByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
                if (listByTronconId == null) {
                    listByTronconId = new ArrayList<>();
                    structuresByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
                }
                listByTronconId.add(objet);
            }
        }
    }
}
