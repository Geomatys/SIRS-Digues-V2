package fr.sirs.importer.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.Prestation;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.objet.prestation.PrestationImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class PrestationIntervenantImporter extends GenericEntityLinker {

    private final PrestationImporter prestationImporter;
    private final IntervenantImporter intervenantImporter;
    
    public PrestationIntervenantImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final PrestationImporter prestationImporter,
            final IntervenantImporter intervenantImporter) {
        super(accessDatabase, couchDbConnector);
        this.prestationImporter = prestationImporter;
        this.intervenantImporter = intervenantImporter;
    }

    private enum Columns {
        ID_PRESTATION,
        ID_INTERVENANT,
//        DATE_DERNIERE_MAJ
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
        return DbImporter.TableName.PRESTATION_INTERVENANT.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, Prestation> prestations = prestationImporter.getById();
        final Map<Integer, Contact> intervenants = intervenantImporter.getIntervenants();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            final Prestation prestation = prestations.get(row.getInt(Columns.ID_PRESTATION.toString()));
            final Contact intervenant = intervenants.get(row.getInt(Columns.ID_INTERVENANT.toString()));
            
            if(prestation!=null && intervenant!=null){
                prestation.getIntervenantsIds().add(intervenant.getId());
            }
        }
    }
}
