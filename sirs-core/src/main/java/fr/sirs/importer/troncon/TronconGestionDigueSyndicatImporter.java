package fr.sirs.importer.troncon;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.ContactTroncon;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.RefProprietaire;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.GenericImporter;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.OrganismeImporter;
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
class TronconGestionDigueSyndicatImporter extends GenericImporter {

    private Map<Integer, List<ContactTroncon>> syndicatsByTronconId = null;
    private final SyndicatImporter syndicatImporter;

    TronconGestionDigueSyndicatImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final SyndicatImporter syndicatImporter) {
        super(accessDatabase, couchDbConnector);
        this.syndicatImporter = syndicatImporter;
    }

    private enum Columns {
//        ID_TRONCON_SYNDICAT,
        ID_TRONCON_GESTION,
        ID_SYNDICAT,
        DATE_DEBUT,
        DATE_FIN,
//        ID_TYPE_COTE, // Pas dans le nouveau modèle
//        PR_DEBUT_CALCULE, // Pas dans le nouveau modèle
//        PR_FIN_CALCULE, // Pas dans le nouveau modèle
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
//        COMMENTAIRE, // Pas dans le nouveau modèle
        DATE_DERNIERE_MAJ
    };
    

    /**
     *
     * @return A map containing all ContactTroncon instances accessibles from
     * the internal database <em>TronconGestion</em> identifier.
     * @throws IOException
     * @throws fr.sirs.importer.AccessDbImporterException
     */
    public Map<Integer, List<ContactTroncon>> getSyndicatsByTronconId() throws IOException, AccessDbImporterException {
        if (syndicatsByTronconId == null) compute();
        return syndicatsByTronconId;
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
        return DbImporter.TableName.TRONCON_GESTION_DIGUE_SYNDICAT.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        syndicatsByTronconId = new HashMap<>();

        final Map<Integer, Contact> syndicats = syndicatImporter.getSyndicats();
        
        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final ContactTroncon perideSyndicale = new ContactTroncon();
            
            perideSyndicale.setTypeContact("Syndicat");

            if (row.getDate(Columns.DATE_DEBUT.toString()) != null) {
                perideSyndicale.setDate_debut(LocalDateTime.parse(row.getDate(Columns.DATE_DEBUT.toString()).toString(), dateTimeFormatter));
            }
            if (row.getDate(Columns.DATE_FIN.toString()) != null) {
                perideSyndicale.setDate_fin(LocalDateTime.parse(row.getDate(Columns.DATE_FIN.toString()).toString(), dateTimeFormatter));
            }
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                perideSyndicale.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }

            // Don't set the old ID, but save it into the dedicated map in order to keep the reference.
            List<ContactTroncon> listeGestions = syndicatsByTronconId.get(row.getInt(Columns.ID_TRONCON_GESTION.toString()));
            if(listeGestions == null){
                listeGestions = new ArrayList<>();
            }
            listeGestions.add(perideSyndicale);
            syndicatsByTronconId.put(row.getInt(Columns.ID_TRONCON_GESTION.toString()), listeGestions);

            // Set the references.
            if(row.getInt(Columns.ID_SYNDICAT.toString())!=null){
                final Contact syndicat = syndicats.get(row.getInt(Columns.ID_SYNDICAT.toString()));
                if (syndicat.getId() != null) {
                    perideSyndicale.setContactId(syndicat.getId());
                } else {
                    throw new AccessDbImporterException("Le contact " + syndicat + " n'a pas encore d'identifiant CouchDb !");
                }
            }
        }
    }
}
