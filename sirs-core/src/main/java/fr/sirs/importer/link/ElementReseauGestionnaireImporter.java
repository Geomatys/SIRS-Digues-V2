package fr.sirs.importer.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import static fr.sirs.core.model.ElementCreator.createAnonymValidElement;
import fr.sirs.core.model.GestionObjet;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.Organisme;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.OrganismeImporter;
import fr.sirs.importer.objet.reseau.ElementReseauImporter;
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
public class ElementReseauGestionnaireImporter extends GenericEntityLinker {

    private final ElementReseauImporter elementReseauImporter;
    private final OrganismeImporter organismeImporter;
    
    public ElementReseauGestionnaireImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final ElementReseauImporter elementReseauImporter,
            final OrganismeImporter organismeImporter) {
        super(accessDatabase, couchDbConnector);
        this.elementReseauImporter = elementReseauImporter;
        this.organismeImporter = organismeImporter;
    }

    private enum Columns {
        ID_ELEMENT_RESEAU,
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
        return ELEMENT_RESEAU_GESTIONNAIRE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, ObjetReseau> reseaux = elementReseauImporter.getById();
        final Map<Integer, Organisme> organismes = organismeImporter.getOrganismes();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            final ObjetReseau reseau = reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()));
            final Organisme organisme = organismes.get(row.getInt(Columns.ID_ORG_GESTION.toString()));
            
            if(reseau!=null && organisme!=null){
                final GestionObjet gestion = createAnonymValidElement(GestionObjet.class);
                
                gestion.setOrganismeId(organisme.getId());
            
                if (row.getDate(Columns.DATE_DEBUT_GESTION.toString()) != null) {
                    gestion.setDate_debut(DbImporter.parseLocalDate(row.getDate(Columns.DATE_DEBUT_GESTION.toString()), dateTimeFormatter));
                }

                if (row.getDate(Columns.DATE_FIN_GESTION.toString()) != null) {
                    gestion.setDate_fin(DbImporter.parseLocalDate(row.getDate(Columns.DATE_FIN_GESTION.toString()), dateTimeFormatter));
                }

                if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                    gestion.setDateMaj(DbImporter.parseLocalDate(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()), dateTimeFormatter));
                }
                
                // Jointure, donc pas d'id propre : on choisit arbitrairement l'id du gestionnaire.
                gestion.setDesignation(String.valueOf(row.getInt(Columns.ID_ORG_GESTION.toString())));
                
                reseau.getGestions().add(gestion);
            }
        }
    }
}
