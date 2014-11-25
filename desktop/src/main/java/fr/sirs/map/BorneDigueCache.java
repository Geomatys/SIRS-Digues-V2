
package fr.sirs.map;

import fr.sirs.Injector;
import fr.sirs.Session;
import fr.sirs.core.component.BorneDigueRepository;
import fr.sirs.core.component.DocumentListener;
import fr.sirs.core.model.BorneDigue;
import fr.sirs.core.model.Element;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class BorneDigueCache implements DocumentListener{
    
    private final BorneDigueRepository bornesRepo;
    private final Map<String,BorneDigue> cache = new ConcurrentHashMap<>();

    public BorneDigueCache() {
        final Session session = Injector.getSession();
        bornesRepo = session.getBorneDigueRepository();
        Injector.getDocumentChangeEmiter().addListener(this);
        
        for(BorneDigue bd : bornesRepo.getAll()){
            cache.put(bd.getDocumentId(), bd);
        }
    }
    
    public Collection<BorneDigue> getAll(){
        return cache.values();
    }

    @Override
    public void documentCreated(Element candidate) {
        if(candidate instanceof BorneDigue){
            cache.put(candidate.getDocumentId(), (BorneDigue) candidate);
        }
    }

    @Override
    public void documentChanged(Element candidate) {
        if(candidate instanceof BorneDigue){
            cache.put(candidate.getDocumentId(), (BorneDigue) candidate);
        }
    }

    @Override
    public void documentDeleted(Element candidate) {
        if(candidate instanceof BorneDigue){
            cache.remove(candidate.getDocumentId());
        }
    }
    
}
