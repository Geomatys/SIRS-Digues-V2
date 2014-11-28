package fr.sirs.importer.theme.document.related.profilTravers;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.ProfilTraversRepository;
import fr.sirs.core.model.LeveeProfilTravers;
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
        profils = new HashMap<>();
        
        final Map<Integer, List<LeveeProfilTravers>> levesImport = 
                profilTraversDescriptionImporter.getLeveeProfilTraversByProfilId();
    
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while(it.hasNext()){
            final Row row = it.next();
            final ProfilTravers profil = new ProfilTravers();
            
            profil.setLibelle(row.getString(Columns.NOM.toString()));
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                profil.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            final List<LeveeProfilTravers> leve = levesImport.get(row.getInt(Columns.ID_PROFIL_EN_TRAVERS.toString()));
            if(leve!=null) profil.setLeveeIds(leve);
            
            profils.put(row.getInt(Columns.ID_PROFIL_EN_TRAVERS.toString()), profil);
        }
        couchDbConnector.executeBulk(profils.values());
    }
    
}
