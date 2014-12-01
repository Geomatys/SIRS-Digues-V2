package fr.sirs.importer.link;

import com.healthmarketscience.jackcess.Database;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.objet.reseau.ElementReseauImporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ElementReseauEvenementHydrauImporter extends GenericEntityLinker {

    private final ElementReseauImporter elementReseauImporter;
    private final EvenementHydrauliqueImporter evenementHydrauliqueImporter;
    
    public ElementReseauEvenementHydrauImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final ElementReseauImporter elementReseauImporter,
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter) {
        super(accessDatabase, couchDbConnector);
        this.elementReseauImporter = elementReseauImporter;
        this.evenementHydrauliqueImporter = evenementHydrauliqueImporter;
    }

    private enum Columns {
        ID_ELEMENT_RESEAU,
        ID_EVENEMENT_HYDRAU,
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
        return DbImporter.TableName.ELEMENT_RESEAU_EVENEMENT_HYDRAU.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
//        final Map<Integer, Objet> elementsReseau = elementReseauImporter.getStructures();
//        final Map<Integer, EvenementHydraulique> evenements = evenementHydrauliqueImporter.getEvenementHydraulique();
//        
//        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
//        while (it.hasNext()) {
//            final Row row = it.next();
//            final CrueDesordre crueDesordre = new CrueDesordre();
//            
//            final Objet elementReseau = elementsReseau.get(row.getInt(Columns.ID_DESORDRE.toString()));
//            final EvenementHydraulique evenement = evenements.get(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString()));
//            
//            if(elementReseau!=null && evenement!=null){
//                if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
//                    crueDesordre.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
//                }
//                
//                crueDesordre.setEvenementId(cleanNullString(evenement.getId()));
//
//                List<CrueDesordre> listReseauOuvrage = elementReseau.get();
//                if (listReseauOuvrage == null) {
//                    listReseauOuvrage = new ArrayList<>();
//                    elementReseau.setCrueDesordre(listReseauOuvrage);
//                }
//                listReseauOuvrage.add(crueDesordre);
//            }
//        }
    }
}
