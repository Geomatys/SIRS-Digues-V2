package fr.sirs.importer.objet.desordre;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.RefTypeDesordreRepository;
import fr.sirs.core.model.RefTypeDesordre;
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
class TypeDesordreImporter extends GenericImporter {

    private Map<Integer, RefTypeDesordre> typesDesordre = null;
    private final RefTypeDesordreRepository refTypeDesordreRepository;

    TypeDesordreImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final RefTypeDesordreRepository refTypeDesordreRepository) {
        super(accessDatabase, couchDbConnector);
        this.refTypeDesordreRepository = refTypeDesordreRepository;
    }
    
    private enum TypeDesordreColumns {
        ID_TYPE_DESORDRE,
        LIBELLE_TYPE_DESORDRE,
        ABREGE_TYPE_DESORDRE,
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the RefTypeDesordre referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, RefTypeDesordre> getTypeDesordre() throws IOException {
        if(typesDesordre == null) compute();
        return typesDesordre;
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeDesordreColumns c : TypeDesordreColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TYPE_DESORDRE.toString();
    }

    @Override
    protected void compute() throws IOException {
        typesDesordre = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefTypeDesordre typeDesordre = new RefTypeDesordre();
            
            typeDesordre.setLibelle(row.getString(TypeDesordreColumns.LIBELLE_TYPE_DESORDRE.toString()));
            typeDesordre.setAbrege(row.getString(TypeDesordreColumns.ABREGE_TYPE_DESORDRE.toString()));
            if (row.getDate(TypeDesordreColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeDesordre.setDateMaj(LocalDateTime.parse(row.getDate(TypeDesordreColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            typesDesordre.put(row.getInt(String.valueOf(TypeDesordreColumns.ID_TYPE_DESORDRE.toString())), typeDesordre);
        }
        couchDbConnector.executeBulk(typesDesordre.values());
    }
}
