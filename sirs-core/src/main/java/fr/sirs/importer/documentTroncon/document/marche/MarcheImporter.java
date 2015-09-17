package fr.sirs.importer.documentTroncon.document.marche;

import fr.sirs.importer.v2.contact.OrganismeImporter;
import fr.sirs.importer.*;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.Marche;
import fr.sirs.core.model.Organisme;
import static fr.sirs.importer.DbImporter.TableName.MARCHE;
import fr.sirs.importer.documentTroncon.document.GenericDocumentImporter;
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
public class MarcheImporter extends GenericDocumentImporter<Marche> {
    
    private final OrganismeImporter organismeImporter;
    
    public MarcheImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final OrganismeImporter organismeImporter) {
        super(accessDatabase, couchDbConnector);
        this.organismeImporter = organismeImporter;
    }

    private enum Columns {
        ID_MARCHE,
        LIBELLE_MARCHE,
        ID_MAITRE_OUVRAGE,
        DATE_DEBUT_MARCHE,
        DATE_FIN_MARCHE,
        MONTANT_MARCHE,
        N_OPERATION,
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
        return MARCHE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        related = new HashMap<>();
        
        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            related.put(row.getInt(Columns.ID_MARCHE.toString()), importRow(row));
        }
        context.outputDb.executeBulk(related.values());
    }
    
    public public  importRow(final Row row) throws IOException{
        
        final Map<Integer, Organisme> organismes = organismeImporter.getOrganismes();
        
        final Marche marche = createAnonymValidElement(Marche.class);
            
        marche.setLibelle(row.getString(Columns.LIBELLE_MARCHE.toString()));

        marche.setMaitreOuvrageId(organismes.get(row.getInt(Columns.ID_MAITRE_OUVRAGE.toString())).getId());

        if (row.getDate(Columns.DATE_DEBUT_MARCHE.toString()) != null) {
            marche.setDate_debut(DbImporter.parseLocalDate(row.getDate(Columns.DATE_DEBUT_MARCHE.toString()), dateTimeFormatter));
        }

        if (row.getDate(Columns.DATE_FIN_MARCHE.toString()) != null) {
            marche.setDate_fin(DbImporter.parseLocalDate(row.getDate(Columns.DATE_FIN_MARCHE.toString()), dateTimeFormatter));
        }

        if (row.getDouble(Columns.MONTANT_MARCHE.toString()) != null) {
            marche.setMontant(row.getDouble(Columns.MONTANT_MARCHE.toString()).floatValue());
        }

        if (row.getInt(Columns.N_OPERATION.toString()) != null) {
            marche.setNumOperation(row.getInt(Columns.N_OPERATION.toString()));
        }

        if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
            marche.setDateMaj(DbImporter.parseLocalDate(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
        }
        
        marche.setDesignation(String.valueOf(row.getInt(Columns.ID_MARCHE.toString())));
        
        return marche;
    }
}
