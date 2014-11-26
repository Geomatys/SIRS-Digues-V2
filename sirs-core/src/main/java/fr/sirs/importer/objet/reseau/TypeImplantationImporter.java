package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.RefImplantation;
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
class TypeImplantationImporter extends GenericImporter {

    private Map<Integer, RefImplantation> typesImplantation = null;
    
    
    TypeImplantationImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum TypeImplantationColumns {
        ID_IMPLANTATION,
        LIBELLE_IMPLANTATION,
        ABREGE_TYPE_IMPLANTATION,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database RefImplantation referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefImplantation> getTypeImplantation() throws IOException {
        if(typesImplantation == null) compute();
        return typesImplantation;
    }
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeImplantationColumns c : TypeImplantationColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.IMPLANTATION.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesImplantation = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefImplantation typeImplantation = new RefImplantation();
            
            typeImplantation.setLibelle(row.getString(TypeImplantationColumns.LIBELLE_IMPLANTATION.toString()));
            typeImplantation.setAbrege(row.getString(TypeImplantationColumns.ABREGE_TYPE_IMPLANTATION.toString()));
            if (row.getDate(TypeImplantationColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeImplantation.setDateMaj(LocalDateTime.parse(row.getDate(TypeImplantationColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typesImplantation.put(row.getInt(String.valueOf(TypeImplantationColumns.ID_IMPLANTATION.toString())), typeImplantation);
        }
        couchDbConnector.executeBulk(typesImplantation.values());
    }
    
}
