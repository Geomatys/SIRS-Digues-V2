package fr.sirs.importer.documentTroncon.document.profilTravers;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.LeveProfilTravers;
import fr.sirs.core.model.ParametreHydrauliqueProfilTravers;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.documentTroncon.document.GenericDocumentRelatedImporter;
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
public class ProfilEnTraversImporter extends GenericDocumentRelatedImporter<ProfilTravers> {
    
    private ProfilEnTraversDescriptionImporter profilTraversDescriptionImporter;
    private ProfilEnTraversTronconImporter profilTraversTronconImporter;
    
    private ProfilEnTraversImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    public ProfilEnTraversImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final ProfilEnTraversDescriptionImporter profilTraversDescriptionImporter){
        this(accessDatabase, couchDbConnector);
        this.profilTraversDescriptionImporter = profilTraversDescriptionImporter;
        profilTraversTronconImporter = new ProfilEnTraversTronconImporter(
                accessDatabase, couchDbConnector, profilTraversDescriptionImporter);
    }
    
    public ProfilEnTraversTronconImporter getProfilEnTraversTronconImporter(){
        return profilTraversTronconImporter;
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
        return PROFIL_EN_TRAVERS.toString();
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
            final ProfilTravers profil = createAnonymValidElement(ProfilTravers.class);
            
            profil.setLibelle(row.getString(Columns.NOM.toString()));
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                profil.setDateMaj(DbImporter.parseLocalDateTime(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            profil.setDesignation(String.valueOf(row.getInt(Columns.ID_PROFIL_EN_TRAVERS.toString())));
            
            final List<LeveProfilTravers> leves = levesImport.get(row.getInt(Columns.ID_PROFIL_EN_TRAVERS.toString()));
            if(leves!=null) {
                for(final LeveProfilTravers leve : leves){
                    profil.getLeveIds().add(leve.getId());
                }
            }
            
            final List<ParametreHydrauliqueProfilTravers> param = params.get(row.getInt(Columns.ID_PROFIL_EN_TRAVERS.toString()));
            if(param!=null) profil.setParametresHydrauliques(param);
            
            related.put(row.getInt(Columns.ID_PROFIL_EN_TRAVERS.toString()), profil);
        }
        couchDbConnector.executeBulk(related.values());
    }
}
