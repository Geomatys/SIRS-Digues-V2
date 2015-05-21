package fr.sirs.importer.objet.reseau;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.RefImplantation;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericTypeReferenceImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class ImplantationImporter extends GenericTypeReferenceImporter<RefImplantation> {
    
    ImplantationImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }

    private enum Columns {
        ID_IMPLANTATION,
        LIBELLE_IMPLANTATION,
        ABREGE_TYPE_IMPLANTATION,
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
        return IMPLANTATION.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefImplantation typeImplantation = createAnonymValidElement(RefImplantation.class);
            
            typeImplantation.setId(typeImplantation.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_IMPLANTATION.toString())));
            typeImplantation.setLibelle(row.getString(Columns.LIBELLE_IMPLANTATION.toString()));
            typeImplantation.setAbrege(row.getString(Columns.ABREGE_TYPE_IMPLANTATION.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeImplantation.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            typeImplantation.setDesignation(String.valueOf(row.getInt(String.valueOf(Columns.ID_IMPLANTATION.toString()))));
            
            types.put(row.getInt(String.valueOf(Columns.ID_IMPLANTATION.toString())), typeImplantation);
        }
        couchDbConnector.executeBulk(types.values());
    }
}
