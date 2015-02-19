package fr.sirs.importer.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.ContactStructure;
import fr.sirs.core.model.Objet;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.objet.reseau.ElementReseauImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ElementReseauGardienImporter extends GenericEntityLinker {

    private final ElementReseauImporter elementReseauImporter;
    private final IntervenantImporter intervenantImporter;
    
    public ElementReseauGardienImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final ElementReseauImporter elementReseauImporter,
            final IntervenantImporter intervenantImporter) {
        super(accessDatabase, couchDbConnector);
        this.elementReseauImporter = elementReseauImporter;
        this.intervenantImporter = intervenantImporter;
    }

    private enum Columns {
        ID_ELEMENT_RESEAU,
        ID_INTERV_GARDIEN,
        DATE_DEBUT_GARDIEN,
        DATE_FIN_GARDIEN,
        DATE_DERNIERE_MAJ
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
        return DbImporter.TableName.ELEMENT_RESEAU_GARDIEN.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, Objet> reseaux = elementReseauImporter.getById();
        final Map<Integer, Contact> intervenants = intervenantImporter.getIntervenants();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            final Objet reseau = reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()));
            final Contact intervenant = intervenants.get(row.getInt(Columns.ID_INTERV_GARDIEN.toString()));
            
            if(reseau!=null && intervenant!=null){
                final ContactStructure contactStructure = new ContactStructure();
                
                contactStructure.setContactId(intervenant.getId());
            
                if (row.getDate(Columns.DATE_DEBUT_GARDIEN.toString()) != null) {
                    contactStructure.setDate_debut(LocalDateTime.parse(row.getDate(Columns.DATE_DEBUT_GARDIEN.toString()).toString(), dateTimeFormatter));
                }

                if (row.getDate(Columns.DATE_FIN_GARDIEN.toString()) != null) {
                    contactStructure.setDate_fin(LocalDateTime.parse(row.getDate(Columns.DATE_FIN_GARDIEN.toString()).toString(), dateTimeFormatter));
                }

                if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                    contactStructure.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
                }
            
                contactStructure.setTypeContact(intervenant.getClass().getSimpleName());
                
                // Jointure, donc pas d'id propre : on choisit arbitrairement l'id du gardien.
                contactStructure.setPseudoId(String.valueOf(row.getInt(Columns.ID_INTERV_GARDIEN.toString())));
                contactStructure.setValid(true);
                
                reseau.getContactStructure().add(contactStructure);
            }
        }
    }
}
