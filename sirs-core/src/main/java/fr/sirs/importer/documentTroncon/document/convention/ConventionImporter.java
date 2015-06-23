package fr.sirs.importer.documentTroncon.document.convention;

import fr.sirs.importer.*;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;

import fr.sirs.core.model.Convention;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.RefConvention;
import static fr.sirs.importer.DbImporter.TableName.CONVENTION;
import fr.sirs.importer.documentTroncon.document.GenericDocumentRelatedImporter;

import java.io.IOException;
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
        return CONVENTION.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        related = new HashMap<>();
        
        final Map<Integer, RefConvention> typesConvention = typeConventionImporter.getTypeReferences();
        final Map<Integer, List<String>> orgSignataires = conventionSignataireOrganismeImporter.getOrganisationsSignatairesIds();
        final Map<Integer, List<String>> intSignataires = conventionSignataireIntervenantImporter.getIntervenantsSignatairesIds();

        final Iterator<Row> it = this.accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final Convention convention = createAnonymValidElement(Convention.class);
            
            convention.setLibelle(row.getString(Columns.LIBELLE_CONVENTION.toString()));
            
            convention.setTypeConventionId(typesConvention.get(row.getInt(Columns.ID_TYPE_CONVENTION.toString())).getId());
            
            if (row.getDate(Columns.DATE_DEBUT_CONVENTION.toString()) != null) {
                convention.setDate_debut(DbImporter.parseLocalDate(row.getDate(Columns.DATE_DEBUT_CONVENTION.toString()), dateTimeFormatter));
            }
            
            if (row.getDate(Columns.DATE_FIN_CONVENTION.toString()) != null) {
                convention.setDate_fin(DbImporter.parseLocalDate(row.getDate(Columns.DATE_FIN_CONVENTION.toString()), dateTimeFormatter));
            }
            
            convention.setReferencePapier(row.getString(Columns.REFERENCE_PAPIER.toString()));
            
            convention.setChemin(row.getString(Columns.REFERENCE_NUMERIQUE.toString()));
            
            convention.setCommentaire(row.getString(Columns.COMMENTAIRE.toString()));
            
            if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                convention.setDateMaj(DbImporter.parseLocalDateTime(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
            }
            
            final List<String> organismesSignatairesIds = orgSignataires.get(row.getInt(Columns.ID_CONVENTION.toString()));
            if(organismesSignatairesIds!=null){
                convention.setOrganismeSignataireIds(organismesSignatairesIds);
            }
            
            final List<String> intervenantsSignatairesIds = intSignataires.get(row.getInt(Columns.ID_CONVENTION.toString()));
            if(intervenantsSignatairesIds!=null){
                convention.setContactSignataireIds(intervenantsSignatairesIds);
            }

            convention.setDesignation(String.valueOf(row.getInt(Columns.ID_CONVENTION.toString())));
            
            related.put(row.getInt(Columns.ID_CONVENTION.toString()), convention);
        }
        couchDbConnector.executeBulk(related.values());
    }
}
