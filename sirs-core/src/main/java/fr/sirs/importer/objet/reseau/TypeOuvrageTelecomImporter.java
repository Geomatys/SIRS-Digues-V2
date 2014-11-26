package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefOuvrageTelecomEnergie;
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
class TypeOuvrageTelecomImporter extends GenericImporter {

    private Map<Integer, RefOuvrageTelecomEnergie> typesOuvrageTelecom = null;
    
    TypeOuvrageTelecomImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum TypeOuvrageTelecomColumns {
        ID_TYPE_OUVRAGE_TELECOM_NRJ,
        ABREGE_TYPE_OUVRAGE_TELECOM_NRJ,
        LIBELLE_TYPE_OUVRAGE_TELECOM_NRJ,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database RefOuvrageTelecomEnergie referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefOuvrageTelecomEnergie> getTypeOuvrageTelecom() throws IOException {
        if(typesOuvrageTelecom == null) compute();
        return typesOuvrageTelecom;
    }
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeOuvrageTelecomColumns c : TypeOuvrageTelecomColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TYPE_OUVRAGE_TELECOM_NRJ.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesOuvrageTelecom = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefOuvrageTelecomEnergie typeOuvrage = new RefOuvrageTelecomEnergie();
            
            typeOuvrage.setLibelle(row.getString(TypeOuvrageTelecomColumns.LIBELLE_TYPE_OUVRAGE_TELECOM_NRJ.toString()));
            typeOuvrage.setAbrege(row.getString(TypeOuvrageTelecomColumns.ABREGE_TYPE_OUVRAGE_TELECOM_NRJ.toString()));
            if (row.getDate(TypeOuvrageTelecomColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeOuvrage.setDateMaj(LocalDateTime.parse(row.getDate(TypeOuvrageTelecomColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typesOuvrageTelecom.put(row.getInt(String.valueOf(TypeOuvrageTelecomColumns.ID_TYPE_OUVRAGE_TELECOM_NRJ.toString())), typeOuvrage);
        }
        couchDbConnector.executeBulk(typesOuvrageTelecom.values());
    }
}
