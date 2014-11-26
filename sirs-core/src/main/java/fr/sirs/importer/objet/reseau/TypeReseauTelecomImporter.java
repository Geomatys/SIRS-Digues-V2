package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefReseauTelecomEnergie;
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
class TypeReseauTelecomImporter extends GenericImporter {

    private Map<Integer, RefReseauTelecomEnergie> typesReseauTelecom = null;
    
    TypeReseauTelecomImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum TypeReseauTelecomColumns {
        ID_TYPE_RESEAU_COMMUNICATION,
        LIBELLE_TYPE_RESEAU_COMMUNICATION,
        ABREGE_TYPE_RESEAU_COMMUNICATION,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database RefReseauTelecomEnergie referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefReseauTelecomEnergie> getTypeReseauTelecom() throws IOException {
        if(typesReseauTelecom == null) compute();
        return typesReseauTelecom;
    }
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeReseauTelecomColumns c : TypeReseauTelecomColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TYPE_RESEAU_TELECOMMUNIC.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesReseauTelecom = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefReseauTelecomEnergie typeEcoulement = new RefReseauTelecomEnergie();
            
            typeEcoulement.setLibelle(row.getString(TypeReseauTelecomColumns.LIBELLE_TYPE_RESEAU_COMMUNICATION.toString()));
            typeEcoulement.setAbrege(row.getString(TypeReseauTelecomColumns.ABREGE_TYPE_RESEAU_COMMUNICATION.toString()));
            if (row.getDate(TypeReseauTelecomColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeEcoulement.setDateMaj(LocalDateTime.parse(row.getDate(TypeReseauTelecomColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typesReseauTelecom.put(row.getInt(String.valueOf(TypeReseauTelecomColumns.ID_TYPE_RESEAU_COMMUNICATION.toString())), typeEcoulement);
        }
        couchDbConnector.executeBulk(typesReseauTelecom.values());
    }
}
