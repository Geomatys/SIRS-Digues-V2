package fr.sirs.importer.theme.document.related.convention;

import fr.sirs.importer.*;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.component.ConventionRepository;
import fr.sirs.core.model.ContactTroncon;
import fr.sirs.core.model.Convention;
import fr.sirs.core.model.RefConvention;
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
public class ConventionImporter extends GenericImporter {

    private Map<Integer, Convention> conventions = null;
    private ConventionRepository conventionRepository;
    private TypeConventionImporter typeConventionImporter;
    private ConventionSignataireIntervenantImporter conventionSignataireIntervenantImporter;
    private ConventionSignataireOrganismeImporter conventionSignataireOrganismeImporter;
    
    private ConventionImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    public ConventionImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector, 
            final ConventionRepository conventionRepository,
            final IntervenantImporter intervenantImporter,
            final OrganismeImporter organismeImporter) {
        this(accessDatabase, couchDbConnector);
        this.conventionRepository = conventionRepository;
        this.typeConventionImporter = new TypeConventionImporter(accessDatabase, couchDbConnector);
        this.conventionSignataireIntervenantImporter = new ConventionSignataireIntervenantImporter(
                accessDatabase, couchDbConnector, intervenantImporter);
        this.conventionSignataireOrganismeImporter = new ConventionSignataireOrganismeImporter(
                accessDatabase, couchDbConnector, organismeImporter);
    }

    private enum ConventionColumns {
        ID_CONVENTION,
        LIBELLE_CONVENTION,
        ID_TYPE_CONVENTION,
        DATE_DEBUT_CONVENTION,
        DATE_FIN_CONVENTION,
        REFERENCE_PAPIER,
        REFERENCE_NUMERIQUE,
        COMMENTAIRE,
        DATE_DERNIERE_MAJ
    };
    
    
    @Override
    public List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (ConventionColumns c : ConventionColumns.values()) {
            columns.add(c.toString());
        }
        return columns;
    }

    @Override
    public String getTableName() {
        return DbImporter.TableName.CONVENTION.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        conventions = new HashMap<>();
        
        final Map<Integer, RefConvention> typesConvention = typeConventionImporter.getTypeConvention();
        final Map<Integer, List<ContactTroncon>> orgSignataires = conventionSignataireOrganismeImporter.getOrganisationSignataire();
        final Map<Integer, List<ContactTroncon>> intSignataires = conventionSignataireIntervenantImporter.getIntervenantSignataire();

        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final Convention convention = new Convention();
            
            convention.setLibelle(row.getString(ConventionColumns.LIBELLE_CONVENTION.toString()));
            
            convention.setTypeConventionId(typesConvention.get(row.getInt(ConventionColumns.ID_TYPE_CONVENTION.toString())).getId());
            
            if (row.getDate(ConventionColumns.DATE_DEBUT_CONVENTION.toString()) != null) {
                convention.setDate_debut(LocalDateTime.parse(row.getDate(ConventionColumns.DATE_DEBUT_CONVENTION.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDate(ConventionColumns.DATE_FIN_CONVENTION.toString()) != null) {
                convention.setDate_fin(LocalDateTime.parse(row.getDate(ConventionColumns.DATE_FIN_CONVENTION.toString()).toString(), dateTimeFormatter));
            }
            
            convention.setReference_papier(row.getString(ConventionColumns.REFERENCE_PAPIER.toString()));
            
            convention.setReference_numerique(row.getString(ConventionColumns.REFERENCE_NUMERIQUE.toString()));
            
            convention.setCommentaire(row.getString(ConventionColumns.COMMENTAIRE.toString()));
            
            if (row.getDate(ConventionColumns.DATE_DERNIERE_MAJ.toString()) != null) {
                convention.setDateMaj(LocalDateTime.parse(row.getDate(ConventionColumns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
             List<ContactTroncon> contacts;
            
            final List<ContactTroncon> organisationsSignataires = orgSignataires.get(row.getInt(ConventionColumns.ID_CONVENTION.toString()));
            contacts=organisationsSignataires;
            
            final List<ContactTroncon> intervenantsSignataires = intSignataires.get(row.getInt(ConventionColumns.ID_CONVENTION.toString()));
            if(contacts != null && intervenantsSignataires!=null) contacts.addAll(intervenantsSignataires);
            else if(contacts==null) contacts=intervenantsSignataires;
            
            if(contacts!=null) convention.setContacts(contacts);

            conventions.put(row.getInt(ConventionColumns.ID_CONVENTION.toString()), convention);
        }
        couchDbConnector.executeBulk(conventions.values());
    }
    
    /**
     *
     * @return A map containing all Convention instances accessibles from the
     * internal database identifier.
     * @throws IOException
     * @throws AccessDbImporterException
     */
    public Map<Integer, Convention> getConventions() throws IOException, AccessDbImporterException {
        if (conventions == null)  compute();
        return conventions;
    }
}
