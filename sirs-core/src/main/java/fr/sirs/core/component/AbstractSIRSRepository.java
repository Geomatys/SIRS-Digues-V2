package fr.sirs.core.component;

import fr.sirs.core.Repository;
import fr.sirs.core.model.AvecDateMaj;
import fr.sirs.core.model.AvecForeignParent;
import fr.sirs.core.model.Identifiable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.collection.Cache;
import org.ektorp.ComplexKey;
import org.ektorp.CouchDbConnector;
import org.ektorp.DocumentOperationResult;
import org.ektorp.ViewQuery;
import org.ektorp.support.CouchDbRepositorySupport;

/**
 * Base pour les outils gérant les échanges avec la bdd CouchDB.
 *
 * Note : Le cache qui permet de garder une instance unique en mémoire pour un
 * objet donné est extrêmement important pour les opérations de sauvegarde.
 *
 * Ex : On a un objet A, éditable. C'est un CouchDB document, avec un id et un
 * numéro de révision.
 *
 * On ouvre l'éditeur de A. Il est donc chargé en mémoire. Dans le même temps on
 * ouvre une table dans un autre onglet. Elle contient tous les objets du même
 * type que A. Elle contient donc aussi A.
 *
 * Maintenant, que se passe t'il sans cache ? On a deux copies de A en mémoire
 * pour une même révision (disons 0).
 *
 * Si on sauvegarde via l'éditeur de A, il passe en révision 1 dans la bdd, mais
 * pour UNE SEULE des 2 copies en mémoire. En conséquence de quoi, lorsqu'on
 * modifie la copie dans la table, on demande à la base de faire une mise à jour
 * de la révision 1 en utilisant un objet de la révision 0.
 *
 * Résultat : ERREUR !
 *
 * Avec le cache, les deux éditeurs pointent sur la même copie en mémoire.
 * Lorsqu'un éditeur met à jour le tronçon, la révision de la copie est
 * indentée, le deuxième éditeur a donc un tronçon avec un numéro de révision
 * correct.
 *
 * @author Alexis Manin (Geomatys)
 * @param <T> The type of object managed by the current repository implementation.
 */
public abstract class AbstractSIRSRepository<T extends Identifiable> extends CouchDbRepositorySupport<T> implements Repository<T> {

    protected final Cache<String, T> cache = new Cache(20, 0, false);

    protected AbstractSIRSRepository(Class<T> type, CouchDbConnector db) {
        super(type, db);
    }

    @Override
    public T get(String id) {
        try {
            return cache.getOrCreate(id, () -> onLoad(super.get(id)));
        } catch (Exception ex) {
            // should never happen...
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<T> getAll() {
        return cacheList(super.getAll());
    }
    
    private void checkIntegrity(T entity){
        if(entity instanceof AvecForeignParent){
            if(((AvecForeignParent) entity).getForeignParentId()==null) throw new IllegalArgumentException("L'élément ne peut être enregistré sans élement parent.");
        }
    }
    
    /**
     * Retrieve the elements of the given parameter class which Ids are provided 
     * as parameters.
     * 
     * @param ids
     * @param clazz
     * @return 
     */
    public List<T> get(final String... ids) {
        return get(Arrays.asList(ids));
    }
    
    public List<T> get(final List<String> ids) {
        final ArrayList result = new ArrayList();
        
        final List<String> toGet = new ArrayList<>(ids);
        final Iterator<String> idIt = toGet.iterator();
        while (idIt.hasNext()) {
            final T cached = cache.get(idIt.next());
            if (cached != null) {
                result.add(cached);
                idIt.remove();
            }
        }
        
        // On va chercher uniquement les documents qui ne sont pas en cache
        final ViewQuery q = new ViewQuery().allDocs().includeDocs(true).keys(toGet);
        final List<T> bulkLoaded = db.queryView(q, getModelClass());
        
        for(final T loaded : bulkLoaded) {
            cache.put(loaded.getId(), loaded);
            result.add(loaded);
        }
        
        return result;
    }
    
    /**
     * Execute bulk for add/update operation on several documents.
     * 
     * @param bulkList
     * @return 
     */
    public List<DocumentOperationResult> executeBulk(final List<T> bulkList){
        final List<T> cachedBulkList = new ArrayList<>();
        for(T entity : bulkList){
            if (entity instanceof AvecDateMaj) {
                ((AvecDateMaj) entity).setDateMaj(LocalDateTime.now());
            }
            // Put the updated entity into cache in case the old entity is different.
            if (entity != cache.get(entity.getId())) {
                entity = onLoad(entity);
                cache.put(entity.getId(), entity);
            }
            cachedBulkList.add(entity);
        } 
        return db.executeBulk(cachedBulkList);
    }

    @Override
    public void add(T entity) {
        ArgumentChecks.ensureNonNull("Document à ajouter", entity);
        checkIntegrity(entity);
        if (entity instanceof AvecDateMaj) {
            ((AvecDateMaj)entity).setDateMaj(LocalDateTime.now());
        }
        super.add(entity);
        cache.put(entity.getId(), onLoad(entity));
    }

    @Override
    public void update(T entity) {
        ArgumentChecks.ensureNonNull("Document à mettre à jour", entity);
        checkIntegrity(entity);
        if (entity instanceof AvecDateMaj) {
            ((AvecDateMaj) entity).setDateMaj(LocalDateTime.now());
        }
        super.update(entity);
        // Put the updated entity into cache in case the old entity is different.
        if (entity != cache.get(entity.getId())) {
            cache.put(entity.getId(), onLoad(entity));
        }
    }

    @Override
    public void remove(T entity) {
        ArgumentChecks.ensureNonNull("Document à supprimer", entity);
        cache.remove(entity.getId());
        super.remove(entity);
    }
    
    public void clearCache() {
        cache.clear();
    }

    @Override
    protected List<T> queryView(String viewName) {
        return cacheList(super.queryView(viewName));
    }

    @Override
    protected List<T> queryView(String viewName, ComplexKey key) {
        return cacheList(super.queryView(viewName, key));
    }

    @Override
    protected List<T> queryView(String viewName, int key) {
        return cacheList(super.queryView(viewName, key));
    }

    @Override
    protected List<T> queryView(String viewName, String key) {
        return cacheList(super.queryView(viewName, key));
    }
    
    /**
     * Put all input element in cache, or replace by a previously cached element
     * if an input element Id can be found in the cache. Cannot be null.
     * @param source The list of element to put in cache or replace by previously cached value.
     * @return A list of cached elements. Never null, but can be empty. Should be of the 
     * same size as input list.
     * 
     * @throws NullPointerException if input list is null.
     */
    protected List<T> cacheList(List<T> source) {
        final List<T> result = new ArrayList<>(source.size());
        for (T element : source) {
            try {
                result.add(cache.getOrCreate(element.getId(), () -> onLoad(element)));
            } catch (Exception ex) {
                // Should never happen ...
                throw new RuntimeException(ex);
            }
        }
        return result;        
    }
    
    /**
     * Perform an operation when loading an object. By default, nothing is done,
     * but implementations can override this to work with an element before putting
     * it in cache. 
     * @param loaded The object which must be loaded.
     * @return The object to load. By default, the one in parameter.
     */
    protected T onLoad(final T loaded) {
        return loaded;
    }
    
    
    /**
     * Return the class of the managed object type.
     * @return 
     */
    @Override
    public abstract Class<T> getModelClass();
    
    /**
     * Create a new instance of Pojo in memory. No creation in database.
     * @return 
     */
    @Override
    public abstract T create();
}
