/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.symadrem.sirs.core.model.SystemeReperage;
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
public class SystemeReperageImporter extends GenericImporter {

    private Map<Integer, SystemeReperage> systemesReperage = null;

    SystemeReperageImporter(Database accessDatabase) {
        super(accessDatabase);
    }

    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (SystemeRepLineaireColumns c : SystemeRepLineaireColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return "SYSTEME_REP_LINEAIRE";
    }
    
    private enum SystemeRepLineaireColumns {
        ID_SYSTEME_REP, 
        ID_TRONCON_GESTION, 
        LIBELLE_SYSTEME_REP,
        COMMENTAIRE_SYSTEME_REP, 
        DATE_DERNIERE_MAJ
    };

    /**
     * 
     * @return A map containing the SystemeRepLineaire instances references by
     * their internal database identifier.
     * @throws IOException 
     */
    public Map<Integer, SystemeReperage> getSystemeRepLineaire() throws IOException {

        if (systemesReperage == null) {
            systemesReperage = new HashMap<>();
            final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();

            while (it.hasNext()) {
                final Row row = it.next();
                final SystemeReperage systemeReperage = new SystemeReperage();
                systemeReperage.setNom(row.getString(SystemeRepLineaireColumns.LIBELLE_SYSTEME_REP.toString()));
                systemeReperage.setCommentaire(row.getString(SystemeRepLineaireColumns.COMMENTAIRE_SYSTEME_REP.toString()));
                if (row.getDate(SystemeRepLineaireColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                    systemeReperage.setDateMaj(LocalDateTime.parse(row.getDate(SystemeRepLineaireColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
                }
                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                systemesReperage.put(row.getInt(SystemeRepLineaireColumns.ID_SYSTEME_REP.toString()), systemeReperage);
            }
        }
        return systemesReperage;
    }
}
