package fr.sirs.importer.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.ContactStructure;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.OrganismeStructure;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.IntervenantImporter;
import fr.sirs.importer.OrganismeImporter;
import fr.sirs.importer.objet.reseau.ElementReseauImporter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ElementReseauProprietaireImporter extends GenericEntityLinker {

    private final ElementReseauImporter elementReseauImporter;
    private final IntervenantImporter intervenantImporter;
    private final OrganismeImporter organismeImporter;
    
    public ElementReseauProprietaireImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final ElementReseauImporter elementReseauImporter,
            final IntervenantImporter intervenantImporter,
            final OrganismeImporter organismeImporter) {
        super(accessDatabase, couchDbConnector);
        this.elementReseauImporter = elementReseauImporter;
        this.intervenantImporter = intervenantImporter;
        this.organismeImporter = organismeImporter;
    }

    private enum Columns {
        ID_ELEMENT_RESEAU,
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
        return DbImporter.TableName.ELEMENT_RESEAU_PROPRIETAIRE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, ObjetReseau> reseaux = elementReseauImporter.getById();
        final Map<Integer, Contact> intervenants = intervenantImporter.getIntervenants();
        final Map<Integer, Organisme> organismes = organismeImporter.getOrganismes();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            final Objet reseau = reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()));
            final Contact intervenant = intervenants.get(row.getInt(Columns.ID_INTERV_PROPRIO.toString()));
            final Organisme organisme = organismes.get(row.getInt(Columns.ID_ORG_PROPRIO.toString()));
            
            if(reseau!=null && (intervenant!=null || organisme!=null)){
            
                if(intervenant!=null){
                    final ContactStructure contactStructure = readContactStructure(row);
                    contactStructure.setContactId(intervenant.getId());
                    contactStructure.setTypeContact(intervenant.getClass().getSimpleName());
                    reseau.getContactStructure().add(contactStructure);
                }
                else{
                    final OrganismeStructure organismeStructure = readOrganismeStructure(row);
                    organismeStructure.setOrganismeId(organisme.getId());
                    organismeStructure.setTypeOrganisme(organisme.getClass().getSimpleName());
                    reseau.getOrganismeStructure().add(organismeStructure);
                }
            }
        }
    }
    
    
    private ContactStructure readContactStructure(final Row row){
        
        final ContactStructure contactStructure = new ContactStructure();

        if (row.getDate(Columns.DATE_DEBUT_PROPRIO.toString()) != null) {
            try{
                contactStructure.setDate_debut(LocalDateTime.parse(row.getDate(Columns.DATE_DEBUT_PROPRIO.toString()).toString(), dateTimeFormatter));
            } catch (DateTimeParseException e) {
                SirsCore.LOGGER.log(Level.FINE, e.getMessage());
            }
        }

        if (row.getDate(Columns.DATE_FIN_PROPRIO.toString()) != null) {
            contactStructure.setDate_fin(LocalDateTime.parse(row.getDate(Columns.DATE_FIN_PROPRIO.toString()).toString(), dateTimeFormatter));
        }

        if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
            contactStructure.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
        }
        // Jointure, donc pas d'id propre : on choisit arbitrairement l'id du proprio.
        contactStructure.setDesignation(String.valueOf(row.getInt(Columns.ID_INTERV_PROPRIO.toString())));
        contactStructure.setValid(true);
        return contactStructure;
    }
    
    
    private OrganismeStructure readOrganismeStructure(final Row row){
        
        final OrganismeStructure organismeStructure = new OrganismeStructure();

        if (row.getDate(Columns.DATE_DEBUT_PROPRIO.toString()) != null) {
            try{
                organismeStructure.setDate_debut(LocalDateTime.parse(row.getDate(Columns.DATE_DEBUT_PROPRIO.toString()).toString(), dateTimeFormatter));
            } catch (DateTimeParseException e) {
                SirsCore.LOGGER.log(Level.FINE, e.getMessage());
            }
        }

        if (row.getDate(Columns.DATE_FIN_PROPRIO.toString()) != null) {
            organismeStructure.setDate_fin(LocalDateTime.parse(row.getDate(Columns.DATE_FIN_PROPRIO.toString()).toString(), dateTimeFormatter));
        }

        if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
            organismeStructure.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
        }
        // Jointure, donc pas d'id propre : on choisit arbitrairement l'id du proprio.
        organismeStructure.setDesignation(String.valueOf(row.getInt(Columns.ID_ORG_PROPRIO.toString())));
        organismeStructure.setValid(true);
        return organismeStructure;
    }
}
