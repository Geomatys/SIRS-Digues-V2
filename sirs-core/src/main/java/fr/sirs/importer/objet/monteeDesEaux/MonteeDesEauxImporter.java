package fr.sirs.importer.objet.monteeDesEaux;

import fr.sirs.core.SirsCore;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.MonteeEaux;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.objet.SourceInfoImporter;
import fr.sirs.importer.objet.TypeRefHeauImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class MonteeDesEauxImporter extends GenericMonteeDesEauxImporter {
    
    private final MonteeDesEauxMesuresImporter monteeDesEauxMesuresImporter;
    private final SysEvtMonteeDesEauHydroImporter sysEvtMonteeDesEauHydroImporter;

    public MonteeDesEauxImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter,
            final IntervenantImporter intervenantImporter,
            final TypeRefHeauImporter typeRefHeauImporter,
            final SourceInfoImporter sourceInfoImporter) {
        super(accessDatabase, couchDbConnector, 
                systemeReperageImporter, borneDigueImporter, 
                evenementHydrauliqueImporter);
        monteeDesEauxMesuresImporter = new MonteeDesEauxMesuresImporter(
                accessDatabase, couchDbConnector, intervenantImporter, 
                sourceInfoImporter, typeRefHeauImporter);
        sysEvtMonteeDesEauHydroImporter = new SysEvtMonteeDesEauHydroImporter(
                accessDatabase, couchDbConnector, 
                systemeReperageImporter, borneDigueImporter, 
                evenementHydrauliqueImporter, monteeDesEauxMesuresImporter);
    }

    private enum Columns {
        ID_MONTEE_DES_EAUX,
//        ID_EVENEMENT_HYDRAU,
        ID_TRONCON_GESTION,
//        PR_CALCULE,
//        X,
//        Y,
//        ID_SYSTEME_REP,
//        ID_BORNEREF,
//        AMONT_AVAL,
//        DIST_BORNEREF,
//        COMMENTAIRE,
////        ID_ECHELLE_LIMNI,// Correspondance ?? Référence quelle table ??
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
        return DbImporter.TableName.MONTEE_DES_EAUX.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        structures = new HashMap<>();
        structuresByTronconId = new HashMap<>();
        
        // Commenté pour ignorer la table d'événements.
//        this.structures = sysEvtMonteeDesEauHydroImporter.getById();
//        this.structuresByTronconId = sysEvtMonteeDesEauHydroImporter.getByTronconId();
        
       final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final MonteeEaux objet;
            final boolean nouvelObjet;
            
            if(structures.get(row.getInt(Columns.ID_MONTEE_DES_EAUX.toString()))!=null){
                objet = structures.get(row.getInt(Columns.ID_MONTEE_DES_EAUX.toString()));
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
                structures.put(row.getInt(Columns.ID_MONTEE_DES_EAUX.toString()), objet);

                // Set the list ByTronconId
                List<MonteeEaux> listByTronconId = structuresByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
                if (listByTronconId == null) {
                    listByTronconId = new ArrayList<>();
                    structuresByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
                }
                listByTronconId.add(objet);
            }
        }
    }

    @Override
    public MonteeEaux importRow(Row row) throws IOException, AccessDbImporterException {
        return sysEvtMonteeDesEauHydroImporter.importRow(row);
    }
}
