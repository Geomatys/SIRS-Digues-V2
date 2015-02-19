package fr.sirs.importer.evenementHydraulique;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefFrequenceEvenementHydraulique;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericTypeReferenceImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeFrequenceEvenementHydrauliqueImporter extends GenericTypeReferenceImporter<RefFrequenceEvenementHydraulique> {

    TypeFrequenceEvenementHydrauliqueImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    private enum Columns {
        ID_TYPE_FREQUENCE_EVENEMENT_HYDRAU,
        LIBELLE_TYPE_FREQUENCE_EVENEMENT_HYDRAU,
        ABREGE_TYPE_FREQUENCE_EVENEMENT_HYDRAU,
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
        return DbImporter.TableName.TYPE_FREQUENCE_EVENEMENT_HYDRAU.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefFrequenceEvenementHydraulique typeFrequence = new RefFrequenceEvenementHydraulique();
            
            typeFrequence.setId(typeFrequence.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_FREQUENCE_EVENEMENT_HYDRAU.toString())));
            typeFrequence.setLibelle(row.getString(Columns.LIBELLE_TYPE_FREQUENCE_EVENEMENT_HYDRAU.toString()));
            typeFrequence.setAbrege(row.getString(Columns.ABREGE_TYPE_FREQUENCE_EVENEMENT_HYDRAU.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeFrequence.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typeFrequence.setPseudoId(String.valueOf(row.getInt(Columns.ID_TYPE_FREQUENCE_EVENEMENT_HYDRAU.toString())));
            typeFrequence.setValid(true);
            
            types.put(row.getInt(String.valueOf(Columns.ID_TYPE_FREQUENCE_EVENEMENT_HYDRAU.toString())), typeFrequence);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
