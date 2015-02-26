package fr.sirs.importer.objet.prestation;

import fr.sirs.core.SirsCore;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Prestation;
import fr.sirs.core.model.RefSource;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.BorneDigueImporter;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.SystemeReperageImporter;
import fr.sirs.importer.objet.SourceInfoImporter;
import fr.sirs.importer.objet.TypeCoteImporter;
import fr.sirs.importer.objet.TypePositionImporter;
import fr.sirs.importer.documentTroncon.document.marche.MarcheImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
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
            final SystemeReperageImporter systemeReperageImporter, 
            final BorneDigueImporter borneDigueImporter, 
            final MarcheImporter marcheImporter,
            final SourceInfoImporter typeSourceImporter,
            final TypeCoteImporter typeCoteImporter,
            final TypePositionImporter typePositionImporter) {
        super(accessDatabase, couchDbConnector, 
                systemeReperageImporter, borneDigueImporter, marcheImporter, 
                typeSourceImporter, typePositionImporter, typeCoteImporter);
        this.typePrestationImporter = new TypePrestationImporter(accessDatabase, 
                couchDbConnector);
        this.sysEvtPrestationImporter = new SysEvtPrestationImporter(
                accessDatabase, couchDbConnector, systemeReperageImporter, 
                borneDigueImporter, marcheImporter, typePositionImporter, 
                typeCoteImporter, typePrestationImporter);
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
        return DbImporter.TableName.PRESTATION.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        this.structures = sysEvtPrestationImporter.getById();
        this.structuresByTronconId = sysEvtPrestationImporter.getByTronconId();
        
        
        final Map<Integer, RefSource> typesSource = sourceInfoImporter.getTypeReferences();
        
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final Prestation objet;
            final boolean nouvelObjet;
            
            if(structures.get(row.getInt(Columns.ID_PRESTATION.toString()))!=null){
                objet = structures.get(row.getInt(Columns.ID_PRESTATION.toString()));
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
            
            
            
            {
                final boolean realisation = row.getBoolean(Columns.REALISATION_INTERNE.toString());
                    if(nouvelObjet){
                        objet.setRealisation_interne(realisation);
                    }
            }
            
            if (row.getDouble(Columns.COUT_AU_METRE.toString()) != null) {
                objet.setCout_metre(row.getDouble(Columns.COUT_AU_METRE.toString()).floatValue());
            }
            
            if (row.getDouble(Columns.COUT_GLOBAL.toString()) != null) {
                objet.setCout_global(row.getDouble(Columns.COUT_GLOBAL.toString()).floatValue());
            }
            
            if(row.getInt(Columns.ID_SOURCE.toString())!=null){
                final RefSource typeSource = typesSource.get(row.getInt(Columns.ID_SOURCE.toString()));
                if(typeSource!=null){
                    if(objet.getSourceId()==null){
                        objet.setSourceId(typeSource.getId());
                    }
                }
            }
            
            if (nouvelObjet) {
            
                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                structures.put(row.getInt(Columns.ID_PRESTATION.toString()), objet);

                // Set the list ByTronconId
                List<Prestation> listByTronconId = structuresByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
                if (listByTronconId == null) {
                    listByTronconId = new ArrayList<>();
                    structuresByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listByTronconId);
                }
                listByTronconId.add(objet);
            }
        }
    }
    
    

    @Override
    public Prestation importRow(Row row) throws IOException, AccessDbImporterException {
        return sysEvtPrestationImporter.importRow(row);
    }
}
