package fr.sirs.importer.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Contact;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.ProprieteObjet;
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
                    final ProprieteObjet contactStructure = readContactStructure(row);
                    contactStructure.setContactId(intervenant.getId());
                    structure.getProprietes().add(contactStructure);
                }
                else{
                    final ProprieteObjet organismeStructure = readOrganismeStructure(row);
                    organismeStructure.setOrganismeId(organisme.getId());
                    structure.getProprietes().add(organismeStructure);
                }
            }
        }
    }
    
    
    private ProprieteObjet readContactStructure(final Row row){
        
        final ProprieteObjet contactStructure = new ProprieteObjet();

        if (row.getDate(Columns.DATE_DEBUT_PROPRIO.toString()) != null) {
            contactStructure.setDate_debut(DbImporter.parse(row.getDate(Columns.DATE_DEBUT_PROPRIO.toString()), dateTimeFormatter));
        }

        if (row.getDate(Columns.DATE_FIN_PROPRIO.toString()) != null) {
            contactStructure.setDate_fin(DbImporter.parse(row.getDate(Columns.DATE_FIN_PROPRIO.toString()), dateTimeFormatter));
        }

        if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
            contactStructure.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
        }
        // Jointure, donc pas d'id propre : on choisit arbitrairement l'id du proprio.
        contactStructure.setDesignation(String.valueOf(row.getInt(Columns.ID_INTERV_PROPRIO.toString())));
        contactStructure.setValid(true);
        return contactStructure;
    }
    
    
    private ProprieteObjet readOrganismeStructure(final Row row){
        
        final ProprieteObjet organismeStructure = new ProprieteObjet();

        if (row.getDate(Columns.DATE_DEBUT_PROPRIO.toString()) != null) {
            organismeStructure.setDate_debut(DbImporter.parse(row.getDate(Columns.DATE_DEBUT_PROPRIO.toString()), dateTimeFormatter));
        }

        if (row.getDate(Columns.DATE_FIN_PROPRIO.toString()) != null) {
            organismeStructure.setDate_fin(DbImporter.parse(row.getDate(Columns.DATE_FIN_PROPRIO.toString()), dateTimeFormatter));
        }

        if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
            organismeStructure.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
        }
        // Jointure, donc pas d'id propre : on choisit arbitrairement l'id du proprio.
        organismeStructure.setDesignation(String.valueOf(row.getInt(Columns.ID_ORG_PROPRIO.toString())));
        organismeStructure.setValid(true);
        return organismeStructure;
    }
}
