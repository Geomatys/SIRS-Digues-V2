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
import java.util.HashMap;
import java.util.Iterator;
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
    
    /***************************************************************************
    TYPE_RIVE.
    ----------------------------------------------------------------------------
    x ID_TRONCON_GESTION //
     * ID_ORG_GESTION // L'identifiant de gestionnaire est mappé avec les identifiants des organismes créés dans CouchDb
     * DATE_DEBUT_GESTION
     * DATE_FIN_GESTION
    x DATE_DERNIERE_MAJ // On n'a pas de date de mise à jour : est-ce normal ?
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
            final Iterator<Row> it = accessDatabase.getTable("TYPE_RIVE").iterator();

            while (it.hasNext()) {
                final Row row = it.next();
                typesRive.put(row.getInt(String.valueOf(TypeRiveColumns.ID.toString())),
                            TypeRive.toTypeRive(row.getString(TypeRiveColumns.LIBELLE.toString())));
            }
        }
        return typesRive;
    }
    
}
