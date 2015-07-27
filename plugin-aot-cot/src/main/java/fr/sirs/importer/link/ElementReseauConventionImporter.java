package fr.sirs.importer.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Convention;
import fr.sirs.core.model.ElementCreator;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.PositionConvention;
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
                // VERSION SI LES CONVENTIONS RÉFÉRENCENT DIRECTEMENT LES OBJETS
//                if(reseau instanceof OuvertureBatardable){
//                    convention.getOuvertureBatardableIds().add(reseau.getId());
//                }
//                if(reseau instanceof VoieAcces){
//                    convention.getVoieAccesIds().add(reseau.getId());
//                }
//                if(reseau instanceof VoieDigue){
//                    convention.getVoieDigueIds().add(reseau.getId());
//                }
//                if(reseau instanceof OuvrageVoirie){
//                    convention.getOuvrageVoirieIds().add(reseau.getId());
//                }
//                if(reseau instanceof OuvrageFranchissement){
//                    convention.getOuvrageFranchissementIds().add(reseau.getId());
//                }
//                if(reseau instanceof StationPompage){
//                    convention.getStationPompageIds().add(reseau.getId());
//                }
//                if(reseau instanceof ReseauHydrauliqueCielOuvert){
//                    convention.getReseauHydrauliqueCielOuvertIds().add(reseau.getId());
//                }
//                if(reseau instanceof ReseauHydrauliqueFerme){
//                    convention.getReseauHydrauliqueFermeIds().add(reseau.getId());
//                }
//                if(reseau instanceof OuvrageHydrauliqueAssocie){
//                    convention.getOuvrageHydrauliqueAssocieIds().add(reseau.getId());
//                }
//                if(reseau instanceof OuvrageTelecomEnergie){
//                    convention.getOuvrageTelecomEnergieIds().add(reseau.getId());
//                }
//                if(reseau instanceof ReseauTelecomEnergie){
//                    convention.getReseauTelecomEnergieIds().add(reseau.getId());
//                }
//                if(reseau instanceof OuvrageParticulier){
//                    convention.getOuvrageParticulierIds().add(reseau.getId());
//                }
//                if(reseau instanceof EchelleLimnimetrique){
//                    convention.getEchelleLimnimetriqueIds().add(reseau.getId());
//                }
                
                // VERSION SI LES CONVENTIONS RÉFÉRENCENT INDIRECTEMENT LES OBJETS MAIS EN ÉVITANT UN POSITIONNEMENT SUR LE TRONCON
//                final PorteeConvention portee = ElementCreator.createAnonymValidElement(PorteeConvention.class);
//                portee.setObjetReseauId(reseau.getId());
//                // Par défaut, on initialise la portée à l'aide des pr et des dates de l'objet.
//                portee.setPrDebut(reseau.getPrDebut());
//                portee.setPrFin(reseau.getPrFin());
//                portee.setDate_debut(reseau.getDate_debut());
//                portee.setDate_fin(reseau.getDate_fin());
//                convention.getPortees().add(portee);
                
                // VERSION SI LES CONVENTIONS RÉFÉRENCES INDIRECTEMENT LES OBJETS EN PASSANT PAR UN POSITIONNEMENT SUR LE TRONCON
                final PositionConvention position = ElementCreator.createAnonymValidElement(PositionConvention.class);
                position.setObjetReseauId(reseau.getId());
                // Par défaut, on initialise la portée à l'aide des pr et des dates de l'objet.
                position.setPrDebut(reseau.getPrDebut());
                position.setPrFin(reseau.getPrFin());
                position.setDate_debut(reseau.getDate_debut());
                position.setDate_fin(reseau.getDate_fin());
                position.setSystemeRepId(reseau.getSystemeRepId());
                position.setBorneDebutId(reseau.getBorneDebutId());
                position.setBorneFinId(reseau.getBorneFinId());
                position.setBorne_debut_aval(reseau.getBorne_debut_aval());
                position.setBorne_fin_aval(reseau.getBorne_fin_aval());
                position.setBorne_debut_distance(reseau.getBorne_debut_distance());
                position.setBorne_fin_distance(reseau.getBorne_fin_distance());
                position.setDesignation(convention.getDesignation()+"/"+reseau.getDesignation());
                position.setGeometry(reseau.getGeometry());
                position.setLatitudeMax(reseau.getLatitudeMax());
                position.setLatitudeMin(reseau.getLatitudeMin());
                position.setLongitudeMax(reseau.getLongitudeMax());
                position.setLongitudeMin(reseau.getLongitudeMin());
                position.setLinearId(reseau.getLinearId());
                position.setPositionDebut(reseau.getPositionDebut());
                position.setPositionFin(reseau.getPositionFin());
                position.setSirsdocument(convention.getId());
            }
        }
        couchDbConnector.executeBulk(conventions.values());
    }
}
