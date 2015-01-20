package fr.sirs.importer.objet.link;

import com.healthmarketscience.jackcess.Database;
import fr.sirs.core.model.Objet;
import fr.sirs.importer.AccessDbImporterException;
import fr.sirs.importer.GenericLinker;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public abstract class GenericObjetLinker extends GenericLinker {
    
    protected final List<Entry<Objet, Objet>> associations = new ArrayList<Entry<Objet, Objet>>();

    GenericObjetLinker(Database accessDatabase, CouchDbConnector couchDbConnector) {
        super(accessDatabase, couchDbConnector);
    }
    
    @Override
    public void link() throws IOException, AccessDbImporterException{
        compute();
        checkSameParents();
    }
    
    public boolean intoSameParent(final Objet a, final Objet b) throws AccessDbImporterException{
        if(a.getParent()!=null && b.getParent()!=null
                && a.getParent().getId()!=null && b.getParent().getId()!=null){
            return a.getParent().getId().equals(b.getParent().getId());
        }
        else{
            throw new AccessDbImporterException("Anomalie sur les parents des objets !");
        }
    }
    
    public void checkSameParents() throws AccessDbImporterException{
        for(final Entry<Objet, Objet> entry : associations){
            final Objet a = entry.getKey();
            final Objet b = entry.getValue();
            if(a!=null && b!=null){
//                System.out.println("====================");
//                System.out.println(a);
//                System.out.println(b);
                System.out.println(intoSameParent(a, b));
            } else {
                throw new AccessDbImporterException("Anomalie sur les objets enregistrés !");
            }
        }
    }
}
