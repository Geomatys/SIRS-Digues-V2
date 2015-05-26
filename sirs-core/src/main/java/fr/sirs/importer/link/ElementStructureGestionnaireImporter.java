package fr.sirs.importer.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.GestionObjet;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.ObjetStructure;
import fr.sirs.core.model.Organisme;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.OrganismeImporter;
import fr.sirs.importer.objet.structure.ElementStructureImporter;
import java.io.IOException;
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
        return ELEMENT_STRUCTURE_GESTIONNAIRE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, ObjetStructure> structures = elementStructureImporter.getById();
        final Map<Integer, Organisme> organismes = organismeImporter.getOrganismes();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            final ObjetStructure structure = structures.get(row.getInt(Columns.ID_ELEMENT_STRUCTURE.toString()));
            final Organisme organisme = organismes.get(row.getInt(Columns.ID_ORG_GESTION.toString()));
            
            if(structure!=null && organisme!=null){
                final GestionObjet gestion = createAnonymValidElement(GestionObjet.class);
                
                gestion.setOrganismeId(organisme.getId());
            
                if (row.getDate(Columns.DATE_DEBUT_GESTION.toString()) != null) {
                    gestion.setDate_debut(DbImporter.parse(row.getDate(Columns.DATE_DEBUT_GESTION.toString()), dateTimeFormatter));
                }

                if (row.getDate(Columns.DATE_FIN_GESTION.toString()) != null) {
                    gestion.setDate_fin(DbImporter.parse(row.getDate(Columns.DATE_FIN_GESTION.toString()), dateTimeFormatter));
                }

                if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                    gestion.setDateMaj(DbImporter.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
                }
                
                // Jointure, donc pas d'id propre : on choisit arbitrairement l'id du gestionnaire.
                gestion.setDesignation(String.valueOf(row.getInt(Columns.ID_ORG_GESTION.toString())));
                
                structure.getGestions().add(gestion);
            }
        }
    }
}
