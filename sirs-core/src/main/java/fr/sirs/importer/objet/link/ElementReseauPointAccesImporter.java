package fr.sirs.importer.objet.link;


import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.OuvertureBatardable;
import fr.sirs.core.model.OuvrageFranchissement;
import fr.sirs.core.model.OuvrageVoirie;
import fr.sirs.core.model.VoieAcces;
import fr.sirs.core.model.VoieDigue;
import fr.sirs.importer.AccessDbImporterException;
import static fr.sirs.importer.DbImporter.TableName.*;
import fr.sirs.importer.objet.reseau.ElementReseauImporter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ElementReseauPointAccesImporter extends GenericObjetLinker {

    private final ElementReseauImporter reseauImpoter;
    
    public ElementReseauPointAccesImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final ElementReseauImporter reseauImpoter) {
        super(accessDatabase, couchDbConnector);
        this.reseauImpoter = reseauImpoter;
    }

    private enum Columns {
        ID_ELEMENT_RESEAU,
        ID_ELEMENT_RESEAU_POINT_ACCES,
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
        return ELEMENT_RESEAU_POINT_ACCES.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, ObjetReseau> reseaux = reseauImpoter.getById();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            
            final Objet reseauPointAcces = reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU_POINT_ACCES.toString()));
            final Objet reseau =  reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()));
            
            if(reseauPointAcces instanceof OuvrageFranchissement){
                final OuvrageFranchissement pointAcces = (OuvrageFranchissement) reseauPointAcces;
                if(pointAcces!=null && reseau!=null){

                    if(reseau instanceof VoieDigue){
                        final VoieDigue voieDigue = (VoieDigue) reseau;
                        voieDigue.getOuvrageFranchissementIds().add(pointAcces.getId());
                        pointAcces.getVoieDigueIds().add(voieDigue.getId());
                        associations.add(new AbstractMap.SimpleEntry<>(reseauPointAcces, voieDigue));
                    }
                    else if(reseau instanceof OuvertureBatardable){
                        final OuvertureBatardable ouvertureBatardable = (OuvertureBatardable) reseau;
                        ouvertureBatardable.getOuvrageFranchissementIds().add(pointAcces.getId());
                        pointAcces.getOuvertureBatardableIds().add(ouvertureBatardable.getId());
                        associations.add(new AbstractMap.SimpleEntry<>(reseauPointAcces, ouvertureBatardable));
                    }
                    else if(reseau instanceof OuvrageVoirie){
                        final OuvrageVoirie ouvrageVoirie = (OuvrageVoirie) reseau;
                        ouvrageVoirie.getOuvrageFranchissementIds().add(pointAcces.getId());
                        pointAcces.getOuvrageVoirieIds().add(ouvrageVoirie.getId());
                        associations.add(new AbstractMap.SimpleEntry<>(reseauPointAcces, ouvrageVoirie));
                    }
                    else if(reseau instanceof VoieAcces){
                        final VoieAcces voieAcces = (VoieAcces) reseau;
                        voieAcces.getOuvrageFranchissementIds().add(pointAcces.getId());
                        pointAcces.getVoieAccesIds().add(voieAcces.getId());
                        associations.add(new AbstractMap.SimpleEntry<>(reseauPointAcces, voieAcces));
                    }
                    else {
                        throw new AccessDbImporterException("Bad type");
                    }
                }
            }
        }
    }
}
