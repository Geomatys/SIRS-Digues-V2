package fr.sirs.importer.troncon;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.ContactTroncon;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.IntervenantImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
class GardienTronconGestionImporter extends GenericImporter {

    private Map<Integer, List<ContactTroncon>> gardiensByTronconId = null;
    private final IntervenantImporter intervenantImporter;

    GardienTronconGestionImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final IntervenantImporter intervenantImporter) {
        super(accessDatabase, couchDbConnector);
        this.intervenantImporter = intervenantImporter;
    }

    private enum Columns {
        ID_GARDIEN_TRONCON_GESTION, // Pas dans le nouveau modèle
        ID_INTERVENANT,
        ID_TRONCON_GESTION,
        DATE_DEBUT,
        DATE_FIN,
//        PR_DEBUT_CALCULE, // Pas dans le nouveau modèle
//        PR_FIN_CALCULE,  // Pas dans le nouveau modèle
//        X_DEBUT, // Pas dans le nouveau modèle
//        X_FIN, // Pas dans le nouveau modèle
//        Y_DEBUT, // Pas dans le nouveau modèle
//        Y_FIN, // Pas dans le nouveau modèle
//        ID_BORNEREF_DEBUT, // Pas dans le nouveau modèle
//        ID_BORNEREF_FIN, // Pas dans le nouveau modèle
//        ID_SYSTEME_REP, // Pas dans le nouveau modèle
//        DIST_BORNEREF_DEBUT, // Pas dans le nouveau modèle
//        DIST_BORNEREF_FIN, // Pas dans le nouveau modèle
//        AMONT_AVAL_DEBUT, // Pas dans le nouveau modèle
//        AMONT_AVAL_FIN, // Pas dans le nouveau modèle
        DATE_DERNIERE_MAJ
    };

    /**
     *
     * @return A map containing all ContactTroncon instances accessibles from
     * the internal database <em>TronconGestion</em> identifier.
     * @throws IOException
     * @throws fr.sirs.importer.AccessDbImporterException
     */
    public Map<Integer, List<ContactTroncon>> getGardiensByTronconId() throws IOException, AccessDbImporterException {
        if (gardiensByTronconId == null) compute();
        return gardiensByTronconId;
    }

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
        return DbImporter.TableName.GARDIEN_TRONCON_GESTION.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        gardiensByTronconId = new HashMap<>();

        final Map<Integer, Contact> intervenants = intervenantImporter.getIntervenants();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final ContactTroncon gardien = new ContactTroncon();
            
            gardien.setTypeContact("Gardien");
            
            if (row.getDate(Columns.DATE_DEBUT.toString()) != null) {
                gardien.setDate_debut(LocalDateTime.parse(row.getDate(Columns.DATE_DEBUT.toString()).toString(), dateTimeFormatter));
            }
            if (row.getDate(Columns.DATE_FIN.toString()) != null) {
                gardien.setDate_fin(LocalDateTime.parse(row.getDate(Columns.DATE_FIN.toString()).toString(), dateTimeFormatter));
            }
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                gardien.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }

            // Set the references.
            final Contact intervenant = intervenants.get(row.getInt(Columns.ID_INTERVENANT.toString()));
            if (intervenant.getId() != null) {
                gardien.setContactId(intervenant.getId());
            } else {
                throw new AccessDbImporterException("Le contact " + intervenant + " n'a pas encore d'identifiant CouchDb !");
            }
            
            gardien.setPseudoId(String.valueOf(row.getInt(Columns.ID_GARDIEN_TRONCON_GESTION.toString())));
            gardien.setValid(true);
            
            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            List<ContactTroncon> listeGestions = gardiensByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if(listeGestions == null){
                listeGestions = new ArrayList<>();
            }
            listeGestions.add(gardien);
            gardiensByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listeGestions);
        }
    }
}
