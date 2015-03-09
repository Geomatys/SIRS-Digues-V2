package fr.sirs.core.component;

import fr.sirs.core.Repository;
import fr.sirs.core.model.Identifiable;
import java.util.ArrayList;
import java.util.List;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.collection.Cache;
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
        List<T> all = super.getAll();
        final ArrayList<T> result = new ArrayList<>(all.size());
        for (T tr : all) {
            try {
                result.add(cache.getOrCreate(tr.getId(), () -> {
                    return tr;
                }));
            } catch (Exception ex) {
                // Should never happen ...
                throw new RuntimeException(ex);
            }
        }
        return result;
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
}
