package fr.sirs.importer.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.core.model.Prestation;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.event.EvenementHydrauliqueImporter;
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
public class PrestationEvenementHydrauImporter extends GenericEntityLinker {

    private final PrestationImporter prestationImporter;
    private final EvenementHydrauliqueImporter evenementHydrauliqueImporter;
    
    public PrestationEvenementHydrauImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final PrestationImporter prestationImporter,
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter) {
        super(accessDatabase, couchDbConnector);
        this.prestationImporter = prestationImporter;
        this.evenementHydrauliqueImporter = evenementHydrauliqueImporter;
    }

    private enum Columns {
        ID_PRESTATION,
        ID_EVENEMENT_HYDRAU,
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
        return PRESTATION_EVENEMENT_HYDRAU.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, Prestation> prestations = prestationImporter.getById();
        final Map<Integer, EvenementHydraulique> evenements = evenementHydrauliqueImporter.getEvenements();
        
        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            final Prestation prestation = prestations.get(row.getInt(Columns.ID_PRESTATION.toString()));
            final EvenementHydraulique evenement = evenements.get(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString()));
            
            if(prestation!=null && evenement!=null){
                prestation.getEvenementHydrauliqueIds().add(evenement.getId());
            }
        }
        
        context.outputDb.executeBulk(prestations.values());
    }
}
