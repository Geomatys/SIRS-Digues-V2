/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sym.util.importer;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.symadrem.sirs.core.model.GestionTroncon;
import fr.symadrem.sirs.core.model.Organisme;
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
public class TronconGestionDigueGestionnaireImporter extends GenericImporter {

    private Map<Integer, List<GestionTroncon>> gestionsByTronconId = null;
    private OrganismeImporter organismeImporter;

    private TronconGestionDigueGestionnaireImporter(Database accessDatabase) {
        super(accessDatabase);
    }

    public TronconGestionDigueGestionnaireImporter(final Database accessDatabase, final OrganismeImporter organismeImporter) {
        this(accessDatabase);
        this.organismeImporter = organismeImporter;
    }

    /**
     * *************************************************************************
     * TRONCON_GESTION_DIGUE_GESTIONNAIRE.
     * ----------------------------------------------------------------------------
     * x ID_TRONCON_GESTION // ID_ORG_GESTION // L'identifiant de gestionnaire
     * est mappé avec les identifiants des organismes créés dans CouchDb
     * DATE_DEBUT_GESTION DATE_FIN_GESTION x DATE_DERNIERE_MAJ // On n'a pas de
     * date de mise à jour : est-ce normal ?
     * ----------------------------------------------------------------------------
     * La classe Gestionnaires est mal nommée ("Gestion" ou "EpisodeGestion"
     * conviendrait mieux.
     */
    public static enum TronconGestionDigueGestionnaireColumns {

        ID_TRONCON_GESTION("ID_TRONCON_GESTION"),
        ID_ORG_GESTION("ID_ORG_GESTION"),
        DATE_DEBUT_GESTION("DATE_DEBUT_GESTION"), DATE_FIN_GESTION("DATE_FIN_GESTION"),
        MAJ("DATE_DERNIERE_MAJ");
        private final String column;

        private TronconGestionDigueGestionnaireColumns(final String column) {
            this.column = column;
        }

        @Override
        public String toString() {
            return this.column;
        }
    };

    /**
     *
     * @return A map containing all GestionTroncon instances accessibles from
     * the internal database <em>TronconGestion</em> identifier.
     * @throws IOException
     * @throws fr.sym.util.importer.AccessDbImporterException
     */
    public Map<Integer, List<GestionTroncon>> getGestionsByTronconId() throws IOException, AccessDbImporterException {

        if (gestionsByTronconId == null) {
            gestionsByTronconId = new HashMap<>();
            final Iterator<Row> it = this.accessDatabase.getTable("TRONCON_GESTION_DIGUE_GESTIONNAIRE").iterator();

            while (it.hasNext()) {
                final Row row = it.next();
                final GestionTroncon gestion = new GestionTroncon();
                if (row.getDate(TronconGestionDigueGestionnaireColumns.DATE_DEBUT_GESTION.toString()) != null) {
                    gestion.setDate_debut(LocalDateTime.parse(row.getDate(TronconGestionDigueGestionnaireColumns.DATE_DEBUT_GESTION.toString()).toString(), dateTimeFormatter));
                }
                if (row.getDate(TronconGestionDigueGestionnaireColumns.DATE_FIN_GESTION.toString()) != null) {
                    gestion.setDate_fin(LocalDateTime.parse(row.getDate(TronconGestionDigueGestionnaireColumns.DATE_FIN_GESTION.toString()).toString(), dateTimeFormatter));
                }

                // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
                List<GestionTroncon> listeGestions = gestionsByTronconId.get(row.getInt(TronconGestionDigueGestionnaireColumns.ID_TRONCON_GESTION.toString()));
                if(listeGestions == null){
                    listeGestions = new ArrayList<>();
                    gestionsByTronconId.put(row.getInt(TronconGestionDigueGestionnaireColumns.ID_TRONCON_GESTION.toString()), listeGestions);
                }
                listeGestions.add(gestion);

                // Set the references.
                final Organisme organisme = organismeImporter.getOrganismes().get(row.getInt(TronconGestionDigueGestionnaireColumns.ID_ORG_GESTION.toString()));
                if (organisme.getId() != null) {
                    System.out.println(organisme.getId());
                    gestion.setGestionnaireId(organisme.getId());
                } else {
                    throw new AccessDbImporterException("L'organisme " + organisme + " n'a pas encore d'identifiant CouchDb !");
                }
            }
        }
        return gestionsByTronconId;
    }
}
