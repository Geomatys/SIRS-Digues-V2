package fr.sirs.importer.objet.link;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.model.Objet;
import fr.sirs.core.model.OuvrageFranchissement;
import fr.sirs.core.model.VoieAcces;
import fr.sirs.core.model.VoieDigue;
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
        return DbImporter.TableName.ELEMENT_RESEAU_POINT_ACCES.toString();
    }

    @Override
    protected void compute() throws IOException, AccessDbImporterException {
        
        final Map<Integer, Objet> reseaux = reseauImpoter.getById();
        
        final Iterator<Row> it = accessDatabase.getTable(getTableName()).iterator();
        while (it.hasNext()) {
            final Row row = it.next();
            final OuvrageFranchissement ouvrage = (OuvrageFranchissement) reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU_POINT_ACCES.toString()));
            final Objet reseau =  reseaux.get(row.getInt(Columns.ID_ELEMENT_RESEAU.toString()));
            
            if(ouvrage!=null && reseau!=null){
                
                if(reseau instanceof VoieAcces){
                    final VoieAcces voieAcces = (VoieAcces) reseau;
                    voieAcces.getOuvrage_franchissement().add(ouvrage.getId());
                    ouvrage.getVoie_acces().add(voieAcces.getId());
                }
                else if(reseau instanceof VoieDigue){
                    System.out.println("Pas de lien pour le moment entre les voies sur digue et les ouvrages de franchissement.");
//                    final VoieDigue voieDigue = (VoieDigue) reseau;
//                    voieDigue.getOuvrage_franchissement().add(ouvrage.getId());
//                    ouvrage.getVoie_digue().add(voieDigue.getId());
                }
                else {
                    throw new AccessDbImporterException("Bad type");
                }
            }
            else if(reseau==null){
                
            System.out.println(reseau+" => "+row.getInt(Columns.ID_ELEMENT_RESEAU.toString()));
            }
        }
    }
}
