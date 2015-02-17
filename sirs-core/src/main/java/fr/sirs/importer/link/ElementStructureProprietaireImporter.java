package fr.sirs.importer.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.ContactStructure;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.OrganismeStructure;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.OrganismeImporter;
import fr.sirs.importer.objet.structure.ElementStructureImporter;
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
public class ElementStructureProprietaireImporter extends GenericEntityLinker {

    private final ElementStructureImporter elementStructureImporter;
    private final IntervenantImporter intervenantImporter;
    private final OrganismeImporter organismeImporter;
    
    public ElementStructureProprietaireImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final ElementStructureImporter elementStructureImporter,
            final IntervenantImporter intervenantImporter,
            final OrganismeImporter organismeImporter) {
        super(accessDatabase, couchDbConnector);
        this.elementStructureImporter = elementStructureImporter;
        this.intervenantImporter = intervenantImporter;
        this.organismeImporter = organismeImporter;
    }

    private enum Columns {
        ID_ELEMENT_STRUCTURE,
        DATE_DEBUT_PROPRIO,
        DATE_FIN_PROPRIO,
        ID_ORG_PROPRIO,
        ID_INTERV_PROPRIO,
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
        return DbImporter.TableName.ELEMENT_STRUCTURE_PROPRIETAIRE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, Objet> structures = elementStructureImporter.getById();
        final Map<Integer, Contact> intervenants = intervenantImporter.getIntervenants();
        final Map<Integer, Organisme> organismes = organismeImporter.getOrganismes();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            final Objet structure = structures.get(row.getInt(Columns.ID_ELEMENT_STRUCTURE.toString()));
            final Contact intervenant = intervenants.get(row.getInt(Columns.ID_INTERV_PROPRIO.toString()));
            final Organisme organisme = organismes.get(row.getInt(Columns.ID_ORG_PROPRIO.toString()));
            
            if(structure!=null && (intervenant!=null || organisme!=null)){
                if(intervenant!=null){
                    final ContactStructure contactStructure = readContactStructure(row);
                    contactStructure.setContactId(intervenant.getId());
                    contactStructure.setTypeContact(intervenant.getClass().getSimpleName());
                    structure.getContactStructure().add(contactStructure);
                }
                else{
                    final OrganismeStructure organismeStructure = readOrganismeStructure(row);
                    organismeStructure.setOrganismeId(organisme.getId());
                    organismeStructure.setTypeOrganisme(organisme.getClass().getSimpleName());
                    structure.getOrganismeStructure().add(organismeStructure);
                }
            }
        }
    }
    
    
    private ContactStructure readContactStructure(final Row row){
        
        final ContactStructure contactStructure = new ContactStructure();

        if (row.getDate(Columns.DATE_DEBUT_PROPRIO.toString()) != null) {
            contactStructure.setDate_debut(LocalDateTime.parse(row.getDate(Columns.DATE_DEBUT_PROPRIO.toString()).toString(), dateTimeFormatter));
        }

        if (row.getDate(Columns.DATE_FIN_PROPRIO.toString()) != null) {
            contactStructure.setDate_fin(LocalDateTime.parse(row.getDate(Columns.DATE_FIN_PROPRIO.toString()).toString(), dateTimeFormatter));
        }

        if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
            contactStructure.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
        }
        // Jointure, donc pas d'id propre : on choisit arbitrairement l'id du proprio.
        contactStructure.setPseudoId(row.getInt(Columns.ID_INTERV_PROPRIO.toString()));

        return contactStructure;
    }
    
    
    private OrganismeStructure readOrganismeStructure(final Row row){
        
        final OrganismeStructure organismeStructure = new OrganismeStructure();

        if (row.getDate(Columns.DATE_DEBUT_PROPRIO.toString()) != null) {
            organismeStructure.setDate_debut(LocalDateTime.parse(row.getDate(Columns.DATE_DEBUT_PROPRIO.toString()).toString(), dateTimeFormatter));
        }

        if (row.getDate(Columns.DATE_FIN_PROPRIO.toString()) != null) {
            organismeStructure.setDate_fin(LocalDateTime.parse(row.getDate(Columns.DATE_FIN_PROPRIO.toString()).toString(), dateTimeFormatter));
        }

        if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
            organismeStructure.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
        }
        // Jointure, donc pas d'id propre : on choisit arbitrairement l'id du proprio.
        organismeStructure.setPseudoId(row.getInt(Columns.ID_ORG_PROPRIO.toString()));

        return organismeStructure;
    }
}
