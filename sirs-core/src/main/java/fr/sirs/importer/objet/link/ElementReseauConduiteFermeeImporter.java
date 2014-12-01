package fr.sirs.importer.objet.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.ObjetReferenceObjet;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.StationPompage;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.cleanNullString;
import fr.sirs.importer.objet.reseau.ElementReseauImporter;
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
public class ElementReseauConduiteFermeeImporter extends GenericObjectLinker {

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
        return DbImporter.TableName.ELEMENT_RESEAU_CONDUITE_FERMEE.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, Objet> reseaux = reseauImpoter.getStructures();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final ObjetReferenceObjet reseauConduite = new ObjetReferenceObjet();
            
            final ReseauHydrauliqueFerme conduiteFermee = (ReseauHydrauliqueFerme) reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU_CONDUITE_FERMEE.toString()));
            final StationPompage stationPompage = (StationPompage) reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()));
            
            if(conduiteFermee!=null && stationPompage!=null){
            
                if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                    reseauConduite.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
                }
                
                reseauConduite.setObjetId(cleanNullString(conduiteFermee.getId()));

                List<ObjetReferenceObjet> listByReseau = stationPompage.getObjet();
                if (listByReseau == null) {
                    listByReseau = new ArrayList<>();
                    stationPompage.setObjet(listByReseau);
                }
                listByReseau.add(reseauConduite);
            }
        }
    }
}
