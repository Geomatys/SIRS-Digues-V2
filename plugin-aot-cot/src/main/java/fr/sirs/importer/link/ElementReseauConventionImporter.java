package fr.sirs.importer.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Convention;
import fr.sirs.core.model.EchelleLimnimetrique;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.OuvertureBatardable;
import fr.sirs.core.model.OuvrageFranchissement;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.OuvrageParticulier;
import fr.sirs.core.model.OuvrageTelecomEnergie;
import fr.sirs.core.model.OuvrageVoirie;
import fr.sirs.core.model.ReseauHydrauliqueCielOuvert;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauTelecomEnergie;
import fr.sirs.core.model.StationPompage;
import fr.sirs.core.model.VoieAcces;
import fr.sirs.core.model.VoieDigue;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.ELEMENT_RESEAU_CONVENTION;
import fr.sirs.importer.documentTroncon.document.convention.ConventionImporter;
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
public class ElementReseauConventionImporter extends GenericEntityLinker {

    private final ElementReseauImporter elementReseauImporter;
    private final ConventionImporter conventionImporter;
    
    public ElementReseauConventionImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final ElementReseauImporter elementReseauImporter,
            final ConventionImporter conventionImporter) {
        super(accessDatabase, couchDbConnector);
        this.elementReseauImporter = elementReseauImporter;
        this.conventionImporter = conventionImporter;
    }

    private enum Columns {
        ID_ELEMENT_RESEAU,
        ID_CONVENTION
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
        return ELEMENT_RESEAU_CONVENTION.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, ObjetReseau> reseaux = elementReseauImporter.getById();
        final Map<Integer, Convention> conventions = conventionImporter.getRelated();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            final ObjetReseau reseau = reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()));
            final Convention convention = conventions.get(row.getInt(Columns.ID_CONVENTION.toString()));
            
            if(reseau!=null && convention!=null){
                if(reseau instanceof OuvertureBatardable){
                    convention.getOuvertureBatardableIds().add(reseau.getId());
                }
                if(reseau instanceof VoieAcces){
                    convention.getVoieAccesIds().add(reseau.getId());
                }
                if(reseau instanceof VoieDigue){
                    convention.getVoieDigueIds().add(reseau.getId());
                }
                if(reseau instanceof OuvrageVoirie){
                    convention.getOuvrageVoirieIds().add(reseau.getId());
                }
                if(reseau instanceof OuvrageFranchissement){
                    convention.getOuvrageFranchissementIds().add(reseau.getId());
                }
                if(reseau instanceof StationPompage){
                    convention.getStationPompageIds().add(reseau.getId());
                }
                if(reseau instanceof ReseauHydrauliqueCielOuvert){
                    convention.getReseauHydrauliqueCielOuvertIds().add(reseau.getId());
                }
                if(reseau instanceof ReseauHydrauliqueFerme){
                    convention.getReseauHydrauliqueFermeIds().add(reseau.getId());
                }
                if(reseau instanceof OuvrageHydrauliqueAssocie){
                    convention.getOuvrageHydrauliqueAssocieIds().add(reseau.getId());
                }
                if(reseau instanceof OuvrageTelecomEnergie){
                    convention.getOuvrageTelecomEnergieIds().add(reseau.getId());
                }
                if(reseau instanceof ReseauTelecomEnergie){
                    convention.getReseauTelecomEnergieIds().add(reseau.getId());
                }
                if(reseau instanceof OuvrageParticulier){
                    convention.getOuvrageParticulierIds().add(reseau.getId());
                }
                if(reseau instanceof EchelleLimnimetrique){
                    convention.getEchelleLimnimetriqueIds().add(reseau.getId());
                }
            }
        }
        couchDbConnector.executeBulk(conventions.values());
    }
}
