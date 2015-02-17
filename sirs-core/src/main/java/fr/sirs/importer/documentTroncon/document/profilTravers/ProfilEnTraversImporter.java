package fr.sirs.importer.documentTroncon.document.profilTravers;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.LeveProfilTravers;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.documentTroncon.document.GenericDocumentRelatedImporter;
import java.io.IOException;
import java.time.LocalDateTime;
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
    
    private ProfilEnTraversImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    public ProfilEnTraversImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final ProfilEnTraversDescriptionImporter profilTraversDescriptionImporter){
        this(accessDatabase, couchDbConnector);
        this.profilTraversDescriptionImporter = profilTraversDescriptionImporter;
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
                profilTraversDescriptionImporter.getLeveeProfilTraversByProfilId();
    
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final ProfilTravers profil = new ProfilTravers();
            
            profil.setLibelle(row.getString(Columns.NOM.toString()));
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                profil.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            profil.setPseudoId(row.getInt(Columns.ID_PROFIL_EN_TRAVERS.toString()));
            
            final List<LeveProfilTravers> leve = levesImport.get(row.getInt(Columns.ID_PROFIL_EN_TRAVERS.toString()));
            if(leve!=null) profil.setLeveeIds(leve);
            
            related.put(row.getInt(Columns.ID_PROFIL_EN_TRAVERS.toString()), profil);
        }
        couchDbConnector.executeBulk(related.values());
    }
    
}
