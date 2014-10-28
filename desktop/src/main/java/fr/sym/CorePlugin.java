

package fr.sym;

import java.io.IOException;
import java.sql.SQLException;

import static fr.sym.Session.PROJECTION;
import fr.sym.digue.Injector;
import org.geotoolkit.data.bean.BeanStore;
import fr.sym.theme.ContactsTheme;
import fr.sym.theme.DesordreTheme;
import fr.sym.theme.DocumentsTheme;
import fr.sym.theme.EmpriseCommunaleTheme;
import fr.sym.theme.EvenementsHydrauliquesTheme;
import fr.sym.theme.FrancBordTheme;
import fr.sym.theme.MesureEvenementsTheme;
import fr.sym.theme.PrestationsTheme;
import fr.sym.theme.ProfilsEnTraversTheme;
import fr.sym.theme.ReseauxDeVoirieTheme;
import fr.sym.theme.ReseauxEtOuvragesTheme;
import fr.sym.theme.StructuresTheme;
import fr.symadrem.sirs.core.component.BorneDigueRepository;
import fr.symadrem.sirs.core.component.TronconDigueRepository;
import fr.symadrem.sirs.core.model.BorneDigue;
import fr.symadrem.sirs.core.model.Crete;
import fr.symadrem.sirs.core.model.Fondation;
import fr.symadrem.sirs.core.model.Structure;
import fr.symadrem.sirs.core.model.TronconDigue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import javafx.scene.control.MenuItem;
import javax.imageio.ImageIO;
import org.apache.sis.storage.DataStoreException;
import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.bean.BeanFeatureSupplier;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.RandomStyleBuilder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class CorePlugin extends Plugin{
    
    public CorePlugin() {
    }

    @Override
    public List<MapItem> getMapItems() {
        final List<MapItem> items = new ArrayList<>();
        
        final TronconDigueRepository repo = getSession().getTronconDigueRepository();
        final BorneDigueRepository bornesRepo = getSession().getBorneDigueRepository();
        
        try{
            
            final CouchDbConnector connector = Injector.getBean(CouchDbConnector.class);
            
            //todo : rendre dynamique
            // Nécessité de l'utilisation d'un dépôt de bornes depuis que les 
            // tronçons ne référencent plus directement les bornes mais leurs ID
            // Du coup il est probable que ceci devienne très lent. Cela pourrait
            // peut-être être amélioré par un map/reduce du côté serveur couchDb
            // mais il faudrait pour cela que les bornes contiennent un id de 
            // navigation vers les tronçons, ce qui n'est pas le cas actuellement.
            final List<BorneDigue> bornes = new ArrayList<>();
            for(TronconDigue td : getSession().getTronconDigueRepository().getAll()){
                // SOLUTION 1 (brutale)
//                td.getBorneIds().stream().forEach((id) -> {
//                    bornes.add(bornesRepo.get(id));
//                });
                
                // SOLUTION 2 (ektorp bulk)
                ViewQuery vq = new ViewQuery()
                      .allDocs()
                      .includeDocs(true)
                      .keys(td.getBorneIds());
                bornes.addAll(connector.queryView(vq, BorneDigue.class));
            }
            
            final BeanStore store = new BeanStore(
                    new BeanFeatureSupplier(TronconDigue.class, "id", "geometry", null, PROJECTION, ()-> repo.getAll()),
                    new BeanFeatureSupplier(Fondation.class, "id", "geometry", null, PROJECTION, ()-> repo.getAllFondations()),
                    new BeanFeatureSupplier(BorneDigue.class, "id", "geometry", null, PROJECTION, ()-> bornes),
                    new BeanFeatureSupplier(Crete.class, "id", "geometry", null, PROJECTION, new StructSupplier((Predicate) (Object t) -> t instanceof Crete))
            );
                    
                         
            items.addAll(buildLayers(store));
            
        }catch(DataStoreException ex){
            Symadrem.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        
        return items;
    }

    private class StructSupplier implements BeanStore.FeatureSupplier{

        private final Predicate predicate;
        
        public StructSupplier(Predicate predicate) {
            this.predicate = predicate;
        }
        
        @Override
        public Iterable get() {
            return new SubIterable(predicate);
        }
        
    }
    
    private class SubIterable implements Iterable{

        private final Predicate predicate;

        public SubIterable(Predicate predicate) {
            this.predicate = predicate;
        }
        
        public Iterator iterator() {
            final TronconDigueRepository repo = getSession().getTronconDigueRepository();
            final List<TronconDigue> troncons = repo.getAll();
            final List col = new ArrayList();
            for(TronconDigue td : troncons){
                col.addAll(td.stuctures.filtered(predicate));
            }
            return col.iterator();
        }
        
    }
    
    private List<MapLayer> buildLayers(FeatureStore store) throws DataStoreException{
        final List<MapLayer> layers = new ArrayList<>();
        final org.geotoolkit.data.session.Session symSession = store.createSession(false);
        for(Name name : store.getNames()){
            final FeatureCollection col = symSession.getFeatureCollection(QueryBuilder.all(name));
            final MutableStyle style = RandomStyleBuilder.createRandomVectorStyle(col.getFeatureType());
            final FeatureMapLayer fml = MapBuilder.createFeatureLayer(col, style);
            fml.setName(name.getLocalPart());
            layers.add(fml);
        }
        return layers;
    }

    @Override
    public List<MenuItem> getMapActions(Object obj) {
        final List<MenuItem> lst = new ArrayList<>();
        
        if(obj instanceof TronconDigue){
            final TronconDigue candidate = (TronconDigue) obj;
            final String nom = candidate.getNom();
            lst.add(new MenuItem(nom));            
            final String docId = candidate.getDocumentId();
            
        }else if(obj instanceof Structure){
            final Structure candidate = (Structure) obj;
            final String docId = candidate.getDocumentId();
            lst.add(new MenuItem(docId));
            
        }
        
        return lst;
    }
    
    @Override
    public void load() throws SQLException, IOException {
        themes.add(new StructuresTheme());
        themes.add(new FrancBordTheme());
        themes.add(new ReseauxDeVoirieTheme());
        themes.add(new ReseauxEtOuvragesTheme());
        themes.add(new DesordreTheme());
        themes.add(new PrestationsTheme());
        themes.add(new MesureEvenementsTheme());
        themes.add(new EmpriseCommunaleTheme());
        themes.add(new ProfilsEnTraversTheme());
        themes.add(new ContactsTheme());
        themes.add(new EvenementsHydrauliquesTheme());
        themes.add(new DocumentsTheme());
        
    }

}
