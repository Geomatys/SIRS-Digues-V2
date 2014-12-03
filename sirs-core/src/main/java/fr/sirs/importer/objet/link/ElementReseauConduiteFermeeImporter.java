package fr.sirs.importer.objet.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.core.model.StationPompage;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
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
        
        final Map<Integer, Objet> reseaux = reseauImpoter.getById();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
//            final ObjetReferenceObjet referenceConduite = new ObjetReferenceObjet();
//            final ObjetReferenceObjet referenceStation = new ObjetReferenceObjet();
            
            final ReseauHydrauliqueFerme conduiteFermee = (ReseauHydrauliqueFerme) reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU_CONDUITE_FERMEE.toString()));
            final StationPompage stationPompage = (StationPompage) reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()));
            
            if(conduiteFermee!=null && stationPompage!=null){
            
//                if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
//                    referenceConduite.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
//                    referenceStation.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
//                }
//                
//                referenceConduite.setObjetId(cleanNullString(conduiteFermee.getId()));
//                referenceStation.setObjetId(cleanNullString(stationPompage.getId()));
//
//                List<ObjetReferenceObjet> listConduites = stationPompage.getObjet();
//                if (listConduites == null) {
//                    listConduites = new ArrayList<>();
//                    stationPompage.setObjet(listConduites);
//                }
//                listConduites.add(referenceConduite);
//
//                List<ObjetReferenceObjet> listStations = conduiteFermee.getObjet();
//                if (listStations == null) {
//                    listStations = new ArrayList<>();
//                    conduiteFermee.setObjet(listStations);
//                }
//                listStations.add(referenceStation);
            }
        }
    }
}
