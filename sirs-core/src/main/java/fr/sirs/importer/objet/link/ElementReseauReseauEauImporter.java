package fr.sirs.importer.objet.link;

import fr.sirs.core.SirsCore;
import java.util.logging.Level;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.ReseauHydrauliqueCielOuvert;
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
public class ElementReseauReseauEauImporter extends GenericObjetLinker {

    private final ElementReseauImporter reseauImpoter;
    
    public ElementReseauReseauEauImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final ElementReseauImporter reseauImpoter) {
        super(accessDatabase, couchDbConnector);
        this.reseauImpoter = reseauImpoter;
    }

    private enum Columns {
        ID_ELEMENT_RESEAU,
        ID_ELEMENT_RESEAU_RESEAU_EAU,
//        DATE_DERNIERE_MAJ,
//        GESTION_SYNCHRO // Qu'est-ce que cela ?
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
        return ELEMENT_RESEAU_RESEAU_EAU.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, ObjetReseau> reseaux = reseauImpoter.getById();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final ReseauHydrauliqueCielOuvert reseauCielOuvert = (ReseauHydrauliqueCielOuvert) reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU_RESEAU_EAU.toString()));
            final Objet reseau =  reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()));
            
            if(reseauCielOuvert!=null && reseau!=null){
                
                if(reseau instanceof ReseauHydrauliqueFerme){
                    final ReseauHydrauliqueFerme reseauFerme = (ReseauHydrauliqueFerme) reseau;
                    reseauFerme.getReseauHydrauliqueCielOuvertIds().add(reseauCielOuvert.getId());
                    reseauCielOuvert.getReseauHydrauliqueFermeIds().add(reseauFerme.getId());
                
                    associations.add(new AbstractMap.SimpleEntry<>(reseauCielOuvert, reseauFerme));
                }
                else {
                    throw new AccessDbImporterException("Bad type");
                }
            }
            else if(reseau==null){
                SirsCore.LOGGER.log(Level.FINE, reseau+" => "+row.getInt(Columns.ID_ELEMENT_RESEAU.toString()));
            }
        }
    }
}
