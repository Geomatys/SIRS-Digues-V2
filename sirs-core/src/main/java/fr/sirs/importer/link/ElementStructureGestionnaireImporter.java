package fr.sirs.importer.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.OrganismeStructure;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
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
public class ElementStructureGestionnaireImporter extends GenericEntityLinker {

    private final ElementStructureImporter elementStructureImporter;
    private final OrganismeImporter organismeImporter;
    
    public ElementStructureGestionnaireImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final ElementStructureImporter elementStructureImporter,
            final OrganismeImporter organismeImporter) {
        super(accessDatabase, couchDbConnector);
        this.elementStructureImporter = elementStructureImporter;
        this.organismeImporter = organismeImporter;
    }

    private enum Columns {
        ID_ELEMENT_STRUCTURE,
        ID_ORG_GESTION,
        DATE_DEBUT_GESTION,
        DATE_FIN_GESTION,
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
        return DbImporter.TableName.ELEMENT_STRUCTURE_GESTIONNAIRE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, Objet> structures = elementStructureImporter.getById();
        final Map<Integer, Organisme> organismes = organismeImporter.getOrganismes();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            final Objet structure = structures.get(row.getInt(Columns.ID_ELEMENT_STRUCTURE.toString()));
            final Organisme organisme = organismes.get(row.getInt(Columns.ID_ORG_GESTION.toString()));
            
            if(structure!=null && organisme!=null){
                final OrganismeStructure organismeStructure = new OrganismeStructure();
                
                organismeStructure.setOrganismeId(organisme.getId());
            
                if (row.getDate(Columns.DATE_DEBUT_GESTION.toString()) != null) {
                    organismeStructure.setDate_debut(LocalDateTime.parse(row.getDate(Columns.DATE_DEBUT_GESTION.toString()).toString(), dateTimeFormatter));
                }

                if (row.getDate(Columns.DATE_FIN_GESTION.toString()) != null) {
                    organismeStructure.setDate_fin(LocalDateTime.parse(row.getDate(Columns.DATE_FIN_GESTION.toString()).toString(), dateTimeFormatter));
                }

                if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                    organismeStructure.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
                }
            
                organismeStructure.setTypeOrganisme(organisme.getClass().getSimpleName());
                
                // Jointure, donc pas d'id propre : on choisit arbitrairement l'id du gestionnaire.
                organismeStructure.setPseudoId(String.valueOf(row.getInt(Columns.ID_ORG_GESTION.toString())));
                organismeStructure.setValid(true);
                structure.getOrganismeStructure().add(organismeStructure);
            }
        }
    }
}
