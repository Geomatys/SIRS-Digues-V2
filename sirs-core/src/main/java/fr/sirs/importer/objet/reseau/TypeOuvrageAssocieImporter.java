package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefOuvrageHydrauliqueAssocie;
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
class TypeOuvrageAssocieImporter extends GenericImporter {

    private Map<Integer, RefOuvrageHydrauliqueAssocie> typesOuvrageAssocie = null;
    
    TypeOuvrageAssocieImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum TypeOuvrageAssocieColumns {
        ID_TYPE_OUVR_HYDRAU_ASSOCIE,
        LIBELLE_TYPE_OUVR_HYDRAU_ASSOCIE,
        ABREGE_TYPE_OUVR_HYDRAU_ASSOCIE,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database RefOuvrageHydrauliqueAssocie referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefOuvrageHydrauliqueAssocie> getTypeOuvrageAssocie() throws IOException {
        if(typesOuvrageAssocie == null) compute();
        return typesOuvrageAssocie;
    }
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeOuvrageAssocieColumns c : TypeOuvrageAssocieColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TYPE_OUVRAGE_HYDRAU_ASSOCIE.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesOuvrageAssocie = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefOuvrageHydrauliqueAssocie typeOuvrage = new RefOuvrageHydrauliqueAssocie();
            
            typeOuvrage.setLibelle(row.getString(TypeOuvrageAssocieColumns.LIBELLE_TYPE_OUVR_HYDRAU_ASSOCIE.toString()));
            typeOuvrage.setAbrege(row.getString(TypeOuvrageAssocieColumns.ABREGE_TYPE_OUVR_HYDRAU_ASSOCIE.toString()));
            if (row.getDate(TypeOuvrageAssocieColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeOuvrage.setDateMaj(LocalDateTime.parse(row.getDate(TypeOuvrageAssocieColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typesOuvrageAssocie.put(row.getInt(String.valueOf(TypeOuvrageAssocieColumns.ID_TYPE_OUVR_HYDRAU_ASSOCIE.toString())), typeOuvrage);
        }
        couchDbConnector.executeBulk(typesOuvrageAssocie.values());
    }
}
