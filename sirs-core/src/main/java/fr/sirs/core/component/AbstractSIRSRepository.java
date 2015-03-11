package fr.sirs.core.component;

import fr.sirs.core.Repository;
import fr.sirs.core.model.Identifiable;
import java.util.ArrayList;
import java.util.List;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.collection.Cache;
import org.ektorp.ComplexKey;
import org.ektorp.CouchDbConnector;
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
            return cache.getOrCreate(id, () -> {
                return super.get(id);
            });
        } catch (Exception ex) {
            // should never happen...
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<T> getAll() {
        return cacheList(super.getAll());
    }

    @Override
    public void add(T entity) {
        ArgumentChecks.ensureNonNull("Document à ajouter", entity);
        super.add(entity);
        cache.put(entity.getId(), entity);
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
        final ArrayList<T> result = new ArrayList<>(source.size());
        for (T element : source) {
            try {
                result.add(cache.getOrCreate(element.getId(), () -> {
                    return element;
                }));
            } catch (Exception ex) {
                // Should never happen ...
                throw new RuntimeException(ex);
            }
        }
        return result;        
    }
}
