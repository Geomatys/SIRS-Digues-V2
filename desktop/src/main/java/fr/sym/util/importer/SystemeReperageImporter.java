/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.vividsolutions.jts.geom.Geometry;
import fr.symadrem.sirs.core.model.SystemeReperage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Samuel Andrés
 */
public class SystemeReperageImporter extends GenericImporter {

    private Map<Integer, SystemeReperage> systemesReperage = null;

    public SystemeReperageImporter(Database accessDatabase) {
        super(accessDatabase);
    }

    /*==========================================================================
     SYSTEME_REP_LINEAIRE
     ----------------------------------------------------------------------------
     x ID_SYSTEME_REP
     x ID_TRONCON_GESTION
     * LIBELLE_SYSTEME_REP
     * COMMENTAIRE_SYSTEME_REP
     DATE_DERNIERE_MAJ
     ----------------------------------------------------------------------------
     Remarque : pas de date de mise à jour dans le modèle mais des dates de début
     et de fin qui ne se retrouvent pas dans la base.
     */
    public static enum SystemeRepLineaireColumns {

        ID("ID_SYSTEME_REP"), ID_TRONCON("ID_TRONCON_GESTION"), LIBELLE("LIBELLE_SYSTEME_REP"),
        COMMENTAIRE("COMMENTAIRE_SYSTEME_REP"), MAJ("DATE_DERNIERE_MAJ");
        private final String column;

        private SystemeRepLineaireColumns(final String column) {
            this.column = column;
        }

        @Override
        public String toString() {
            return this.column;
        }
    };

    public Map<Integer, SystemeReperage> getSystemeRepLineaire() throws IOException {

        if (systemesReperage == null) {
            systemesReperage = new HashMap<>();
            final Iterator<Row> it = this.accessDatabase.getTable("SYSTEME_REP_LINEAIRE").iterator();

            while (it.hasNext()) {
                final Row row = it.next();
                final SystemeReperage systemeReperage = new SystemeReperage();
                systemeReperage.setNom(row.getString(SystemeRepLineaireColumns.LIBELLE.toString()));
                systemeReperage.setCommentaire(row.getString(SystemeRepLineaireColumns.COMMENTAIRE.toString()));
                if (row.getDate(SystemeRepLineaireColumns.MAJ.toString()) != null) {
                    systemeReperage.setDateMaj(LocalDateTime.parse(row.getDate(SystemeRepLineaireColumns.MAJ.toString()).toString(), dateTimeFormatter));
                }
                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                systemesReperage.put(row.getInt(SystemeRepLineaireColumns.ID.toString()), systemeReperage);
            }
        }
        return systemesReperage;
    }
}
