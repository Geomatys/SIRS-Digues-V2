package fr.sirs.importer.theme.document.related;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.RefConventionRepository;
import fr.sirs.core.model.RefConvention;
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
public class TypeConventionImporter extends GenericImporter {

    private Map<Integer, RefConvention> typesConvention = null;
    private final RefConventionRepository refConventionRepository;

    public TypeConventionImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final RefConventionRepository refConventionRepository) {
        super(accessDatabase, couchDbConnector);
        this.refConventionRepository = refConventionRepository;
    }
    
    private enum TypeConventionColumns {
        ID_TYPE_CONVENTION,
        LIBELLE_TYPE_CONVENTION,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database TypeConvention referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefConvention> getTypeConvention() throws IOException {
        if(typesConvention == null) compute();
        return typesConvention;
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeConventionColumns c : TypeConventionColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TYPE_CONVENTION.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesConvention = new HashMap<>();
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();

        while (it.hasNext()) {
            final Row row = it.next();
            final RefConvention typeConvention = new RefConvention();
            typeConvention.setLibelle(row.getString(TypeConventionColumns.LIBELLE_TYPE_CONVENTION.toString()));
            
            if (row.getDate(TypeConventionColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeConvention.setDateMaj(LocalDateTime.parse(row.getDate(TypeConventionColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typesConvention.put(row.getInt(String.valueOf(TypeConventionColumns.ID_TYPE_CONVENTION.toString())), typeConvention);
            refConventionRepository.add(typeConvention);
        }
    }
}
