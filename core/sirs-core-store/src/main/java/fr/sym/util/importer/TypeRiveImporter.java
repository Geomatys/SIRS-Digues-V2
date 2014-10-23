/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class TypeRiveImporter extends GenericImporter {

    private Map<Integer, String> typesRive = null;

    TypeRiveImporter(Database accessDatabase) {
        super(accessDatabase);
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeRiveColumns c : TypeRiveColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.TYPE_RIVE.toString();
    }
    
    private enum TypeRiveColumns {
        ID_TYPE_RIVE, 
        LIBELLE_TYPE_RIVE, 
        //DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing all the database TypeRive referenced by their
     * internal ID.
     * @throws IOException 
     */
    public Map<Integer, String> getTypeRive() throws IOException {

        if(typesRive == null){
            typesRive = new HashMap<>();
            final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();

            while (it.hasNext()) {
                final Row row = it.next();
                typesRive.put(row.getInt(String.valueOf(TypeRiveColumns.ID_TYPE_RIVE.toString())),
                        row.getString(TypeRiveColumns.LIBELLE_TYPE_RIVE.toString()));
            }
        }
        return typesRive;
    }
}
