package fr.sirs.importer.evenementHydraulique;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.RefFrequenceEvenementHydrauliqueRepository;
import fr.sirs.core.model.RefFrequenceEvenementHydraulique;
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
public class TypeFrequenceEvenementHydrauliqueImporter extends GenericImporter {

    private Map<Integer, RefFrequenceEvenementHydraulique> typesFrequence = null;

    public TypeFrequenceEvenementHydrauliqueImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final RefFrequenceEvenementHydrauliqueRepository refFrequenceEvenementHydrauliqueRepository) {
        super(accessDatabase, couchDbConnector);
    }
    
    private enum TypeFrequenceEvenementHydrauliqueColumns {
        ID_TYPE_FREQUENCE_EVENEMENT_HYDRAU,
        LIBELLE_TYPE_FREQUENCE_EVENEMENT_HYDRAU,
        ABREGE_TYPE_FREQUENCE_EVENEMENT_HYDRAU,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the RefFrequenceEvenementHydraulique referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefFrequenceEvenementHydraulique> getTypeFrequenceEvenementHydraulique() throws IOException {
        if(typesFrequence == null) compute();
        return typesFrequence;
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeFrequenceEvenementHydrauliqueColumns c : TypeFrequenceEvenementHydrauliqueColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TYPE_FREQUENCE_EVENEMENT_HYDRAU.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesFrequence = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefFrequenceEvenementHydraulique typeFrequence = new RefFrequenceEvenementHydraulique();
            
            typeFrequence.setLibelle(row.getString(TypeFrequenceEvenementHydrauliqueColumns.LIBELLE_TYPE_FREQUENCE_EVENEMENT_HYDRAU.toString()));
            typeFrequence.setAbrege(row.getString(TypeFrequenceEvenementHydrauliqueColumns.ABREGE_TYPE_FREQUENCE_EVENEMENT_HYDRAU.toString()));
            if (row.getDate(TypeFrequenceEvenementHydrauliqueColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeFrequence.setDateMaj(LocalDateTime.parse(row.getDate(TypeFrequenceEvenementHydrauliqueColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
            typesFrequence.put(row.getInt(String.valueOf(TypeFrequenceEvenementHydrauliqueColumns.ID_TYPE_FREQUENCE_EVENEMENT_HYDRAU.toString())), typeFrequence);
        }
        couchDbConnector.executeBulk(typesFrequence.values());
    }
}
