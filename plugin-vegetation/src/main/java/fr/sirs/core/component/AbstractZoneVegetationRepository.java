package fr.sirs.core.component;

import fr.sirs.core.SessionCore;
import fr.sirs.core.model.ParcelleVegetation;
import fr.sirs.core.model.ZoneVegetation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.sis.util.ArgumentChecks;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author Samuel Andrés (Geomatys)
 * @param <T>
 */
public abstract class AbstractZoneVegetationRepository<T extends ZoneVegetation> extends AbstractSIRSRepository<T> {
    
    public static final String BY_PARCELLE_ID = "byParcelleId";
    
    public AbstractZoneVegetationRepository(Class<T> type, CouchDbConnector db) {
        super(type, db);
    }
    
    public List<T> getByParcelleId(final String parcelleId) {
        ArgumentChecks.ensureNonNull("Parcelle", parcelleId);
        return this.queryView(BY_PARCELLE_ID, parcelleId);
    }
    
    public List<T> getByParcelle(final ParcelleVegetation parcelle){
        ArgumentChecks.ensureNonNull("Parcelle", parcelle);
        return this.getByParcelleId(parcelle.getId());
    }

    public List<T> getByParcelleIds(final String... parcelleIds) {
        ArgumentChecks.ensureNonNull("Parcelles", parcelleIds);
        return this.queryView(BY_PARCELLE_ID, (Object[]) parcelleIds);
    }

    public List<T> getByParcelleIds(final Collection<String> parcelleIds) {
        ArgumentChecks.ensureNonNull("Parcelles", parcelleIds);
        return this.queryView(BY_PARCELLE_ID, parcelleIds);
    }

    /**
     * Need to loop over parcelles to extract their ids. For retrieving zones
     * by parcelle from a loop, prefer extracting ids once out of the loop and
     * use getByParcelleIds.
     * 
     * @param parcelles
     * @return 
     */
    public List<T> getByParcelles(final Collection<ParcelleVegetation> parcelles) {
        ArgumentChecks.ensureNonNull("Parcelles", parcelles);
        final Collection<String> parcelleIds = new ArrayList<>();
        for(final ParcelleVegetation parcelle : parcelles) parcelleIds.add(parcelle.getId());
        return getByParcelleIds(parcelleIds);
    }

    /**
     * Need to loop over parcelles to extract their ids. For retrieving zones
     * by parcelle from a loop, prefer extracting ids once out of the loop and
     * use getByParcelleIds.
     * 
     * @param parcelles
     * @return 
     */
    public List<T> getByParcelles(final ParcelleVegetation... parcelles) {
        ArgumentChecks.ensureNonNull("Parcelles", parcelles);
        final Collection<String> parcelleIds = new ArrayList<>();
        for(final ParcelleVegetation parcelle : parcelles) parcelleIds.add(parcelle.getId());
        return getByParcelleIds(parcelleIds);
    }
    
    
    
    /**
     * Returns the ZoneVegetation linked to one Parcelle specified by the given id.
     *
     * @param parcelleId
     * @param session
     * @return : - An observable list of ZoneVegetation related to the parcelle
     *           - An empty observable list if no ZoneVegetation was found
     */
    public static ObservableList<? extends ZoneVegetation> getAllZoneVegetationByParcelleId(final String parcelleId, final SessionCore session){
        ObservableList<? extends ZoneVegetation> result = null;
        final Collection<AbstractSIRSRepository> candidateRepos = session.getRepositoriesForClass(ZoneVegetation.class);
        for(AbstractSIRSRepository candidateRepo : candidateRepos){
            if(candidateRepo instanceof AbstractZoneVegetationRepository){
                final List returned = ((AbstractZoneVegetationRepository) candidateRepo).getByParcelleId(parcelleId);
                if(!returned.isEmpty()){
                    if(result==null) result = FXCollections.observableList(returned);
                    else result.addAll(returned);
                }
            }
        }
        /*
        Si aucun repo n'a été trouvé (ce qui est normalement impossible étant
        donné le modèle, on renvoie null. Si des repos ont été trouvés mais qu'
        on arrive tout de même à ce point c'est qu'ils ont tous renvoyé une
        liste vide. Parmi elles, la dernière est renvoyée.
        */
        return result;
    }
}
