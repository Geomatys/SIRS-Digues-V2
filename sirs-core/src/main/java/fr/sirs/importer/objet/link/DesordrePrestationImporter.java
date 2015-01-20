package fr.sirs.importer.objet.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Desordre;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.core.model.Prestation;
import fr.sirs.importer.objet.desordre.DesordreImporter;
import fr.sirs.importer.objet.prestation.PrestationImporter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DesordrePrestationImporter extends GenericObjetLinker {
    
    private final PrestationImporter prestationImporter;
    private final DesordreImporter desordreImporter;

    public DesordrePrestationImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final PrestationImporter prestationImporter,
            final DesordreImporter desordreImporter) {
        super(accessDatabase, couchDbConnector);
        this.prestationImporter = prestationImporter;
        this.desordreImporter = desordreImporter;
    }

    private enum Columns {
        ID_DESORDRE,
        ID_PRESTATION,
//        DATE_DERNIERE_MAJ
    };

    @Override
    public String getTableName() {
        return DbImporter.TableName.DESORDRE_PRESTATION.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, Prestation> prestations = prestationImporter.getById();
        final Map<Integer, Desordre> desordres = desordreImporter.getById();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            final Prestation prestation = prestations.get(row.getInt(Columns.ID_PRESTATION.toString()));
            final Desordre desordre = desordres.get(row.getInt(Columns.ID_DESORDRE.toString()));
            
            if(prestation!=null && desordre!=null){
                prestation.getDesordre().add(desordre.getId());
                desordre.getPrestation().add(prestation.getId());
                
                associations.add(new AbstractMap.SimpleEntry<>(prestation, desordre));
            }
        }
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
