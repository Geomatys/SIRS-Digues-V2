package fr.sirs.importer.objet.desordre;


import fr.sirs.importer.objet.TypePositionImporter;
import fr.sirs.importer.TypeCoteImporter;
import fr.sirs.importer.objet.SourceInfoImporter;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.core.model.Desordre;
import fr.sirs.importer.IntervenantImporter;
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
public class DesordreImporter extends GenericDesordreImporter {
    
    private final TypeDesordreImporter typeDesordreImporter;
    private final SysEvtDesordreImporter sysEvtDesordreImporter;
    private final DesordreObservationImporter desordreObservationImporter;

    public DesordreImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,  
            final TronconGestionDigueImporter tronconGestionDigueImporter,
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter,
            final IntervenantImporter intervenantImporter,
            final SourceInfoImporter typeSourceImporter,
            final TypePositionImporter typePositionImporter,
            final TypeCoteImporter typeCoteImporter) {
        super(accessDatabase, couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, typeSourceImporter, 
                typeCoteImporter, typePositionImporter);
        this.typeDesordreImporter = new TypeDesordreImporter(accessDatabase, 
                couchDbConnector);
        this.desordreObservationImporter = new DesordreObservationImporter(
                accessDatabase, couchDbConnector, intervenantImporter);
        this.sysEvtDesordreImporter = new SysEvtDesordreImporter(accessDatabase, 
                couchDbConnector, tronconGestionDigueImporter,
                systemeReperageImporter, borneDigueImporter, 
                desordreObservationImporter, typeSourceImporter, 
                typePositionImporter, typeCoteImporter, typeDesordreImporter);
    }

    private enum Columns {
        ID_DESORDRE,
//        ID_TYPE_DESORDRE,
//        ID_TYPE_COTE,
//        ID_SOURCE,
        ID_TRONCON_GESTION,
//        DATE_DEBUT_VAL,
//        DATE_FIN_VAL,
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
//        COMMENTAIRE, // Apparemment bsolète voir le champ DESCRIPTION_DESORDRE
//        LIEU_DIT_DESORDRE,
//        ID_TYPE_POSITION,
//        ID_PRESTATION, // La colonne est vide dans la base de l'Isère. Il s'agit visiblement d'une colonne obsolète remplacée par la table d'association DESORDRE_PRESTATION
//        ID_CRUE, // La colonne est vide dans la base de l'Isère. Il s'agit visiblement d'une colonne obsolète remplacée par la table d'association DESORDRE_EVENEMENT_HYDRAULIQUE
//        DESCRIPTION_DESORDRE,
//        DISPARU,
//        DEJA_OBSERVE,
        DATE_DERNIERE_MAJ
    };

    @Override
    public String getTableName() {
        return DESORDRE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        objets = new HashMap<>();
        objetsByTronconId = new HashMap<>();
        
        // Commenté pour ignorer la table d'événements.
//        this.structures = sysEvtDesordreImporter.getById();
//        this.structuresByTronconId = sysEvtDesordreImporter.getByTronconId();
        
        // Parcours de la table pour compléter l'importation.
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final Desordre objet;
//            final boolean nouvelObjet;
            
//            if(structures.get(row.getInt(Columns.ID_DESORDRE.toString()))!=null){
//                objet = structures.get(row.getInt(Columns.ID_DESORDRE.toString()));
//                nouvelObjet=false;
//            }
//            else{
//                SirsCore.LOGGER.log(Level.FINE, "Nouvel objet !!");
                objet = importRow(row);
//                nouvelObjet=true;
//            }
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                objet.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            
//            if (nouvelObjet) {
            
                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                objets.put(row.getInt(Columns.ID_DESORDRE.toString()), objet);

                // Set the list ByTronconId
                List<Desordre> listByTronconId = objetsByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
                if (listByTronconId == null) {
                    listByTronconId = new ArrayList<>();
                    objetsByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
                }
                listByTronconId.add(objet);
//            }
        }
        
        couchDbConnector.executeBulk(objets.values());
    }

    @Override
    public Desordre importRow(Row row) throws IOException, AccessDbImporterException {
        return sysEvtDesordreImporter.importRow(row);
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
