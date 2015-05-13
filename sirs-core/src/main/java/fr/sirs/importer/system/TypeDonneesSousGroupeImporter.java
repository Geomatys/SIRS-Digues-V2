package fr.sirs.importer.system;

import fr.sirs.core.SirsCore;
import java.util.logging.Level;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.GenericImporter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class TypeDonneesSousGroupeImporter extends GenericImporter {
    
    private Map<Entry<Integer, Integer>, DbImporter.TableName> types = null;

    public TypeDonneesSousGroupeImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    private enum Columns {
        ID_GROUPE_DONNEES,
        ID_SOUS_GROUPE_DONNEES,
        ID_TYPE_DONNEE,
//        LIBELLE_SOUS_GROUPE_DONNEES,
        NOM_TABLE_EVT,
//        ID_NOM_TABLE_EVTTYPE_OBJET_CARTO,
//        DECALAGE,
//        DATE_DERNIERE_MAJ
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
        return TYPE_DONNEES_SOUS_GROUPE.toString();
    }

    @Override
    protected void compute() throws IOException {
        types = new HashMap<>();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final Entry<Integer, Integer> entry = new AbstractMap.SimpleEntry<Integer, Integer>(
                    row.getInt(Columns.ID_GROUPE_DONNEES.toString()), 
                    row.getInt(Columns.ID_SOUS_GROUPE_DONNEES.toString()));
            try{
                types.put(entry, valueOf(row.getString(String.valueOf(Columns.NOM_TABLE_EVT.toString()))));
            } catch(IllegalArgumentException e){
                SirsCore.LOGGER.log(Level.FINE, e.getMessage());
            }
        }
    }
    
    public Map<Entry<Integer, Integer>, DbImporter.TableName> getTypes() throws IOException{
        if(types==null) compute();
        return types;
    }
}
