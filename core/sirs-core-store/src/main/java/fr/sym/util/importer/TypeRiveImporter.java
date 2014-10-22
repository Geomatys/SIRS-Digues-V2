/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.symadrem.sirs.core.model.TypeRive;
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

    private Map<Integer, TypeRive> typesRive = null;

    public TypeRiveImporter(Database accessDatabase) {
        super(accessDatabase);
    }


    @Override
    public List<String> getColumns() {
        final List<String> columns = new ArrayList<>();
        for (TypeRiveColumns c : TypeRiveColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return "TYPE_RIVE";
    }
    
    /*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    TYPE_RIVE.
    ----------------------------------------------------------------------------
    x ID_TYPE_RIVE
    x LIBELLE_TYPE_RIVE
    x DATE_DERNIERE_MAJ
    ----------------------------------------------------------------------------
    Le type de rive est représenté par une énumération.
     */
    
    public static enum TypeRiveColumns {
        ID("ID_TYPE_RIVE"), LIBELLE("LIBELLE_TYPE_RIVE"), MAJ("DATE_DERNIERE_MAJ");
        private final String column;

        private TypeRiveColumns(final String column) {
            this.column = column;
        }

        @Override
        public String toString() {
            return this.column;
        }
    };

    public Map<Integer, TypeRive> getTypeRive() throws IOException {

        if(typesRive == null){
            typesRive = new HashMap<>();
            final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();

            while (it.hasNext()) {
                final Row row = it.next();
                typesRive.put(row.getInt(String.valueOf(TypeRiveColumns.ID.toString())),
                            TypeRive.toTypeRive(row.getString(TypeRiveColumns.LIBELLE.toString())));
            }
        }
        return typesRive;
    }
    
}
