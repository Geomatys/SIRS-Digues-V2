package fr.sirs.importer.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.SirsCore;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.Organisme;
import fr.sirs.core.model.OrganismeStructure;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
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
        return DbImporter.TableName.ELEMENT_RESEAU_GESTIONNAIRE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, ObjetReseau> reseaux = elementReseauImporter.getById();
        final Map<Integer, Organisme> organismes = organismeImporter.getOrganismes();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            final Objet reseau = reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()));
            final Organisme organisme = organismes.get(row.getInt(Columns.ID_ORG_GESTION.toString()));
            
            if(reseau!=null && organisme!=null){
                final OrganismeStructure organismeStructure = new OrganismeStructure();
                
                organismeStructure.setOrganismeId(organisme.getId());
            
                if (row.getDate(Columns.DATE_DEBUT_GESTION.toString()) != null) {
                    try{
                        organismeStructure.setDate_debut(LocalDateTime.parse(row.getDate(Columns.DATE_DEBUT_GESTION.toString()).toString(), dateTimeFormatter));
                    } catch (DateTimeParseException e) {
                        SirsCore.LOGGER.log(Level.FINE, e.getMessage());
                    }
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
                
                reseau.getOrganismeStructure().add(organismeStructure);
            }
        }
    }
}
