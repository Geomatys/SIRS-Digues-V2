package fr.sirs.importer.evenementHydraulique;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.SirsCore;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.RefEvenementHydraulique;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericTypeReferenceImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class TypeEvenementHydrauliqueImporter extends GenericTypeReferenceImporter<RefEvenementHydraulique> {

    TypeEvenementHydrauliqueImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    private enum Columns {
        ID_TYPE_EVENEMENT_HYDRAU,
        ABREGE_TYPE_EVENEMENT_HYDRAU,
        LIBELLE_TYPE_EVENEMENT_HYDRAU,
//        NOM_TABLE_EVT, // Ces tables sont innexistentes dans la base de l'isère
//        ID_TYPE_OBJET_CARTO,
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
        return TYPE_EVENEMENT_HYDRAU.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final RefEvenementHydraulique typeEvenement = createAnonymValidElement(RefEvenementHydraulique.class);
            
            typeEvenement.setId(typeEvenement.getClass().getSimpleName()+":"+row.getInt(String.valueOf(Columns.ID_TYPE_EVENEMENT_HYDRAU.toString())));
            typeEvenement.setLibelle(row.getString(Columns.LIBELLE_TYPE_EVENEMENT_HYDRAU.toString()));
            typeEvenement.setAbrege(row.getString(Columns.ABREGE_TYPE_EVENEMENT_HYDRAU.toString()));
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                typeEvenement.setDateMaj(DbImporter.parseLocalDateTime(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            typeEvenement.setDesignation(String.valueOf(row.getInt(Columns.ID_TYPE_EVENEMENT_HYDRAU.toString())));
            
            try{
                types.put(row.getInt(String.valueOf(Columns.ID_TYPE_EVENEMENT_HYDRAU.toString())), typeEvenement);
            } catch (IllegalArgumentException e) {
                SirsCore.LOGGER.log(Level.FINE, e.getMessage());
            }
        }
        couchDbConnector.executeBulk(types.values());
    }
}
