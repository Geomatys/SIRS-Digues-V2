package fr.sirs.importer.theme.document.related;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.ProfilTraversRepository;
import fr.sirs.core.model.ProfilTravers;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
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
public class ProfilTraversImporter extends GenericImporter {

    private Map<Integer, ProfilTravers> profils = null;
    private ProfilTraversRepository profilTraversRepository;
    private ProfilTraversDescriptionImporter profilTraversDescriptionImporter;
    
    private ProfilTraversImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    public ProfilTraversImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final ProfilTraversRepository profilTraversRepository, 
            final ProfilTraversDescriptionImporter profilTraversDescriptionImporter){
        this(accessDatabase, couchDbConnector);
        this.profilTraversRepository = profilTraversRepository;
        this.profilTraversDescriptionImporter = profilTraversDescriptionImporter;
    }
    
    public Map<Integer, ProfilTravers> getProfilTravers() throws IOException, AccessDbImporterException{
        if(profils==null) compute();
        return profils;
    }
    
    private enum ProfilTraversColumns {
        ID_PROFIL_EN_TRAVERS,
        NOM,
        DATE_DERNIERE_MAJ
    }
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (ProfilTraversColumns c : ProfilTraversColumns.values()) {
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
        profils = new HashMap<>();
    
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final ProfilTravers profil = new ProfilTravers();
            
            profil.setLibelle(row.getString(ProfilTraversColumns.NOM.toString()));
            
            if (row.getDate(ProfilTraversColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                profil.setDateMaj(LocalDateTime.parse(row.getDate(ProfilTraversColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            profils.put(row.getInt(ProfilTraversColumns.ID_PROFIL_EN_TRAVERS.toString()), profil);
        }
        couchDbConnector.executeBulk(profils.values());
    }
    
}
