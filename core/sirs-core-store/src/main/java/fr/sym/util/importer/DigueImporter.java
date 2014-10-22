/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.symadrem.sirs.core.component.DigueRepository;
import fr.symadrem.sirs.core.model.Digue;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class DigueImporter extends GenericImporter {

    private DigueRepository digueRepository;
    private Map<Integer, Digue> digues = null;
    
    private DigueImporter(Database accessDatabase) {
        super(accessDatabase);
    }
    
    public DigueImporter(final Database accessDatabase, final DigueRepository digueRepository) {
        this(accessDatabase);
        this.digueRepository = digueRepository;
    }
    
    @Override
    public List<String> getColumns() {
        final List<String> columns = new ArrayList<>();
        for(DigueColumns c : DigueColumns.values())
            columns.add(c.toString());
        return columns;
    }

    @Override
    public String getTableName() {
        return "DIGUE";
    }
    

    /*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
     DIGUE
    ----------------------------------------------------------------------------
     x ID_DIGUE
     % LIBELLE_DIGUE
     % COMMENTAIRE_DIGUE
     % DATE_DERNIERE_MAJ
     */
    public static enum DigueColumns {

        ID("ID_DIGUE"), LIBELLE("LIBELLE_DIGUE"), COMMENTAIRE("COMMENTAIRE_DIGUE"), MAJ("DATE_DERNIERE_MAJ");
        private final String column;

        private DigueColumns(final String column) {
            this.column = column;
        }

        @Override
        public String toString() {
            return this.column;
        }
    };
    
    /**
     * 
     * @return A map containing all Digue instances accessibles from 
     * the internal database identifier.
     * @throws IOException 
     */
    public Map<Integer, Digue> getDigues() throws IOException {

        if(digues==null){
            digues = new HashMap<>();
            final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();

            while (it.hasNext()) {
                final Row row = it.next();
                final Digue digue = new Digue();
                
                digue.setLibelle(row.getString(DigueColumns.LIBELLE.toString()));
                digue.setCommentaire(row.getString(DigueColumns.COMMENTAIRE.toString()));
                if (row.getDate(DigueColumns.MAJ.toString()) != null) {
                    digue.setDateMaj(LocalDateTime.parse(row.getDate(DigueColumns.MAJ.toString()).toString(), dateTimeFormatter));
                }

                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                //digue.setId(String.valueOf(row.getInt(DigueColumns.ID.toString())));
                digues.put(row.getInt(DigueColumns.ID.toString()), digue);
                
                // Register the digue to retrieve a CouchDb ID.
                digueRepository.add(digue);
            }
        }
        return digues;
    }
}
