package fr.sirs.importer.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Marche;
import fr.sirs.core.model.Organisme;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.v2.contact.OrganismeImporter;
import fr.sirs.importer.documentTroncon.document.marche.MarcheImporter;
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
public class MarcheFinanceurImporter extends GenericEntityLinker {

    private final MarcheImporter marcheImporter;
    private final OrganismeImporter organismeImporter;
    
    public MarcheFinanceurImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final MarcheImporter marcheImporter,
            final OrganismeImporter organismeImporter) {
        super(accessDatabase, couchDbConnector);
        this.marcheImporter = marcheImporter;
        this.organismeImporter = organismeImporter;
    }

    private enum Columns {
        ID_ORGANISME,
        ID_MARCHE,
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
        return MARCHE_FINANCEUR.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, Marche> marches = marcheImporter.getRelated();
        final Map<Integer, Organisme> organismes = organismeImporter.getOrganismes();
        
        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            final Marche marche = marches.get(row.getInt(Columns.ID_MARCHE.toString()));
            final Organisme financeur = organismes.get(row.getInt(Columns.ID_ORGANISME.toString()));
            
            if(marche!=null && financeur!=null){
                marche.getFinanceurIds().add(financeur.getId());
            }
        }
        
        context.outputDb.executeBulk(marches.values());
    }
}
