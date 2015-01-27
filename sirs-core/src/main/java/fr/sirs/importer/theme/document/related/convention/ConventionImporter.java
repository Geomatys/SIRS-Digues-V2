package fr.sirs.importer.theme.document.related.convention;

import fr.sirs.importer.*;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;

import fr.sirs.core.model.ContactConvention;
import fr.sirs.core.model.ContactTroncon;
import fr.sirs.core.model.Convention;
import fr.sirs.core.model.RefConvention;
import fr.sirs.importer.theme.document.related.GenericDocumentRelatedImporter;

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
public class ConventionImporter extends GenericDocumentRelatedImporter<Convention> {
    
    private final TypeConventionImporter typeConventionImporter;
    private final ConventionSignataireIntervenantImporter conventionSignataireIntervenantImporter;
    private final ConventionSignataireOrganismeImporter conventionSignataireOrganismeImporter;
    
    public ConventionImporter(final Database accessDatabase,
            final CouchDbConnector couchDbConnector,
            final IntervenantImporter intervenantImporter,
            final OrganismeImporter organismeImporter) {
        super(accessDatabase, couchDbConnector);
        
        this.typeConventionImporter = new TypeConventionImporter(accessDatabase, couchDbConnector);
        this.conventionSignataireIntervenantImporter = new ConventionSignataireIntervenantImporter(
                accessDatabase, couchDbConnector, intervenantImporter);
        this.conventionSignataireOrganismeImporter = new ConventionSignataireOrganismeImporter(
                accessDatabase, couchDbConnector, organismeImporter);
    }

    private enum Columns {
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
    protected List<String> getUsedColumns() {
        final List<String> columns = new ArrayList<>();
        for (Columns c : Columns.values()) {
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
        related = new HashMap<>();
        
        final Map<Integer, RefConvention> typesConvention = typeConventionImporter.getTypes();
        final Map<Integer, List<ContactConvention>> orgSignataires = conventionSignataireOrganismeImporter.getOrganisationSignataire();
        final Map<Integer, List<ContactConvention>> intSignataires = conventionSignataireIntervenantImporter.getIntervenantSignataire();

        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final Convention convention = new Convention();
            
            convention.setLibelle(row.getString(Columns.LIBELLE_CONVENTION.toString()));
            
            convention.setTypeConventionId(typesConvention.get(row.getInt(Columns.ID_TYPE_CONVENTION.toString())).getId());
            
            if (row.getDate(Columns.DATE_DEBUT_CONVENTION.toString()) != null) {
                convention.setDate_debut(LocalDateTime.parse(row.getDate(Columns.DATE_DEBUT_CONVENTION.toString()).toString(), dateTimeFormatter));
            }
            
            if (row.getDate(Columns.DATE_FIN_CONVENTION.toString()) != null) {
                convention.setDate_fin(LocalDateTime.parse(row.getDate(Columns.DATE_FIN_CONVENTION.toString()).toString(), dateTimeFormatter));
            }
            
            convention.setReferencePapier(row.getString(Columns.REFERENCE_PAPIER.toString()));
            
            convention.setReferenceNumerique(row.getString(Columns.REFERENCE_NUMERIQUE.toString()));
            
            convention.setCommentaire(row.getString(Columns.COMMENTAIRE.toString()));
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                convention.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
            }
            
             List<ContactConvention> contacts;
            
            final List<ContactConvention> organisationsSignataires = orgSignataires.get(row.getInt(Columns.ID_CONVENTION.toString()));
            contacts=organisationsSignataires;
            
            final List<ContactConvention> intervenantsSignataires = intSignataires.get(row.getInt(Columns.ID_CONVENTION.toString()));
            if(contacts != null && intervenantsSignataires!=null) contacts.addAll(intervenantsSignataires);
            else if(contacts==null) contacts=intervenantsSignataires;
            
            if(contacts!=null) convention.setContacts(contacts);

            related.put(row.getInt(Columns.ID_CONVENTION.toString()), convention);
        }
        couchDbConnector.executeBulk(related.values());
    }
}
