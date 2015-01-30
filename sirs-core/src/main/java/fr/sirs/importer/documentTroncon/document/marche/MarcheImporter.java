package fr.sirs.importer.documentTroncon.document.marche;

import fr.sirs.importer.*;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Marche;
import fr.sirs.core.model.Organisme;
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
public class MarcheImporter extends GenericDocumentRelatedImporter<Marche> {
    
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
        return DbImporter.TableName.MARCHE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        related = new HashMap<>();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            related.put(row.getInt(Columns.ID_MARCHE.toString()), importRow(row));
        }
        couchDbConnector.executeBulk(related.values());
    }
    
    public Marche importRow(final Row row) throws IOException{
        
        final Map<Integer, Organisme> organismes = organismeImporter.getOrganismes();
        
        final Marche marche = new Marche();
            
        marche.setLibelle(row.getString(Columns.LIBELLE_MARCHE.toString()));

        marche.setMaitre_ouvrage(organismes.get(row.getInt(Columns.ID_MAITRE_OUVRAGE.toString())).getId());

        if (row.getDate(Columns.DATE_DEBUT_MARCHE.toString()) != null) {
            marche.setDate_debut(LocalDateTime.parse(row.getDate(Columns.DATE_DEBUT_MARCHE.toString()).toString(), dateTimeFormatter));
        }

        if (row.getDate(Columns.DATE_FIN_MARCHE.toString()) != null) {
            marche.setDate_fin(LocalDateTime.parse(row.getDate(Columns.DATE_FIN_MARCHE.toString()).toString(), dateTimeFormatter));
        }

        if (row.getDouble(Columns.MONTANT_MARCHE.toString()) != null) {
            marche.setMontant(row.getDouble(Columns.MONTANT_MARCHE.toString()).floatValue());
        }

        if (row.getInt(Columns.N_OPERATION.toString()) != null) {
            marche.setNum_operation(row.getInt(Columns.N_OPERATION.toString()));
        }

        if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
            marche.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
        }
        
        return marche;
    }
}
