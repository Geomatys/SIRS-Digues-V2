package fr.sirs.importer.documentTroncon.document.profilTravers;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.DocumentTroncon;
import fr.sirs.core.model.LeveProfilTravers;
import fr.sirs.core.model.ParametreHydrauliqueProfilTravers;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.core.model.SIRSDocument;
import fr.sirs.core.model.TronconDigue;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.documentTroncon.DocumentImporter;
import fr.sirs.importer.documentTroncon.document.GenericDocumentRelatedImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ProfilEnTraversImporter extends GenericDocumentRelatedImporter<ProfilTravers> {
    
    private ProfilEnTraversDescriptionImporter profilTraversDescriptionImporter;
//    private ProfilEnTraversTronconImporter profilTraversTronconImporter;
    
    private ProfilEnTraversImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    public ProfilEnTraversImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final ProfilEnTraversDescriptionImporter profilTraversDescriptionImporter,
            final DocumentImporter documentImporter){
        this(accessDatabase, couchDbConnector);
        this.profilTraversDescriptionImporter = profilTraversDescriptionImporter;
//        this.profilTraversTronconImporter = new ProfilEnTraversTronconImporter(
//                accessDatabase, couchDbConnector, documentImporter);
    }
    
    private enum Columns {
        ID_PROFIL_EN_TRAVERS,
        NOM,
        DATE_DERNIERE_MAJ
    }
    
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
        return DbImporter.TableName.PROFIL_EN_TRAVERS.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        related = new HashMap<>();
        
        final Map<Integer, List<LeveProfilTravers>> levesImport = 
                profilTraversDescriptionImporter.getLeveProfilTraversByProfilId();
        final Map<Integer, List<ParametreHydrauliqueProfilTravers>> params = 
                profilTraversDescriptionImporter.getParametreHydrauliqueProfilTraversByProfilId();
    
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final ProfilTravers profil = new ProfilTravers();
            
            profil.setLibelle(row.getString(Columns.NOM.toString()));
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                profil.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            profil.setDesignation(String.valueOf(row.getInt(Columns.ID_PROFIL_EN_TRAVERS.toString())));
            profil.setValid(true);
            
            final List<LeveProfilTravers> leves = levesImport.get(row.getInt(Columns.ID_PROFIL_EN_TRAVERS.toString()));
            if(leves!=null) profil.setLeves(leves);
            
            final List<ParametreHydrauliqueProfilTravers> param = params.get(row.getInt(Columns.ID_PROFIL_EN_TRAVERS.toString()));
            if(param!=null) profil.setParametresHydrauliques(param);
            
            related.put(row.getInt(Columns.ID_PROFIL_EN_TRAVERS.toString()), profil);
        }
        couchDbConnector.executeBulk(related.values());
    }

//    @Override
//    public void update() throws IOException, AccessDbImporterException {
//        if(related==null) compute();
//        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
//        while(it.hasNext()){
//            final Row row = it.next();
//            final ProfilTravers profil = related.get(row.getInt(Columns.ID_PROFIL_EN_TRAVERS.toString()));
//            final List<DocumentTroncon> docs = getDocumentTroncons(row.getInt(Columns.ID_PROFIL_EN_TRAVERS.toString()));
//            if(docs!=null && !docs.isEmpty()){
//                switch(docs.size()){
//                    case 2: profil.setTronconB(getTronconId(docs.get(1)));
//                    case 1: profil.setTronconA(getTronconId(docs.get(0))); break;
//                    default: Logger.getLogger(ProfilEnTraversImporter.class.getName()).log(Level.SEVERE, "Trop de tonçons pour le profil en travers."); break;
//                }
//            }
//        }
//        couchDbConnector.executeBulk(related.values());
//    }
//    
//    private List<DocumentTroncon> getDocumentTroncons(final Integer profilId) throws IOException, AccessDbImporterException{
//        final List<DocumentTroncon> result = new ArrayList<>();
//        final Map<Integer, List<Integer>> leveIds = profilTraversDescriptionImporter.getLeveProfilTraversIdsByProfilId();
//        if(profilId==null || leveIds.get(profilId)==null) return result;
//        
//        for(final Integer leveId : leveIds.get(profilId)){
//            final DocumentTroncon[] documentTroncons = profilTraversTronconImporter.getDocumentTronconsByLeveId().get(leveId);
//            if(documentTroncons!=null){
//                for(int i=0; i<=1; i++){
//                    if(documentTroncons[i]!=null && !result.contains(documentTroncons[i])) result.add(documentTroncons[i]);
//                }
//            }
//        }
//        return result;
//    }
//    
//    private String getTronconId(final DocumentTroncon documentTroncon){
//        if(documentTroncon!=null
//                && documentTroncon.getParent()!=null
//                && documentTroncon.getParent() instanceof TronconDigue)
//            return documentTroncon.getParent().getId();
//        else return null;
//    }
}
