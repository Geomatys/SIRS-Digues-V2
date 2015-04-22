package fr.sirs.importer.objet.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.DbImporter;
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
public class ElementReseauAutreOuvrageHydrauImporter extends GenericObjetLinker {

    private final ElementReseauImporter reseauImpoter;
    
    public ElementReseauAutreOuvrageHydrauImporter(final Database accessDatabase, 
            final CouchDbConnector couchDbConnector,
            final ElementReseauImporter reseauImpoter) {
        super(accessDatabase, couchDbConnector);
        this.reseauImpoter = reseauImpoter;
    }

    private enum Columns {
        ID_ELEMENT_RESEAU,
        ID_ELEMENT_RESEAU_AUTRE_OUVRAGE_HYDRAU,
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
        return DbImporter.TableName.ELEMENT_RESEAU_AUTRE_OUVRAGE_HYDRAU.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, ObjetReseau> reseaux = reseauImpoter.getById();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            
            final OuvrageHydrauliqueAssocie ouvrageHydrauliqueAssocie = (OuvrageHydrauliqueAssocie) reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU_AUTRE_OUVRAGE_HYDRAU.toString()));
            final ReseauHydrauliqueFerme reseauHydrau = (ReseauHydrauliqueFerme) reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()));
            
            if(ouvrageHydrauliqueAssocie!=null && reseauHydrau!=null){
                ouvrageHydrauliqueAssocie.getReseauHydrauliqueFermeIds().add(reseauHydrau.getId());
                reseauHydrau.getOuvrageHydrauliqueAssocieIds().add(ouvrageHydrauliqueAssocie.getId());
                
                associations.add(new AbstractMap.SimpleEntry<>(ouvrageHydrauliqueAssocie, reseauHydrau));
            }
        }
    }
}
