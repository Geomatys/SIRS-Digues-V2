package fr.sirs.util;

import fr.sirs.Injector;
import fr.sirs.core.component.Previews;
import fr.sirs.core.model.ObjetReseau;
import fr.sirs.core.model.ObservationReseauHydrauliqueFerme;
import fr.sirs.core.model.OuvrageHydrauliqueAssocie;
import fr.sirs.core.model.Photo;
import fr.sirs.core.model.ReseauHydrauliqueCielOuvert;
import fr.sirs.core.model.ReseauHydrauliqueFerme;
import static fr.sirs.util.JRDomWriterDesordreSheet.OBSERVATION_TABLE_DATA_SOURCE;
import static fr.sirs.util.JRDomWriterDesordreSheet.PHOTO_DATA_SOURCE;
import static fr.sirs.util.JRDomWriterDesordreSheet.RESEAU_OUVRAGE_TABLE_DATA_SOURCE;
import java.util.ArrayList;
import java.util.List;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

/**
 *
 * @author Samuel Andrés (Geomatys)
 */
public class ReseauHydrauliqueFermeDataSource extends ObjectDataSource<ReseauHydrauliqueFerme> {

    public ReseauHydrauliqueFermeDataSource(Iterable<ReseauHydrauliqueFerme> iterable) {
        super(iterable);
    }

    public ReseauHydrauliqueFermeDataSource(final Iterable<ReseauHydrauliqueFerme> iterable, final Previews previewLabelRepository){
        super(iterable, previewLabelRepository);
    }
    
    public ReseauHydrauliqueFermeDataSource(final Iterable<ReseauHydrauliqueFerme> iterable, final Previews previewLabelRepository, final SirsStringConverter stringConverter){
        super(iterable, previewLabelRepository, stringConverter);
    }
    
    @Override
    public Object getFieldValue(final JRField jrf) throws JRException {

        final String name = jrf.getName();

        if(PHOTO_DATA_SOURCE.equals(name)){
            final List<Photo> photos = new ArrayList<>();
            for(final ObservationReseauHydrauliqueFerme observation : currentObject.getObservations()){
                if(observation.getPhotos()!=null && !observation.getPhotos().isEmpty()){
                    photos.addAll(observation.getPhotos());
                }
            }
            if(currentObject.getPhotos()!=null && !currentObject.getPhotos().isEmpty()){
                photos.addAll(currentObject.getPhotos());
            }
            return new ObjectDataSource<>(photos, previewRepository, stringConverter);
        }
        else if(OBSERVATION_TABLE_DATA_SOURCE.equals(name)){
            return new ObjectDataSource<>(currentObject.getObservations(), previewRepository, stringConverter);
        }
        else if(RESEAU_OUVRAGE_TABLE_DATA_SOURCE.equals(name)){
            final List<ObjetReseau> reseauOuvrageList = new ArrayList<>();
            final List<List<? extends ObjetReseau>> retrievedLists = new ArrayList();
            retrievedLists.add(Injector.getSession().getRepositoryForClass(OuvrageHydrauliqueAssocie.class).get(currentObject.getOuvrageHydrauliqueAssocieIds()));
            retrievedLists.add(Injector.getSession().getRepositoryForClass(ReseauHydrauliqueCielOuvert.class).get(currentObject.getReseauHydrauliqueCielOuvertIds()));
            retrievedLists.add(Injector.getSession().getRepositoryForClass(ReseauHydrauliqueFerme.class).get(currentObject.getStationPompageIds()));

            for(final List candidate : retrievedLists){
                if(candidate!=null && !candidate.isEmpty()){
                    reseauOuvrageList.addAll(candidate);
                }
            }
            return new ObjectDataSource<>(reseauOuvrageList, previewRepository, stringConverter);
        }
        else return super.getFieldValue(jrf);
    }
    
}
