package fr.sirs.importer.objet.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.StationPompage;
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
public class ElementReseauConduiteFermeeImporter extends GenericObjetLinker {

    private final ElementReseauImporter reseauImpoter;
    
    public ElementReseauConduiteFermeeImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final ElementReseauImporter reseauImpoter) {
        super(accessDatabase, couchDbConnector);
        this.reseauImpoter = reseauImpoter;
    }

    private enum Columns {
        ID_ELEMENT_RESEAU,
        ID_ELEMENT_RESEAU_CONDUITE_FERMEE,
//        DATE_DERNIERE_MAJ
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
        return ELEMENT_RESEAU_CONDUITE_FERMEE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, ObjetReseau> reseaux = reseauImpoter.getById();
        
        final Iterator<Row> it = context.inputDb.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            final ReseauHydrauliqueFerme conduiteFermee = (ReseauHydrauliqueFerme) reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU_CONDUITE_FERMEE.toString()));
            final StationPompage stationPompage = (StationPompage) reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()));
            
            if(conduiteFermee!=null && stationPompage!=null){
                
                conduiteFermee.getStationPompageIds().add(stationPompage.getId());
                stationPompage.getReseauHydrauliqueFermeIds().add(conduiteFermee.getId());
                
                associations.add(new AbstractMap.SimpleEntry<>(conduiteFermee, stationPompage));
            }
        }
    }
}
