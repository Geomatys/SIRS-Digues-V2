package fr.sirs.importer.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.CrueDesordre;
import fr.sirs.core.model.Desordre;
import fr.sirs.core.model.EvenementHydraulique;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
import static fr.sirs.importer.DbImporter.cleanNullString;
import fr.sirs.importer.evenementHydraulique.EvenementHydrauliqueImporter;
import fr.sirs.importer.objet.desordre.DesordreImporter;
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
public class DesordreEvenementHydrauImporter extends GenericEntityLinker {

    private final DesordreImporter desordreImporter;
    private final EvenementHydrauliqueImporter evenementHydrauliqueImporter;
    
    public DesordreEvenementHydrauImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final DesordreImporter desordreImporter,
            final EvenementHydrauliqueImporter evenementHydrauliqueImporter) {
        super(accessDatabase, couchDbConnector);
        this.desordreImporter = desordreImporter;
        this.evenementHydrauliqueImporter = evenementHydrauliqueImporter;
    }

    private enum Columns {
        ID_DESORDRE,
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
        return DbImporter.TableName.DESORDRE_EVENEMENT_HYDRAU.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, Desordre> desordres = desordreImporter.getStructures();
        final Map<Integer, EvenementHydraulique> evenements = evenementHydrauliqueImporter.getEvenementHydraulique();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final CrueDesordre crueDesordre = new CrueDesordre();
            
            final Desordre desordre = desordres.get(row.getInt(Columns.ID_DESORDRE.toString()));
            final EvenementHydraulique evenement = evenements.get(row.getInt(Columns.ID_EVENEMENT_HYDRAU.toString()));
            
            if(desordre!=null && evenement!=null){
                if (row.getDate(Columns.DATE_DERNIERE_MAJ.toString()) != null) {
                    crueDesordre.setDateMaj(LocalDateTime.parse(row.getDate(Columns.DATE_DERNIERE_MAJ.toString()).toString(), dateTimeFormatter));
                }
                
                crueDesordre.setEvenementId(cleanNullString(evenement.getId()));

                List<CrueDesordre> listReseauOuvrage = desordre.getCrueDesordre();
                if (listReseauOuvrage == null) {
                    listReseauOuvrage = new ArrayList<>();
                    desordre.setCrueDesordre(listReseauOuvrage);
                }
                listReseauOuvrage.add(crueDesordre);
            }
        }
    }
}
