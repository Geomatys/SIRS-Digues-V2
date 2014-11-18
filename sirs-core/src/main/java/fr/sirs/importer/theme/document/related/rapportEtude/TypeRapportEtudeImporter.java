package fr.sirs.importer.theme.document.related.rapportEtude;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.RefRapportEtudeRepository;
import fr.sirs.core.model.RefRapportEtude;
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
public class TypeRapportEtudeImporter extends GenericImporter {

    private Map<Integer, RefRapportEtude> typesRapportEtude = null;
    private final RefRapportEtudeRepository refRapportEtudeRepository;

    public TypeRapportEtudeImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final RefRapportEtudeRepository refRapportEtudeRepository) {
        super(accessDatabase, couchDbConnector);
        this.refRapportEtudeRepository = refRapportEtudeRepository;
    }
    
    private enum TypeRapportEtudeColumns {
        ID_TYPE_RAPPORT_ETUDE,
        LIBELLE_TYPE_RAPPORT_ETUDE,
        ABREGE_TYPE_RAPPORT_ETUDE,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database RefRapportEtude referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefRapportEtude> getTypeRapportEtude() throws IOException {
        if(typesRapportEtude == null) compute();
        return typesRapportEtude;
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeRapportEtudeColumns c : TypeRapportEtudeColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TYPE_RAPPORT_ETUDE.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesRapportEtude = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefRapportEtude typeRapportEtude = new RefRapportEtude();
            
            typeRapportEtude.setLibelle(row.getString(TypeRapportEtudeColumns.LIBELLE_TYPE_RAPPORT_ETUDE.toString()));
            typeRapportEtude.setAbrege(row.getString(TypeRapportEtudeColumns.ABREGE_TYPE_RAPPORT_ETUDE.toString()));
            if (row.getDate(TypeRapportEtudeColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeRapportEtude.setDateMaj(LocalDateTime.parse(row.getDate(TypeRapportEtudeColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typesRapportEtude.put(row.getInt(String.valueOf(TypeRapportEtudeColumns.ID_TYPE_RAPPORT_ETUDE.toString())), typeRapportEtude);
            refRapportEtudeRepository.add(typeRapportEtude);
        }
    }
}
