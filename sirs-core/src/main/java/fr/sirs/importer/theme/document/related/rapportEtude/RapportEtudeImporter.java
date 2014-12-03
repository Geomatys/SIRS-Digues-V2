package fr.sirs.importer.theme.document.related.rapportEtude;

import fr.sirs.importer.*;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RapportEtude;
import fr.sirs.core.model.RefRapportEtude;
import static fr.sirs.importer.DbImporter.cleanNullString;
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
public class RapportEtudeImporter extends GenericImporter {

    private Map<Integer, RapportEtude> related = null;
    
    private final TypeRapportEtudeImporter typeRapportEtudeImporter;
    
    public RapportEtudeImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
        this.typeRapportEtudeImporter = new TypeRapportEtudeImporter(
                accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_RAPPORT_ETUDE,
//        TITRE_RAPPORT_ETUDE, // Pas dans le nouveau modèle
        ID_TYPE_RAPPORT_ETUDE,
        AUTEUR_RAPPORT,
//        DATE_RAPPORT, // Pas dans le nouveau modèle
        REFERENCE_PAPIER,
        REFERENCE_NUMERIQUE,
//        COMMENTAIRE, // Pas dans le nouveau modèle
//        DATE_DERNIERE_MAJ // Pas dans le nouveau modèle
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
        return DbImporter.TableName.RAPPORT_ETUDE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        related = new HashMap<>();
        
        final Map<Integer, RefRapportEtude> typesRapport = typeRapportEtudeImporter.getTypes();

        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RapportEtude rapport = new RapportEtude();
            
            rapport.setAuteur(cleanNullString(row.getString(Columns.REFERENCE_PAPIER.toString())));
            
            rapport.setTypeRapportEtudeId(typesRapport.get(row.getInt(Columns.ID_TYPE_RAPPORT_ETUDE.toString())).getId());
            
            rapport.setReference_papier(cleanNullString(row.getString(Columns.REFERENCE_PAPIER.toString())));
            
            rapport.setReference_numerique(cleanNullString(row.getString(Columns.REFERENCE_NUMERIQUE.toString())));
            
            related.put(row.getInt(Columns.ID_RAPPORT_ETUDE.toString()), rapport);
        }
        couchDbConnector.executeBulk(related.values());
    }
    
    /**
     *
     * @return A map containing all RapportEtude instances accessibles from the
     * internal database identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, RapportEtude> getRelated() throws IOException, AccessDbImporterException {
        if (related == null)  compute();
        return related;
    }
}
